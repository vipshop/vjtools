package com.vip.vjtools.vjkit.number;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.Validate;

import com.vip.vjtools.vjkit.base.MoreValidate;

/**
 * 随机数工具集.
 * 
 * 1. 获取无锁的ThreadLocalRandom, 性能较佳的SecureRandom
 * 
 * 2. 保证没有负数陷阱，也能更精确设定范围的nextInt/nextLong/nextDouble
 *  (copy from Common Lang RandomUtils，但默认使用性能较优的ThreadLocalRandom，并可配置其他的Random)
 * 
 * 3. 随机字符串 (via Common Lang RandomStringUtils)
 * 
 * @author calvin
 */
public class RandomUtil {

	/////////////////// 获取Random实例//////////////
	/**
	 * 返回无锁的ThreadLocalRandom
	 */
	public static Random threadLocalRandom() {
		return ThreadLocalRandom.current();
	}


	/**
	 * SecureRandom使用性能更好的SHA1PRNG, Tomcat的sessionId生成也用此算法.
	 * 
	 * 但JDK7中，需要在启动参数加入 -Djava.security=file:/dev/./urandom （中间那个点很重要）
	 * 
	 * 详见：《SecureRandom的江湖偏方与真实效果》http://calvin1978.blogcn.com/articles/securerandom.html
	 */
	public static SecureRandom secureRandom() {
		try {
			return SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {// NOSONAR
			return new SecureRandom();
		}
	}

	////////////////// nextInt 相关/////////
	/**
	 * 返回0到Intger.MAX_VALUE的随机Int, 使用ThreadLocalRandom.
	 */
	public static int nextInt() {
		return nextInt(ThreadLocalRandom.current());
	}

	/**
	 * 返回0到Intger.MAX_VALUE的随机Int, 可传入ThreadLocalRandom或SecureRandom
	 */
	public static int nextInt(Random random) {
		int n = random.nextInt();
		if (n == Integer.MIN_VALUE) {
			n = 0; // corner case
		} else {
			n = Math.abs(n);
		}

		return n;
	}

	/**
	 * 返回0到max的随机Int, 使用ThreadLocalRandom.
	 */
	public static int nextInt(int max) {
		return nextInt(ThreadLocalRandom.current(), max);
	}

	/**
	 * 返回0到max的随机Int, 可传入SecureRandom或ThreadLocalRandom
	 */
	public static int nextInt(Random random, int max) {
		return random.nextInt(max);
	}

	/**
	 * 返回min到max的随机Int, 使用ThreadLocalRandom.
	 * 
	 * min必须大于0.
	 */
	public static int nextInt(int min, int max) {
		return nextInt(ThreadLocalRandom.current(), min, max);
	}

	/**
	 * 返回min到max的随机Int,可传入SecureRandom或ThreadLocalRandom.
	 * 
	 * min必须大于0.
	 * 
	 * JDK本身不具有控制两端范围的nextInt，因此参考Commons Lang RandomUtils的实现, 不直接复用是因为要传入Random实例
	 * 
	 * @see org.apache.commons.lang3.RandomUtils#nextInt(long, long)
	 */
	public static int nextInt(Random random, int min, int max) {
		Validate.isTrue(max >= min, "Start value must be smaller or equal to end value.");
		MoreValidate.nonNegative("min", min);

		if (min == max) {
			return min;
		}

		return min + random.nextInt(max - min);
	}

	////////////////// long 相关/////////
	/**
	 * 返回0－Long.MAX_VALUE间的随机Long, 使用ThreadLocalRandom.
	 */
	public static long nextLong() {
		return nextLong(ThreadLocalRandom.current());
	}

	/**
	 * 返回0－Long.MAX_VALUE间的随机Long, 可传入SecureRandom或ThreadLocalRandom
	 */
	public static long nextLong(Random random) {
		long n = random.nextLong();
		if (n == Long.MIN_VALUE) {
			n = 0; // corner case
		} else {
			n = Math.abs(n);
		}
		return n;
	}

	/**
	 * 返回0－max间的随机Long, 使用ThreadLocalRandom.
	 */
	public static long nextLong(long max) {
		return nextLong(ThreadLocalRandom.current(), 0, max);
	}

	/**
	 * 返回0-max间的随机Long, 可传入SecureRandom或ThreadLocalRandom
	 */
	public static long nextLong(Random random, long max) {
		return nextLong(random, 0, max);
	}

	/**
	 * 返回min－max间的随机Long, 使用ThreadLocalRandom.
	 * 
	 * min必须大于0.
	 */
	public static long nextLong(long min, long max) {
		return nextLong(ThreadLocalRandom.current(), min, max);
	}

	/**
	 * 返回min-max间的随机Long,可传入SecureRandom或ThreadLocalRandom.
	 * 
	 * min必须大于0.
	 * 
	 * JDK本身不具有控制两端范围的nextLong，因此参考Commons Lang RandomUtils的实现, 不直接复用是因为要传入Random实例
	 *
	 * @see org.apache.commons.lang3.RandomUtils#nextLong(long, long)
	 */
	public static long nextLong(Random random, long min, long max) {
		Validate.isTrue(max >= min, "Start value must be smaller or equal to end value.");
		MoreValidate.nonNegative("min", min);

		if (min == max) {
			return min;
		}

		return (long) (min + ((max - min) * random.nextDouble()));
	}

	///////// Double //////
	/**
	 * 返回0-之间的double, 使用ThreadLocalRandom
	 */
	public static double nextDouble() {
		return nextDouble(ThreadLocalRandom.current(), 0, Double.MAX_VALUE);
	}

	/**
	 * 返回0-Double.MAX之间的double
	 */
	public static double nextDouble(Random random) {
		return nextDouble(random, 0, Double.MAX_VALUE);
	}

	/**
	 * 返回0-max之间的double, 使用ThreadLocalRandom
	 * 
	 * 注意：与JDK默认返回0-1的行为不一致.
	 */
	public static double nextDouble(double max) {
		return nextDouble(ThreadLocalRandom.current(), 0, max);
	}

	/**
	 * 返回0-max之间的double
	 */
	public static double nextDouble(Random random, double max) {
		return nextDouble(random, 0, max);
	}

	/**
	 * 返回min-max之间的double,ThreadLocalRandom
	 */
	public static double nextDouble(final double min, final double max) {
		return nextDouble(ThreadLocalRandom.current(), min, max);
	}

	/**
	 * 返回min-max之间的double
	 */
	public static double nextDouble(Random random, final double min, final double max) {
		Validate.isTrue(max >= min, "Start value must be smaller or equal to end value.");
		MoreValidate.nonNegative("min", min);

		if (Double.compare(min, max) == 0) {
			return min;
		}

		return min + ((max - min) * random.nextDouble());
	}
	//////////////////// String/////////

	/**
	 * 随机字母或数字，固定长度
	 */
	public static String randomStringFixLength(int length) {
		return RandomStringUtils.random(length, 0, 0, true, true, null, threadLocalRandom());
	}

	/**
	 * 随机字母或数字，固定长度
	 */
	public static String randomStringFixLength(Random random, int length) {
		return RandomStringUtils.random(length, 0, 0, true, true, null, random);
	}

	/**
	 * 随机字母或数字，随机长度
	 */
	public static String randomStringRandomLength(int minLength, int maxLength) {
		return RandomStringUtils.random(nextInt(minLength, maxLength), 0, 0, true, true, null, threadLocalRandom());
	}

	/**
	 * 随机字母或数字，随机长度
	 */
	public static String randomStringRandomLength(Random random, int minLength, int maxLength) {
		return RandomStringUtils.random(nextInt(random, minLength, maxLength), 0, 0, true, true, null, random);
	}

	/**
	 * 随机字母，固定长度
	 */
	public static String randomLetterFixLength(int length) {
		return RandomStringUtils.random(length, 0, 0, true, false, null, threadLocalRandom());
	}

	/**
	 * 随机字母，固定长度
	 */
	public static String randomLetterFixLength(Random random, int length) {
		return RandomStringUtils.random(length, 0, 0, true, false, null, random);
	}

	/**
	 * 随机字母，随机长度
	 */
	public static String randomLetterRandomLength(int minLength, int maxLength) {
		return RandomStringUtils.random(nextInt(minLength, maxLength), 0, 0, true, false, null, threadLocalRandom());
	}

	/**
	 * 随机字母，随机长度
	 */
	public static String randomLetterRandomLength(Random random, int minLength, int maxLength) {
		return RandomStringUtils.random(nextInt(random, minLength, maxLength), 0, 0, true, false, null, random);
	}

	/**
	 * 随机ASCII字符(含字母，数字及其他符号)，固定长度
	 */
	public static String randomAsciiFixLength(int length) {
		return RandomStringUtils.random(length, 32, 127, false, false, null, threadLocalRandom());
	}

	/**
	 * 随机ASCII字符(含字母，数字及其他符号)，固定长度
	 */
	public static String randomAsciiFixLength(Random random, int length) {
		return RandomStringUtils.random(length, 32, 127, false, false, null, random);
	}

	/**
	 * 随机ASCII字符(含字母，数字及其他符号)，随机长度
	 */
	public static String randomAsciiRandomLength(int minLength, int maxLength) {
		return RandomStringUtils.random(nextInt(minLength, maxLength), 32, 127, false, false, null,
				threadLocalRandom());
	}

	/**
	 * 随机ASCII字符(含字母，数字及其他符号)，随机长度
	 */
	public static String randomAsciiRandomLength(Random random, int minLength, int maxLength) {
		return RandomStringUtils.random(nextInt(random, minLength, maxLength), 32, 127, false, false, null, random);
	}
}
