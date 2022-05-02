package org.kot.workweek;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * iCal holiday calendar reader.
 * <p>
 * Reads the holiday calendar ascribed in iCal format (RFC-5545). It is necessary to name such file(s) with ".ics"
 * extension, to hint which are loadable. The URL of that loadable file(s), must be specified with system property
 * {@value #CALENDAR_URL} and could be a single local file, a local folder or a packed ZIP archive. Later should be
 * provided as a URL with valid JAR schema that conform the URL specification. For instance:
 * <ul>
 *     <li>file:///holiday/ireland-2020.ics &dash; would load single local file, {@code /holiday/ireland-2020.ics},</li>
 *     <li>file:///holidays/ &dash; would load all files in folder {@code /holiday/},</li>
 *     <li>jar:file:///holidays.zip!/ireland-2020.ics &dash; would load single file {@code ireland-2020.ics} packed in a
 *     ZIP file {@code /holidays.zip} and</li>
 *     <li>jar:file:///holidays.zip!/holidays &dash; would load all files within folder {@code /holidays} packed in a
 *     ZIP file {@code /holidays.zip}.</li>
 * </ul>
 * <p>
 * The extension case is ignored for user convenience.
 *
 * @author <a href="mailto:striped@gmail.com">Kot Behemoth</a>
 * @created 20/04/2022 15:23
 * @see <a href="https://tools.ietf.org/html/rfc5545">RFC-5545</a>
 * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/net/JarURLConnection.html">JAR URL schema</a>
 */
public class ICalHolidays extends Holidays {

	/**
	 * System property name with URL of calendar configuration to load on instantiation.
	 */
	public static final String CALENDAR_URL = "holiday.calendar.url";

	private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("uuuuMMdd")
			.withResolverStyle(ResolverStyle.SMART);

	private static final String HOLIDAY_DATE_TAG = "DTSTART;VALUE=DATE:";

	/**
	 * Default constructor.
	 * <p>
	 * Instantiate the holiday calendar provider that reads persisted dates in format iCal (RFC-5545) as holidays. The
	 * resource location might be identified by system property {@value CALENDAR_URL} in form of URL and can be a file,
	 * a folder or ZIP archive with files extension ".ics".
	 *
	 * @throws IOException        If unexpected I/O failure occurred.
	 * @throws URISyntaxException If specified by {@value CALENDAR_URL} property URL doesn't comply RFC 3986.
	 */
	public ICalHolidays() throws IOException, URISyntaxException {
		URL url = new URL(Objects.requireNonNull(System.getProperty(CALENDAR_URL),
				() -> String.format("Required a valid URL in %s property", CALENDAR_URL)));

		try (Stream<String> lines = ResourceCrawler.lines(url, ".ics")) {
			lines.map(this::lookupDate)
					.filter(Objects::nonNull)
					.map(v -> LocalDate.parse(v, FORMAT))
					.forEach(holidays::add);
		}
	}

	/**
	 * Lookup the date from line in iCal format (RFC-5545), {@value HOLIDAY_DATE_TAG} if there is any.
	 *
	 * @param line The line with date in "DTSTART" form, if any.
	 * @return The date or {@code null} if there is no "DTSTART" tag.
	 */
	private String lookupDate(String line) {
		int pos = line.indexOf(HOLIDAY_DATE_TAG);
		if (0 > pos)
			return null;
		return line.substring(pos + HOLIDAY_DATE_TAG.length());
	}
}
