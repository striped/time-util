package org.kot.time.workday;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.DayOfWeek;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;

/**
 * Business day calculation utilities.
 *
 * @author <a href=mailto:striped@gmail.com>striped</a>
 * @created 01/04/2021 16:02
 */
public class WorkDayUtil {

	private static final NavigableSet<Temporal> HOLS = new TreeSet<>(Comparator.comparingLong(ChronoField.EPOCH_DAY::getFrom));

	private static final HolidayICalReader I_CAL_READER = new HolidayICalReader();

	/**
	 * Default constructor to prevent explicit instantiation.
	 */
	private WorkDayUtil() {
	}

	/**
	 * Initialize holiday set from specified URL.
	 * <p>
	 * The passed URL identifies holiday calendar in iCal format (RFC 5545). Those holidays will be read and translated
	 * into internal representation suitable for fast lookup.
	 * <p>
	 * Any holidays initialized before will be purged.
	 *
	 * @param url The URL of holiday calendar to read.
	 * @throws IOException On any unexpected I/O failure during calendar translation.
	 */
	public static void initHolidays(URL url) throws IOException {
		initHolidays(url, I_CAL_READER);
	}

	/**
	 * Initialize holiday set from specified URL with provided holiday reader.
	 * <p>
	 * The passed URL identifies holiday calendar in any format that can be managed by provided {@code reader}. Those
	 * holidays might be read and correctly translated into internal representation suitable for fast lookup.
	 * <p>
	 * Any holidays initialized before will be purged.
	 *
	 * @param url    The URL of holiday calendar to read from.
	 * @param reader The reader instance can translate the calendar representation into internal representation.
	 * @throws IOException On any unexpected I/O failure during calendar translation.
	 */
	public static void initHolidays(URL url, HolidayReader reader) throws IOException {
		HOLS.clear();
		reader.read(url.openStream(), HOLS);
	}

	/**
	 * Initialize holiday set from specified local file system.
	 * <p>
	 * The passed {@code path} may point out on file or folder that contains holidays in iCal format (RFC 5545). Those
	 * holidays will be read and translated into internal representation suitable for fast lookup.
	 * <p>
	 * Any holidays initialized before will be purged.
	 *
	 * @param path The path to file (or folder) to read holiday calendar from.
	 * @throws IOException On any unexpected I/O failure during calendar translation.
	 */
	public static void initHolidays(Path path) throws IOException {
		initHolidays(path, I_CAL_READER);
	}

	/**
	 * Initialize holiday set from specified local file system w/ provided holiday reader.
	 * <p>
	 * The passed {@code path} may point out on file or folder that contains holidays in any format that can be managed
	 * by provided {@code reader}. Those holidays might be read and correctly translated into internal representation
	 * suitable for fast lookup.
	 * <p>
	 * Any holidays initialized before will be purged.
	 *
	 * @param path   The path to file (or folder) to read holiday calendar from.
	 * @param reader The reader instance can translate the calendar representation into internal representation.
	 * @throws IOException On any unexpected I/O failure during calendar translation.
	 */
	public static void initHolidays(Path path, HolidayReader reader) throws IOException {
		HOLS.clear();
		if (Files.isDirectory(path)) {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					reader.read(Files.newInputStream(file), HOLS);
					return FileVisitResult.CONTINUE;
				}
			});
			return;
		}
		reader.read(Files.newInputStream(path), HOLS);
	}

	/**
	 * Creates workday adjuster instance for justification of temporal on specified number of {@code days} before one.
	 * <p>
	 * Calculates the new temporal that stands on requested number of days before applicable temporal. The typical usage
	 * could be like this:
	 * <pre>{@code
	 * 		final TemporalAdjuster THIRTY_DAYS_BEFORE = WorkDayUtil.beforeBusinessDays(30);
	 * 	    ...
	 * 	    LocalDate date = ...
	 * 	    LocalDate reviewDate = date.with(THIRTY_DAYS_BEFORE);
	 * }</pre>
	 * Temporal must have {@link ChronoUnit#DAYS} information for correct calculation.
	 *
	 * @param days The business days to justify before the applied.
	 * @return The temporal adjuster instance.
	 * @throws IllegalArgumentException If specified days are negative.
	 * @see #initHolidays(Path)
	 * @see #initHolidays(URL)
	 * @see #initHolidays(Path, HolidayReader)
	 * @see #initHolidays(URL, HolidayReader)
	 */
	public static TemporalAdjuster beforeBusinessDays(int days) {
		requirePositive(days);
		return new WorkdayAdjuster.Left(days);
	}

	/**
	 * Creates workday adjuster instance for justification of temporal on specified number of {@code days} after.
	 * <p>
	 * Calculates the new temporal that stands on requested number of days after applicable temporal. The typical usage
	 * could be like this:
	 * <pre>{@code
	 * 		final TemporalAdjuster THIRTY_DAYS_AFTER = WorkDayUtil.beforeBusinessDays(30);
	 * 	    ...
	 * 	    LocalDate reviewDate = LocalDate.now().with(THIRTY_DAYS_AFTER);
	 * }</pre>
	 * Temporal must have {@link ChronoUnit#DAYS} information for correct calculation.
	 *
	 * @param days The business days to justify after the applied.
	 * @return The temporal adjuster instance.
	 * @throws IllegalArgumentException If specified days are negative.
	 * @see #initHolidays(Path)
	 * @see #initHolidays(URL)
	 * @see #initHolidays(Path, HolidayReader)
	 * @see #initHolidays(URL, HolidayReader)
	 */
	public static TemporalAdjuster afterBusinessDays(int days) {
		requirePositive(days);
		return new WorkdayAdjuster.Right(days);
	}

	/**
	 * Calculate the number of business days inside specified calendar period.
	 * <p>
	 * Calculates the number of business days, excluding weekends and bank holidays (if holiday calendar is
	 * initialized), between two temporals provided.
	 * <p>
	 * The typical usage could be like this:
	 * <pre>{@code
	 *      LocalDate date = ...
	 * 	    long passedDays = WorkDayUtil.workdaysBetween(date, LocalDate.now());
	 * }</pre>
	 * Temporals must have {@link ChronoUnit#DAYS} information for correct calculation.
	 *
	 * @param start The start of period (inclusive), must be before .
	 * @param end   The end of period (exclusive).
	 * @return The number of business day in requested period.
	 * @throws java.time.DateTimeException If temporal has no information about date.
	 * @throws NullPointerException        If temporal is {@code null}.
	 * @throws IllegalArgumentException    If {@code start} temporal is after an {@code end}.
	 * @see #initHolidays(Path)
	 * @see #initHolidays(URL)
	 * @see #initHolidays(Path, HolidayReader)
	 * @see #initHolidays(URL, HolidayReader)
	 */
	public static long workdaysBetween(Temporal start, Temporal end) {
		Objects.requireNonNull(start, "Start is expected");
		Objects.requireNonNull(end, "End is expected");

		long days = ChronoUnit.DAYS.between(start, end);
		if (0 == days) return 0;
		days -= (days + DayOfWeek.from(start).getValue()) / 7 * 2;
		if (DayOfWeek.SUNDAY == DayOfWeek.from(start)) days++;
		if (DayOfWeek.SUNDAY == DayOfWeek.from(end)) days++;
		days -= HOLS.subSet(start, true, end, false).size();
		return days;
	}

	/**
	 * Checks if specified day is a weekend.
	 *
	 * @param date The temporal to check on.
	 * @return {@code true} if and only if specified date is a weekend (i.e. {@link DayOfWeek#from(TemporalAccessor)
	 * day of week} is "Saturday" or "Sunday").
	 * @throws java.time.DateTimeException If temporal has no information about day of week.
	 * @throws NullPointerException        If temporal is {@code null}.
	 */
	public static boolean isWeekend(Temporal date) {
		Objects.requireNonNull(date, "Date is expected");

		switch (DayOfWeek.from(date)) {
			case SATURDAY:
			case SUNDAY:
				return true;
		}
		return false;
	}

	/**
	 * Checks if specified day is holiday (according to calendar).
	 * <p>
	 * Requires the holiday calendar being initialized before usage. Otherwise will return {@code false} always.
	 *
	 * @param date The temporal to check on.
	 * @return {@code true} if and only if specified date is a holiday according to calendar.
	 * @throws java.time.DateTimeException If temporal has no information about date.
	 * @throws NullPointerException        If temporal is {@code null}.
	 * @see #initHolidays(Path)
	 * @see #initHolidays(URL)
	 * @see #initHolidays(Path, HolidayReader)
	 * @see #initHolidays(URL, HolidayReader)
	 */
	public static boolean isHoliday(Temporal date) {
		Objects.requireNonNull(date, "Date is expected");

		return HOLS.contains(date);
	}

	static int holidays(Temporal start, Temporal end) {
		assert null != start: "Start is expected";
		assert null != end: "End is expected";

		return HOLS.subSet(start, true, end, true).size();
	}

	private static void requirePositive(int days) {
		if (0 > days) throw new IllegalArgumentException("Specified days can't be negative");
	}
}
