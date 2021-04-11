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
import java.time.temporal.TemporalAdjuster;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Business day adjuster test suite.
 *
 * @author <a href=mailto:striped@gmail.com>striped</a>
 * @created 01/04/2021 19:44
 */
public class WorkdayAdjusterTest {

	/**
	 * Init holiday calendar on 2020, 2021 and 2022 years from test resources provided.
	 *
	 * @throws IOException        On unexpected I/O failures.
	 * @throws URISyntaxException Never happen, required by design of {@link URL}.
	 */
	@BeforeAll
	public static void initHolidays() throws IOException, URISyntaxException {
		URL holidays = Thread.currentThread().getContextClassLoader().getResource("holidays");
		assert null != holidays : "Failed initialize calendars";
		WorkDayUtil.initHolidays(Paths.get(holidays.toURI()));
	}

	@ParameterizedTest(name = "[{index}] Adjust {0} on {1} day(s) left")
	@MethodSource("params")
	public void testAdjustLeft(LocalDate date, int days) {
		LocalDate expected = minusBusinessDays(date, days);

		TemporalAdjuster adjuster = WorkDayUtil.beforeBusinessDays(days);
		LocalDate actual = date.with(adjuster);

		assertThat(actual, is(expected));
	}

	@ParameterizedTest(name = "[{index}] Adjust {0} on {1} day(s) right")
	@MethodSource("params")
	public void testAdjustRight(LocalDate date, int days) {
		LocalDate expected = plusBusinessDays(date, days);

		LocalDate actual = date.with(WorkDayUtil.afterBusinessDays(days));

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
				.mapToObj(day -> arguments(base, day)));

	}

	/**
	 * Brute-force calculation of calendar days by provided business days.
	 * @param date The start date.
	 * @param days The number of business days in future.
	 * @return The calendar date that outstanding on specified number of business days in future.
	 */
	private static LocalDate plusBusinessDays(LocalDate date, int days) {
		assert 0 <= days : "Don't support reverse";
		assert null != date : "Date is expected";

		LocalDate result = LocalDate.from(date);
		while (WorkDayUtil.isWeekend(result) || WorkDayUtil.isHoliday(result)) result = result.plusDays(1);
		for (int d = days; d-->0;) {
			result = result.plusDays(1);
			while (WorkDayUtil.isWeekend(result) || WorkDayUtil.isHoliday(result)) result = result.plusDays(1);
		}
		return result;
	}

	/**
	 * Brute-force calculation of calendar days by provided business days.
	 * @param date The start date.
	 * @param days The number of business days in past.
	 * @return The calendar date that outstanding on specified number of business days in past.
	 */
	private static LocalDate minusBusinessDays(LocalDate date, int days) {
		assert 0 <= days : "Don't support reverse";
		assert null != date : "Date is expected";

		LocalDate result = LocalDate.from(date);
		while (WorkDayUtil.isWeekend(result) || WorkDayUtil.isHoliday(result)) result = result.minusDays(1);
		for (int d = days; d-->0;) {
			result = result.minusDays(1);
			while (WorkDayUtil.isWeekend(result) || WorkDayUtil.isHoliday(result)) result = result.minusDays(1);
		}
		return result;
	}
}
