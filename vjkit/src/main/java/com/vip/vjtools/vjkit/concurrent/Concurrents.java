package com.vip.vjtools.vjkit.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.RateLimiter;
import com.vip.vjtools.vjkit.concurrent.jsr166e.LongAdder;
import com.vip.vjtools.vjkit.concurrent.limiter.RateLimiterUtil;
import com.vip.vjtools.vjkit.concurrent.limiter.Sampler;
import com.vip.vjtools.vjkit.concurrent.limiter.TimeIntervalLimiter;

/**
 * 并发常用工具类
 */
public class Concurrents {

	/**
	 * 返回没有激烈CAS冲突的LongAdder, 并发的＋1将在不同的Counter里进行，只在取值时将多个Counter求和.
	 * 
	 * 为了保持JDK版本兼容性，统一采用移植版
	 */
	public static LongAdder longAdder() {
		return new LongAdder();
	}

	/**
	 * 返回CountDownLatch, 每条线程减1，减到0时正在latch.wait()的进程继续进行
	 */
	public static CountDownLatch countDownLatch(int count) {
		return new CountDownLatch(count);
	}

	/**
	 * 返回CyclicBarrier，每条线程减1并等待，减到0时，所有线程继续运行
	 */
	public static CyclicBarrier cyclicBarrier(int count) {
		return new CyclicBarrier(count);
	}

	/**
	 * 返回默认的非公平信号量，先请求的线程不一定先拿到信号量
	 */
	public static Semaphore nonFairSemaphore(int permits) {
		return new Semaphore(permits);
	}

	/**
	 * 返回公平的信号量，先请求的线程先拿到信号量
	 */
	public static Semaphore fairSemaphore(int permits) {
		return new Semaphore(permits, true);
	}

	/////////// 限流采样 //////
	/**
	 * 返回令牌桶算法的RateLimiter默认版，默认令牌桶大小等于期望的QPS，且刚启动时桶为空。
	 * 
	 * @permitsPerSecond 每秒允许的请求数，可看成QPS，同时将QPS平滑到毫秒级别上，请求到达速度不平滑时依赖缓冲能力.
	 */
	public static RateLimiter rateLimiter(int permitsPerSecond) {
		return RateLimiter.create(permitsPerSecond);
	}

	/**
	 * 返回令牌桶算法的RateLimiter定制版，可定制令牌桶的大小，且刚启动时桶已装满。
	 * 
	 * @param permitsPerSecond 每秒允许的请求数，可看成QPS，同时将QPS平滑到毫秒级别上，请求到达速度不平滑时依赖缓冲能力.
	 * @param maxBurstSeconds 可看成桶的容量，Guava中最大的突发流量缓冲时间，默认是1s, permitsPerSecond * maxBurstSeconds，就是闲时能累积的缓冲token最大数量。
	 */
	public static RateLimiter rateLimiter(int permitsPerSecond, int maxBurstSeconds)
			throws ReflectiveOperationException {
		return RateLimiterUtil.create(permitsPerSecond, maxBurstSeconds);
	}

	/**
	 * 返回采样器.
	 * 
	 * @param selectPercent 采样率，在0-100 之间，可以有小数位
	 */
	public static Sampler sampler(double selectPercent) {
		return Sampler.create(selectPercent);
	}

	/**
	 * 返回时间间隔限制器.
	 * @param interval 间隔时间
	 * @param timeUnit 间隔时间单位
	 */
	public static TimeIntervalLimiter timeIntervalLimiter(long interval, TimeUnit timeUnit) {
		return new TimeIntervalLimiter(interval, timeUnit);
	}
}
