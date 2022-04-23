package org.kot.workweek;

/**
 * Utility for date assertions routines.
 *
 * @author <a href=mailto:striped@gmail.com>Kot Behemoth</a>
 * @created 20/04/2022 15:23
 */
class WorkingDayAssertions {

	/**
	 * Default constructor.
	 * <p>
	 * To prevent explicit instantiation.
	 */
	private WorkingDayAssertions() {
	}

	/**
	 * Asserts that specified number is greater or equals to {@code 0}.
	 *
	 * @param number      The number to perform check upon.
	 * @param description The number description, in case of assertion violation will be printed as violation details.
	 * @throws IllegalArgumentException If provided number id less then {@code 0}.
	 */
	public static void requirePositive(int number, String description) {
		if (0 > number)
			throw new IllegalArgumentException(description + " can't be negative");
	}
}
