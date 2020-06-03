package com.vip.vjtools.vjkit.datamasking.strategy;

import com.vip.vjtools.vjkit.text.EncodeUtil;
import com.vip.vjtools.vjkit.text.HashUtil;
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
		String encrypt = EncodeUtil.encodeHex(HashUtil.sha1("test" + salt));
		assertThat(mask.mask("test", null)).isEqualTo(encrypt);

	}
}
