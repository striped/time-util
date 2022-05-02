package org.kot.workweek;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Test utilities.
 *
 * @author <a href="mailto:striped@gmail.com">Kot Behemoth</a>
 * @created 20/04/2022 15:23
 */
public class TestUtils {

	private TestUtils() {
	}

	static URL testResourceByName(String name) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource(name);
		assert null != url: "Failed to find test resources in 'holidays'";
		return url;
	}

	/**
	 * Packs the files referenced by {@code source} into ZIP with {@code name}.
	 *
	 * @param source The folder where requested files can be sourced.
	 * @param name   The name of ZIP target file.
	 * @return The path to the newly created ZIP pack.
	 * @throws IOException On unexpected I/O failures during packing.
	 */
	static Path zip(final Path source, final String name) throws IOException {
		assert null != source: "Source(s) of ZIP archive is expected";
		assert null != name: "The name of ZIP archive is expected";

		Path output = Optional.ofNullable(source.getParent())
				.map(f -> f.resolve(name)).orElse(source);
		try (Stream<Path> src = Files.walk(source); ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(output))) {
			src.filter(p -> !Files.isDirectory(p))
					.forEach(path -> ((ResourceCrawler.SafeRunnable) () -> {
						String entry = source.relativize(path).toString();
						out.putNextEntry(new ZipEntry(entry));
						Files.copy(path, out);
						out.closeEntry();
					}).run());
		}
		return output;
	}
}
