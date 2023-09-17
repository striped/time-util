package org.kot.workweek;

import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.util.Objects;

/**
 * Implementation of the temporal adjuster on arbitrary number of working day(s) into the future.
 * <p>
 * Implements the {@link TemporalAdjuster} for projecting of the provided temporal by arbitrary number of working days
 * in the future. Tunes the internal state on instantiation accordingly the specified configuration thus each and every
 * consequent usage calculates the requested calendar day in the constant time.
 * <p>
 * Semantics of the {@code 0} working day adjustment equivalent to moving to the nearest working day in the future, if
 * and only if the current day falls on a weekend.
 * <p>
 * This adjuster doesn't consider bank / public holidays that are specific to {@link Holidays cultural region} and must
 * be used in additional.
 *
 * @author <a href="mailto:striped@gmail.com">Kot Behemoth</a>
 * @implSpec Implementation doesn't change its internal state as well as passed temporal, thus is thread safe and can be
 * called concurrently.
 * @created 01/04/2021 00:38
 * @see Holidays
 */
class AfterWorkingDayAdjuster implements TemporalAdjuster {

	/* holds number of working days the temporal should be adjusted forward */
	private final int workDays;

	/* holds adjuster on week start day */
	private final int weekStart;

	/* holds the workweek length, always complementary to {@code #weekendLength} */
	private final int weekLength;

	/* holds the weekend length, always complementary to {@code #weekLength} */
	private final int weekendLength;

	/**
	 * Constructs the working day adjuster in the future with specified workweek parameters.
	 * <p>
	 * Specifying {@code 0} as a working day to adjust to, semantically equivalent creation an adjuster to the nearest
	 * working day in the future. Meant the provided temporal will be adjusted if and only if it falls on a weekend.
	 *
	 * @param workDays   The working days required to adjust provided temporal on each {@code #adjustInto usage}.
	 * @param weekStart  The workweek start day ordinal, must be in [0..7).
	 * @param weekLength The workweek length, must be in (0..7).
	 */
	AfterWorkingDayAdjuster(int workDays, int weekStart, int weekLength) {
		assert 0 <= workDays: "Number of days should be positive or zero";
		assert 0 <= weekStart && weekStart < 7: "Week start must be in [0..7)";
		assert 0 < weekLength && weekLength < 7: "Workweek length should be greater then zero but less then 7";

		this.workDays = workDays;
		this.weekStart = weekStart;
		this.weekLength = weekLength;
		this.weekendLength = 7 - weekLength;
	}

	/**
	 * Adjust the specified temporal by predefined working days in the future.
	 * <p>
	 * Returns the new temporal instance that stands onto arbitrary number of working days (defined on instantiation)
	 * in the future.
	 * <p>
	 * This adjuster doesn't consider bank / public holidays that are specific to {@link Holidays cultural region}.
	 *
	 * @param temporal The temporal to be adjusted.
	 * @return The adjusted temporal, never {@code null}.
	 */
	@Override
	public Temporal adjustInto(Temporal temporal) {
		Objects.requireNonNull(temporal, "Temporal can't be null");

		int start = weekStart + DayOfWeek.from(temporal).ordinal();
		int days = workDays;
		if (6 == start) {
			/* if falls in weekend we rather skip day without attempt to calculate a week count */
			start++;
			days++;
		}
		if (6 < start) start -= 7; // simple modulo 7, no loop as start can't be bigger than 2 * 7
		long weeks = (workDays + start) / weekLength;
		return temporal.plus(days + weekendLength * weeks, ChronoUnit.DAYS);
	}
}
