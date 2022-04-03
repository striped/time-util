package org.kot.workweek;

import org.junit.jupiter.api.BeforeAll;
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
import static org.kot.workweek.TestUtils.testResourceByName;

/**
 * Business day adjuster test suite.
 * <p>
 * Verifies calendar date justification, in both directions, on arbitrary number of business days. The verification
 * performed for all {@link WorkingWeek supported business days calendars} in conjunction with holiday calendar for
 * corresponding period.
 *
 * @author <a href=mailto:striped@gmail.com>striped</a>
 * @created 08/01/2022 18:07
 */
public class BusinessDayAdjusterTest {

	private static Holidays HOLIDAYS;

	@BeforeAll
	public static void setupHolidayCalendar() {
		System.setProperty(ICalHolidays.CALENDAR_URL, testResourceByName("holidays/ireland-2021.ics").toString());
		HOLIDAYS = Holidays.getInstance(s -> s instanceof ICalHolidays);
	}

	@ParameterizedTest(name = "[{0} {index}] Adjust {1} on {2} day(s) right")
	@MethodSource("params")
	public void testAdjustRight(WorkingWeek workingWeek, LocalDate date, int days) {
		LocalDate expected = plusBusinessDays(workingWeek, date, days);

		LocalDate actual = date.with(HOLIDAYS.adjustDaysAfter(days, workingWeek));

		assertThat("" + DayOfWeek.from(date), actual, is(expected));
	}

	@ParameterizedTest(name = "[{0} {index}] Adjust {1} on {2} day(s) right")
	@MethodSource("params")
	public void testAdjustLeft(WorkingWeek workingWeek, LocalDate date, int days) {
		LocalDate expected = minusBusinessDays(workingWeek, date, days);

		LocalDate actual = date.with(HOLIDAYS.adjustDaysBefore(days, workingWeek));

		assertThat(actual, is(expected));
	}

	/**
	 * Generates pairs of dates to be used as test argument for testing chosen calendar.
	 * <p>
	 * Started from arbitrary day generates complement outstanding on [0..100] days in the future.
	 *
	 * @return The streamable test arguments.
	 */
	public static Stream<Arguments> params() {
		LocalDate date = LocalDate.of(2021, 6, 1);
		return Stream.of(WorkingWeek.values())
				.flatMap(c -> IntStream.range(0, 7)
						.mapToObj(date::plusDays)
						.flatMap(base -> IntStream.range(0, 100)
								.mapToObj(day -> arguments(c, base, day))));
	}

	/**
	 * Brute-force calculation of calendar date that stands after chosen one by provided business days.
	 *
	 * @param date The chosen date.
	 * @param days The number of business days in the future.
	 * @return The calendar date that outstanding on specified number of business days in the future.
	 */
	private static LocalDate plusBusinessDays(WorkingWeek calendar, LocalDate date, int days) {
		assert null != date: "Date is expected";
		assert 0 <= days: "Addition can't be negative";

		LocalDate result = date;
		while (!calendar.isWorkingDay(result) || HOLIDAYS.isHoliday(result))
			result = result.plusDays(1);
		for (int d = days; d-- > 0; ) {
			result = result.plusDays(1);
			while (!calendar.isWorkingDay(result) || HOLIDAYS.isHoliday(result))
				result = result.plusDays(1);
		}
		return result;
	}

	/**
	 * Brute-force calculation of calendar date that stands before chosen one by provided business days.
	 *
	 * @param date The chosen date.
	 * @param days The number of business days in the past.
	 * @return The calendar date that outstanding on specified number of business days in the past.
	 */
	private static LocalDate minusBusinessDays(WorkingWeek calendar, LocalDate date, int days) {
		assert null != date: "Date is expected";
		assert 0 <= days: "Subtraction can't be negative";

		LocalDate result = date;
		while (!calendar.isWorkingDay(result) || HOLIDAYS.isHoliday(result))
			result = result.minusDays(1);
		for (int d = days; d-- > 0; ) {
			result = result.minusDays(1);
			while (!calendar.isWorkingDay(result) || HOLIDAYS.isHoliday(result))
				result = result.minusDays(1);
		}
		return result;
	}
}
