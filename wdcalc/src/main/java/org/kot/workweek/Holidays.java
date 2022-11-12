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
 * Holiday calendar SPI.
 * <p>
 * Provides the service factory and the general holiday calendar API contract. Implementation should be discoverable
 * through the standard Java SPI mechanism (i.e. provides ... with). With the {@link #getInstance(Predicate) optional
 * filter} client may select the specific instance beside many potentially available in a modules.
 * <p>
 * Intentions of such holiday calendar service is to provide the client with a conventional way to check:
 * <ol>
 *     <li>is the specific local day is holiday or not (see {@link #isHoliday(Temporal)}),</li>
 *     <li>how many holidays are in observed chronological interval (see {@link #holidaysBetween(Temporal, Temporal)})
 *     and</li>
 *     <li>instantiate temporal adjuster for arbitrary number of working days {@link #adjustDaysBefore(int, WorkingWeek)
 *     into the past} as well as {@link #adjustDaysAfter(int, WorkingWeek) into the future}.</li>
 * </ol>
 * All those operations implemented in the base class, the only responsibility for the implementation is to fulfill the
 * holidays from external dictionary.
 *
 * @author <a href="mailto:striped@gmail.com">Kot Behemoth</a>
 * @implNote All provided API operations with runtime no worse than logarithmic computational complexity.
 * @created 20/04/2022 15:23
 */
public abstract class Holidays implements Iterable<ChronoLocalDate> {

	/**
	 * BST for holidays searchable .
	 */
	protected final NavigableSet<ChronoLocalDate> holidays;

	/**
	 * Default constructor.
	 * <p>
	 * Implementation is responsible for population the {@link #holidays BST} on its instantiation. Meant, the BST must
	 * be populated with all holidays required for normal usage of this calendar immediately after its construction.
	 */
	protected Holidays() {
		holidays = new TreeSet<>(Comparator.comparingLong(ChronoField.EPOCH_DAY::getFrom));
	}

	/**
	 * Default holiday calendar factory.
	 * <p>
	 * Instantiate the first available in classpath holiday calendar.
	 *
	 * @return The required holiday calendar instance ready for usage.
	 * @apiNote Implementations must provide ready for usage instance from a moment of constructions. All consequent calls
	 * of teh provided API should not change its internal state thus.
	 */
	public static Holidays getInstance() {
		return getInstance(s -> true);
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
			if (predicate.test(service)) return service;
		throw new DateTimeException("Can't find implementation of " + Holidays.class);
	}

	/**
	 * Returns known holidays iterator.
	 * <p>
	 * Provided iterator of all known holidays in chronological order.
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
		WorkingDayAssertions.requirePositive(days, "Number of days");
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
		WorkingDayAssertions.requirePositive(days, "Number of days");
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
