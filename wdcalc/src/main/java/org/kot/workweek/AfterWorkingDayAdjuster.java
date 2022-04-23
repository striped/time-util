package org.kot.workweek;

import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;

/**
 * Working day(s) to future temporal adjuster implementation.
 * <p>
 * Implements the {@link TemporalAdjuster} for adjusting the provided temporal on arbitrary specified number of working
 * days in the future. Implementation is tuning the internal state, accordingly to the specified workweek and all
 * consequent calls to adjust the provided temporal just reuse that state for fast calculation of the calendar day that
 * correspond that.
 *
 * @author <a href="mailto:striped@gmail.com">Kot Behemoth</a>
 * @created 20/04/2022 15:23
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
	 * Construct forward working day adjuster with specified workweek parameters.
	 *
	 * @param workDays   The working days required to adjust provided temporal on each {@code #adjustInto} invocation.
	 * @param weekStart  The workweek start day ordinal.
	 * @param weekLength The workweek length.
	 */
	AfterWorkingDayAdjuster(int workDays, int weekStart, int weekLength) {
		assert 0 <= workDays: "Number of days should be positive";
		assert 0 <= weekLength: "Workweek length should be positive";

		this.workDays = workDays;
		this.weekStart = weekStart;
		this.weekLength = weekLength;
		this.weekendLength = 7 - weekLength;
	}

	/**
	 * Adjust the specified temporal by adding pre-configured number of working days.
	 * <p>
	 * Returns the new temporal instance that stands onto arbitrary number of working days (specified on instantiation
	 * of this adjuster).
	 * <p>
	 * Doesn't consider bank / public holidays, only workweek preset.
	 *
	 * @param temporal The temporal to be adjusted.
	 * @return The adjusted temporal, never {@code null}.
	 */
	@Override
	public Temporal adjustInto(Temporal temporal) {
		int start = weekStart + DayOfWeek.from(temporal).ordinal();
		int days = workDays;
		if (6 == start) {
			/* if that is a middle of weekend we would rather shift day without attempt to calculate a week */
			start++;
			days++;
		}
		if (6 < start)
			start -= 7; // just modulo 7, no while as start can't be big then 2 * 7
		long weeks = (workDays + start) / weekLength;
		return temporal.plus(days + weekendLength * weeks, ChronoUnit.DAYS);
	}
}
