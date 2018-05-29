package com.vip.vjtools.vjkit.time;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.ParseException;
import java.util.Date;

import org.junit.Test;

public class DateFormatUtilTest {

	@Test
	public void isoDateFormat() {
		Date date = new Date(116, 10, 1, 12, 23, 44);
		assertThat(DateFormatUtil.ISO_FORMAT.format(date)).contains("2016-11-01T12:23:44.000");
		assertThat(DateFormatUtil.ISO_ON_SECOND_FORMAT.format(date)).contains("2016-11-01T12:23:44");
		assertThat(DateFormatUtil.ISO_ON_DATE_FORMAT.format(date)).isEqualTo("2016-11-01");
	}

	@Test
	public void defaultDateFormat() {
		Date date = new Date(116, 10, 1, 12, 23, 44);
		assertThat(DateFormatUtil.DEFAULT_FORMAT.format(date)).isEqualTo("2016-11-01 12:23:44.000");
		assertThat(DateFormatUtil.DEFAULT_ON_SECOND_FORMAT.format(date)).isEqualTo("2016-11-01 12:23:44");
	}

	@Test
	public void formatWithPattern() {
		Date date = new Date(116, 10, 1, 12, 23, 44);
		assertThat(DateFormatUtil.formatDate(DateFormatUtil.PATTERN_DEFAULT, date))
				.isEqualTo("2016-11-01 12:23:44.000");
		assertThat(DateFormatUtil.formatDate(DateFormatUtil.PATTERN_DEFAULT, date.getTime()))
				.isEqualTo("2016-11-01 12:23:44.000");
	}

	@Test
	public void parseWithPattern() throws ParseException {
		Date date = new Date(116, 10, 1, 12, 23, 44);
		Date resultDate = DateFormatUtil.parseDate(DateFormatUtil.PATTERN_DEFAULT, "2016-11-01 12:23:44.000");
		assertThat(resultDate.getTime() == date.getTime()).isTrue();
	}

	@Test
	public void formatDuration() {
		assertThat(DateFormatUtil.formatDuration(100)).isEqualTo("00:00:00.100");

		assertThat(DateFormatUtil.formatDuration(new Date(100), new Date(3000))).isEqualTo("00:00:02.900");

		assertThat(DateFormatUtil.formatDuration(DateUtil.MILLIS_PER_DAY * 2 + DateUtil.MILLIS_PER_HOUR * 4))
				.isEqualTo("52:00:00.000");

		assertThat(DateFormatUtil.formatDurationOnSecond(new Date(100), new Date(3000))).isEqualTo("00:00:02");

		assertThat(DateFormatUtil.formatDurationOnSecond(2000)).isEqualTo("00:00:02");

		assertThat(DateFormatUtil.formatDurationOnSecond(DateUtil.MILLIS_PER_DAY * 2 + DateUtil.MILLIS_PER_HOUR * 4))
				.isEqualTo("52:00:00");
	}

	@Test
	public void formatFriendlyTimeSpanByNow() throws ParseException {
		try {
			Date now = DateFormatUtil.DEFAULT_ON_SECOND_FORMAT.parse("2016-12-11 23:30:00");

			ClockUtil.useDummyClock(now);

			Date lessOneSecond = DateFormatUtil.DEFAULT_FORMAT.parse("2016-12-11 23:29:59.500");
			assertThat(DateFormatUtil.formatFriendlyTimeSpanByNow(lessOneSecond)).isEqualTo("刚刚");

			Date lessOneMinute = DateFormatUtil.DEFAULT_FORMAT.parse("2016-12-11 23:29:55.000");
			assertThat(DateFormatUtil.formatFriendlyTimeSpanByNow(lessOneMinute)).isEqualTo("5秒前");

			Date lessOneHour = DateFormatUtil.DEFAULT_ON_SECOND_FORMAT.parse("2016-12-11 23:00:00");
			assertThat(DateFormatUtil.formatFriendlyTimeSpanByNow(lessOneHour)).isEqualTo("30分钟前");

			Date today = DateFormatUtil.DEFAULT_ON_SECOND_FORMAT.parse("2016-12-11 1:00:00");
			assertThat(DateFormatUtil.formatFriendlyTimeSpanByNow(today)).isEqualTo("今天01:00");

			Date yesterday = DateFormatUtil.DEFAULT_ON_SECOND_FORMAT.parse("2016-12-10 1:00:00");
			assertThat(DateFormatUtil.formatFriendlyTimeSpanByNow(yesterday)).isEqualTo("昨天01:00");

			Date threeDayBefore = DateFormatUtil.DEFAULT_ON_SECOND_FORMAT.parse("2016-12-09 1:00:00");
			assertThat(DateFormatUtil.formatFriendlyTimeSpanByNow(threeDayBefore)).isEqualTo("2016-12-09");

		} finally {

			ClockUtil.useDefaultClock();
		}
	}

}
