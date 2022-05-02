package org.kot.workweek;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * iCal holiday calendar test suite.
 *
 * @author <a href="mailto:striped@gmail.com">Kot Behemoth</a>
 * @created 20/04/2022 15:23
 */
public class ICalHolidaysTest {

	/**
	 * Testing ability to read the holiday calendar from different sources.
	 *
	 * @param expectedDays Expected number of holidays.
	 * @param url          The URl of calendar to check.
	 * @see #params()
	 */
	@ParameterizedTest(name = "[{index}] Verify {1} has {0} holidays")
	@MethodSource("params")
	public void testSize(int expectedDays, URI url) {
		System.setProperty(ICalHolidays.CALENDAR_URL, url.toString());

		Holidays holidays = Holidays.getInstance();

		assertThat(holidays, iterableWithSize(expectedDays));
	}

	@Test
	public void testIsHoliday() {
		System.setProperty(ICalHolidays.CALENDAR_URL, TestUtils.testResourceByName("holidays/ireland-2021.ics").toString());

		Holidays holidays = Holidays.getInstance();

		assertTrue(holidays.isHoliday(LocalDate.of(2021, 1, 1)));
		assertTrue(holidays.isHoliday(LocalDate.of(2021, 3, 17)));
		assertTrue(holidays.isHoliday(LocalDate.of(2021, 4, 5)));
		assertTrue(holidays.isHoliday(LocalDate.of(2021, 5, 3)));
		assertTrue(holidays.isHoliday(LocalDate.of(2021, 6, 7)));
		assertTrue(holidays.isHoliday(LocalDate.of(2021, 8, 2)));
		assertTrue(holidays.isHoliday(LocalDate.of(2021, 10, 25)));
		assertTrue(holidays.isHoliday(LocalDate.of(2021, 12, 27)));
		assertTrue(holidays.isHoliday(LocalDate.of(2021, 12, 28)));
	}

	@Test
	public void testCalculateHolidayBetween() {
		System.setProperty(ICalHolidays.CALENDAR_URL, TestUtils.testResourceByName("holidays/ireland-2021.ics").toString());

		Holidays holidays = Holidays.getInstance();
		LocalDate from = LocalDate.of(2021, 1, 1);
		LocalDate to = LocalDate.of(2021, 12, 31);

		assertThat(holidays.holidaysBetween(from, to), is(9L));
		assertThat(holidays.holidaysBetween(to, from), is(0L));
	}

	public static Stream<Arguments> params() throws IOException, URISyntaxException {
		URL url = TestUtils.testResourceByName("holidays");

		Path folder = Paths.get(url.toURI());
		Path zip1 = TestUtils.zip(folder, "holidays.zip");
		Path zip2 = TestUtils.zip(folder.getParent(), "holidays-1.zip");
		return Stream.of(
				Arguments.arguments(9, folder.resolve("ireland-2020.ics").toUri()),
				Arguments.arguments(9, folder.resolve("ireland-2021.ics").toUri()),
				Arguments.arguments(9, folder.resolve("ireland-2022.ics").toUri()),
				Arguments.arguments(27, folder.toUri()),
				Arguments.arguments(9, URI.create("jar:" + zip1.toUri() + "!/ireland-2020.ics")),
				Arguments.arguments(9, URI.create("jar:" + zip1.toUri() + "!/ireland-2021.ics")),
				Arguments.arguments(9, URI.create("jar:" + zip1.toUri() + "!/ireland-2022.ics")),
				Arguments.arguments(27, URI.create("jar:" + zip1.toUri() + "!/")),
				Arguments.arguments(27, URI.create("jar:" + zip2.toUri() + "!/holidays"))
		);
	}

}
