package org.kot.workweek;

import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.util.Objects;

/**
 * Working week configuration representation enum provides tooling for a translation of the working days into
 * the calendar ones and vise versa.
 * <p>
 * In a different cultures around the world, the three sabbath types derive from the culture's main religious tradition:
 * <ul>
 * <li>Friday (Muslim),</li>
 * <li>Saturday (Jewish, Adventist) and</li>
 * <li>Sunday (Christian).</li>
 * </ul>
 * The first more than a single day as a "weekend" arose in Britain since 19th century and was a voluntary arrangement
 * between factory owners and workers allowing Saturday afternoon off. The Oxford English Dictionary traces the 1st use
 * of the term "weekend" to the British magazine "Notes and Queries", 1879. In the 1940s, an increasing number of
 * countries adopted either a Friday–Saturday or a Saturday–Sunday weekend to harmonize with international markets and
 * these process is still going on with overall tendency towards to decreasing work week length. Here is most widely
 * used working week configuration provides tooling for fast translation the arbitrary number of working days into
 * calendar and vise versa.
 *
 * @author <a href="mailto:striped@gmail.com">Kot Behemoth</a>
 * @created 20/04/2022 15:23
 * @see <a href="https://en.wikipedia.org/wiki/Workweek_and_weekend">Work week and weekend</a>
 */
public enum WorkingWeek {

	/**
	 * Traditional (Christian) working week.
	 * <p>
	 * The traditional, 5 days per week working week. Adopted in majority developed countries.
	 */
	MONDAY_FRIDAY {
		@Override
		public boolean isWorkingDay(Temporal date) {
			Objects.requireNonNull(date, "Date is expected");

			return switch (DayOfWeek.from(date)) {
				case SATURDAY, SUNDAY -> false;
				default -> true;
			};
		}

		@Override
		public long workdaysBetween(Temporal start, Temporal end) {
			Objects.requireNonNull(start, "Start date is expected");
			Objects.requireNonNull(end, "End date is expected");

			long days = ChronoUnit.DAYS.between(start, end);
			if (0 >= days) return 0;
			days -= (days + DayOfWeek.from(start).getValue()) / 7 * 2;
			if (DayOfWeek.SUNDAY == DayOfWeek.from(start)) days++;
			if (DayOfWeek.SUNDAY == DayOfWeek.from(end)) days++;
			return days;
		}

		@Override
		public TemporalAdjuster adjustDaysAfter(int days) {
			WorkingDayAssertions.requirePositive(days, "Number of days");

			return new AfterWorkingDayAdjuster(days, DayOfWeek.MONDAY.ordinal(), 5);
		}

		@Override
		public TemporalAdjuster adjustDaysBefore(int days) {
			WorkingDayAssertions.requirePositive(days, "Number of days");

			return new BeforeWorkingDayAdjuster(days, -DayOfWeek.FRIDAY.ordinal(), 5);
		}
	},

	/**
	 * 6 days working week with Saturday as a weekend.
	 * <p>
	 * Shorten than traditional (Christian), a single day weekend working week. Still in use by some developing
	 * countries like Mexico.
	 */
	MONDAY_SATURDAY {
		@Override
		public boolean isWorkingDay(Temporal date) {
			Objects.requireNonNull(date, "Date is expected");

			return DayOfWeek.SUNDAY != DayOfWeek.from(date);
		}

		@Override
		public long workdaysBetween(Temporal start, Temporal end) {
			Objects.requireNonNull(start, "Start date is expected");
			Objects.requireNonNull(end, "End date is expected");

			long days = ChronoUnit.DAYS.between(start, end);
			if (0 >= days) return 0;
			days -= (days + DayOfWeek.from(start).getValue() - 1) / 7;
			return days;
		}

		@Override
		public TemporalAdjuster adjustDaysAfter(int days) {
			WorkingDayAssertions.requirePositive(days, "Number of days");

			return new AfterWorkingDayAdjuster(days, DayOfWeek.MONDAY.ordinal(), 6);
		}

		@Override
		public TemporalAdjuster adjustDaysBefore(int days) {
			WorkingDayAssertions.requirePositive(days, "Number of days");

			return new BeforeWorkingDayAdjuster(days, -DayOfWeek.SATURDAY.ordinal(), 6);
		}
	},

	/**
	 * Jewish working week.
	 * <p>
	 * 5 days working week with sabbaths on Friday and Saturday, adopted by Israel and majority Muslim countries.
	 */
	SUNDAY_THURSDAY {
		@Override
		public boolean isWorkingDay(Temporal date) {
			Objects.requireNonNull(date, "Date is expected");

			return switch (DayOfWeek.from(date)) {
				case FRIDAY, SATURDAY -> false;
				default -> true;
			};
		}

		@Override
		public long workdaysBetween(Temporal start, Temporal end) {
			Objects.requireNonNull(start, "Start date is expected");
			Objects.requireNonNull(end, "End date is expected");

			long days = ChronoUnit.DAYS.between(start, end);
			if (0 >= days) return 0;
			days -= (days + (DayOfWeek.from(start).getValue() + 1) % 7) / 7 * 2;
			if (DayOfWeek.SATURDAY == DayOfWeek.from(start)) days--;
			if (DayOfWeek.SATURDAY == DayOfWeek.from(end)) days++;
			return days;
		}

		@Override
		public TemporalAdjuster adjustDaysAfter(int days) {
			WorkingDayAssertions.requirePositive(days, "Number of days");

			return new AfterWorkingDayAdjuster(days, DayOfWeek.MONDAY.ordinal() + 1, 5);
		}

		@Override
		public TemporalAdjuster adjustDaysBefore(int days) {
			WorkingDayAssertions.requirePositive(days, "Number of days");

			return new BeforeWorkingDayAdjuster(days, -DayOfWeek.FRIDAY.ordinal() + 1, 5);
		}
	},

	/**
	 * 6 days working day per week with sabbaths on Friday.
	 * <p>
	 * Adopted by few Muslim countries.
	 */
	SATURDAY_THURSDAY {
		@Override
		public boolean isWorkingDay(Temporal date) {
			Objects.requireNonNull(date, "Date is expected");

			return DayOfWeek.FRIDAY != DayOfWeek.from(date);
		}

		@Override
		public long workdaysBetween(Temporal start, Temporal end) {
			Objects.requireNonNull(start, "Start date is expected");
			Objects.requireNonNull(end, "End date is expected");

			long days = ChronoUnit.DAYS.between(start, end);
			if (0 >= days) return 0;
			days -= (days + (DayOfWeek.from(start).getValue() + 1) % 7) / 7;
			return days;
		}

		@Override
		public TemporalAdjuster adjustDaysAfter(int days) {
			WorkingDayAssertions.requirePositive(days, "Number of days");

			return new AfterWorkingDayAdjuster(days, DayOfWeek.MONDAY.ordinal() + 2, 6);
		}

		@Override
		public TemporalAdjuster adjustDaysBefore(int days) {
			WorkingDayAssertions.requirePositive(days, "Number of days");

			return new BeforeWorkingDayAdjuster(days, -DayOfWeek.FRIDAY.ordinal() + 1, 6);
		}
	},

	/**
	 * Nepal's working week.
	 * <p>
	 * 6 days working week with sabbaths on Saturday.
	 */
	SUNDAY_FRIDAY {
		@Override
		public boolean isWorkingDay(Temporal date) {
			Objects.requireNonNull(date, "Date is expected");

			return DayOfWeek.SATURDAY != DayOfWeek.from(date);
		}

		@Override
		public long workdaysBetween(Temporal start, Temporal end) {
			Objects.requireNonNull(start, "Start date is expected");
			Objects.requireNonNull(end, "End date is expected");

			long days = ChronoUnit.DAYS.between(start, end);
			if (0 >= days) return 0;
			days -= (days + (DayOfWeek.from(start).getValue() + 1) % 7) / 7;
			if (DayOfWeek.SATURDAY == DayOfWeek.from(start)) days--;
			if (DayOfWeek.SATURDAY == DayOfWeek.from(end)) days++;
			return days;
		}

		@Override
		public TemporalAdjuster adjustDaysAfter(int days) {
			WorkingDayAssertions.requirePositive(days, "Number of days");

			return new AfterWorkingDayAdjuster(days, DayOfWeek.MONDAY.ordinal() + 1, 6);
		}

		@Override
		public TemporalAdjuster adjustDaysBefore(int days) {
			WorkingDayAssertions.requirePositive(days, "Number of days");

			return new BeforeWorkingDayAdjuster(days, -DayOfWeek.FRIDAY.ordinal(), 6);
		}
	},
	;

	/**
	 * Checks if the specified temporal is a working day according to this workweek configuration.
	 *
	 * @param date The temporal that represented the day to check upon.
	 * @return {@code true} if and only if the specified temporal is a working day according to this workweek.
	 * @throws java.time.DateTimeException If temporal has no information about day of the week.
	 * @throws NullPointerException        If temporal is {@code null}.
	 */
	public abstract boolean isWorkingDay(Temporal date);

	/**
	 * Calculate the number of working days inside specified calendar period from the {@code start} up to the {@code end}.
	 * <p>
	 * Calculates the number of working days, excluding weekends, between specified temporals. If the {@code start} is
	 * chronologically after the {@code end}, the result is {@code 0}.
	 * <p>
	 * The typical usage could be like this:
	 * <pre>{@code
	 *      WorkingWeek week = ...
	 *      LocalDate date = ...
	 * 	    long passedDays = week.workdaysBetween(date, LocalDate.now());
	 * }</pre>
	 * Provided temporals must carry {@link ChronoUnit#DAYS} information for such calculation.
	 *
	 * @param start The start of period (inclusive).
	 * @param end   The end of period (exclusive).
	 * @return The number of working days within specified interval of time.
	 * @throws java.time.DateTimeException                         if the amount cannot be calculated, or if either of
	 *                                                             temporal can't be converted to the same type so
	 *                                                             calculation will be possible.
	 * @throws java.time.temporal.UnsupportedTemporalTypeException if either temporal doesn't support
	 *                                                             {@link ChronoUnit#DAYS} unit.
	 * @throws NullPointerException                                If any temporal is {@code null}.
	 * @throws ArithmeticException                                 if numeric overflow occurs
	 * @throws IllegalArgumentException                            If {@code start} temporal is after an {@code end}.
	 */
	public abstract long workdaysBetween(Temporal start, Temporal end);

	/**
	 * Creates temporal adjuster instance for justification of temporal on specified number of {@code days} to the
	 * right (into the future).
	 * <p>
	 * Calculates the new temporal that stands on requested number of working days after specified temporal. The typical
	 * usage could be like following:
	 * <pre>{@code
	 *      WorkingWeek week = ...
	 * 	    ...
	 * 	    LocalDate date = ...
	 * 	    LocalDate reviewDate = date.with(week.adjustDaysAfter(20));
	 * }</pre>
	 * Temporal must have {@link ChronoUnit#DAYS} information for correct calculation.
	 *
	 * @param days The working days to justify after the one it applied to.
	 * @return The requested temporal adjuster instance.
	 * @throws IllegalArgumentException If specified days are negative.
	 */
	public abstract TemporalAdjuster adjustDaysAfter(int days);

	/**
	 * Creates temporal adjuster instance for justification of temporal on specified number of {@code days} left (into
	 * the past).
	 * <p>
	 * Calculates the new temporal that stands on requested number of working days before specified temporal. The
	 * typical usage could be like following:
	 * <pre>{@code
	 *      WorkingWeek week = ...
	 * 	    ...
	 * 	    LocalDate date = ...
	 * 	    LocalDate reviewDate = date.with(week.adjustDaysBefore(20));
	 * }</pre>
	 * Temporal must have {@link ChronoUnit#DAYS} information for correct calculation.
	 *
	 * @param days The working days to justify before the one it applied to.
	 * @return The requested temporal adjuster instance.
	 * @throws IllegalArgumentException If specified days are negative.
	 */
	public abstract TemporalAdjuster adjustDaysBefore(int days);
}