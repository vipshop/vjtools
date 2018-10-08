package com.vip.vjtools.vjkit.concurrent.limiter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.google.common.util.concurrent.RateLimiter;

public class RateLimiterUtil {

	/**
	 * 一个用来定制RateLimiter的方法，默认一开始就桶里就装满token。
	 * 
	 * @param permitsPerSecond 每秒允许的请求数，可看成QPS。
	 * @param maxBurstSeconds 可看成桶的容量，Guava中最大的突发流量缓冲时间，默认是1s, permitsPerSecond * maxBurstSeconds，就是闲置时累积的缓冲token最大值。
	 */
	public static RateLimiter create(double permitsPerSecond, double maxBurstSeconds)
			throws ReflectiveOperationException {
		return create(permitsPerSecond, maxBurstSeconds, true);
	}

	/**
	 * 一个用来定制RateLimiter的方法。
	 * 
	 * @param permitsPerSecond 每秒允许的请求书，可看成QPS
	 * @param maxBurstSeconds 最大的突发缓冲时间。用来应对突发流量。Guava的实现默认是1s。permitsPerSecond * maxBurstSeconds的数量，就是闲置时预留的缓冲token数量
	 * @param filledWithToken 是否需要创建时就保留有permitsPerSecond * maxBurstSeconds的token
	 */
	public static RateLimiter create(double permitsPerSecond, double maxBurstSeconds, boolean filledWithToken)
			throws ReflectiveOperationException {
		Class<?> sleepingStopwatchClass = Class
				.forName("com.google.common.util.concurrent.RateLimiter$SleepingStopwatch");
		Method createStopwatchMethod = sleepingStopwatchClass.getDeclaredMethod("createFromSystemTimer");
		createStopwatchMethod.setAccessible(true);
		Object stopwatch = createStopwatchMethod.invoke(null);

		Class<?> burstyRateLimiterClass = Class
				.forName("com.google.common.util.concurrent.SmoothRateLimiter$SmoothBursty");
		Constructor<?> burstyRateLimiterConstructor = burstyRateLimiterClass.getDeclaredConstructors()[0];
		burstyRateLimiterConstructor.setAccessible(true);

		// set maxBurstSeconds
		RateLimiter rateLimiter = (RateLimiter) burstyRateLimiterConstructor.newInstance(stopwatch, maxBurstSeconds);
		rateLimiter.setRate(permitsPerSecond);

		if (filledWithToken) {
			// set storedPermits
			setField(rateLimiter, "storedPermits", permitsPerSecond * maxBurstSeconds);
		}

		return rateLimiter;
	}

	private static boolean setField(Object targetObject, String fieldName, Object fieldValue) {
		Field field;
		try {
			field = targetObject.getClass().getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			field = null;
		}
		Class superClass = targetObject.getClass().getSuperclass();
		while (field == null && superClass != null) {
			try {
				field = superClass.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
				superClass = superClass.getSuperclass();
			}
		}
		if (field == null) {
			return false;
		}
		field.setAccessible(true);
		try {
			field.set(targetObject, fieldValue);
			return true;
		} catch (IllegalAccessException e) {
			return false;
		}
	}
}
