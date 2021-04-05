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
 * Calculate business days between dates test suite.
 *
 * @author <a href=mailto:striped@gmail.com>striped</a>
 * @created 01/04/2021 17:07
 */
public class CountWorkdayTest {

	@BeforeAll
	public static void initHolidays() throws IOException, URISyntaxException {
		URL holidays = Thread.currentThread().getContextClassLoader().getResource("holidays");
		assert null != holidays : "Failed initialize calendars";
		WorkDayUtil.initHolidays(Paths.get(holidays.toURI()));
	}

	@ParameterizedTest(name = "[{index}] Check workdays between {0} and {1}")
	@MethodSource("params")
	public void testCalculateWorkdaysBetween(LocalDate start, LocalDate end) {
		long expected = calculateWorkdays(start, end);

		long actual = WorkDayUtil.workdaysBetween(start, end);

		assertThat(actual, is(expected));
	}

	public static Stream<Arguments> params() {
		LocalDate date = LocalDate.of(2021, 2, 15);
		return IntStream.range(0, 7)
				.mapToObj(date::plusDays)
				.flatMap(base -> IntStream.range(0, 366)
						.mapToObj(day -> arguments(base, base.plusDays(day))));

	}

	private static long calculateWorkdays(LocalDate start, LocalDate end) {
		long result = 0;
		for (LocalDate day = start; day.isBefore(end); day = day.plusDays(1))
			if (!WorkDayUtil.isHoliday(day) && !WorkDayUtil.isWeekend(day)) result++;
		return result;
	}
}
