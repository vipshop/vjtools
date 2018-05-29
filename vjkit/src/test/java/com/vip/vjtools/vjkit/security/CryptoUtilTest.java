package com.vip.vjtools.vjkit.security;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

import com.vip.vjtools.vjkit.text.EncodeUtil;


public class CryptoUtilTest {
	@Test
	public void mac() {
		String input = "foo message";

		// key可为任意字符串
		// byte[] key = "a foo key".getBytes();
		byte[] key = CryptoUtil.generateHmacSha1Key();
		assertThat(key).hasSize(20);

		byte[] macResult = CryptoUtil.hmacSha1(input.getBytes(), key);
		System.out.println("hmac-sha1 key in hex      :" + EncodeUtil.encodeHex(key));
		System.out.println("hmac-sha1 in hex result   :" + EncodeUtil.encodeHex(macResult));

		assertThat(CryptoUtil.isMacValid(macResult, input.getBytes(), key)).isTrue();
	}

	@Test
	public void aes() {
		byte[] key = CryptoUtil.generateAesKey();
		assertThat(key).hasSize(16);
		String input = "foo message";

		byte[] encryptResult = CryptoUtil.aesEncrypt(input.getBytes(), key);
		String descryptResult = CryptoUtil.aesDecrypt(encryptResult, key);

		System.out.println("aes key in hex            :" + EncodeUtil.encodeHex(key));
		System.out.println("aes encrypt in hex result :" + EncodeUtil.encodeHex(encryptResult));
		assertThat(descryptResult).isEqualTo(input);
	}

	@Test
	public void aesWithIV() {
		byte[] key = CryptoUtil.generateAesKey();
		byte[] iv = CryptoUtil.generateIV();
		assertThat(key).hasSize(16);
		assertThat(iv).hasSize(16);
		String input = "foo message";

		byte[] encryptResult = CryptoUtil.aesEncrypt(input.getBytes(), key, iv);
		String descryptResult = CryptoUtil.aesDecrypt(encryptResult, key, iv);

		System.out.println("aes key in hex            :" + EncodeUtil.encodeHex(key));
		System.out.println("iv in hex                 :" + EncodeUtil.encodeHex(iv));
		System.out.println("aes encrypt in hex result :" + EncodeUtil.encodeHex(encryptResult));
		assertThat(descryptResult).isEqualTo(input);
	}
}
