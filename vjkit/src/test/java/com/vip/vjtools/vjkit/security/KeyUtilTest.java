package com.vip.vjtools.vjkit.security;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vip.vjtools.vjkit.enums.KeyGeneratorType;
import com.vip.vjtools.vjkit.enums.KeyPairAlgorithms;
import com.vip.vjtools.vjkit.text.EncodeUtil;
import org.junit.Test;

import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPublicKey;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * keyutil工具测试类
 * @author  haven.zhang on 2018/10/23.
 */
public class KeyUtilTest {

	@Test
	public void generatKeyTest() throws NoSuchAlgorithmException {

		byte[] key = KeyUtil.generateKey(128, KeyGeneratorType.AES);
		assertThat(key).hasSize(16);

		key = KeyUtil.generateKey(256, KeyGeneratorType.AES);
		assertThat(key).hasSize(32);

		key = KeyUtil.generateKey(192, KeyGeneratorType.AES);
		assertThat(key).hasSize(24);

		key = KeyUtil.generateKey(56, KeyGeneratorType.DES);
		assertThat(key).hasSize(8);

		key = KeyUtil.generateKey(168, KeyGeneratorType.DESede);
		assertThat(key).hasSize(24);

		//HmacXXX的key可以任意长度
		key = KeyUtil.generateKey(168, KeyGeneratorType.HmacMD5);
		assertThat(key).hasSize(21);

		key = KeyUtil.generateKey(188, KeyGeneratorType.HmacMD5);
		assertThat(key).hasSize(24);

	}

	@Test
	public void generatePairKeyTest() throws Exception {
		//DSA密钥长度均为512～1024(64的整数倍)，默认长度为1024
//		KeyPair keyPair = KeyUtil.generateKeyPair(KeyPairAlgorithms.DSA, 1024);
//		assertThat(keyPair.getPrivate().getEncoded()).hasSize(335);
//		assertThat(keyPair.getPublic().getEncoded()).hasSize(443);

		KeyPair keyPair = KeyUtil.generateKeyPair(KeyPairAlgorithms.DSA, 512);
		assertThat(keyPair.getPrivate()).isNotNull();
		assertThat(keyPair.getPublic()).isNotNull();

		keyPair = KeyUtil.generateKeyPair(KeyPairAlgorithms.RSA, 512);
		assertThat(keyPair.getPrivate()).isNotNull();
		assertThat(keyPair.getPublic()).isNotNull();

		keyPair = KeyUtil.generateKeyPair(KeyPairAlgorithms.DiffieHellman, 512);
		assertThat(keyPair.getPrivate()).isNotNull();
		assertThat(keyPair.getPublic()).isNotNull();

		KeyPair keyPair2 = KeyUtil.generateKeyPair(KeyPairAlgorithms.DiffieHellman,
				((DHPublicKey) keyPair.getPublic()).getParams());

		System.out.println(EncodeUtil.encodeBase64(keyPair2.getPrivate().getEncoded()));
		System.out.println(EncodeUtil.encodeBase64(keyPair2.getPublic().getEncoded()));

//		System.setProperty("jdk.crypto.KeyAgreement.legacyKDF","true");

		SecretKey sceret = KeyUtil.generateKey(keyPair.getPublic(), keyPair2.getPrivate(), KeyGeneratorType.AES);

		System.out.println(sceret.getAlgorithm());
		System.out.println(EncodeUtil.encodeBase64(sceret.getEncoded()));
	}

}
