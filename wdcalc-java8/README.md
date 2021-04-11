# Business day utilities (for Java 8)

Business day arithmetics based on following facts:

* Each year has the same number of weeks, 52, always,
* Each week has 7 calendar days,
* Each week has 5 working days.

Thus, translation between calendar and business days might be trivial task applying corresponding multiplier:

```math
days = workdays / 5 * 7
workdays = days / 7 * 5
```

There is only one anomaly: holidays. The worse part, some holidays don't have a fixed date, like the Easter, and are 
different in different countries. Therefore, the calendar is imminent.

To adjust to the holidays happen on calculated calendar interval is also trivial: ordinal of subset in ordered tree. It
is a bit tricky when calendar interval is calculated as result. Here we might calculate it multiple times, each time
the interval is adjusted by calculated weekends. However, it is easy to convince yourself that such recalculation may 
happen at most twice for each such holiday.

Utilities for calendar days calculation implemented in form of j.t.t.TemporalAdjuster and is convenient in usage:

```java
static final TemporalAdjuster THIRTY_DAYS_BEFORE = WorkDayUtil.beforeBusinessDays(30);
...
LocalDate date = ...
LocalDate reviewDate = date.with(THIRTY_DAYS_BEFORE);
// reviewDate is exact 30 business days before the date
```

Implied before/after semantic may be useful in align to left or right even in case if day should be adjusted to `0` 
business days. For instance, to adjust to previous business day:

```java
static final TemporalAdjuster PREV_BUSINESS_DAY = WorkDayUtil.beforeBusinessDays(0);
...
LocalDate date = LocalDate.now().with(PREV_BUSINESS_DAY);
// the date guaranteed be this day or before, if today is weekend of bank holiday
```

and if date in question is only today or in future:

```java
static final TemporalAdjuster NEXT_BUSINESS_DAY = WorkDayUtil.beforeBusinessDays(0);
...
LocalDate date = LocalDate.now().with(NEXT_BUSINESS_DAY);
// the date guaranteed be this day or after, if today is weekend of bank holiday
```
