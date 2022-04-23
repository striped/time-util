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
 * Read a holidays ascribed in iCal format (RFC-5545).
 *
 * @author <a href=mailto:striped@gmail.com>Kot Behemoth</a>
 * @created 20/04/2022 15:23
 * @see <a href="https://tools.ietf.org/html/rfc5545">RFC-5545</a>
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
	 * Instantiate this holiday calendar provider that reads persisted dates in format iCal (RFC-5545) as holidays. The
	 * resource location might be identified by system property {@value CALENDAR_URL} in form of URL and can be a file,
	 * a folder or ZIP archive with files extension ".ics".
	 *
	 * @throws IOException        If unexpected I/O failure occurred.
	 * @throws URISyntaxException If specified by {@value CALENDAR_URL} property URL doesn't comply RFC 3986.
	 */
	public ICalHolidays() throws IOException, URISyntaxException {
		URL url = new URL(System.getProperty(CALENDAR_URL));

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
