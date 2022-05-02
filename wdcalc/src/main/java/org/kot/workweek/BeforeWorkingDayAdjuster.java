package org.kot.workweek;

import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;

/**
 * Implementation of the temporal adjuster on arbitrary number of working day(s) into the past.
 * <p>
 * Implements the {@link TemporalAdjuster} for adjusting the provided temporal on arbitrary number of working days into
 * the past. Tuning the internal state on instantiation accordingly to the specified workweek configuration thus each
 * and every consequent invocation to adjust the provided temporal instance calculates the requested calendar day in the
 * constant time.
 * <p>
 * Semantics of the {@code 0} working day adjustment equivalent to moving to the nearest working day into the past, if
 * and only if current day falls on weekend.
 *
 * @author <a href="mailto:striped@gmail.com">Kot Behemoth</a>
 * @implSpec Implementation doesn't change its internal state as well as passed temporal, thus is thread safe and can be
 * called concurrently.
 * @created 20/04/2022 15:23
 */
class BeforeWorkingDayAdjuster implements TemporalAdjuster {

	/* holds number of working days the temporal should be adjusted back in time */
	private final int workDays;

	/* holds adjuster on week start day */
	private final int weekStart;

	/* holds the workweek length, always complementary to {@code #weekendLength} */
	private final int weekLength;

	/* holds the weekend length, always complementary to {@code #weekLength} */
	private final int weekendLength;

	/**
	 * Constructs the working day adjuster in the past with specified workweek parameters.
	 * <p>
	 * Specifying {@code 0} as working day to adjust to, equivalent to creation adjuster on nearest working day in
	 * past. Meant the provided temporal will be adjusted if and only if it falls on weekend.
	 *
	 * @param workDays   The working days required to adjust provided temporal on each {@code #adjustInto usage}.
	 * @param weekStart  The negative workweek start day ordinal, must be in (-7..0].
	 * @param weekLength The workweek length, must be in (0..7).
	 */
	public BeforeWorkingDayAdjuster(int workDays, int weekStart, int weekLength) {
		assert 0 <= workDays: "Number of days should be positive or zero";
		assert -7 < weekStart && weekStart <= 0: "Week start must be in (-7..0]";
		assert 0 < weekLength && weekLength < 7: "Workweek length should be greater then zero but less then 7";

		this.workDays = workDays;
		this.weekStart = weekStart;
		this.weekLength = weekLength;
		this.weekendLength = 7 - weekLength;
	}

	/**
	 * Adjust the specified temporal by subtracting pre-configured number of working days.
	 * <p>
	 * Returns the new temporal instance that stands in past onto arbitrary number of working days (specified on
	 * instantiation of this adjuster).
	 * <p>
	 * This adjuster doesn't consider bank / public holidays that are specific to {@link Holidays cultural region}.
	 *
	 * @param temporal The temporal to be adjusted.
	 * @return The adjusted temporal, never {@code null}.
	 */
	@Override
	public Temporal adjustInto(Temporal temporal) {
		int start = weekStart + DayOfWeek.from(temporal).ordinal();
		int days = -workDays;
		if (weekendLength < start) start -= 7;
		if (0 < start) {
			days -= start;
			start = 0;
		}
		long weeks = (start - workDays) / weekLength;
		return temporal.plus(days + weekendLength * weeks, ChronoUnit.DAYS);
	}
}
