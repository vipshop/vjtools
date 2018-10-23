package com.vip.vjtools.vjkit.security;

import com.vip.vjtools.vjkit.enums.KeyPairAlgorithms;
import com.vip.vjtools.vjkit.enums.SignAlgorithm;
import com.vip.vjtools.vjkit.text.EncodeUtil;
import org.junit.Test;

import java.nio.charset.Charset;
import java.security.KeyPair;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 签名工具测试
 * @author  haven.zhang on 2018/10/23.
 */
public class SignUtilTest {

	@Test
	public void rsaSignTest() throws Exception {
		//随机产生rsa秘钥对，长度为512、1024、2048、
		KeyPair keyPair = KeyUtil.generateKeyPair(KeyPairAlgorithms.RSA, 1024);

		//签名
		byte[] msgBytes = "test".getBytes(Charset.forName("UTF-8"));
		byte[] signBytes = SignUtil.sign(keyPair.getPrivate(),msgBytes, SignAlgorithm.SHA512withRSA);
		System.out.println(EncodeUtil.encodeHex(signBytes));
		//验证签名
		boolean isValid = SignUtil.verify(keyPair.getPublic(),signBytes,msgBytes, SignAlgorithm.SHA512withRSA);
		assertThat(isValid);
	}

	@Test
	public void dsaSignTest() throws Exception {
		//随机产生Dsa秘钥对，长度为512、1024、
		KeyPair keyPair = KeyUtil.generateKeyPair(KeyPairAlgorithms.DSA, 1024);

		//签名
		byte[] msgBytes = "test".getBytes(Charset.forName("UTF-8"));
		byte[] signBytes = SignUtil.sign(keyPair.getPrivate(),msgBytes, SignAlgorithm.SHA224withDSA);
		System.out.println(EncodeUtil.encodeHex(signBytes));
		//验证签名
		boolean isValid = SignUtil.verify(keyPair.getPublic(),signBytes,msgBytes, SignAlgorithm.SHA224withDSA);
		assertThat(isValid);
	}

	@Test
	public void ecSignTest() throws Exception {
		//椭圆曲线数字签名算法 随机产生ec秘钥对，长度为112-571 默认长度256
		KeyPair keyPair = KeyUtil.generateKeyPair(KeyPairAlgorithms.EC, 256);

		//签名
		byte[] msgBytes = "test".getBytes(Charset.forName("UTF-8"));
		byte[] signBytes = SignUtil.sign(keyPair.getPrivate(),msgBytes, SignAlgorithm.SHA512withECDSA);
		System.out.println(EncodeUtil.encodeHex(signBytes));
		//验证签名
		boolean isValid = SignUtil.verify(keyPair.getPublic(),signBytes,msgBytes, SignAlgorithm.SHA512withECDSA);
		assertThat(isValid);
	}
}
