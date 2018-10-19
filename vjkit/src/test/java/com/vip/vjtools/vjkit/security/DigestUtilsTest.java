package com.vip.vjtools.vjkit.security;

import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author haven.zhang on 2018/10/19.
 * 摘要工具测试
 */
public class DigestUtilsTest {

	@Test
	public void generateKeyTest() throws Exception {
		byte[] key = DigestUtils.generateHmacMD2Key();
		assertThat(key).hasSize(16);
		key = DigestUtils.generateHmacSHAKey();
		assertThat(key).hasSize(64);

		key = DigestUtils.generateHmacSHA384Key();
		assertThat(key).hasSize(48);
	}

	@Test
	public void digestTest() throws Exception {
		byte[] key = DigestUtils.generateHmacSHA384Key();

		byte[] data = "message".getBytes(Charset.defaultCharset());
		byte[] encryptBytes = DigestUtils.hmacSHA384(data, key);
		String encryptMsg = DigestUtils.hmacSHA384Hex(data, key);
		assertThat(encryptMsg).isEqualTo(new String(Hex.encode(encryptBytes)));
	}
}
