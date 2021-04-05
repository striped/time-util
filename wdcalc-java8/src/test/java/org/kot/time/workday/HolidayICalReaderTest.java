package org.kot.time.workday;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.iterableWithSize;

/**
 * iCal holiday calendar reader test suite.
 *
 * @author <a href=mailto:striped@gmail.com>striped</a>
 * @created 02/04/2021 19:05
 */
public class HolidayICalReaderTest {

	@ParameterizedTest(name = "[{index}] Verify {1} has {0} holidays")
	@MethodSource("params")
	public void test(int expectedDays, URL uri) throws IOException {
		NavigableSet<LocalDate> holidays = new TreeSet<>();
		new HolidayICalReader().read(uri.openStream(), holidays);
		assertThat(holidays, iterableWithSize(expectedDays));
	}

	static Stream<Arguments> params() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		return Stream.of(
				Arguments.arguments(9, loader.getResource("holidays/ireland-2020.ics")),
				Arguments.arguments(9, loader.getResource("holidays/ireland-2021.ics")),
				Arguments.arguments(9, loader.getResource("holidays/ireland-2022.ics"))
		);
	}
}
