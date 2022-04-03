package org.kot.workweek;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Working day calculator test suite.
 * <p>
 * Verifies calculator of business days between arbitrary dates. The verification  performed for all {@link WorkingWeek
 * supported business days calendars}.
 *
 * @author <a href=mailto:striped@gmail.com>striped</a>
 * @created 02/01/2022 19:11
 */
public class WorkingDayCalculatorTest {

	@ParameterizedTest(name = "[{0} {index}] Check workdays between {1} and {2}")
	@MethodSource("params")
	public void testCalculateWorkdaysBetween(WorkingWeek workingWeek, LocalDate start, LocalDate end) {
		long expected = calculateWorkdays(workingWeek, start, end);

		long actual = workingWeek.workdaysBetween(start, end);

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
		LocalDate date = LocalDate.of(2022, 1, 1);
		return Stream.of(WorkingWeek.values())
				.flatMap(c -> IntStream.range(0, 7)
						.mapToObj(date::plusDays)
						.flatMap(base -> IntStream.range(0, 365)
								.mapToObj(day -> arguments(c, base, base.plusDays(day)))));
	}

	/**
	 * Brute-force calculation of business days between provided dates.
	 *
	 * @param calendar The
	 * @param start    The start date.
	 * @param end      The end date.
	 * @return the number of business days between provided dates.
	 */
	private static long calculateWorkdays(WorkingWeek calendar, LocalDate start, LocalDate end) {
		long result = 0;
		for (LocalDate day = start; day.isBefore(end); day = day.plusDays(1))
			if (calendar.isWorkingDay(day)) result++;
		return result;
	}
}
