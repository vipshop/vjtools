package com.vip.vjtools.vjkit.time;

import static org.assertj.core.api.Assertions.*;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

public class DateUtilTest {

	@Test
	public void isSameDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2006, Calendar.OCTOBER, 1, 0, 0, 0);
		Date date1 = new Date(calendar.getTime().getTime());

		calendar.set(2006, Calendar.OCTOBER, 1, 12, 23, 44);
		Date date2 = new Date(calendar.getTime().getTime());
		assertThat(DateUtil.isSameDay(date1, date2)).isTrue();

		calendar.set(2006, Calendar.OCTOBER, 1, 0, 0, 0);
		Date date3 = new Date(calendar.getTime().getTime());

		assertThat(DateUtil.isSameTime(date1, date3)).isTrue();

		calendar.set(2006, Calendar.OCTOBER, 2);
		Date date5 = calendar.getTime();

		assertThat(DateUtil.isSameTime(date1, date5)).isFalse();

		calendar.set(2006, Calendar.OCTOBER, 1, 12, 23, 43);
		Date date4 = calendar.getTime();
		assertThat(DateUtil.isBetween(date3, date1, date2)).isTrue();
		assertThat(DateUtil.isBetween(date4, date1, date2)).isTrue();

		try {
			DateUtil.isBetween(null, date1, date2);
			fail("should fail before");
		} catch (Exception e) {
			assertThat(e.fillInStackTrace() instanceof IllegalArgumentException).isTrue();
		}

		try {
			DateUtil.isBetween(date3, date2, date1);
			fail("should fail before");
		} catch (Exception e) {
			assertThat(e.fillInStackTrace() instanceof IllegalArgumentException).isTrue();
		}

		assertThat(DateUtil.isBetween(date5, date1, date2)).isFalse();
	}

	@Test
	public void truncateAndCelling() {
		Calendar calendar = Calendar.getInstance();
		// 设置为2017年1月21日12点12分12秒
		calendar.set(2017, Calendar.JANUARY, 21, 12, 12, 12);

		Date date = calendar.getTime();
		calendar.set(2017, Calendar.JANUARY, 1, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date beginYear = calendar.getTime();
		calendar.set(2017, Calendar.DECEMBER, 31, 23, 59, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		Date endYear = calendar.getTime();
		calendar.set(2018, Calendar.JANUARY, 1, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date nextYear = calendar.getTime();
		calendar.set(2017, Calendar.JANUARY, 1, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date beginMonth = calendar.getTime();
		calendar.set(2017, Calendar.JANUARY, 31, 23, 59, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		Date endMonth = calendar.getTime();
		calendar.set(2017, Calendar.FEBRUARY, 1, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date nextMonth = calendar.getTime();
		calendar.set(2017, Calendar.JANUARY, 16, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date beginWeek = calendar.getTime();
		calendar.set(2017, Calendar.JANUARY, 22, 23, 59, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		Date endWeek = calendar.getTime();
		calendar.set(2017, Calendar.JANUARY, 23, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date nextWeek = calendar.getTime();
		calendar.set(2017, Calendar.JANUARY, 21, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date beginDate = calendar.getTime();
		calendar.set(2017, Calendar.JANUARY, 21, 23, 59, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		Date endDate = calendar.getTime();
		calendar.set(2017, Calendar.JANUARY, 22, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date nextDate = calendar.getTime();
		calendar.set(2017, Calendar.JANUARY, 21, 12, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date beginHour = calendar.getTime();
		calendar.set(2017, Calendar.JANUARY, 21, 12, 59, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		Date endHour = calendar.getTime();
		calendar.set(2017, Calendar.JANUARY, 21, 13, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date nextHour = calendar.getTime();
		calendar.set(2017, Calendar.JANUARY, 21, 12, 12, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date beginMinute = calendar.getTime();
		calendar.set(2017, Calendar.JANUARY, 21, 12, 12, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		Date endMinute = calendar.getTime();
		calendar.set(2017, Calendar.JANUARY, 21, 12, 13, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date nextMinute = calendar.getTime();

		assertThat(DateUtil.isSameTime(DateUtil.beginOfYear(date), beginYear)).isTrue();
		assertThat(DateUtil.isSameTime(DateUtil.endOfYear(date), endYear)).isTrue();
		assertThat(DateUtil.isSameTime(DateUtil.nextYear(date), nextYear)).isTrue();

		assertThat(DateUtil.isSameTime(DateUtil.beginOfMonth(date), beginMonth)).isTrue();
		assertThat(DateUtil.isSameTime(DateUtil.endOfMonth(date), endMonth)).isTrue();
		assertThat(DateUtil.isSameTime(DateUtil.nextMonth(date), nextMonth)).isTrue();

		assertThat(DateUtil.isSameTime(DateUtil.beginOfWeek(date), beginWeek)).isTrue();
		assertThat(DateUtil.isSameTime(DateUtil.endOfWeek(date), endWeek)).isTrue();
		assertThat(DateUtil.isSameTime(DateUtil.nextWeek(date), nextWeek)).isTrue();

		assertThat(DateUtil.isSameTime(DateUtil.beginOfDate(date), beginDate)).isTrue();
		assertThat(DateUtil.isSameTime(DateUtil.endOfDate(date), endDate)).isTrue();
		assertThat(DateUtil.isSameTime(DateUtil.nextDate(date), nextDate)).isTrue();

		assertThat(DateUtil.isSameTime(DateUtil.beginOfHour(date), beginHour)).isTrue();
		assertThat(DateUtil.isSameTime(DateUtil.endOfHour(date), endHour)).isTrue();
		assertThat(DateUtil.isSameTime(DateUtil.nextHour(date), nextHour)).isTrue();

		assertThat(DateUtil.isSameTime(DateUtil.beginOfMinute(date), beginMinute)).isTrue();
		assertThat(DateUtil.isSameTime(DateUtil.endOfMinute(date), endMinute)).isTrue();
		assertThat(DateUtil.isSameTime(DateUtil.nextMinute(date), nextMinute)).isTrue();
	}

	@Test
	public void changeDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(2016, Calendar.NOVEMBER, 1, 12, 23, 44);
		Date date = calendar.getTime();
		calendar.set(2016, Calendar.NOVEMBER, 3, 0, 0, 0);
		Date expectDate1 = calendar.getTime();
		calendar.set(2016, Calendar.OCTOBER, 31, 0, 0, 0);
		Date expectDate2 = calendar.getTime();
		calendar.set(2016, Calendar.DECEMBER, 1, 0, 0, 0);
		Date expectDate3 = calendar.getTime();
		calendar.set(2016, Calendar.AUGUST, 1, 0, 0, 0);
		Date expectDate4 = calendar.getTime();
		calendar.set(2016, Calendar.NOVEMBER, 1, 13, 23, 44);
		Date expectDate5 = calendar.getTime();
		calendar.set(2016, Calendar.NOVEMBER, 1, 10, 23, 44);
		Date expectDate6 = calendar.getTime();
		calendar.set(2016, Calendar.NOVEMBER, 1, 12, 24, 44);
		Date expectDate7 = calendar.getTime();
		calendar.set(2016, Calendar.NOVEMBER, 1, 12, 21, 44);
		Date expectDate8 = calendar.getTime();
		calendar.set(2016, Calendar.NOVEMBER, 1, 12, 23, 45);
		Date expectDate9 = calendar.getTime();
		calendar.set(2016, Calendar.NOVEMBER, 1, 12, 23, 42);
		Date expectDate10 = calendar.getTime();

		calendar.set(2016, Calendar.NOVEMBER, 8, 0, 0, 0);
		Date expectDate11 = calendar.getTime();
		calendar.set(2016, Calendar.OCTOBER, 25, 0, 0, 0);
		Date expectDate12 = calendar.getTime();

		assertThat(DateUtil.isSameDay(DateUtil.addDays(date, 2), expectDate1)).isTrue();
		assertThat(DateUtil.isSameDay(DateUtil.subDays(date, 1), expectDate2)).isTrue();

		assertThat(DateUtil.isSameDay(DateUtil.addWeeks(date, 1), expectDate11)).isTrue();
		assertThat(DateUtil.isSameDay(DateUtil.subWeeks(date, 1), expectDate12)).isTrue();

		assertThat(DateUtil.isSameDay(DateUtil.addMonths(date, 1), expectDate3)).isTrue();
		assertThat(DateUtil.isSameDay(DateUtil.subMonths(date, 3), expectDate4)).isTrue();

		assertThat(DateUtil.isSameTime(DateUtil.addHours(date, 1), expectDate5)).isTrue();
		assertThat(DateUtil.isSameTime(DateUtil.subHours(date, 2), expectDate6)).isTrue();

		assertThat(DateUtil.isSameTime(DateUtil.addMinutes(date, 1), expectDate7)).isTrue();
		assertThat(DateUtil.isSameTime(DateUtil.subMinutes(date, 2), expectDate8)).isTrue();

		assertThat(DateUtil.isSameTime(DateUtil.addSeconds(date, 1), expectDate9)).isTrue();
		assertThat(DateUtil.isSameTime(DateUtil.subSeconds(date, 2), expectDate10)).isTrue();

	}

	@Test
	public void setDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(2016, Calendar.NOVEMBER, 1, 10, 10, 1);   // 2016年11月1日10点10分1秒
		Date date = calendar.getTime();

		calendar.set(2016, Calendar.NOVEMBER, 3, 0, 0, 0);
		Date expectedDate = calendar.getTime();
		calendar.set(2016, Calendar.DECEMBER, 1, 0, 0, 0);
		Date expectedDate2 = calendar.getTime();
		calendar.set(2017, Calendar.NOVEMBER, 1, 0, 0, 0);

		Date expectedDate3 = calendar.getTime();

		calendar.set(2016, Calendar.NOVEMBER, 1);
		// 手动设置期望时间
		calendar.set(Calendar.HOUR_OF_DAY, 9);
		calendar.set(Calendar.MINUTE, 10);
		calendar.set(Calendar.SECOND, 1);
		Date expectedDate4 = calendar.getTime(); // 2016年11月1日9点10分1秒

		// 手动设置期望时间
		calendar.set(Calendar.HOUR_OF_DAY, 10);
		calendar.set(Calendar.MINUTE, 9);
		calendar.set(Calendar.SECOND, 1);
		Date expectedDate5 = calendar.getTime(); // 2016年11月1日10点9分1秒

		// 手动设置期望时间
		calendar.set(Calendar.HOUR_OF_DAY, 10);
		calendar.set(Calendar.MINUTE, 10);
		calendar.set(Calendar.SECOND, 10);
		Date expectedDate6 = calendar.getTime(); // 2016年11月1日10点10分10秒

		assertThat(DateUtil.isSameDay(DateUtil.setDays(date, 3), expectedDate)).isTrue();
		assertThat(DateUtil.isSameDay(DateUtil.setMonths(date, 11), expectedDate2)).isTrue();
		assertThat(DateUtil.isSameDay(DateUtil.setYears(date, 2017), expectedDate3)).isTrue();

		assertThat(DateUtil.isSameTime(DateUtil.setHours(date, 9), expectedDate4)).isTrue();
		assertThat(DateUtil.isSameTime(DateUtil.setMinutes(date, 9), expectedDate5)).isTrue();
		assertThat(DateUtil.isSameTime(DateUtil.setSeconds(date, 10), expectedDate6)).isTrue();
	}

	@Test
	public void getDayOfWeek() {
		// 2017-01-09
		Calendar calendar = Calendar.getInstance();
		calendar.set(2017, Calendar.JANUARY, 9, 0, 0, 0);
		Date date = calendar.getTime();
		assertThat(DateUtil.getDayOfWeek(date)).isEqualTo(1);
		calendar.set(2017, Calendar.JANUARY, 15, 0, 0, 0);
		Date date2 = calendar.getTime();
		assertThat(DateUtil.getDayOfWeek(date2)).isEqualTo(7);
	}

	@Test
	public void isLeapYear() {
		// 2008-01-09,整除4年, true
		Calendar calendar = Calendar.getInstance();
		calendar.set(2008, Calendar.JANUARY, 9);
		Date date = calendar.getTime();
		assertThat(DateUtil.isLeapYear(date)).isTrue();

		// 2000-01-09,整除400年，true
		calendar.set(2000, Calendar.JANUARY, 9);
		date = calendar.getTime();
		assertThat(DateUtil.isLeapYear(date)).isTrue();

		// 1900-01-09，整除100年，false
		calendar.set(1900, Calendar.JANUARY, 9);
		date = calendar.getTime();
		assertThat(DateUtil.isLeapYear(date)).isFalse();
	}

	@Test
	public void getXXofXX() {
		Calendar calendar = Calendar.getInstance();

		// 2008-02-09, 整除4年, 闰年
		calendar.set(2008, 2, 9);
		Date date = calendar.getTime();
		assertThat(DateUtil.getMonthLength(date)).isEqualTo(29);

		// 2009-02-09, 整除4年, 非闰年
		calendar.set(2009, 2, 9);
		Date date2 = calendar.getTime();
		assertThat(DateUtil.getMonthLength(date2)).isEqualTo(28);

		calendar.set(2008, 10, 9);
		Date date3 = calendar.getTime();
		assertThat(DateUtil.getMonthLength(date3)).isEqualTo(31);

		calendar.set(2009, Calendar.DECEMBER, 30);
		Date date4 = calendar.getTime();
		assertThat(DateUtil.getDayOfYear(date4)).isEqualTo(364);
		calendar.set(2017, Calendar.JANUARY, 12);
		Date date5 = calendar.getTime();
		assertThat(DateUtil.getWeekOfMonth(date5)).isEqualTo(3);
		assertThat(DateUtil.getWeekOfYear(date5)).isEqualTo(3);
	}

	@Test
	public void testGetMonthLength() {
		Calendar calendar = Calendar.getInstance();

		calendar.set(2020, 2, 14);
		assertThat(DateUtil.getMonthLength(calendar.getTime())).isEqualTo(29);


		calendar.set(2023, 2, 14);
		assertThat(DateUtil.getMonthLength(calendar.getTime())).isEqualTo(28);

		calendar.set(2023, 4, 14);
		assertThat(DateUtil.getMonthLength(calendar.getTime())).isEqualTo(30);

		calendar.set(2023, 1, 14);
		assertThat(DateUtil.getMonthLength(calendar.getTime())).isEqualTo(31);

		assertThat(DateUtil.getMonthLength(2023, 1)).isEqualTo(31);
		assertThat(DateUtil.getMonthLength(2023, 2)).isEqualTo(28);
		assertThat(DateUtil.getMonthLength(2023, 4)).isEqualTo(30);

	}
}
