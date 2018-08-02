package com.vip.vjtools.vjkit.base;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.vip.vjtools.vjkit.base.ValueValidator.Validator;

public class ValueValidatorTest {
	@Test
	public void testValidator() {
		assertThat(ValueValidator.checkAndGet(-1, 1, Validator.INTEGER_GT_ZERO_VALIDATOR)).isEqualTo(1);
		assertThat(ValueValidator.checkAndGet("testUnEmpty", "isEmpty", Validator.STRING_EMPTY_VALUE_VALIDATOR))
				.isEqualTo("testUnEmpty");
		assertThat(ValueValidator.checkAndGet("flase", "true", Validator.STRICT_BOOL_VALUE_VALIDATOR))
				.isEqualTo("true");
	}
}
