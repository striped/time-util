package org.kot.time.workday;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Test suite for calculation business days between specific calendar dates.
 *
 * @author <a href=mailto:striped@gmail.com>striped</a>
 * @created 01/04/2021 17:07
 */
public class CountWorkdayTest {

	/**
	 * Init holiday calendar on 2020, 2021 and 2022 years from test resources provided.
	 *
	 * @throws IOException        On unexpected I/O failures.
	 * @throws URISyntaxException Never happen, required by design of {@link URL}.
	 */
	@BeforeAll
	public static void initHolidays() throws IOException, URISyntaxException {
		URL holidays = Thread.currentThread().getContextClassLoader().getResource("holidays");
		assert null != holidays: "Failed initialize calendars";
		WorkDayUtil.initHolidays(Paths.get(holidays.toURI()));
	}

	@ParameterizedTest(name = "[{index}] Check workdays between {0} and {1}")
	@MethodSource("params")
	public void testCalculateWorkdaysBetween(LocalDate start, LocalDate end) {
		long expected = calculateWorkdays(start, end);

		long actual = WorkDayUtil.workdaysBetween(start, end);

		assertThat(actual, is(expected));
	}

	/**
	 * Generates pairs of date to be used as test argument.
	 * <p>
	 * Started from arbitrary week generates complement outstanding on [0..366] days in future.
	 *
	 * @return The streamable test arguments.
	 */
	public static Stream<Arguments> params() {
		LocalDate date = LocalDate.of(2021, 1, 10);
		return IntStream.range(0, 7)
				.mapToObj(date::plusDays)
				.flatMap(base -> IntStream.range(0, 366)
						.mapToObj(day -> arguments(base, base.plusDays(day))));

	}

	/**
	 * Brute-force calculation of business days between provided dates.
	 * @param start The start date.
	 * @param end The end date.
	 * @return the number of business days between provided dates.
	 */
	private static long calculateWorkdays(LocalDate start, LocalDate end) {
		long result = 0;
		for (LocalDate day = start; day.isBefore(end); day = day.plusDays(1))
			if (!WorkDayUtil.isHoliday(day) && !WorkDayUtil.isWeekend(day)) result++;
		return result;
	}
}
