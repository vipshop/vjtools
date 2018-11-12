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
	 * 记录结束时间
	 */
	public static void end() {
		localTimer.get().end();
	}

	/**
	 * 计算耗时
	 */
	public static long duration() {
		return localTimer.get().duration();
	}

	/**
	 * 清除ThreadLocal Timer
	 */
	public static void remove() {
		localTimer.remove();
	}

	/**
	 * 记录特定Timer的开始时间
	 */
	public static void start(String key) {
		getTimer(key).start();
	}

	/**
	 * 记录特定Timer结束时间
	 */
	public static void end(String key) {
		getTimer(key).end();
	}

	/**
	 * 计算特定Timer耗时
	 */
	public static long duration(String key) {
		return getTimer(key).duration();
	}

	/**
	 * 清除特定ThreadLocal Timer
	 */
	public static void remove(String key) {
		localTimerMap.get().remove(key);
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
	 * @param logger 写日志的logger
	 * @param threshold 阈值（单位：ms）
	 */
	public static void warn(Logger logger, long threshold) {
		long duration = duration();
		if (duration > threshold) {
			logger.warn("[Performance Warning] use {}ms， slow than {}ms", duration, threshold);
		}
	}

	/**
	 * 当处理时间超过预定的阈值时发出警告信息
	 * @param logger
	 * @param key
	 * @param threshold 阈值（单位：ms）
	 */
	public static void warn(Logger logger, String key, long threshold) {
		long duration = duration(key);
		if (duration > threshold) {
			logger.warn("[Performance Warning] task {} use {}ms， slow than {}ms", key, duration, threshold);
		}
	}

	/**
	 * 当处理时间超过预定的阈值时发出警告信息
	 * 
	 * @param logger 写日志的logger
	 * @param threshold 阈值（单位：ms）
	 * @param context 需要记录的context信息，如请求的json等
	 */
	public static void warn(Logger logger, long threshold, String context) {
		long duration = duration();
		if (duration > threshold) {
			logger.warn("[Performance Warning] use {}ms， slow than {}ms, context={}", duration, threshold, context);
		}
	}

	/**
	 * 当处理时间超过预定的阈值时发出警告信息
	 * @param logger
	 * @param key
	 * @param threshold 阈值（单位：ms）
	 * @param context 需要记录的context信息，如请求的json等
	 */
	public static void warn(Logger logger, String key, long threshold, String context) {
		long duration = duration(key);
		if (duration > threshold) {
			logger.warn("[Performance Warning] task {} use {}ms， slow than {}ms, contxt={}", key, duration, threshold,
					context);
		}
	}


	/**
	 * 记录结束时间并当处理时间超过预定的阈值时发出警告信息，最后清除
	 * @param logger
	 * @param threshold 阈值（单位：ms）
	 */
	public static void endWithWarnAndRemove(Logger logger, long threshold) {
		end();
		warn(logger, threshold);
		remove();
	}

	/**
	 * 记录结束时间并当处理时间超过预定的阈值时发出警告信息，最后清除
	 * @param log
	 * @param key
	 * @param threshold 阈值（单位：ms）
	 */
	public static void endWithWarnAndRemove(Logger logger, String key, long threshold) {
		end(key);
		warn(logger, key, threshold);
		remove(key);
	}

	/**
	 * 记录结束时间并当处理时间超过预定的阈值时发出警告信息，最后清除
	 * @param logger
	 * @param threshold 阈值（单位：ms）
	 * @param context 需要记录的context信息，如请求的json等
	 */
	public static void endWithWarnAndRemove(Logger logger, long threshold, String context) {
		end();
		warn(logger, threshold, context);
		remove();
	}

	/**
	 * 记录结束时间并当处理时间超过预定的阈值时发出警告信息，最后清除
	 * @param log
	 * @param key
	 * @param threshold 阈值（单位：ms）
	*  @param context 需要记录的context信息，如请求的json等
	 */
	public static void endWithWarnAndRemove(Logger logger, String key, long threshold, String context) {
		end(key);
		warn(logger, key, threshold, context);
		remove(key);
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


	private static class Timer {
		private long start;
		private long end;

		public void start() {
			start = System.currentTimeMillis();
		}

		public void end() {
			end = System.currentTimeMillis();
		}

		public long duration() {
			return end - start;
		}
	}
}
