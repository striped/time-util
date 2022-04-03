package org.kot.workweek;

import java.time.DateTimeException;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.TreeSet;
import java.util.function.Predicate;

/**
 * Holiday calendar SPI representation.
 * <p>
 * Provides general API for holiday calendar service which can be customized through the standard Java SPI
 * (META-INF/services). Main purpose of such service is to provide the client with conventional way to check:
 * <ul><li>is the specific local day is holiday or not and</li>
 * <li>how many holidays are in observed chronological interval</li></ul>
 * and guarantees all in no worse than logarithmic time complexity.
 * <p>
 * Provider implementation responsible for instantiation holiday calendar service ready for usage from a moment just
 * after its construction.
 *
 * @author <a href=mailto:striped@gmail.com>Kot Behemoth</a>
 * @created 03/04/2021 23:35
 */
public abstract class Holidays implements Iterable<ChronoLocalDate> {

	/**
	 * BST for holidays searchable .
	 */
	protected final NavigableSet<ChronoLocalDate> holidays;

	/**
	 * Default constructor.
	 * <p>
	 * Implementation is responsible for configuring the {@link #holidays BST} on its instantiation. Meant, the BST
	 * should be populated with all holidays required for normal usage of this calendar.
	 */
	protected Holidays() {
		holidays = new TreeSet<>(Comparator.comparingLong(ChronoField.EPOCH_DAY::getFrom));
	}

	/**
	 * Holiday calendar factory.
	 * <p>
	 * Instantiate holiday calendar available as service (SPI) in classpath. The {@code predicate} can be used to
	 * select specific instance beyond many registered.
	 *
	 * @param predicate The predicate to testify calendar instance, in case if there are many registered and it is
	 *                  necessary to get specific.
	 * @return The required holiday calendar instance ready for usage.
	 */
	public static Holidays getInstance(Predicate<? super Holidays> predicate) {
		Objects.requireNonNull(predicate, "Holiday calendar predicate is expected");

		for (Holidays service : ServiceLoader.load(Holidays.class))
			if (predicate.test(service))
				return service;
		throw new DateTimeException("Can't find implementation of " + Holidays.class);
	}

	/**
	 * Returns read holidays iterator.
	 *
	 * @return The known holidays iterator.
	 */
	@Override
	public Iterator<ChronoLocalDate> iterator() {
		return holidays.iterator();
	}

	/**
	 * Checks if specified {@code date} is holiday or not.
	 *
	 * @param date The temporal to check up on.
	 * @return {@code true} if and only if the specified {@code date} is a holiday.
	 * @throws DateTimeException    If specified {@code date} doesn't have a date information.
	 * @throws NullPointerException If specified {@code date} is {@code null}.
	 */
	public boolean isHoliday(Temporal date) {
		return holidays.contains(ChronoLocalDate.from(date));
	}

	/**
	 * Calculate the number of holidays inside specified calendar period within by provided {@code start} and {@code
	 * end}.
	 * Returns the number of holidays between specified temporals, according to this calendar. If the {@code start} is
	 * after {@code end} chronologically, the result is {@code 0}.
	 *
	 * @param start The start of period (inclusive).
	 * @param end   The end of period (exclusive).
	 * @return The number of holidays within requested period of time.
	 * @throws DateTimeException    If specified {@code date} doesn't have a date information.
	 * @throws NullPointerException If specified {@code date} is {@code null}.
	 */
	public long holidaysBetween(Temporal start, Temporal end) {
		ChronoLocalDate from = ChronoLocalDate.from(start);
		ChronoLocalDate to = ChronoLocalDate.from(end);
		if (to.isBefore(from)) return 0;
		return holidays.subSet(from, to)
				.size();
	}

	/**
	 * Provides forward temporal adjuster for specific business days according specified workweek configuration.
	 *
	 * @param days        The number of business days to adjust forward.
	 * @param workingWeek The workweek configuration to apply.
	 * @return The temporal adjuster instance.
	 */
	public TemporalAdjuster adjustDaysAfter(int days, WorkingWeek workingWeek) {
		DateAssertions.requirePositive(days, "Number of days");
		Objects.requireNonNull(workingWeek, "Working week is expected");

		TemporalAdjuster adjuster = workingWeek.adjustDaysAfter(days);
		return start -> {
			Temporal end = adjuster.adjustInto(start);
			long d = holidaysBetween(start, end);
			if (isHoliday(end)) d++;
			while (d-- > 0) {
				do end = end.plus(1, ChronoUnit.DAYS);
				while (!workingWeek.isWorkingDay(end) || isHoliday(end));
			}
			return end;
		};
	}

	/**
	 * Provides forward temporal adjuster for specific business days according specified workweek configuration.
	 *
	 * @param days        The number of business days to adjust forward.
	 * @param workingWeek The workweek configuration to apply.
	 * @return The temporal adjuster instance.
	 */
	public TemporalAdjuster adjustDaysBefore(int days, WorkingWeek workingWeek) {
		DateAssertions.requirePositive(days, "Number of days");
		Objects.requireNonNull(workingWeek, "Working week is expected");

		TemporalAdjuster adjuster = workingWeek.adjustDaysBefore(days);
		return end -> {
			Temporal start = adjuster.adjustInto(end);
			long d = holidaysBetween(start, end);
			if (isHoliday(end)) d++;
			while (d-- > 0) {
				do start = start.minus(1, ChronoUnit.DAYS);
				while (!workingWeek.isWorkingDay(start) || isHoliday(start));
			}
			return start;
		};
	}
}
