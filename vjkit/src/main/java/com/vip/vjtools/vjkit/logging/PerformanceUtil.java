package com.vip.vjtools.vjkit.logging;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

/**
 * 性能日志工具
 */
public class PerformanceUtil {
	private PerformanceUtil() {
	}

	// 全局共享ThreadLocal Timer
	private static ThreadLocal<Timer> localTimer = new ThreadLocal<Timer>() {
		@Override
		protected Timer initialValue() {
			return new Timer();
		}
	};

	// 按Key定义多个ThreadLocal Timer
	private static ThreadLocal<Map<String, Timer>> localTimerMap = new ThreadLocal<Map<String, Timer>>() {
		@Override
		protected Map<String, Timer> initialValue() {
			return new HashMap<String, Timer>();
		}
	};

	/**
	 * 记录开始时间
	 */
	public static void start() {
		localTimer.get().start();
	}

	/**
	 * 返回开始到现在的时间
	 */
	public static long duration() {
		return localTimer.get().duration();
	}

	/**
	 * 记录结束时间
	 */
	public static long end() {
		long duration = localTimer.get().duration();
		localTimer.remove();
		return duration;
	}

	/**
	 * 记录特定Timer的开始时间
	 */
	public static void start(String key) {
		getTimer(key).start();
	}

	/**
	 * 记录特定Timer的开始时间
	 */
	public static long duration(String key) {
		return getTimer(key).duration();
	}

	/**
	 * 记录特定Timer结束时间，返回耗时
	 */
	public static long end(String key) {
		long duration = getTimer(key).duration();
		localTimerMap.get().remove(key);
		return duration;
	}

	/**
	 * 清除所有ThreadLocal Timer
	 */
	public static void removeAll() {
		localTimer.remove();
		localTimerMap.remove();
	}


	/**
	 * 当处理时间超过预定的阈值时发出警告信息
	 * @param logger
	 * @param key
	 * @param threshold 阈值（单位：ms）
	 */
	public static void slowLog(Logger logger, long duration, long threshold) {
		if (duration > threshold) {
			logger.warn("[Performance Warning]  use {}ms, slow than {}ms", duration, threshold);
		}
	}

	/**
	 * 当处理时间超过预定的阈值时发出警告信息
	 * @param logger
	 * @param key
	 * @param threshold 阈值（单位：ms）
	 */
	public static void slowLog(Logger logger, String key, long duration, long threshold) {
		if (duration > threshold) {
			logger.warn("[Performance Warning] task {} use {}ms, slow than {}ms", key, duration, threshold);
		}
	}

	/**
	 * 当处理时间超过预定的阈值时发出警告信息
	 * 
	 * @param logger 写日志的logger
	 * @param threshold 阈值（单位：ms）
	 * @param context 需要记录的context信息，如请求的json等
	 */
	public static void slowLog(Logger logger, long duration, long threshold, String context) {
		if (duration > threshold) {
			logger.warn("[Performance Warning] use {}ms, slow than {}ms, context={}", duration, threshold, context);
		}
	}

	/**
	 * 当处理时间超过预定的阈值时发出警告信息
	 * @param logger
	 * @param key
	 * @param threshold 阈值（单位：ms）
	 * @param context 需要记录的context信息，如请求的json等
	 */
	public static void slowLog(Logger logger, String key, long duration, long threshold, String context) {
		if (duration > threshold) {
			logger.warn("[Performance Warning] task {} use {}ms, slow than {}ms, contxt={}", key, duration, threshold,
					context);
		}
	}

	/**
	 * 记录结束时间并当处理时间超过预定的阈值时发出警告信息，最后清除
	 * @param logger
	 * @param threshold 阈值（单位：ms）
	 */
	public static void endWithSlowLog(Logger logger, long threshold) {
		slowLog(logger, end(), threshold);
	}

	/**
	 * 记录结束时间并当处理时间超过预定的阈值时发出警告信息，最后清除
	 * @param logger
	 * @param key
	 * @param threshold 阈值（单位：ms）
	 */
	public static void endWithSlowLog(Logger logger, String key, long threshold) {
		slowLog(logger, key, end(key), threshold);
	}

	/**
	 * 记录结束时间并当处理时间超过预定的阈值时发出警告信息，最后清除
	 * @param logger
	 * @param threshold 阈值（单位：ms）
	 * @param context 需要记录的context信息，如请求的json等
	 */
	public static void endWithSlowLog(Logger logger, long threshold, String context) {
		slowLog(logger, end(), threshold, context);
	}

	/**
	 * 记录结束时间并当处理时间超过预定的阈值时发出警告信息，最后清除
	 * @param log
	 * @param key
	 * @param threshold 阈值（单位：ms）
	*  @param context 需要记录的context信息，如请求的json等
	 */
	public static void endWithSlowLog(Logger logger, String key, long threshold, String context) {
		slowLog(logger, key, end(key), threshold, context);
	}

	private static Timer getTimer(String key) {
		Map<String, Timer> map = localTimerMap.get();
		Timer timer = map.get(key);
		if (timer == null) {
			timer = new Timer();
			map.put(key, timer);
		}
		return timer;
	}


	static class Timer {
		private long start;

		public void start() {
			start = System.currentTimeMillis();
		}

		public long duration() {
			return System.currentTimeMillis() - start;
		}
	}
}
