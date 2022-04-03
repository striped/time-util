/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.kot.workweek;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjuster;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class AdjustWorkdayBenchmark {

	@Benchmark
	public LocalDate adjustAfter(AdjustingState state) {
		return state.start.with(state.daysAfter);
	}

	@Benchmark
	public LocalDate adjustBefore(AdjustingState state) {
		return state.start.with(state.daysAfter);
	}

	@Benchmark
	public LocalDate adjustAfterLoop(AdjustingState state) {
		LocalDate result = state.start;
		while (!WorkingWeek.MONDAY_FRIDAY.isWorkingDay(result)) result = result.plusDays(1);
		for (int d = state.days; d --> 0;) {
			result = result.plusDays(1);
			while (!WorkingWeek.MONDAY_FRIDAY.isWorkingDay(result)) result = result.plusDays(1);
		}
		return result;
	}

	@Benchmark
	public LocalDate adjustBeforeLoop(AdjustingState state) {
		LocalDate result = state.start;
		while (!WorkingWeek.MONDAY_FRIDAY.isWorkingDay(result)) result = result.minusDays(1);
		for (int d = state.days; d --> 0;) {
			result = result.minusDays(1);
			while (!WorkingWeek.MONDAY_FRIDAY.isWorkingDay(result)) result = result.minusDays(1);
		}
		return result;
	}

	@State(Scope.Benchmark)
	public static class AdjustingState {

		@Param({"1", "7", "15", "30", "60"})
		int days;

		LocalDate start;

		TemporalAdjuster daysAfter;

		TemporalAdjuster daysBefore;

		@Setup(Level.Iteration)
		public void setup() {
			start = LocalDate.of(2022, 6, 30);
			daysAfter = WorkingWeek.MONDAY_FRIDAY.adjustDaysAfter(days);
			daysBefore = WorkingWeek.MONDAY_FRIDAY.adjustDaysBefore(days);
		}
	}
}
