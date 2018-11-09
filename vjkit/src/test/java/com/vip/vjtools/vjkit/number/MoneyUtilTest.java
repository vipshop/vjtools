package com.vip.vjtools.vjkit.number;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;

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
}
