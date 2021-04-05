package org.kot.time.workday;

import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;

import static org.kot.time.workday.WorkDayUtil.holidays;

/**
 * Business day adjuster.
 * <p>
 * Adjusts the {@link Temporal temporal} to which it is applied on arbitrary number of business days to left (or right).
 * Assumes that {@link WorkDayUtil#initHolidays holidays calendar} is initialized before any first usage, otherwise do
 * not consider public holidays and considering only week ends as non-business days.
 * <p>
 * Justification performed in time proportional to number of holidays within specified interval and doesn't depends on
 * number of days adjusted. That may be more preferred than dumb iteration with check up whether day is working day or
 * not.
 *
 * @author <a href=mailto:striped@gmail.com>striped</a>
 * @created 01/04/2021 15:38
 */
abstract class WorkdayAdjuster implements TemporalAdjuster {

	final int workDays;

	public WorkdayAdjuster(int days) {
		this.workDays = days;
	}

	private static int weekends(int workDays, int start) {
		int weeks = (start + workDays) / 5;
		return 2 * weeks;
	}

	/**
	 * Business day adjuster (to left).
	 * <p>
	 * Intends to adjust on {@link #workDays} days before the temporal.
	 */
	static class Left extends WorkdayAdjuster {

		public Left(int days) {
			super(days);
		}

		@Override
		public Temporal adjustInto(Temporal temporal) {
			assert null != temporal: "Object of justification is required";

			int initial = 0;
			DayOfWeek dayOfWeek = DayOfWeek.from(temporal);
			switch (dayOfWeek) {
				case SUNDAY:
					initial--;
				case SATURDAY:
					initial--;
					dayOfWeek = DayOfWeek.FRIDAY;
			}

			Temporal result = null;
			int w = weekends(workDays, DayOfWeek.FRIDAY.ordinal() - dayOfWeek.ordinal()), h = 0;
			for (int pw = -1, ph = -1; pw != w || ph != h; ) {
				result = temporal.plus(initial - workDays - w - h, ChronoUnit.DAYS);
				ph = h;
				pw = w;
				h = holidays(result, temporal);
				w = weekends(workDays + h, DayOfWeek.FRIDAY.ordinal() - dayOfWeek.ordinal());
			}

			return result;
		}
	}

	/**
	 * Business day adjuster (to right).
	 * <p>
	 * Intends to adjust on {@link #workDays} days after the temporal.
	 */
	static class Right extends WorkdayAdjuster {

		public Right(int days) {
			super(days);
		}

		@Override
		public Temporal adjustInto(Temporal temporal) {
			assert null != temporal: "Object of justification is required";

			int initial = 0;
			DayOfWeek dayOfWeek = DayOfWeek.from(temporal);
			switch (dayOfWeek) {
				case SATURDAY:
					initial++;
				case SUNDAY:
					initial++;
					dayOfWeek = DayOfWeek.MONDAY;
			}

			Temporal result = null;
			int w = weekends(workDays, dayOfWeek.ordinal()), h = 0;
			for (int pw = -1, ph = -1; pw != w || ph != h; ) {
				result = temporal.plus(initial + workDays + w + h, ChronoUnit.DAYS);
				ph = h;
				pw = w;
				h = holidays(temporal, result);
				w = weekends(workDays + h, dayOfWeek.ordinal());
			}

			return result;
		}
	}
}
