package com.vip.vjtools.vjkit.security;


import com.vip.vjtools.vjkit.base.ExceptionUtil;
import com.vip.vjtools.vjkit.base.type.Pair;
import com.vip.vjtools.vjkit.enums.KeyFactoryAlgorithms;
import com.vip.vjtools.vjkit.enums.KeyGeneratorType;
import com.vip.vjtools.vjkit.enums.KeyPairAlgorithms;
import com.vip.vjtools.vjkit.enums.KeyStoreType;
import com.vip.vjtools.vjkit.io.IOUtil;

import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.*;
import java.util.Arrays;
import java.util.Enumeration;


/**
 * 秘钥管理相关工具类
 * 如果遇到某些算法不支持的时候，请引入bouncycastle maven配置
 * 并在初始化时加入BC作为安全提供者 Security.addProvider(new BouncyCastleProvider());
 * @author haven.zhang
 */
public class KeyUtil {
	/**
	 * x.509证书
	 */
	private static final String CERTIFICATE_TYPE = "X.509";


	/**
	 * 从x.509证书文件中读取公钥
	 * @param certFilePath
	 * @return PublicKey 公钥对象
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
			IOUtil.closeQuietly(bin);
			IOUtil.closeQuietly(fileStream);
		}
		return pubkey;
	}


	/**
	 * 读取证书公钥
	 * @param certfile x.509公钥证书流
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
	 * 读取公钥
	 * @param keyStore
	 * @param alias 证书别名
	 * @return
	 * @throws Exception
	 */
	public static PublicKey readPubKey(KeyStore keyStore, String alias) throws Exception {

		X509Certificate x509cert = (X509Certificate) keyStore.getCertificate(alias);
		return x509cert == null ? null : x509cert.getPublicKey();

	}

	/**
	 * 读取私钥
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
	 * 从keystore文件中加载私钥
	 * @param keystoreFile 文件路径
	 * @param passWord keystore访问密码
	 * @return PrivateKey 私钥对象
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
			IOUtil.closeQuietly(bin);
			IOUtil.closeQuietly(fileInputStream);
		}
		return prikey;
	}

	/**
	 * 从keystore文件流中加载私钥
	 * @param inputStream 文件流
	 * @param passWord keystore访问密码
	 * @return PrivateKey 私钥对象
	 * @throws Exception
	 */
	public static PrivateKey readPrivKey(InputStream inputStream, String passWord) throws Exception {
		PrivateKey prikey = null;
		KeyStore ks = KeyStore.getInstance(KeyStoreType.PKCS12.getValue());
		ks.load(inputStream, passWord.toCharArray());
		Enumeration myEnum = ks.aliases();
		String keyAlias = null;
		// keyAlias = (String) myEnum.nextElement();
			/* IBM JDK必须使用While循环取最后一个别名，才能得到个人私钥别名 */
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
	 * 从pkcs12格式的密钥库keystore文件中读取公私钥密钥对
	 * @param keystoreFile 密钥库文件，如：xxx.pfx /xxx.p12
	 * @param passWord 密钥库访问密码
	 * @return KeyPair
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
			/* IBM JDK必须使用While循环取最后一个别名，才能得到个人私钥别名 */
			while (myEnum.hasMoreElements()) {
				keyAlias = (String) myEnum.nextElement();
				if (ks.isKeyEntry(keyAlias)) {
					prikey = (PrivateKey) ks.getKey(keyAlias, passWord.toCharArray());
					keyPair = new KeyPair( ks.getCertificate(keyAlias).getPublicKey(),prikey);
					break;
				}
			}
		} finally {
			IOUtil.closeQuietly(bin);
			IOUtil.closeQuietly(fileInputStream);
		}
		return keyPair;
	}


	/**
	 * 从私钥节数组转换为PrivateKey对象
	 * @param keyBytes  节数组私钥
	 * @param algorithm 秘钥算法
	 * @return PrivateKey 私钥对象
	 * @throws Exception
	 */
	public static PrivateKey getPrivateKey(byte[] keyBytes, KeyFactoryAlgorithms algorithm) throws Exception {
		PrivateKey priKey = null;
		// 取得私钥
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm.name());
		// 生成私钥
		priKey = keyFactory.generatePrivate(pkcs8KeySpec);
		return priKey;
	}

	/**
	 * 从公钥字节数组转换为PublicKey对象
	 * @param keyBytes 公钥节数组
	 * @param algorithm 秘钥算法
	 * @return
	 * @throws Exception
	 */
	public static PublicKey getPublicKey(byte[] keyBytes, KeyFactoryAlgorithms algorithm) throws Exception {
		PublicKey pubKey = null;

		// 转换公钥材料
		// 实例化密钥工厂
		KeyFactory keyFactory = KeyFactory.getInstance(algorithm.name());
		// 初始化公钥
		// 密钥材料转换
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
		// 产生公钥
		pubKey = keyFactory.generatePublic(x509KeySpec);

		return pubKey;
	}


	/**
	 * 获取公私钥对
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
	 * 产生非对称秘钥对
	 * @param algorithms
	 * @param keyLen
	 * @return
	 * @throws Exception
	 */
	public static KeyPair generateKeyPair(KeyPairAlgorithms algorithms, int keyLen) throws Exception {
		//获得对象 KeyPairGenerator
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(algorithms.name());
		keyPairGen.initialize(keyLen);
		//通过对象 KeyPairGenerator 获取对象KeyPair
		KeyPair keyPair = keyPairGen.generateKeyPair();
		return keyPair;

	}


	/**
	 * 产生非对称秘钥对
	 * @param algorithms 非对称秘钥算法
	 * @param parameterSpec  cryptographic parameters
	 * @return
	 * @throws Exception
	 */
	public static KeyPair generateKeyPair(KeyPairAlgorithms algorithms, AlgorithmParameterSpec parameterSpec) throws Exception {
		//获得对象 KeyPairGenerator
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(algorithms.name());
		keyPairGen.initialize(parameterSpec);
		//通过对象 KeyPairGenerator 获取对象KeyPair
		KeyPair keyPair = keyPairGen.generateKeyPair();
		return keyPair;

	}

	/**
	 * rc Key length for ARCFOUR must be between 40 and 1024 bits
	 * 生成对称密钥,可选长度
	 * 可选算法见：KeyGeneratorType
	 */
	public static byte[] generateKey(int keysize, KeyGeneratorType type) throws NoSuchAlgorithmException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance(type.name());
		keyGenerator.init(keysize);
		SecretKey secretKey = keyGenerator.generateKey();
		return secretKey.getEncoded();
	}

	/**
	 * 生成DiffieHellman算法本地对称秘钥
	 * @param publicKey 公钥
	 * @param privateKey 私钥
	 * @param secretAlgorithms 指定生成的对称秘钥算法 可以是DES /DESede/ AES等等
	 * @throws Exception
	 */
	public static SecretKey generateKey(PublicKey publicKey,PrivateKey privateKey,KeyGeneratorType secretAlgorithms)
			throws Exception {
		System.setProperty("jdk.crypto.KeyAgreement.legacyKDF","true");
		KeyFactory keyFactory = KeyFactory.getInstance(KeyFactoryAlgorithms.DiffieHellman.name());
		KeyAgreement keyAgree = KeyAgreement.getInstance(keyFactory
				.getAlgorithm());
		keyAgree.init(privateKey);
		keyAgree.doPhase(publicKey, true);
		// 生成本地密钥
		SecretKey secretKey = keyAgree.generateSecret(secretAlgorithms.name());
		return secretKey;
	}

	/**
	 * 生成des算法的秘钥，长度为8个字节
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] generateDesKey() throws NoSuchAlgorithmException {
		return generateKey(56, KeyGeneratorType.DES);
	}

	/**
	 * 生成des3算法的秘钥，长度为24个字节
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] generateDes3Key() throws NoSuchAlgorithmException {
		return generateKey(168,KeyGeneratorType.DESede);
	}

	/**
	 * 生成RSA 公钥
	 * @param modulus the modulus
	 * @param publicExponent the public exponent
	 * @return
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	public static PublicKey generateRsaPublicKey(BigInteger modulus, BigInteger publicExponent) throws
			InvalidKeySpecException, NoSuchAlgorithmException {
		RSAPublicKeySpec rsaSpec = new RSAPublicKeySpec(modulus, publicExponent);
		KeyFactory kf = KeyFactory.getInstance(KeyPairAlgorithms.RSA.name());
		PublicKey publicKey = kf.generatePublic(rsaSpec);
		return publicKey;
	}

	/**
	 * 生成DSA 公钥
	 * @param y the public key.
	 * @param p the prime.
	 * @param q the sub-prime.
	 * @param g the base.
	 * @return PublicKey
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	public static PublicKey generateDsaPublicKey(BigInteger y, BigInteger p, BigInteger q,BigInteger g) throws
			InvalidKeySpecException, NoSuchAlgorithmException {
		DSAPublicKeySpec rsaSpec = new DSAPublicKeySpec(y, p,q,g);
		KeyFactory kf = KeyFactory.getInstance(KeyPairAlgorithms.DSA.name());
		PublicKey publicKey = kf.generatePublic(rsaSpec);
		return publicKey;
	}

	/**
	 * 生成RSA私钥
	 * @param modulus the modulus
	 * @param privateExponent  the private exponent
	 * @return PrivateKey
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	public static PrivateKey generateRsaPrivateKey(BigInteger modulus, BigInteger privateExponent)
			throws InvalidKeySpecException, NoSuchAlgorithmException {
		RSAPrivateKeySpec rsaSpec = new RSAPrivateKeySpec(modulus, privateExponent);
		KeyFactory kf = KeyFactory.getInstance(KeyPairAlgorithms.RSA.name());
		PrivateKey prikey = kf.generatePrivate(rsaSpec);
		return prikey;
	}

	/**
	 * 生成DSA私钥
	 * @param x the private key.
	 * @param p the prime.
	 * @param q the sub-prime.
	 * @param g the base.
	 * @return PrivateKey
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	public static PrivateKey generateDsaPrivateKey(BigInteger x, BigInteger p, BigInteger q,BigInteger g)
			throws InvalidKeySpecException, NoSuchAlgorithmException {
		DSAPrivateKeySpec rsaSpec = new DSAPrivateKeySpec(x, p,q,g);
		KeyFactory kf = KeyFactory.getInstance(KeyPairAlgorithms.RSA.name());
		PrivateKey prikey = kf.generatePrivate(rsaSpec);
		return prikey;
	}
}
