package org.kot.time.workday;

import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;

import static org.kot.time.workday.WorkDayUtil.holidays;

/**
 * Business day adjuster.
 * <p>
 * Adjusts the {@link Temporal temporal} to which it is applied on arbitrary number of business days skipping weekends
 * and bank holidays if they are defined. To consider only weekends, the holiday calendar might be not initialized.
 * <p>
 * Justification performed in time proportional to number of holidays within effective interval and doesn't depends on
 * number of days. That may be more suitable option than just calendar traversal that checks each and every day.
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
	 * Intends to adjust temporal on specified {@link #workDays} days before.
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
