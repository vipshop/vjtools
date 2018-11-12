package com.vip.vjtools.vjkit.number;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.text.ParseException;

import org.junit.Test;

public class MoneyUtilTest {

	@Test
	public void amountConvertTest() {
		// 金额分转换成元
		assertThat(MoneyUtil.fen2yuan(100).doubleValue()).isEqualTo(new BigDecimal(1.00d).doubleValue());
		assertThat(MoneyUtil.fen2yuan("100").doubleValue()).isEqualTo(new BigDecimal(1.00d).doubleValue());
		assertThat(MoneyUtil.fen2yuan(BigDecimal.valueOf(100d)).doubleValue())
				.isEqualTo(new BigDecimal(1.00d).doubleValue());

		// 金额元转换成分
		assertThat(MoneyUtil.yuan2fen(BigDecimal.valueOf(1d)).doubleValue())
				.isEqualTo(new BigDecimal(100d).doubleValue());
		assertThat(MoneyUtil.yuan2fen(1L).doubleValue()).isEqualTo(new BigDecimal(100d).doubleValue());
	}

	@Test
	public void format() {
		assertThat(MoneyUtil.format(1111.111)).isEqualTo("1111.11");
		assertThat(MoneyUtil.prettyFormat(1111.111)).isEqualTo("1,111.11");
		assertThat(MoneyUtil.format(1111.111, "0.0")).isEqualTo("1111.1");
	}

	@Test
	public void parse() throws ParseException {
		assertThat(MoneyUtil.parseString("1111.11")).isEqualTo(new BigDecimal(1111.11));
		assertThat(MoneyUtil.parsePrettyString("1,111.11")).isEqualTo(new BigDecimal(1111.11));
	}
}
