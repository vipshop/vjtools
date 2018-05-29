package com.vip.vjtools.vjkit.text;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.zip.CRC32;

import org.apache.commons.lang3.Validate;

import com.google.common.hash.Hashing;
import com.vip.vjtools.vjkit.base.annotation.NotNull;
import com.vip.vjtools.vjkit.base.annotation.Nullable;

/**
 * 封装各种Hash算法的工具类
 * 
 * 1.SHA-1, 安全性较高, 返回byte[](可用Encodes进一步被编码为Hex, Base64)
 * 
 * 性能优化，使用ThreadLocal的MessageDigest(from ElasticSearch)
 * 
 * 支持带salt并且进行迭代达到更高的安全性.
 * 
 * MD5的安全性较低, 只在文件Checksum时支持.
 * 
 * 2.crc32, murmur32这些不追求安全性, 性能较高, 返回int.
 * 
 * 其中crc32基于JDK, murmurhash基于guava
 */
public class HashUtil {

	public static final int MURMUR_SEED = 1_318_007_700;

	private static final ThreadLocal<MessageDigest> MD5_DIGEST = createThreadLocalMessageDigest("MD5");
	private static final ThreadLocal<MessageDigest> SHA_1_DIGEST = createThreadLocalMessageDigest("SHA-1");

	private static SecureRandom random = new SecureRandom();

	// ThreadLocal重用MessageDigest
	private static ThreadLocal<MessageDigest> createThreadLocalMessageDigest(final String digest) {
		return new ThreadLocal<MessageDigest>() {
			@Override
			protected MessageDigest initialValue() {
				try {
					return MessageDigest.getInstance(digest);
				} catch (NoSuchAlgorithmException e) {
					throw new RuntimeException(
							"unexpected exception creating MessageDigest instance for [" + digest + ']', e);
				}
			}
		};
	}

	////////////////// SHA1 ///////////////////
	/**
	 * 对输入字符串进行sha1散列.
	 */
	public static byte[] sha1(@NotNull byte[] input) {
		return digest(input, get(SHA_1_DIGEST), null, 1);
	}

	/**
	 * 对输入字符串进行sha1散列, 编码默认为UTF8.
	 */
	public static byte[] sha1(@NotNull String input) {
		return digest(input.getBytes(Charsets.UTF_8), get(SHA_1_DIGEST), null, 1);
	}

	/**
	 * 对输入字符串进行sha1散列，带salt达到更高的安全性.
	 */
	public static byte[] sha1(@NotNull byte[] input, @Nullable byte[] salt) {
		return digest(input, get(SHA_1_DIGEST), salt, 1);
	}

	/**
	 * 对输入字符串进行sha1散列，带salt达到更高的安全性.
	 */
	public static byte[] sha1(@NotNull String input, @Nullable byte[] salt) {
		return digest(input.getBytes(Charsets.UTF_8), get(SHA_1_DIGEST), salt, 1);
	}

	/**
	 * 对输入字符串进行sha1散列，带salt而且迭代达到更高更高的安全性.
	 * 
	 * @see #generateSalt(int)
	 */
	public static byte[] sha1(@NotNull byte[] input, @Nullable byte[] salt, int iterations) {
		return digest(input, get(SHA_1_DIGEST), salt, iterations);
	}

	/**
	 * 对输入字符串进行sha1散列，带salt而且迭代达到更高更高的安全性.
	 * 
	 * @see #generateSalt(int)
	 */
	public static byte[] sha1(@NotNull String input, @Nullable byte[] salt, int iterations) {
		return digest(input.getBytes(Charsets.UTF_8), get(SHA_1_DIGEST), salt, iterations);
	}

	private static MessageDigest get(ThreadLocal<MessageDigest> messageDigest) {
		MessageDigest instance = messageDigest.get();
		instance.reset();
		return instance;
	}

	/**
	 * 对字符串进行散列, 支持md5与sha1算法.
	 */
	private static byte[] digest(@NotNull byte[] input, MessageDigest digest, byte[] salt, int iterations) {
		// 带盐
		if (salt != null) {
			digest.update(salt);
		}

		// 第一次散列
		byte[] result = digest.digest(input);

		// 如果迭代次数>1，进一步迭代散列
		for (int i = 1; i < iterations; i++) {
			digest.reset();
			result = digest.digest(result);
		}

		return result;
	}

	/**
	 * 用SecureRandom生成随机的byte[]作为salt.
	 * 
	 * @param numBytes salt数组的大小
	 */
	public static byte[] generateSalt(int numBytes) {
		Validate.isTrue(numBytes > 0, "numBytes argument must be a positive integer (1 or larger)", numBytes);

		byte[] bytes = new byte[numBytes];
		random.nextBytes(bytes);
		return bytes;
	}

	/**
	 * 对文件进行sha1散列.
	 */
	public static byte[] sha1File(InputStream input) throws IOException {
		return digestFile(input, get(SHA_1_DIGEST));
	}

	/**
	 * 对文件进行md5散列，被破解后MD5已较少人用.
	 */
	public static byte[] md5File(InputStream input) throws IOException {
		return digestFile(input, get(MD5_DIGEST));
	}

	private static byte[] digestFile(InputStream input, MessageDigest messageDigest) throws IOException {
		int bufferLength = 8 * 1024;
		byte[] buffer = new byte[bufferLength];
		int read = input.read(buffer, 0, bufferLength);

		while (read > -1) {
			messageDigest.update(buffer, 0, read);
			read = input.read(buffer, 0, bufferLength);
		}

		return messageDigest.digest();
	}

	////////////////// 基于JDK的CRC32 ///////////////////

	/**
	 * 对输入字符串进行crc32散列返回int, 返回值有可能是负数.
	 * 
	 * Guava也有crc32实现, 但返回值无法返回long，所以统一使用JDK默认实现
	 */
	public static int crc32AsInt(@NotNull String input) {
		return crc32AsInt(input.getBytes(Charsets.UTF_8));
	}

	/**
	 * 对输入字符串进行crc32散列返回int, 返回值有可能是负数.
	 * 
	 * Guava也有crc32实现, 但返回值无法返回long，所以统一使用JDK默认实现
	 */
	public static int crc32AsInt(@NotNull byte[] input) {
		CRC32 crc32 = new CRC32();
		crc32.update(input);
		// CRC32 只是 32bit int，为了CheckSum接口强转成long，此处再次转回来
		return (int) crc32.getValue();
	}

	/**
	 * 对输入字符串进行crc32散列，与php兼容，在64bit系统下返回永远是正数的long
	 * 
	 * Guava也有crc32实现, 但返回值无法返回long，所以统一使用JDK默认实现
	 */
	public static long crc32AsLong(@NotNull String input) {
		return crc32AsLong(input.getBytes(Charsets.UTF_8));
	}

	/**
	 * 对输入字符串进行crc32散列，与php兼容，在64bit系统下返回永远是正数的long
	 * 
	 * Guava也有crc32实现, 但返回值无法返回long，所以统一使用JDK默认实现
	 */
	public static long crc32AsLong(@NotNull byte[] input) {
		CRC32 crc32 = new CRC32();
		crc32.update(input);
		return crc32.getValue();
	}

	////////////////// 基于Guava的MurMurHash ///////////////////
	/**
	 * 对输入字符串进行murmur32散列, 返回值可能是负数
	 */
	public static int murmur32AsInt(@NotNull byte[] input) {
		return Hashing.murmur3_32(MURMUR_SEED).hashBytes(input).asInt();
	}

	/**
	 * 对输入字符串进行murmur32散列, 返回值可能是负数
	 */
	public static int murmur32AsInt(@NotNull String input) {
		return Hashing.murmur3_32(MURMUR_SEED).hashString(input, Charsets.UTF_8).asInt();
	}

	/**
	 * 对输入字符串进行murmur128散列, 返回值可能是负数
	 */
	public static long murmur128AsLong(@NotNull byte[] input) {
		return Hashing.murmur3_128(MURMUR_SEED).hashBytes(input).asLong();
	}

	/**
	 * 对输入字符串进行murmur128散列, 返回值可能是负数
	 */
	public static long murmur128AsLong(@NotNull String input) {
		return Hashing.murmur3_128(MURMUR_SEED).hashString(input, Charsets.UTF_8).asLong();
	}
}
