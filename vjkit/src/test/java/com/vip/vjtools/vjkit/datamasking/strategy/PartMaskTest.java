package com.vip.vjtools.vjkit.datamasking.strategy;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author ken
 */
public class PartMaskTest {

	@Test
	public void testMask() {
		PartMask mask = new PartMask();

		assertThat(mask.mask(null, new int[]{1})).isNull();
		assertThat(mask.mask("", new int[]{1})).isEmpty();
		assertThat(mask.mask("1", new int[]{1})).isEqualTo("*");

		assertThat(mask.mask("123", new int[]{1})).isEqualTo("1*3");
		assertThat(mask.mask("12345", new int[]{2})).isEqualTo("12*45");
		assertThat(mask.mask("1234", new int[]{1, 2})).isEqualTo("1*34");
		assertThat(mask.mask("1234", new int[]{1, 0})).isEqualTo("1***");
		assertThat(mask.mask("1234", new int[]{0, 1})).isEqualTo("***4");

		assertThat(mask.mask("1234", new int[]{0, 0})).isEqualTo("****");

		//验证不通过的
		assertThat(mask.mask("12", new int[]{1, 1})).isEqualTo("1*");
		assertThat(mask.mask("1234", new int[]{4, 1})).isEqualTo("1***");
		assertThat(mask.mask("1234", new int[]{5, 1})).isEqualTo("1***");
		assertThat(mask.mask("1234", new int[]{1, 4})).isEqualTo("1***");
		assertThat(mask.mask("1234", new int[]{1, 5})).isEqualTo("1***");
		assertThat(mask.mask("1234", new int[]{3, 2})).isEqualTo("1***");
		assertThat(mask.mask("1234", new int[]{2, 2})).isEqualTo("1***");
	}
}
