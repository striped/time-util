/**
 * Working day calculator module descriptor.
 *
 * @author <a href="mailto:striped@gmail.com">Kot Behemoth</a>
 * @created 20/04/2022 15:21
 */
module kot.time.wdcalc {
	uses org.kot.workweek.Holidays;
	exports org.kot.workweek;
	provides org.kot.workweek.Holidays
			with org.kot.workweek.ICalHolidays;

}