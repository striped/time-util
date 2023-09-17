#!/usr/bin/env bash

jar=$(find wdcalc/target -iname '*-SNAPSHOT.jar' -type f)

echo 'Checking ClassPath'
java -cp $jar \
     -Dholiday.calendar.url=file:"$(pwd)"/wdcalc/src/test/resources/holidays/ireland-2020.ics \
     org.kot.workweek.Holidays

echo 'Checking JPMS'
java --module-path $jar \
     -Dholiday.calendar.url=file:"$(pwd)"/wdcalc/src/test/resources/holidays/ireland-2020.ics \
     --module kot.time.wdcalc/org.kot.workweek.Holidays