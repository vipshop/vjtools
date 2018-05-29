package com.vip.vjtools.vjkit.text;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

public class TextValidatorTest {

	@Test
	public void isMobileSimple() {
		assertThat(TextValidator.isMobileSimple(null)).isFalse();
		assertThat(TextValidator.isMobileSimple("")).isFalse();
		assertThat(TextValidator.isMobileSimple("1234a")).isFalse();
		assertThat(TextValidator.isMobileSimple("1234561")).isFalse();
		assertThat(TextValidator.isMobileSimple("11170998762")).isTrue();
	}

	@Test
	public void isMobileExact() {
		assertThat(TextValidator.isMobileExact("1234a")).isFalse();
		assertThat(TextValidator.isMobileExact("11170998762")).isFalse();
		assertThat(TextValidator.isMobileExact("13970998762")).isTrue();
	}

	@Test
	public void isTel() {
		// 含字母
		assertThat(TextValidator.isTel("8802973a")).isFalse();
		// 太长
		assertThat(TextValidator.isTel("8908222222")).isFalse();
		// 太短
		assertThat(TextValidator.isTel("89081")).isFalse();

		assertThat(TextValidator.isTel("89019739")).isTrue();
		assertThat(TextValidator.isTel("020-89019739")).isTrue();
	}

	@Test
	public void isIdCard() {
		// 含字母
		assertThat(TextValidator.isIdCard("440101198987754ab")).isFalse();
		// 月份不对
		assertThat(TextValidator.isIdCard("440101198987754122")).isFalse();
		// 日期不对
		assertThat(TextValidator.isIdCard("440101891232451")).isFalse();

		// 18位正确
		assertThat(TextValidator.isIdCard("440101198909204518")).isTrue();
		// 15位正确
		assertThat(TextValidator.isIdCard("440101891231451")).isTrue();
	}

	@Test
	public void isEmail() {
		assertThat(TextValidator.isEmail("abc")).isFalse();
		assertThat(TextValidator.isEmail("abc@a")).isFalse();
		assertThat(TextValidator.isEmail("中文@a.com")).isFalse();

		assertThat(TextValidator.isEmail("abc@abc.com")).isTrue();
	}

	@Test
	public void isUrl() {
		assertThat(TextValidator.isUrl("abc.com")).isFalse();
		assertThat(TextValidator.isUrl("http://abc.c om")).isFalse();
		assertThat(TextValidator.isUrl("http2://abc.com")).isFalse();

		assertThat(TextValidator.isUrl("http://abc.com")).isTrue();
	}

	@Test
	public void isDate() {
		assertThat(TextValidator.isDate("2011-02-29")).isFalse();
		assertThat(TextValidator.isDate("201a-02-30")).isFalse();
		assertThat(TextValidator.isDate("2011-0211")).isFalse();

		assertThat(TextValidator.isDate("2011-03-11")).isTrue();
		assertThat(TextValidator.isDate("2012-02-29")).isTrue();
	}

	@Test
	public void isIp() {
		assertThat(TextValidator.isIp("192.168.0.300")).isFalse();
		assertThat(TextValidator.isIp("192.168.300.1")).isFalse();
		assertThat(TextValidator.isIp("192.168.300")).isFalse();
		assertThat(TextValidator.isIp("192.168.A3.1")).isFalse();

		assertThat(TextValidator.isIp("192.168.0.1")).isTrue();
	}

}
