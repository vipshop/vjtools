package com.vip.vjtools.vjkit.datamasking.strategy;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author ken
 */
public class EmailMaskTest {

	@Test
	public void testMask() {
		EmailMask mask = new EmailMask();
		assertThat(mask.mask(null, null)).isNull();
		assertThat(mask.mask("", null)).isEmpty();

		assertThat(mask.mask("test", null)).isEqualTo("t***");
		assertThat(mask.mask("@test", null)).isEqualTo("@****");

		assertThat(mask.mask("123@test", null)).isEqualTo("1*3@test");
		assertThat(mask.mask("13@test", null)).isEqualTo("1*@test");
		assertThat(mask.mask("1@test", null)).isEqualTo("*@test");
	}
}
