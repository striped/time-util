package org.kot.workweek;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Business day adjuster test suite.
 * <p>
 * Verifies calendar date justification, in both directions, on arbitrary number of working days. The verification
 * performed for all {@link WorkingWeek supported working days calendars}.
 *
 * @author <a href=mailto:striped@gmail.com>striped</a>
 * @created 08/01/2022 18:07
 */
public class WorkingDayAdjusterTest {

	@ParameterizedTest(name = "[{0} {index}] Adjust {1} on {2} day(s) right")
	@MethodSource("params")
	public void testAdjustRight(WorkingWeek calendar, LocalDate date, int days) {
		LocalDate expected = plusWorkingDays(calendar, date, days);

		LocalDate actual = date.with(calendar.adjustDaysAfter(days));

		assertThat("" + DayOfWeek.from(date), actual, is(expected));
	}

	@ParameterizedTest(name = "[{0} {index}] Adjust {1} on {2} day(s) right")
	@MethodSource("params")
	public void testAdjustLeft(WorkingWeek calendar, LocalDate date, int days) {
		LocalDate expected = minusWorkingDays(calendar, date, days);

		LocalDate actual = date.with(calendar.adjustDaysBefore(days));

		assertThat(actual, is(expected));
	}

	/**
	 * Generates pairs of dates to be used as test argument for testing chosen calendar.
	 * <p>
	 * Started from arbitrary day generates complement outstanding on [0..366] days in the future.
	 *
	 * @return The streamable test arguments.
	 */
	public static Stream<Arguments> params() {
		LocalDate date = LocalDate.of(2022, 1, 3);
		return Stream.of(WorkingWeek.values())
				.flatMap(c -> IntStream.range(0, 7)
						.mapToObj(date::plusDays)
						.flatMap(base -> IntStream.range(0, 366)
								.mapToObj(day -> arguments(c, base, day))));
	}

	/**
	 * Brute-force calculation of calendar date that stands after chosen one by provided working days.
	 *
	 * @param date The chosen date.
	 * @param days The number of working days in the future.
	 * @return The calendar date that outstanding on specified number of working days in the future.
	 */
	private static LocalDate plusWorkingDays(WorkingWeek calendar, LocalDate date, int days) {
		assert null != date: "Date is expected";
		assert 0 <= days: "Addition can't be negative";

		LocalDate result = date;
		while (!calendar.isWorkingDay(result)) result = result.plusDays(1);
		for (int d = days; d-- > 0; ) {
			result = result.plusDays(1);
			while (!calendar.isWorkingDay(result)) result = result.plusDays(1);
		}
		return result;
	}

	/**
	 * Brute-force calculation of calendar date that stands before chosen one by provided working days.
	 *
	 * @param date The chosen date.
	 * @param days The number of working days in the past.
	 * @return The calendar date that outstanding on specified number of working days in the past.
	 */
	private static LocalDate minusWorkingDays(WorkingWeek calendar, LocalDate date, int days) {
		assert null != date: "Date is expected";
		assert 0 <= days: "Subtraction can't be negative";

		LocalDate result = date;
		while (!calendar.isWorkingDay(result)) result = result.minusDays(1);
		for (int d = days; d-- > 0; ) {
			result = result.minusDays(1);
			while (!calendar.isWorkingDay(result)) result = result.minusDays(1);
		}
		return result;
	}
}
