package org.kot.time.workday;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Objects;
import java.util.Set;

/**
 * iCal holiday calendar reader.
 *
 * Reads holidays prescribed in iCal format as per RFC-5545.
 * @author <a href=mailto:striped@gmail.com>striped</a>
 * @created 02/04/2021 15:50
 * @see <a href="https://tools.ietf.org/html/rfc5545">RFC-5545</a>
 */
class HolidayICalReader implements HolidayReader {

	private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("uuuuMMdd")
			.withResolverStyle(ResolverStyle.SMART);

	private static final String HOLIDAY_DATE_TAG = "DTSTART;VALUE=DATE:";

	@Override
	public void read(InputStream from, Set<? super LocalDate> to) throws IOException {
		Objects.requireNonNull(from, "Input source is expected");
		Objects.requireNonNull(to, "Destination should be provided");

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(from, StandardCharsets.UTF_8))) {
			for (String line = reader.readLine(); null != line; line = reader.readLine()) {
				/* Assuming each VEvent start date is from new line as 'DTSTART;VALUE=DATE:YYYYMMDD\n' */
				int pos = line.indexOf(HOLIDAY_DATE_TAG);
				if (0 <= pos) {
					LocalDate date = LocalDate.parse(line.substring(pos + HOLIDAY_DATE_TAG.length()), FORMAT);
					while (isWeekend(date) || to.contains(date)) date = date.plusDays(1);
					to.add(date);
				}
			}
		}
	}

	private static boolean isWeekend(LocalDate date) {
		switch (date.getDayOfWeek()) {
			case SATURDAY:
			case SUNDAY:
				return true;
		}
		return false;
	}
}
