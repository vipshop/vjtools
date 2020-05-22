package com.vip.vjtools.vjkit.datamasking.strategy;

import com.vip.vjtools.vjkit.datamasking.EncryptUtil;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author ken
 */
public class HashMaskTest {

	@Test
	public void testMask() throws Exception {

		HashMask mask = new HashMask();
		assertThat(mask.mask(null, null)).isNull();

		assertThat(mask.mask("", null)).isEmpty();

		String salt = HashMask.getSalt();
		assertThat(mask.mask("test", null)).isEqualTo(EncryptUtil.sha1("test" + salt));

	}
}
