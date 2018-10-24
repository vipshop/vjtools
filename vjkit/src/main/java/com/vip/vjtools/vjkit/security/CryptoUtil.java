package com.vip.vjtools.vjkit.security;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.vip.vjtools.vjkit.base.ExceptionUtil;
import com.vip.vjtools.vjkit.enums.CipherAlgorithms;
import com.vip.vjtools.vjkit.enums.KeyGeneratorType;
import com.vip.vjtools.vjkit.enums.SecretKeyType;
import com.vip.vjtools.vjkit.number.RandomUtil;
import com.vip.vjtools.vjkit.text.Charsets;

/**
 * 支持HMAC-SHA1消息签名 及 DES/AES对称加密的工具类.
 * 
 * 支持Hex与Base64两种编码方式.
 */
public class CryptoUtil {

	private static final String AES_ALG = "AES";
	private static final String AES_CBC_ALG = "AES/CBC/PKCS5Padding";
	private static final String HMACSHA1_ALG = "HmacSHA1";

	private static final int DEFAULT_HMACSHA1_KEYSIZE = 160; // RFC2401
	private static final int DEFAULT_AES_KEYSIZE = 128;
	private static final int DEFAULT_IVSIZE = 16;

	private static SecureRandom random = RandomUtil.secureRandom();

	// -- HMAC-SHA1 funciton --//
	/**
	 * 使用HMAC-SHA1进行消息签名, 返回字节数组,长度为20字节.
	 * 
	 * @param input 原始输入字符数组
	 * @param key HMAC-SHA1密钥
	 */
	public static byte[] hmacSha1(byte[] input, byte[] key) {
		try {
			SecretKey secretKey = new SecretKeySpec(key, HMACSHA1_ALG);
			Mac mac = Mac.getInstance(HMACSHA1_ALG);
			mac.init(secretKey);
			return mac.doFinal(input);
		} catch (GeneralSecurityException e) {
			throw ExceptionUtil.unchecked(e);
		}
	}

	/**
	 * 校验HMAC-SHA1签名是否正确.
	 * 
	 * @param expected 已存在的签名
	 * @param input 原始输入字符串
	 * @param key 密钥
	 */
	public static boolean isMacValid(byte[] expected, byte[] input, byte[] key) {
		byte[] actual = hmacSha1(input, key);
		return Arrays.equals(expected, actual);
	}

	/**
	 * 生成HMAC-SHA1密钥,返回字节数组,长度为160位(20字节). HMAC-SHA1算法对密钥无特殊要求, RFC2401建议最少长度为160位(20字节).
	 */
	public static byte[] generateHmacSha1Key() {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance(HMACSHA1_ALG);
			keyGenerator.init(DEFAULT_HMACSHA1_KEYSIZE);
			SecretKey secretKey = keyGenerator.generateKey();
			return secretKey.getEncoded();
		} catch (GeneralSecurityException e) {
			throw ExceptionUtil.unchecked(e);
		}
	}

	///////////// -- AES funciton --//////////
	/**
	 * 使用AES加密原始字符串.
	 * 
	 * @param input 原始输入字符数组
	 * @param key 符合AES要求的密钥
	 */
	public static byte[] aesEncrypt(byte[] input, byte[] key) {
		return aes(input, key, Cipher.ENCRYPT_MODE);
	}

	/**
	 * 使用AES加密原始字符串.
	 * 
	 * @param input 原始输入字符数组
	 * @param key 符合AES要求的密钥
	 * @param iv 初始向量
	 */
	public static byte[] aesEncrypt(byte[] input, byte[] key, byte[] iv) {
		return aes(input, key, iv, Cipher.ENCRYPT_MODE);
	}

	/**
	 * 使用AES解密字符串, 返回原始字符串.
	 * 
	 * @param input Hex编码的加密字符串
	 * @param key 符合AES要求的密钥
	 */
	public static String aesDecrypt(byte[] input, byte[] key) {
		byte[] decryptResult = aes(input, key, Cipher.DECRYPT_MODE);
		return new String(decryptResult, Charsets.UTF_8);
	}

	/**
	 * 使用AES解密字符串, 返回原始字符串.
	 * 
	 * @param input Hex编码的加密字符串
	 * @param key 符合AES要求的密钥
	 * @param iv 初始向量
	 */
	public static String aesDecrypt(byte[] input, byte[] key, byte[] iv) {
		byte[] decryptResult = aes(input, key, iv, Cipher.DECRYPT_MODE);
		return new String(decryptResult, Charsets.UTF_8);
	}

	/**
	 * 使用AES加密或解密无编码的原始字节数组, 返回无编码的字节数组结果.
	 * 
	 * @param input 原始字节数组
	 * @param key 符合AES要求的密钥
	 * @param mode Cipher.ENCRYPT_MODE 或 Cipher.DECRYPT_MODE
	 */
	private static byte[] aes(byte[] input, byte[] key, int mode) {
		try {
			SecretKey secretKey = new SecretKeySpec(key, AES_ALG);
			Cipher cipher = Cipher.getInstance(AES_ALG);
			cipher.init(mode, secretKey);
			return cipher.doFinal(input);
		} catch (GeneralSecurityException e) {
			throw ExceptionUtil.unchecked(e);
		}
	}

	/**
	 * 使用AES加密或解密无编码的原始字节数组, 返回无编码的字节数组结果.
	 * 
	 * @param input 原始字节数组
	 * @param key 符合AES要求的密钥
	 * @param iv 初始向量
	 * @param mode Cipher.ENCRYPT_MODE 或 Cipher.DECRYPT_MODE
	 */
	private static byte[] aes(byte[] input, byte[] key, byte[] iv, int mode) {
		try {
			SecretKey secretKey = new SecretKeySpec(key, AES_ALG);
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			Cipher cipher = Cipher.getInstance(AES_CBC_ALG);
			cipher.init(mode, secretKey, ivSpec);
			return cipher.doFinal(input);
		} catch (GeneralSecurityException e) {
			throw ExceptionUtil.unchecked(e);
		}
	}

	/**
	 * 生成AES密钥,返回字节数组, 默认长度为128位(16字节).
	 */
	public static byte[] generateAesKey() {
		return generateAesKey(DEFAULT_AES_KEYSIZE);
	}

	/**
	 * 生成AES密钥,可选长度为128,192,256位.
	 */
	public static byte[] generateAesKey(int keysize) {
		try {
			KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALG);
			keyGenerator.init(keysize);
			SecretKey secretKey = keyGenerator.generateKey();
			return secretKey.getEncoded();
		} catch (GeneralSecurityException e) {
			throw ExceptionUtil.unchecked(e);
		}
	}

	/**
	 * 生成AES随机向量,默认大小为cipher.getBlockSize(), 16字节.
	 */
	public static byte[] generateIV() {
		byte[] bytes = new byte[DEFAULT_IVSIZE];
		random.nextBytes(bytes);
		return bytes;
	}

	/**
	 * 生成DES随机向量,默认大小为cipher.getBlockSize(), 8字节.
	 */
	public static byte[] generateDesIV() {
		byte[] bytes = new byte[8];
		random.nextBytes(bytes);
		return bytes;
	}

	/**
	 * 加密或解密无编码的原始字节数组, 返回无编码的字节数组结果.
	 *
	 * @param input 原始字节数组
	 * @param key 符合SecretKeyType要求的密钥
	 * @param mode Cipher.ENCRYPT_MODE 或 Cipher.DECRYPT_MODE
	 */
	private static byte[] doFinal(byte[] input, byte[] key, int mode,SecretKeyType secretKeyType,CipherAlgorithms encrptAlgorithms) {
		try {
			SecretKey secretKey = new SecretKeySpec(key, secretKeyType.name());
			Cipher cipher = Cipher.getInstance(encrptAlgorithms.getValue());
			cipher.init(mode, secretKey);
			return cipher.doFinal(input);
		} catch (GeneralSecurityException e) {
			throw ExceptionUtil.unchecked(e);
		}
	}

	/**
	 * 加密或解密无编码的原始字节数组, 返回无编码的字节数组结果.
	 *
	 * @param input 原始字节数组
	 * @param key 符合SecretKeyType要求的密钥
	 * @param iv 初始向量
	 * @param mode Cipher.ENCRYPT_MODE 或 Cipher.DECRYPT_MODE
	 */
	private static byte[] doFinal(byte[] input, byte[] key, byte[] iv, int mode,SecretKeyType secretKeyType,CipherAlgorithms encrptAlgorithms) {
		try {
			SecretKey secretKey = new SecretKeySpec(key, secretKeyType.name());
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			Cipher cipher = Cipher.getInstance(encrptAlgorithms.getValue());
			cipher.init(mode, secretKey, ivSpec);
			return cipher.doFinal(input);
		} catch (GeneralSecurityException e) {
			throw ExceptionUtil.unchecked(e);
		}
	}


	/**
	 * des加密
	 * @param input 原始字节数组
	 * @param key des秘钥
	 * @param algorithms 可选不同的加密工作模式/填充模式，具体见CipherAlgorithms枚举
	 * @return byte[] 加密结果
	 */
	public static byte[] desEncrypt(byte[] input, byte[] key,CipherAlgorithms algorithms){
		return doFinal(input,key,Cipher.ENCRYPT_MODE,SecretKeyType.DES,algorithms);
	}


	/**
	 * des解密
	 * @param input 密文数组
	 * @param key des秘钥
	 * @param algorithms 可选不同的加密工作模式/填充模式，具体见CipherAlgorithms枚举
	 * @return byte[] 解密后明文结果
	 */
	public static byte[] desDecrypt(byte[] input, byte[] key,CipherAlgorithms algorithms){
		return doFinal(input,key,Cipher.DECRYPT_MODE,SecretKeyType.DES,algorithms);
	}


	/**
	 * des加密
	 * @param input 原始字节数组
	 * @param key des秘钥
	 * @param iv 初始向量
	 * @param algorithms 可选不同的加密工作模式/填充模式，具体见CipherAlgorithms枚举
	 * @return byte[] 加密结果
	 */
	public static byte[] desEncrypt(byte[] input, byte[] key, byte[] iv,CipherAlgorithms algorithms){
		return doFinal(input,key,iv,Cipher.ENCRYPT_MODE,SecretKeyType.DES,algorithms);
	}


	/**
	 * des解密
	 * @param input 密文数组
	 * @param key des秘钥
	 *  @param iv 初始向量
	 * @param algorithms 可选不同的加密工作模式/填充模式，具体见CipherAlgorithms枚举
	 * @return byte[] 解密后明文结果
	 */
	public static byte[] desDecrypt(byte[] input, byte[] key, byte[] iv,CipherAlgorithms algorithms){
		return doFinal(input,key,iv,Cipher.DECRYPT_MODE,SecretKeyType.DES,algorithms);
	}


	/**
	 * aes加密
	 * @param input 原始字节数组
	 * @param key aes秘钥
	 * @param algorithms 可选不同的加密工作模式/填充模式，具体见CipherAlgorithms枚举
	 * @return byte[] 加密结果
	 */
	public static byte[] aesEncrypt(byte[] input, byte[] key,CipherAlgorithms algorithms){
		return doFinal(input,key,Cipher.ENCRYPT_MODE,SecretKeyType.AES,algorithms);
	}


	/**
	 * aes解密
	 * @param input 密文数组
	 * @param key aes秘钥
	 * @param algorithms 可选不同的加密工作模式/填充模式，具体见CipherAlgorithms枚举
	 * @return byte[] 解密后明文结果
	 */
	public static byte[] aesDecrypt(byte[] input, byte[] key,CipherAlgorithms algorithms){
		return doFinal(input,key,Cipher.DECRYPT_MODE,SecretKeyType.AES,algorithms);
	}


	/**
	 * aes加密
	 * @param input 原始字节数组
	 * @param key aes秘钥
	 * @param iv 初始向量
	 * @param algorithms 可选不同的加密工作模式/填充模式，具体见CipherAlgorithms枚举
	 * @return byte[] 加密结果
	 */
	public static byte[] aesEncrypt(byte[] input, byte[] key,byte[] iv,CipherAlgorithms algorithms){
		return doFinal(input,key,iv,Cipher.ENCRYPT_MODE,SecretKeyType.AES,algorithms);
	}


	/**
	 * aes解密
	 * @param input 密文数组
	 * @param key aes秘钥
	 * @param iv 初始向量
	 * @param algorithms 可选不同的加密工作模式/填充模式，具体见CipherAlgorithms枚举
	 * @return byte[] 解密后明文结果
	 */
	public static byte[] aesDecrypt(byte[] input, byte[] key,byte[] iv,CipherAlgorithms algorithms){
		return doFinal(input,key,iv,Cipher.DECRYPT_MODE,SecretKeyType.AES,algorithms);
	}


	/**
	 * des3加密
	 * @param input 原始字节数组
	 * @param key des3秘钥
	 * @param algorithms 可选不同的加密工作模式/填充模式，具体见CipherAlgorithms枚举
	 * @return byte[] 加密结果
	 */
	public static byte[] des3Encrypt(byte[] input, byte[] key,CipherAlgorithms algorithms){
		return doFinal(input,key,Cipher.ENCRYPT_MODE,SecretKeyType.DESede,algorithms);
	}


	/**
	 * des3解密
	 * @param input 密文数组
	 * @param key des3秘钥
	 * @param algorithms 可选不同的加密工作模式/填充模式，具体见CipherAlgorithms枚举
	 * @return byte[] 解密后明文结果
	 */
	public static byte[] des3Decrypt(byte[] input, byte[] key,CipherAlgorithms algorithms){
		return doFinal(input,key,Cipher.DECRYPT_MODE,SecretKeyType.DESede,algorithms);
	}


	/**
	 * des3加密
	 * @param input 原始字节数组
	 * @param key des3秘钥
	 * @param iv 初始向量
	 * @param algorithms 可选不同的加密工作模式/填充模式，具体见CipherAlgorithms枚举
	 * @return byte[] 加密结果
	 */
	public static byte[] des3Encrypt(byte[] input, byte[] key,byte[] iv,CipherAlgorithms algorithms){
		return doFinal(input,key,iv,Cipher.ENCRYPT_MODE,SecretKeyType.DESede,algorithms);
	}


	/**
	 * des3解密
	 * @param input 密文数组
	 * @param key des3秘钥
	 * @param  iv 初始向量
	 * @param algorithms 可选不同的加密工作模式/填充模式，具体见CipherAlgorithms枚举
	 * @return byte[] 解密后明文结果
	 */
	public static byte[] des3Decrypt(byte[] input, byte[] key,byte[] iv,CipherAlgorithms algorithms){
		return doFinal(input,key,iv,Cipher.DECRYPT_MODE,SecretKeyType.DESede,algorithms);
	}


	/**
	 * rc2加密
	 * @param input 原始字节数组
	 * @param key rc2秘钥
	 * @param algorithms 可选不同的加密工作模式/填充模式，具体见CipherAlgorithms枚举
	 * @return byte[] 加密结果
	 */
	public static byte[] rc2Encrypt(byte[] input, byte[] key,CipherAlgorithms algorithms){
		return doFinal(input,key,Cipher.ENCRYPT_MODE,SecretKeyType.RC2,algorithms);
	}


	/**
	 * rc2解密
	 * @param input 密文数组
	 * @param key rc2秘钥
	 * @param algorithms 可选不同的加密工作模式/填充模式，具体见CipherAlgorithms枚举
	 * @return byte[] 解密后明文结果
	 */
	public static byte[] rc2Decrypt(byte[] input, byte[] key,CipherAlgorithms algorithms){
		return doFinal(input,key,Cipher.DECRYPT_MODE,SecretKeyType.RC2,algorithms);
	}


	/**
	 * rc2加密
	 * @param input 原始字节数组
	 * @param key rc2秘钥
	 * @param iv 初始向量
	 * @param algorithms 可选不同的加密工作模式/填充模式，具体见CipherAlgorithms枚举
	 * @return byte[] 加密结果
	 */
	public static byte[] rc2Encrypt(byte[] input, byte[] key,byte[] iv,CipherAlgorithms algorithms){
		return doFinal(input,key,iv,Cipher.ENCRYPT_MODE,SecretKeyType.RC2,algorithms);
	}


	/**
	 * rc2解密
	 * @param input 密文数组
	 * @param key rc2秘钥
	 * @param  iv 初始向量
	 * @param algorithms 可选不同的加密工作模式/填充模式，具体见CipherAlgorithms枚举
	 * @return byte[] 解密后明文结果
	 */
	public static byte[] rc2Decrypt(byte[] input, byte[] key,byte[] iv,CipherAlgorithms algorithms){
		return doFinal(input,key,iv,Cipher.DECRYPT_MODE,SecretKeyType.RC2,algorithms);
	}

	/**
	 * rc4加密
	 * @param input 原始字节数组
	 * @param key rc4秘钥
	 * @param algorithms 可选不同的加密工作模式/填充模式，具体见CipherAlgorithms枚举
	 * @return byte[] 加密结果
	 */
	public static byte[] rc4Encrypt(byte[] input, byte[] key,CipherAlgorithms algorithms){
		return doFinal(input,key,Cipher.ENCRYPT_MODE,SecretKeyType.RC4,algorithms);
	}


	/**
	 * rc4解密
	 * @param input 密文数组
	 * @param key rc4秘钥
	 * @param algorithms 可选不同的加密工作模式/填充模式，具体见CipherAlgorithms枚举
	 * @return byte[] 解密后明文结果
	 */
	public static byte[] rc4Decrypt(byte[] input, byte[] key,CipherAlgorithms algorithms){
		return doFinal(input,key,Cipher.DECRYPT_MODE,SecretKeyType.RC4,algorithms);
	}


	/**
	 * rc4加密
	 * @param input 原始字节数组
	 * @param key rc4秘钥
	 * @param iv 初始向量
	 * @param algorithms 可选不同的加密工作模式/填充模式，具体见CipherAlgorithms枚举
	 * @return byte[] 加密结果
	 */
	public static byte[] rc4Encrypt(byte[] input, byte[] key,byte[] iv,CipherAlgorithms algorithms){
		return doFinal(input,key,iv,Cipher.ENCRYPT_MODE,SecretKeyType.RC4,algorithms);
	}


	/**
	 * rc4解密
	 * @param input 密文数组
	 * @param key rc4秘钥
	 * @param  iv 初始向量
	 * @param algorithms 可选不同的加密工作模式/填充模式，具体见CipherAlgorithms枚举
	 * @return byte[] 解密后明文结果
	 */
	public static byte[] rc4Decrypt(byte[] input, byte[] key,byte[] iv,CipherAlgorithms algorithms){
		return doFinal(input,key,iv,Cipher.DECRYPT_MODE,SecretKeyType.RC4,algorithms);
	}

	/**
	 * rsa加密
	 * @param input 原始字节数组
	 * @param key rsa 公开秘钥
	 * @param algorithms 可选不同的加密工作模式/填充模式，具体见CipherAlgorithms枚举
	 * @return byte[] 加密结果
	 */
	public static byte[] rsaEncrypt(byte[] input, byte[] key,CipherAlgorithms algorithms){
		try {
			// 转换公钥材料
			// 实例化密钥工厂
			KeyFactory keyFactory = KeyFactory.getInstance(SecretKeyType.RSA.name());
			// 初始化公钥
			// 密钥材料转换
			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(key);
			// 产生公钥
			PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);
			Cipher cipher = Cipher.getInstance(algorithms.getValue());
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			return cipher.doFinal(input);
		} catch (GeneralSecurityException e) {
			throw ExceptionUtil.unchecked(e);
		}
	}


	/**
	 * rsa解密
	 * @param input 密文数组
	 * @param key rsa 私钥秘钥
	 * @param algorithms 可选不同的加密工作模式/填充模式，具体见CipherAlgorithms枚举
	 * @return byte[] 解密后明文结果
	 */
	public static byte[] rsaDecrypt(byte[] input, byte[] key,CipherAlgorithms algorithms){
		try {
			PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(key);
			KeyFactory keyFactory = KeyFactory.getInstance(SecretKeyType.RSA.name());
			Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
			Cipher cipher = Cipher.getInstance(algorithms.getValue());
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			return cipher.doFinal(input);
		} catch (GeneralSecurityException e) {
			throw ExceptionUtil.unchecked(e);
		}
	}

	/**
	 * rsa加密
	 * @param input 原始字节数组
	 * @param key rsa 公开秘钥
	 * @param iv 初始向量
	 * @param algorithms 可选不同的加密工作模式/填充模式，具体见CipherAlgorithms枚举
	 * @return byte[] 加密结果
	 */
	public static byte[] rsaEncrypt(byte[] input, byte[] key,byte[] iv,CipherAlgorithms algorithms){
		try {
			// 转换公钥材料
			// 实例化密钥工厂
			KeyFactory keyFactory = KeyFactory.getInstance(SecretKeyType.RSA.name());
			// 初始化公钥
			// 密钥材料转换
			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(key);
			// 产生公钥
			PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);
			Cipher cipher = Cipher.getInstance(algorithms.getValue());
			cipher.init(Cipher.ENCRYPT_MODE, pubKey,new IvParameterSpec(iv));
			return cipher.doFinal(input);
		} catch (GeneralSecurityException e) {
			throw ExceptionUtil.unchecked(e);
		}
	}


	/**
	 * rsa解密
	 * @param input 密文数组
	 * @param key rsa 私钥秘钥
	 * @param iv 初始向量
	 * @param algorithms 可选不同的加密工作模式/填充模式，具体见CipherAlgorithms枚举
	 * @return byte[] 解密后明文结果
	 */
	public static byte[] rsaDecrypt(byte[] input, byte[] key,byte[] iv,CipherAlgorithms algorithms){
		try {
			PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(key);
			KeyFactory keyFactory = KeyFactory.getInstance(SecretKeyType.RSA.name());
			Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
			Cipher cipher = Cipher.getInstance(algorithms.getValue());
			cipher.init(Cipher.DECRYPT_MODE, privateKey,new IvParameterSpec(iv));
			return cipher.doFinal(input);
		} catch (GeneralSecurityException e) {
			throw ExceptionUtil.unchecked(e);
		}
	}

	/**
	 * pbe加密
	 * @param input 原始字节数组
	 * @param password pbe 字符串秘钥 可以任意长度
	 * @param salt 盐 Salt must be 8 bytes long
	 * @param algorithms 可选不同的加密工作模式/填充模式，具体见CipherAlgorithms枚举
	 * @return byte[] 加密结果
	 */
	public static byte[] pbeEncrypt(byte[] input, String password,byte[] salt,CipherAlgorithms algorithms){
		try {
			PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algorithms.getValue());
			SecretKey secretKey = keyFactory.generateSecret(keySpec);
			PBEParameterSpec paramSpec = new PBEParameterSpec(salt, 100);//100:iterationCount - the iteration count.
			Cipher cipher = Cipher.getInstance(algorithms.getValue());
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, paramSpec);
			return cipher.doFinal(input);
		} catch (GeneralSecurityException e) {
			throw ExceptionUtil.unchecked(e);
		}
	}


	/**
	 * pbe解密
	 * @param input 密文数组
	 * @param password pbe 字符串秘钥 可以任意长度
	 * @param salt 盐值 Salt must be 8 bytes long
	 * @param algorithms 可选不同的加密工作模式/填充模式，具体见CipherAlgorithms枚举
	 * @return byte[] 解密后明文结果
	 */
	public static byte[] pbeDecrypt(byte[] input, String password,byte[] salt,CipherAlgorithms algorithms){
		try {
			PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algorithms.getValue());
			SecretKey secretKey = keyFactory.generateSecret(keySpec);
			PBEParameterSpec paramSpec = new PBEParameterSpec(salt, 100);//100:iterationCount - the iteration count.
			Cipher cipher = Cipher.getInstance(algorithms.getValue());
			cipher.init(Cipher.DECRYPT_MODE, secretKey, paramSpec);
			return cipher.doFinal(input);
		} catch (GeneralSecurityException e) {
			throw ExceptionUtil.unchecked(e);
		}
	}


	/**
	 * DiffieHellman算法加密
	 * @param input 加密原文
	 * @param publicKey 对方公钥
	 * @param privateKey 我方私钥
	 * @param keyAlgorithms 生成的对称秘钥算法：仅支持 DES/DESede/AES
	 * @return byte[] 密文
	 * @throws Exception
	 */
	public static byte[] dhEncrypt(byte[] input,PublicKey publicKey,PrivateKey privateKey,KeyGeneratorType keyAlgorithms)
			throws Exception {
		SecretKey secretKey = KeyUtil.generateKey(publicKey, privateKey, keyAlgorithms);
		// 数据解密
		Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		return cipher.doFinal(input);
	}


	/**
	 * DiffieHellman算法解密
	 * @param input 加密密文
	 * @param publicKey 对方公钥
	 * @param privateKey 我方私钥
	 * @param keyAlgorithms 生成的对称秘钥算法：仅支持 DES/DESede/AES
	 * @return byte[] 解密明文
	 * @throws Exception
	 */
	public static byte[] dhDencrypt(byte[] input,PublicKey publicKey,PrivateKey privateKey,KeyGeneratorType keyAlgorithms)
			throws Exception {
		SecretKey secretKey = KeyUtil.generateKey(publicKey, privateKey, keyAlgorithms);
		// 数据解密
		Cipher cipher = Cipher.getInstance(secretKey.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		return cipher.doFinal(input);
	}
}
