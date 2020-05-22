package com.vip.vjtools.vjkit.datamasking.strategy;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author ken
 */
public class NameMaskTest {

	@Test
	public void testMask() {
		NameMask mask = new NameMask();
		assertThat(mask.mask(null, null)).isNull();
		assertThat(mask.mask("", null)).isEmpty();

		assertThat(mask.mask("1", null)).isEqualTo("*");
		assertThat(mask.mask("中文", null)).isEqualTo("*文");
		assertThat(mask.mask("中文3", null)).isEqualTo("**3");
		assertThat(mask.mask("中文四个", null)).isEqualTo("**四个");
	}
}
