package com.vip.vjtools.vjkit.time;

import static org.assertj.core.api.Assertions.*;

import java.util.Date;

import org.junit.Test;

public class CachingDatFormatterTest {

	@Test
	public void test() {
		Date date = new Date(116, 10, 1, 12, 23, 44);

		CachingDateFormatter formatter = new CachingDateFormatter(DateFormatUtil.PATTERN_DEFAULT);
		assertThat(formatter.format(date.getTime())).isEqualTo("2016-11-01 12:23:44.000");
		assertThat(formatter.format(date.getTime())).isEqualTo("2016-11-01 12:23:44.000");
		assertThat(formatter.format(date.getTime() + 2)).isEqualTo("2016-11-01 12:23:44.002");

		CachingDateFormatter formatterOnSecond = new CachingDateFormatter(DateFormatUtil.PATTERN_DEFAULT_ON_SECOND);
		assertThat(formatterOnSecond.format(date.getTime())).isEqualTo("2016-11-01 12:23:44");
		assertThat(formatterOnSecond.format(date.getTime())).isEqualTo("2016-11-01 12:23:44");
		assertThat(formatterOnSecond.format(date.getTime() + 2)).isEqualTo("2016-11-01 12:23:44");
		assertThat(formatterOnSecond.format(date.getTime() + 1000)).isEqualTo("2016-11-01 12:23:45");
	}
}
