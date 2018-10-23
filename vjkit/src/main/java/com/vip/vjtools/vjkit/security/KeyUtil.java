package com.vip.vjtools.vjkit.security;


import com.vip.vjtools.vjkit.base.ExceptionUtil;
import com.vip.vjtools.vjkit.base.type.Pair;
import com.vip.vjtools.vjkit.enums.KeyFactoryAlgorithms;
import com.vip.vjtools.vjkit.enums.KeyGeneratorType;
import com.vip.vjtools.vjkit.enums.KeyPairAlgorithms;
import com.vip.vjtools.vjkit.enums.KeyStoreType;
import com.vip.vjtools.vjkit.io.IOUtil;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Enumeration;


/**
 * ��Կ������ع�����
 *
 * @author haven.zhang
 */
public class KeyUtil {
	/**
	 * x.509֤��
	 */
	private static final String CERTIFICATE_TYPE = "X.509";


	/**
	 * ��x.509֤���ļ��ж�ȡ��Կ
	 * @param certFilePath
	 * @return PublicKey ��Կ����
	 * @throws Exception
	 */
	public static PublicKey readPubKey(String certFilePath) throws Exception {
		FileInputStream fileStream = null;
		BufferedInputStream bin = null;
		PublicKey pubkey = null;
		try {
			fileStream = new FileInputStream(certFilePath);
			bin = new BufferedInputStream(fileStream);
			CertificateFactory cf = CertificateFactory.getInstance(CERTIFICATE_TYPE);
			X509Certificate x509cert = (X509Certificate) cf.generateCertificate(bin);
			pubkey = x509cert.getPublicKey();
		} finally {
			IOUtil.closeQuietly(fileStream);
			IOUtil.closeQuietly(bin);
		}
		return pubkey;
	}


	/**
	 * ��ȡ֤�鹫Կ
	 * @param certfile x.509��Կ֤����
	 * @return
	 * @throws Exception
	 */
	public static PublicKey readPubKey(InputStream certfile) throws Exception {

		PublicKey pubkey = null;
		CertificateFactory cf = CertificateFactory.getInstance(CERTIFICATE_TYPE);
		X509Certificate x509cert = (X509Certificate) cf.generateCertificate(certfile);
		pubkey = x509cert.getPublicKey();
		return pubkey;
	}


	/**
	 * ��ȡ��Կ
	 * @param keyStore
	 * @param alias ֤�����
	 * @return
	 * @throws Exception
	 */
	public static PublicKey readPubKey(KeyStore keyStore, String alias) throws Exception {

		X509Certificate x509cert = (X509Certificate) keyStore.getCertificate(alias);
		return x509cert == null ? null : x509cert.getPublicKey();

	}

	/**
	 * ��ȡ˽Կ
	 * @param keyStore
	 * @param alias
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public static PrivateKey readPrivKey(KeyStore keyStore, String alias, String password) throws Exception {
		Key key = keyStore.getKey(alias, password.toCharArray());
		return (PrivateKey) key;
	}

	/**
	 * ��keystore�ļ��м���˽Կ
	 * @param keystoreFile �ļ�·��
	 * @param passWord keystore��������
	 * @return PrivateKey ˽Կ����
	 * @throws Exception
	 */
	public static PrivateKey readPrivKey(String keystoreFile, String passWord) throws Exception {
		FileInputStream fileInputStream = null;
		BufferedInputStream bin = null;
		PrivateKey prikey = null;
		try {
			KeyStore ks = KeyStore.getInstance(KeyStoreType.PKCS12.getValue());
			// ks.load(new FileInputStream(KeyFile), PassWord.toCharArray());
			fileInputStream = new FileInputStream(keystoreFile);
			bin = new BufferedInputStream(fileInputStream);
			ks.load(bin, passWord.toCharArray());
			Enumeration myEnum = ks.aliases();
			String keyAlias = null;
			// keyAlias = (String) myEnum.nextElement();
			while (myEnum.hasMoreElements()) {
				keyAlias = (String) myEnum.nextElement();
				if (ks.isKeyEntry(keyAlias)) {
					prikey = (PrivateKey) ks.getKey(keyAlias, passWord.toCharArray());
					break;
				}
			}
		} finally {
			IOUtil.closeQuietly(fileInputStream);
			IOUtil.closeQuietly(bin);
		}
		return prikey;
	}

	/**
	 * ��keystore�ļ����м���˽Կ
	 * @param inputStream �ļ���
	 * @param passWord keystore��������
	 * @return PrivateKey ˽Կ����
	 * @throws Exception
	 */
	public static PrivateKey readPrivKey(InputStream inputStream, String passWord) throws Exception {
		PrivateKey prikey = null;
		KeyStore ks = KeyStore.getInstance(KeyStoreType.PKCS12.getValue());
		ks.load(inputStream, passWord.toCharArray());
		Enumeration myEnum = ks.aliases();
		String keyAlias = null;
		// keyAlias = (String) myEnum.nextElement();
			/* IBM JDK����ʹ��Whileѭ��ȡ���һ�����������ܵõ�����˽Կ���� */
		while (myEnum.hasMoreElements()) {
			keyAlias = (String) myEnum.nextElement();
			// System.out.println("keyAlias==" + keyAlias);
			if (ks.isKeyEntry(keyAlias)) {
				prikey = (PrivateKey) ks.getKey(keyAlias, passWord.toCharArray());
				break;
			}
		}
		return prikey;
	}


	/**
	 * ��keystore�ļ��ж�ȡ��˽Կ
	 * @param keystoreFile
	 * @param passWord
	 * @return
	 * @throws Exception
	 */
	public static KeyPair readKeyPair(String keystoreFile, final String passWord) throws Exception {
		FileInputStream fileInputStream = null;
		BufferedInputStream bin = null;
		PrivateKey prikey = null;
		KeyPair keyPair = null;
		try {
			fileInputStream = new FileInputStream(keystoreFile);
			bin = new BufferedInputStream(fileInputStream);
			KeyStore ks = KeyStore.getInstance(KeyStoreType.PKCS12.getValue());
			ks.load(bin, passWord.toCharArray());
			Enumeration myEnum = ks.aliases();
			String keyAlias = null;
			// keyAlias = (String) myEnum.nextElement();
			/* IBM JDK����ʹ��Whileѭ��ȡ���һ�����������ܵõ�����˽Կ���� */
			while (myEnum.hasMoreElements()) {
				keyAlias = (String) myEnum.nextElement();
				if (ks.isKeyEntry(keyAlias)) {
					prikey = (PrivateKey) ks.getKey(keyAlias, passWord.toCharArray());
					keyPair = new KeyPair( ks.getCertificate(keyAlias).getPublicKey(),prikey);
					break;
				}
			}
		} finally {
			IOUtil.closeQuietly(fileInputStream);
			IOUtil.closeQuietly(bin);
		}
		return keyPair;
	}


	/**
	 * ��˽Կ������ת��ΪPrivateKey����
	 * @param keyBytes  ������˽Կ
	 * @param algorithm ��Կ�㷨
	 * @return PrivateKey ˽Կ����
	 * @throws Exception
	 */
	public static PrivateKey getPrivateKey(byte[] keyBytes, KeyFactoryAlgorithms algorithm) throws Exception {
		PrivateKey priKey = null;
		// ȡ��˽Կ
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm.name());
		// ����˽Կ
		priKey = keyFactory.generatePrivate(pkcs8KeySpec);
		return priKey;
	}

	/**
	 * �ӹ�Կ�ֽ�����ת��ΪPublicKey����
	 * @param keyBytes ��Կ������
	 * @param algorithm ��Կ�㷨
	 * @return
	 * @throws Exception
	 */
	public static PublicKey getPublicKey(byte[] keyBytes, KeyFactoryAlgorithms algorithm) throws Exception {
		PublicKey pubKey = null;

		// ת����Կ����
		// ʵ������Կ����
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm.name());
		// ��ʼ����Կ
		// ��Կ����ת��
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
		// ������Կ
		pubKey = keyFactory.generatePublic(x509KeySpec);

		return pubKey;
	}


	/**
	 * ��ȡ��˽Կ��
	 * @param keyStore
	 * @param alias
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public static KeyPair getKeyPair(KeyStore keyStore, String alias, String password) throws Exception {
		Key key = keyStore.getKey(alias, password.toCharArray());
		Certificate[] chain = keyStore.getCertificateChain(alias);
		KeyPair keyPair = new KeyPair(chain[0].getPublicKey(), (PrivateKey) key);
		return keyPair;
	}


	/**
	 * �����ǶԳ���Կ��
	 * @param algorithms
	 * @param keyLen
	 * @return
	 * @throws Exception
	 */
	public static KeyPair generateKeyPair(KeyPairAlgorithms algorithms, int keyLen) throws Exception {
		//��ö��� KeyPairGenerator
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(algorithms.name());
		keyPairGen.initialize(keyLen);
		//ͨ������ KeyPairGenerator ��ȡ����KeyPair
		KeyPair keyPair = keyPairGen.generateKeyPair();
		return keyPair;

	}

	/**
	 * rc Key length for ARCFOUR must be between 40 and 1024 bits
	 * ���ɶԳ���Կ,��ѡ����
	 * ��ѡ�㷨����KeyGeneratorType
	 */
	public static byte[] generateKey(int keysize, KeyGeneratorType type) throws NoSuchAlgorithmException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance(type.name());
		keyGenerator.init(keysize);
		SecretKey secretKey = keyGenerator.generateKey();
		return secretKey.getEncoded();
	}

	/**
	 * ����des�㷨����Կ������Ϊ8���ֽ�
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] generateDesKey() throws NoSuchAlgorithmException {
		return generateKey(56,KeyGeneratorType.DES);
	}

	/**
	 * ����des3�㷨����Կ������Ϊ24���ֽ�
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] generateDes3Key() throws NoSuchAlgorithmException {
		return generateKey(168,KeyGeneratorType.DESede);
	}
}
