package com.vip.vjtools.vjkit.base;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ValidateTest {

	@Test
	public void test() {
		// int
		int a = Validate.nonNegative("x", 0);
		assertThat(a).isEqualTo(0);
		a = Validate.nonNegative("x", 1);
		assertThat(a).isEqualTo(1);

		a = Validate.positive("x", 1);
		assertThat(a).isEqualTo(1);

		// Integer
		Integer c = Validate.nonNegative("x", Integer.valueOf(0));
		assertThat(c).isEqualTo(0);
		c = Validate.nonNegative("x", Integer.valueOf(21));
		assertThat(c).isEqualTo(21);

		c = Validate.positive("x", Integer.valueOf(1));
		assertThat(c).isEqualTo(1);

		// long
		long b = Validate.nonNegative("x", 0l);
		assertThat(b).isEqualTo(0);

		b = Validate.nonNegative("x", 11l);
		assertThat(b).isEqualTo(11);

		b = Validate.positive("x", 1l);
		assertThat(b).isEqualTo(1);

		double e = Validate.nonNegative("x", 0l);
		assertThat(e).isEqualTo(0);

		e = Validate.nonNegative("x", 11d);
		assertThat(e).isEqualTo(11);

		e = Validate.positive("x", 1.1d);
		assertThat(e).isEqualTo(1.1);

		// Long
		Long d = Validate.nonNegative("x", Long.valueOf(0));
		assertThat(d).isEqualTo(0);

		d = Validate.positive("x", Long.valueOf(1));
		assertThat(d).isEqualTo(1);

		d = Validate.nonNegative("x", Long.valueOf(11));
		assertThat(d).isEqualTo(11);

		// int
		try {
			Validate.nonNegative("x", -1);
			fail("fail");
		} catch (Throwable t) {
			assertThat(t).hasMessage("x (-1) must be >= 0");
			assertThat(t).isInstanceOf(IllegalArgumentException.class);
		}

		try {
			Validate.nonNegative(null, -1);
			fail("fail");
		} catch (Throwable t) {
			assertThat(t).hasMessage("null (-1) must be >= 0");
			assertThat(t).isInstanceOf(IllegalArgumentException.class);

		}

		try {
			Validate.positive("x", -1);
			fail("fail");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(IllegalArgumentException.class);
		}

		try {
			Validate.positive("x", 0);
			fail("fail");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(IllegalArgumentException.class);
		}

		// long
		try {
			Validate.nonNegative("x", -1l);
			fail("fail");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(IllegalArgumentException.class);
		}

		try {
			Validate.positive("x", -1l);
			fail("fail");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(IllegalArgumentException.class);
		}
		try {
			Validate.positive("x", 0l);
			fail("fail");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(IllegalArgumentException.class);
		}

		// Long
		try {
			Validate.nonNegative("x", Long.valueOf(-1));
			fail("fail");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(IllegalArgumentException.class);
		}
		try {
			Validate.positive("x", Long.valueOf(-1));
			fail("fail");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(IllegalArgumentException.class);
		}

		try {
			Validate.positive("x", Long.valueOf(0));
			fail("fail");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(IllegalArgumentException.class);
		}

		// Integer
		try {
			Validate.nonNegative("x", Integer.valueOf(-1));
			fail("fail");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(IllegalArgumentException.class);
		}

		try {
			Validate.positive("x", Integer.valueOf(-1));
			fail("fail");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(IllegalArgumentException.class);
		}

		try {
			Validate.positive("x", Integer.valueOf(0));
			fail("fail");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(IllegalArgumentException.class);
		}

		// double
		try {
			Validate.nonNegative("x", -9999.2d);
			fail("fail");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(IllegalArgumentException.class);
		}

		try {
			Validate.positive("x", -1.2d);
			fail("fail");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(IllegalArgumentException.class);
		}
		try {
			Validate.positive("x", 0d);
			fail("fail");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(IllegalArgumentException.class);
		}


	}

}
