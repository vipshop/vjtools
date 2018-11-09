package com.vip.vjtools.vjkit.base;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

public class BooleanUtilTest {

	@Test
	public void test() {
		assertThat(BooleanUtil.toBoolean("True")).isTrue();
		assertThat(BooleanUtil.toBoolean("tre")).isFalse();
		assertThat(BooleanUtil.toBoolean(null)).isFalse();

		assertThat(BooleanUtil.toBooleanObject("True")).isTrue();
		assertThat(BooleanUtil.toBooleanObject("tre")).isFalse();
		assertThat(BooleanUtil.toBooleanObject(null)).isNull();

		assertThat(BooleanUtil.parseGeneralString("1", false)).isFalse();
		assertThat(BooleanUtil.parseGeneralString("y", false)).isTrue();
		assertThat(BooleanUtil.parseGeneralString("y")).isTrue();
		assertThat(BooleanUtil.parseGeneralString("x")).isNull();
	}

	@Test
	public void logic() {
		assertThat(BooleanUtil.negate(Boolean.TRUE)).isFalse();
		assertThat(BooleanUtil.negate(Boolean.FALSE)).isTrue();

		assertThat(BooleanUtil.negate(true)).isFalse();
		assertThat(BooleanUtil.negate(false)).isTrue();

		assertThat(BooleanUtil.or(Boolean.TRUE, Boolean.TRUE, Boolean.FALSE)).isTrue();
		assertThat(BooleanUtil.or(false, false, false)).isFalse();

		assertThat(BooleanUtil.and(Boolean.TRUE, Boolean.TRUE, Boolean.FALSE)).isFalse();
		assertThat(BooleanUtil.and(true, true, true)).isTrue();
	}
}
