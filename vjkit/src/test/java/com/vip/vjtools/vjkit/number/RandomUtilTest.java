package com.vip.vjtools.vjkit.number;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class RandomUtilTest {

	@Test
	public void getRandom() {
		System.out.println(RandomUtil.secureRandom().nextInt());
		System.out.println(RandomUtil.threadLocalRandom().nextInt());
	}

	@Test
	public void randomNumber() {

		int i = RandomUtil.nextInt();
		assertThat(i).isBetween(0, Integer.MAX_VALUE);
		i = RandomUtil.nextInt(RandomUtil.threadLocalRandom());
		assertThat(i).isBetween(0, Integer.MAX_VALUE);

		i = RandomUtil.nextInt(10);
		assertThat(i).isBetween(0, 10);
		i = RandomUtil.nextInt(RandomUtil.threadLocalRandom(), 10);
		assertThat(i).isBetween(0, 10);

		i = RandomUtil.nextInt(10, 20);
		assertThat(i).isBetween(10, 20);
		i = RandomUtil.nextInt(RandomUtil.threadLocalRandom(), 10, 20);
		assertThat(i).isBetween(10, 20);

		long l = RandomUtil.nextLong();
		assertThat(l).isBetween(0L, Long.MAX_VALUE);
		l = RandomUtil.nextLong(RandomUtil.threadLocalRandom());
		assertThat(l).isBetween(0L, Long.MAX_VALUE);

		l = RandomUtil.nextLong(10);
		assertThat(l).isBetween(0L, 10L);
		l = RandomUtil.nextLong(RandomUtil.threadLocalRandom(), 10L);
		assertThat(l).isBetween(0L, 10L);

		l = RandomUtil.nextLong(10L, 20L);
		assertThat(l).isBetween(10L, 20L);
		l = RandomUtil.nextLong(RandomUtil.threadLocalRandom(), 10, 20);
		assertThat(l).isBetween(10L, 20L);

		double d = RandomUtil.nextDouble();
		assertThat(d).isBetween(0d, Double.MAX_VALUE);
		d = RandomUtil.nextDouble(RandomUtil.threadLocalRandom());
		assertThat(d).isBetween(0d, Double.MAX_VALUE);

		d = RandomUtil.nextDouble(10);
		assertThat(d).isBetween(0d, 10d);
		d = RandomUtil.nextDouble(RandomUtil.threadLocalRandom(), 10L);
		assertThat(d).isBetween(0d, 10d);

		d = RandomUtil.nextDouble(10L, 20L);
		assertThat(d).isBetween(10d, 20d);
		d = RandomUtil.nextDouble(RandomUtil.threadLocalRandom(), 10, 20);
		assertThat(d).isBetween(10d, 20d);

	}

	@Test
	public void generateString() {
		System.out.println(RandomUtil.randomStringFixLength(5));
		System.out.println(RandomUtil.randomStringRandomLength(5, 10));

		System.out.println(RandomUtil.randomStringFixLength(RandomUtil.threadLocalRandom(), 5));
		System.out.println(RandomUtil.randomStringRandomLength(RandomUtil.threadLocalRandom(), 5, 10));

		assertThat(RandomUtil.randomStringFixLength(5)).hasSize(5);
		assertThat(RandomUtil.randomStringFixLength(RandomUtil.threadLocalRandom(), 5)).hasSize(5);

		System.out.println(RandomUtil.randomLetterFixLength(5));
		System.out.println(RandomUtil.randomLetterRandomLength(5, 10));

		System.out.println(RandomUtil.randomLetterFixLength(RandomUtil.threadLocalRandom(), 5));
		System.out.println(RandomUtil.randomLetterRandomLength(RandomUtil.threadLocalRandom(), 5, 10));

		assertThat(RandomUtil.randomLetterFixLength(5)).hasSize(5);
		assertThat(RandomUtil.randomLetterFixLength(RandomUtil.threadLocalRandom(), 5)).hasSize(5);

		System.out.println(RandomUtil.randomAsciiFixLength(5));
		System.out.println(RandomUtil.randomAsciiRandomLength(5, 10));

		System.out.println(RandomUtil.randomAsciiFixLength(RandomUtil.threadLocalRandom(), 5));
		System.out.println(RandomUtil.randomAsciiRandomLength(RandomUtil.threadLocalRandom(), 5, 10));

		assertThat(RandomUtil.randomAsciiFixLength(5)).hasSize(5);
		assertThat(RandomUtil.randomAsciiFixLength(RandomUtil.threadLocalRandom(), 5)).hasSize(5);
	}

	public void test0() {
		double a = 0.0;
		double b = 1.0;
		int x = 0x7fffffff;
	}

}
