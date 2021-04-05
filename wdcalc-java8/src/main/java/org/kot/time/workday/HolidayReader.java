package org.kot.time.workday;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Set;

/**
 * Holiday calendar reader interface.
 *
 * @author <a href=mailto:striped@gmail.com>striped</a>
 * @created 03/04/2021 23:35
 */
public interface HolidayReader {

	/**
	 * Read the holidays from specified source and store in provided storage.
	 * @param from The source of holiday described in specific format this implementation can handle.
	 * @param to The storage for holidays.
	 * @throws IOException On unexpected I/O failure.
	 */
	void read(InputStream from, Set<? super LocalDate> to) throws IOException;
}
