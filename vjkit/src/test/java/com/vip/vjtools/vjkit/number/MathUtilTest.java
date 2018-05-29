package com.vip.vjtools.vjkit.number;

import static org.assertj.core.api.Assertions.*;

import java.math.RoundingMode;

import org.junit.Test;

public class MathUtilTest {

	@Test
	public void power2() {
		try {
			assertThat(MathUtil.nextPowerOfTwo(-5)).isEqualTo(8);
			fail("should fail here");
		} catch (Exception e) {
			assertThat(e).isInstanceOf(IllegalArgumentException.class);
		}
		assertThat(MathUtil.nextPowerOfTwo(5)).isEqualTo(8);
		assertThat(MathUtil.nextPowerOfTwo(99)).isEqualTo(128);

		assertThat(MathUtil.previousPowerOfTwo(5)).isEqualTo(4);
		assertThat(MathUtil.previousPowerOfTwo(99)).isEqualTo(64);

		assertThat(MathUtil.isPowerOfTwo(32)).isTrue();
		assertThat(MathUtil.isPowerOfTwo(31)).isFalse();

		assertThat(MathUtil.nextPowerOfTwo(5L)).isEqualTo(8L);
		assertThat(MathUtil.nextPowerOfTwo(99L)).isEqualTo(128L);

		assertThat(MathUtil.previousPowerOfTwo(5L)).isEqualTo(4L);
		assertThat(MathUtil.previousPowerOfTwo(99L)).isEqualTo(64L);

		assertThat(MathUtil.isPowerOfTwo(32L)).isTrue();
		assertThat(MathUtil.isPowerOfTwo(31L)).isFalse();
		assertThat(MathUtil.isPowerOfTwo(-2)).isFalse();

		assertThat(MathUtil.modByPowerOfTwo(0, 16)).isEqualTo(0);
		assertThat(MathUtil.modByPowerOfTwo(1, 16)).isEqualTo(1);
		assertThat(MathUtil.modByPowerOfTwo(31, 16)).isEqualTo(15);
		assertThat(MathUtil.modByPowerOfTwo(32, 16)).isEqualTo(0);
		assertThat(MathUtil.modByPowerOfTwo(65, 16)).isEqualTo(1);
		assertThat(MathUtil.modByPowerOfTwo(-1, 16)).isEqualTo(15);

	}

	@Test
	public void caculate() {
		assertThat(MathUtil.mod(15, 10)).isEqualTo(5);
		assertThat(MathUtil.mod(-15, 10)).isEqualTo(5);
		assertThat(MathUtil.mod(-5, 3)).isEqualTo(1);

		assertThat(MathUtil.mod(15l, 10l)).isEqualTo(5);
		assertThat(MathUtil.mod(-15l, 10l)).isEqualTo(5);
		assertThat(MathUtil.mod(-5l, 3l)).isEqualTo(1);

		assertThat(MathUtil.mod(15l, 10)).isEqualTo(5);
		assertThat(MathUtil.mod(-15l, 10)).isEqualTo(5);
		assertThat(MathUtil.mod(-5l, 3)).isEqualTo(1);

		assertThat(MathUtil.pow(2, 3)).isEqualTo(8);
		assertThat(MathUtil.pow(2, 0)).isEqualTo(1);

		assertThat(MathUtil.pow(2l, 3)).isEqualTo(8);
		assertThat(MathUtil.pow(2l, 0)).isEqualTo(1);

		assertThat(MathUtil.sqrt(15, RoundingMode.HALF_UP)).isEqualTo(4);
		assertThat(MathUtil.sqrt(16, RoundingMode.HALF_UP)).isEqualTo(4);
		assertThat(MathUtil.sqrt(10l, RoundingMode.HALF_UP)).isEqualTo(3);
	}

	@Test
	public void divide() {
		assertThat(11 / 4).isEqualTo(2);
		assertThat(10L / 4).isEqualTo(2);
		assertThat(MathUtil.divide(10, 4, RoundingMode.HALF_UP)).isEqualTo(3);
		assertThat(MathUtil.divide(10L, 4L, RoundingMode.HALF_DOWN)).isEqualTo(2);
	}

}
