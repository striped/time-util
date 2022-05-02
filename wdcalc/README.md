# Working day utilities

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.striped/wdcalc/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/io.github.striped/wdcalc)
[![Javadoc](https://javadoc.io/badge2/io.github.striped/wdcalc/javadoc.svg?style=plastic)](https://javadoc.io/doc/io.github.striped/wdcalc)

These Working day utilities are implemented for JRE v. 17 and available on Central Maven repository. For a project under
the Maven build, please add following dependency:

```xml
<dependency>
	<groupId>io.github.striped</groupId>
	<artifactId>wdcalc</artifactId>
	<version>[0.0.2)</version>
</dependency>
```

## Usage

### Calendar Day Adjustment

Utilities for the working to calendar day translation realized in form of
[j.t.t.TemporalAdjuster](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/temporal/TemporalAdjuster.html)
to unison with **java.time** API. Once instantiated such adjuster can be re-used concurrently as its inner state won't
change on invocation. For instance, having `date`, the previous 30 working day date, `reviewDate` could be calculated
as:

```java
import java.time.temporal.TemporalAdjuster;
import java.time.LocalDate;
import org.kot.workweek.WorkingWeek;
...        
    static final TemporalAdjuster THIRTY_DAYS_BEFORE = WorkingWeek.MONDAY_FRIDAY.adjustDaysBefore(30);
...
        // somewhere in the code    
        LocalDate date = ...
        LocalDate reviewDate = date.with(THIRTY_DAYS_BEFORE);
```

This adjuster would ignore bank holidays. To consider arbitrary holiday calendar it should be loaded from external
dictionary and holiday aware adjuster can be instantiated with provided factory method:

```java
import java.time.temporal.TemporalAdjuster;
import org.kot.workweek.WorkingWeek;
...
    static final TemporalAdjuster THIRTY_DAYS_BEFORE = Holidays.getInstance(s -> true).adjustDaysBefore(30);
...
        LocalDate date = ...
        LocalDate reviewDate = date.with(THIRTY_DAYS_BEFORE);
```

### Why Before And After?

Worth to note the implied before/after semantic that helps to approach the seeking date from either direction, depends
on requirements. As an example, let's consider the `0` working days adjusting to the previous working day. In other
words, we would like to shift the date left if and only if the current day falls on the weekend:

```java
import java.time.temporal.TemporalAdjuster;
import org.kot.workweek.WorkingWeek;
...
    static final TemporalAdjuster PREV_BUSINESS_DAY = WorkingWeek.MONDAY_FRIDAY.adjustDaysBefore(0);
...
        LocalDate date = LocalDate.now().with(PREV_BUSINESS_DAY); // guaranteed this day or day before weekend
```

Correspondingly, if we would be interested in the guaranteed a next working day:

```java
import java.time.temporal.TemporalAdjuster;
import org.kot.workweek.WorkingWeek;
...
    static final TemporalAdjuster NEXT_BUSINESS_DAY = WorkingWeek.MONDAY_FRIDAY.adjustDaysAfter(0);
...
        LocalDate date = LocalDate.now().with(NEXT_BUSINESS_DAY);
```

### Number of Working Day in the Calendar Interval

The reverse translation, when we would need calculate the number of working days in arbitrary temporal interval,
specified by start and end dates, can be provided by simple utility method:

```java
import java.time.LocalDate;
import org.kot.workweek.WorkingWeek;
...
        LocalDate start = ...
        LocalDate end = ...
        long days = WorkingWeek.MONDAY_FRIDAY.workdaysBetween(start, end);
```

And to consider also holidays we would need justify the result accordingly:

```java
import java.time.LocalDate;
import org.kot.workweek.Holidays;
import org.kot.workweek.ICalHolidays;
...
    static final Holidays HOLIDAYS = Holidays.getInstance(s -> s instanceof ICalHolidays);
...
        LocalDate start = ...
        LocalDate end = ...
        long days = WorkingWeek.MONDAY_FRIDAY.workdaysBetween(start, end) - HOLIDAYS.holidaysBetween(start, end);
```

### The Customizable Holiday Calendar

Holiday calendar is always specific for certain region and defined by the complex religious, cultural or historical
traditions. It means, the calendar instance should be loadable from external source(s) and maximally customizable as
such source(s) could vary from local file up to the REST API call. Thus, the simple service loader facility was
employed to satisfy mentioned above demands. This will provide maximal extensibility with all heavy lifting supplied by
JRE.

```java
import org.kot.workweek.Holidays;
import org.kot.workweek.ICalHolidays;
...
    static final Holidays HOLIDAYS = Holidays.getInstance(s -> s instanceof ICalHolidays);
```

where this factory method realized as:

> ```java
> 	static Holidays getInstance(Predicate<? super Holidays> predicate) {
> 		for (Holidays service : ServiceLoader.load(Holidays.class))
> 			if (predicate.test(service))
> 				return service;
> 	}
> ```

In the code snippet above, the holiday calendar instance was created with provided as parameter a filter `s -> s
instanceof ICalHolidays`. Such predicate would help select the specific calendar beside many others provided in the
Class Path or load all of them, picking them one by one.

Default implementation of the holiday calendar that is provided with this artifact, is `org.kot.workweek.ICalHolidays`.
It can read the files of [iCal format (RFC-5545)](https://tools.ietf.org/html/rfc5545).