# 🇺🇦 STAND WITH UKRAINE

---

> "russia" and its accomplice "belarus" invaded Ukraine. It was an act of unprovoked aggression condemned by whole
> civilized world. Committed atrocity towards to Ukrainian civilian does not leave any doubts that it is genocide.
> 
> ***Strong support such unjustifiable aggression beyond russian indicates this is not an alone mad person problem;
> it is rather nation level issue. Arguments regard to propaganda and autocratic governance doesn't sound: all these
> crimes committed by people with same self-identification consciously and in free will.***
> 
> **Thus, as author of this project, I ask to refrain from usage / distribution / modification of any piece of this:**
> * **by people who consider themselves russian,**
> * **by organization, commercial and non-commercial that employ people who consider themselves russian,**
> * **by organization, commercial and non-commercial, located in belarus territory.**
>
> Any requests for clarification / modification from mentioned people and / or organizations will be ignored.

---

# time-util

Date/Time related utilities provides basic arithmetics around working days into calendar ones translation and vise
versa. Quite often necessary in application using business continuation considering devoted only to labour calendar
days, but specifics to that or another cultural essentials of designated region.

The origin of this tool was dictated by the lack of such functionality in standard and / or widely adopted tools. Most
existing solutions are based on brute force iteration that is working well, but has questionable efficacy. Albeit, the
simple arithmetic of such translation between the standard calendar days and working ones are apparent and
straightforward, it may wonder of such variety of implementations.

Working day arithmetics, for traditional (Christian) 5 days workweek based on following facts:

* Each year has the very number of weeks, 52, always,
* Each week has 7 calendar days,
* Each week has 5 working days.

Thus, translation between calendar and working days might be the trivial task of simple integer arithmetic of a stepped
dependency between days devoted to labour and the rest. Indeed, it is necessary add the weekend each time when number of
calendar days exceed working week length. Thus, the formula for traditional five working day week translation, would
look like:

```math
calendar days = ⌊ (DoW + working days) / 5 ⌋ * 7
working days = ⌊ (DoW + calendar days) / 7 ⌋ * 5
```

It should not raise the eyebrow, the similar calculation can be applied for any other existing workweek, 6 days per week
or with sabbath that falls on Friday and Saturday. Amazingly, all those calculations may be done in constant time,
without looping through all days in question, in attempt to check whether it labour day or not.

Except, perhaps the holidays. Some holidays don't have a fixed calendar date, has quite complex determination based on
something beyond the simple arithmetic. Like the Easter that observable in first Sunday after full Moon occurring on or
after Spring Equinox. Therefore, the static dictionary is imminent just for such moveable days for their observability
for each year. Yet such complication may be solved in logarithmic time with standard BST. To adjust to the holidays
happen on calculated calendar interval is trivial: ordinal of subset in ordered tree falling on calendar in question. It
is a bit tricky when calendar interval is calculated based on specified working days interval. Here we might do
calculation multiple times. Each time the interval is adjusted by weekends and holidays happen to fall inside interval.
However, it is easy to convince, such recalculation is limited as each iteration adjustment will not exceed working week
length.

# Usage

Utilities for calendar days calculation realized in form of j.t.t.TemporalAdjuster for convenience of usage:

```java
static final TemporalAdjuster THIRTY_DAYS_BEFORE=WorkingWeek.MONDAY_FRIDAY.adjustDaysBefore(30);
		...
		LocalDate date=...
		LocalDate reviewDate=date.with(THIRTY_DAYS_BEFORE);
// reviewDate is exact 30 working days before the date
```

Implied before/after semantic may be useful in necessity approach the target day from either side. As example, let's
consider adjusting to `0` working days. For instance, to adjust to previous working day:

```java
static final TemporalAdjuster PREV_BUSINESS_DAY=WorkingWeek.MONDAY_FRIDAY.adjustDaysBefore(0);
		...
		LocalDate date=LocalDate.now().with(PREV_BUSINESS_DAY);
// the date guaranteed be this day or before, if today is weekend
```

and if target date is today or the future:

```java
static final TemporalAdjuster NEXT_BUSINESS_DAY=WorkingWeek.MONDAY_FRIDAY.adjustDaysAfter(0);
		...
		LocalDate date=LocalDate.now().with(NEXT_BUSINESS_DAY);
// the date guaranteed be this day or after, if today is weekend
```

* [Workweek and weekend](https://en.wikipedia.org/wiki/Workweek_and_weekend)
* [Working day arithmetics for Java 8](wdcalc-java8/README.md)
