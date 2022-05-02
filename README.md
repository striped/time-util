# 🇺🇦 STAND WITH UKRAINE

---

> "russia" and its accomplice "belarus" invaded Ukraine. It was an act of unprovoked aggression condemned by the whole
> civilized world. They have committed atrocities towards Ukrainian civilians and this does not leave any doubt that
> it is
> genocide.
>
> ***Strong support of such unjustifiable aggression from the russian people indicates this is not just a one mad person
> problem; it is rather the nation's issue. Arguments regarding propaganda and autocratic governance isn't justifiable:
> all these
> crimes are committed by people identifying themselves as russian, consciously and by free will.***
>
> **Thus, as author of this project, I shall ask the user to refrain from any usage / distribution / modification of
> any piece of this project:**
> * **by people who consider themselves as russian,**
> * **by organizations, commercial and non-commercial that employ people who consider themselves as russian,**
> * **by organizations, commercial and non-commercial, located territorially in belarus and in russia.**
>
> Any requests for clarification / modification from people and / or organizations mentioned above, will be ignored.

---

# Working day arithmetic & etc

This utility tool provides basic functionality for the calculation of the working days to the corresponding calendar
date; namely translating working days into calendar dates and vise versa. Often it is necessary for business
applications to follow only labour days, specifics to that or other religious or cultural traditions of the designated
region.

Intention to offer this to the public was dictated due to the lack of such functionality in standard and / or widely
adopted tools. Existing solutions are based on brute force iteration that is working well but still have questionable
efficacy. Albeit, the simple arithmetic of such translation between the calendar days and working ones is
straightforward.

[Working day arithmetics] [workweek] for a traditional 5 days workweek (i.e. Christian) based on the following facts
that are observed always and are true by definition:

* Each year has the exact number of weeks, 52,
* Each week has 7 calendar days,
* Each week has 5 working days.

Thus, translation between the calendar and the working days might be as trivial as the relation observed as a stepped
integer dependency between days devoted to labour and the calendar days. Indeed, it is essentially sufficient to add
the weekend each time when the number of calendar days exceed the working week length. Saying that, the formulas for
the traditional five working day week translations should look like:

```math
<Calendar Days> = ⌊ (<Day of Week> + <Working Days>) / 5 ⌋ * 7
<Working Days> = ⌊ (<Day of Week> + <Calendar Days>) / 7 ⌋ * 5
```

The similar thoughts can be applied for any other existing workweek: 6 days per week or with sabbath that falls on
Friday and Saturday only. Albeit, small adjustment to the calculations is necessary to make the stepped function to
work when the numerical value of the weekend's *&lt;Day of Week&gt;* doesn't fall to the end of the week. However, *all
such calculations might be done in constant time, without looping through all the days in question to check whether it
is a labour day or not.*

However, the holidays can't be calculated as easily as above. Some of them don't have a fixed calendar date and are
floating from year to year. For instance, Easter is observable on the first Sunday after a full Moon occurring on or
after a Spring Equinox. In terms of Gregorian calendar this day varies between 22 of March and 25 of April. Albeit, the
exact day is possible to calculate, such exercise will not be satisfactory, given it is the only holiday beside many,
across different cultures and traditions. Another aspect of this matter lies in how such details would be used for the
translation of working day(s) into the calendar. The solution discussed above helps to consider weekends in the
required calendar period and for holidays it would be sufficient to check how many days between the start and the end
of calendar dates. Thus, the static dictionary approach would look suitable in such circumstances. To simply check how
many holidays fall into a questionable calendar interval, we may adjust it to satisfy an arbitrary number of requested
working days. This *"holiday dictionary"*, implemented in a form of balanced BST, would do this in the logarithmic time
for an arbitrary chronological period, limited only by business requirements and / or the available heap. Once loaded
and balanced, it can be used many times, within guaranteed computational complexity.

## Releasing

The project is under [Maven build tool][maven] thus, the whole releasing process is standard routine well described in 
[maven plugin][maven-release-plugin]. After the artifact can be promoted to Central repo as described [here][sonatype-oss].

## See also

* [Working day arithmetics artifact](wdcalc/README.md)
* [Working day arithmetics benchmarck](wdcalc-perf/README.md)
* [Working day arithmetics (for Java 8) artifact](wdcalc-java8/README.md)
* [Working day arithmetics (for Java 8) benchmarck](wdcalc-perf8/README.md)

[workweek]: https://en.wikipedia.org/wiki/Workweek_and_weekend "Workweek &amp; weekend"
[maven]: https://maven.apache.org "Maven"
[maven-release-plugin]: https://maven.apache.org/maven-release/maven-release-plugin/examples/non-interactive-release.html "Maven Release plugin HowTo"
[sonatype-oss]: https://central.sonatype.org/publish/release/#locate-and-examine-your-staging-repository "Sonatype releasing guide to Central"
