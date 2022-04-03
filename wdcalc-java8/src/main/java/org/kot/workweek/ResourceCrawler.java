package org.kot.workweek;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Utilities for "crawling" over locally identifiable resources.
 *
 * @author <a href=mailto:striped@gmail.com>striped</a>
 * @created 18/02/2022 20:14
 */
class ResourceCrawler {

	/**
	 * DEfault constructor.
	 * <p>
	 * To prevent explicit instantiation.
	 */
	private ResourceCrawler() {
	}

	/**
	 * Streams the textual file, folder or ZIP archive into string lines.
	 * <p>
	 * Reads content identifiable locally (i.e. protocols: "file" or "jar") as a string lines. The files specified by
	 * URL or inside folder, archive will be asserted to have specified extension.
	 *
	 * @param url       The URL of content to read.
	 * @param extension The extension (file name suffix) to read.
	 * @return The stream of string lines read as content identified by specified URL.
	 * @throws IOException        If unexpected I/O failure occurs.
	 * @throws URISyntaxException If provided URL doesn't conform RFC 3986.
	 * @see java.net.JarURLConnection
	 */
	public static Stream<String> lines(URL url, String extension) throws IOException, URISyntaxException {
		switch (url.getProtocol()) {
			case "file":
				return lines(Paths.get(url.toURI()), extension);
			case "jar":
				FileSystem zip = FileSystems.newFileSystem(url.toURI(), Collections.emptyMap());
				int pos = url.getPath().indexOf("!/");
				Path path = zip.getPath(0 <= pos? url.getPath().substring(pos + 1): "/");
				return lines(path, extension)
						.onClose((SafeRunnable) zip::close);
			default:
				return Stream.empty();
		}
	}

	/**
	 * Streams the textual file or folder into string lines.
	 * <p>
	 * Reads content identifiable locally (i.e. protocols: "file" or "jar") as a string lines. The files specified by
	 * URL or inside folder, archive will be asserted to have specified extension.
	 *
	 * @param path      The local file system path that identify file or folder.
	 * @param extension The extension (file name suffix) to read.
	 * @return The stream of string lines read as content identified by specified {@code path}.
	 * @throws IOException If unexpected I/O failure occurs.
	 */
	public static Stream<String> lines(Path path, String extension) throws IOException {
		return Files.walk(path, FileVisitOption.FOLLOW_LINKS)
				.filter(Files::isRegularFile)
				.filter(p -> p.getFileName().toString().endsWith(extension))
				.flatMap((SafeFunction<Path, Stream<String>>) p -> {
					BufferedReader reader = Files.newBufferedReader(p, StandardCharsets.UTF_8);
					return reader.lines()
							.onClose((SafeRunnable) reader::close);
				});
	}

	interface SafeFunction<I, O> extends Function<I, O> {

		default O apply(I i) {
			try {
				return applySafe(i);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		O applySafe(I input) throws IOException;
	}

	interface SafeRunnable extends Runnable {

		default void run() {
			try {
				runSafe();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		void runSafe() throws IOException;
	}
}
