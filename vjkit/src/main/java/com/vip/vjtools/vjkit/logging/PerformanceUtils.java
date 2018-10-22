package com.vip.vjtools.vjkit.logging;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

/**
 * 效率管理工具
 * @author william.liang
 *
 */
public class PerformanceUtils {
	private PerformanceUtils() {
	}

	private static ThreadLocal<Timer> localTimer = new ThreadLocal<>();
	private static ThreadLocal<Map<String, Timer>> localTimerMap = new ThreadLocal<>();

	/**
	 * 记录开始时间
	 */
	public static void start() {
		getTimer().start();
	}

	/**
	 * 记录结束时间
	 */
	public static void end() {
		getTimer().end();
	}

	/**
	 * 计算耗时
	 * @return
	 */
	public static long duration() {
		return getTimer().duration();
	}

	/**
	 * 清除ThreadLocal
	 */
	public static void remove() {
		localTimer.remove();
		localTimerMap.remove();
	}

	/**
	 * 记录开始时间
	 * @param key
	 */
	public static void start(String key) {
		getTimer(key).start();
	}

	/**
	 * 记录结束时间
	 * @param key
	 */
	public static void end(String key) {
		getTimer(key).end();
	}

	/**
	 * 计算耗时
	 * @param key
	 * @return
	 */
	public static long duration(String key) {
		return getTimer(key).duration();
	}

	/**
	 * 清除ThreadLocal
	 * @param key
	 */
	public static void remove(String key) {
		getTimerMap().remove(key);
	}

	/**
	 * 当处理时间超过预定的阈值时发出警告信息
	 * @param log
	 * @param threshold 阈值（单位：ms）
	 */
	public static void warn(Logger log, long threshold) {
		if (duration() > threshold) {
			log.warn("[Performance Warning] json= msg=任务处理时长超过设定的阈值，总时长为{}ms", duration());
		}
	}

	/**
	 * 当处理时间超过预定的阈值时发出警告信息
	 * @param log
	 * @param key
	 * @param threshold 阈值（单位：ms）
	 */
	public static void warn(Logger log, String key, long threshold) {
		if (duration(key) > threshold) {
			log.warn("[Performance Warning] json= msg=任务【{}】处理时长超过设定的阈值，总时长为{}ms", key, duration(key));
		}
	}

	/**
	 * 当处理时间超过预定的阈值时发出警告信息
	 * @param log
	 * @param threshold 阈值（单位：ms）
	 * @param json 需要记录的对象json
	 */
	public static void warn(Logger log, long threshold, String json) {
		if (duration() > threshold) {
			log.warn("[Performance Warning] json={} msg=任务处理时长超过设定的阈值，总时长为{}ms", json, duration());
		}
	}

	/**
	 * 当处理时间超过预定的阈值时发出警告信息
	 * @param log
	 * @param key
	 * @param threshold 阈值（单位：ms）
	 * @param json 需要记录的对象json
	 */
	public static void warn(Logger log, String key, long threshold, String json) {
		if (duration(key) > threshold) {
			log.warn("[Performance Warning] json={} msg=任务【{}】处理时长超过设定的阈值，总时长为{}ms", json, key, duration(key));
		}
	}

	/**
	 * 记录结束时间并当处理时间超过预定的阈值时发出警告信息，最后清除
	 * @param log
	 * @param threshold 阈值（单位：ms）
	 */
	public static void endWithWarnAndRemove(Logger log, long threshold) {
		end();
		warn(log, threshold);
		remove();
	}

	/**
	 * 记录结束时间并当处理时间超过预定的阈值时发出警告信息，最后清除
	 * @param log
	 * @param key
	 * @param threshold 阈值（单位：ms）
	 */
	public static void endWithWarnAndRemove(Logger log, String key, long threshold) {
		end(key);
		warn(log, key, threshold);
		remove(key);
	}

	/**
	 * 记录结束时间并当处理时间超过预定的阈值时发出警告信息，最后清除
	 * @param log
	 * @param threshold 阈值（单位：ms）
	 * @param json 需要记录的对象json
	 */
	public static void endWithWarnAndRemove(Logger log, long threshold, String json) {
		end();
		warn(log, threshold, json);
		remove();
	}

	/**
	 * 记录结束时间并当处理时间超过预定的阈值时发出警告信息，最后清除
	 * @param log
	 * @param key
	 * @param threshold 阈值（单位：ms）
	 * @param json 需要记录的对象json
	 */
	public static void endWithWarnAndRemove(Logger log, String key, long threshold, String json) {
		end(key);
		warn(log, key, threshold, json);
		remove(key);
	}

	private static Timer getTimer(String key) {
		Timer timer = getTimerMap().getOrDefault(key, new Timer());
		getTimerMap().putIfAbsent(key, timer);
		return timer;
	}

	private static Timer getTimer() {
		Timer timer = localTimer.get();
		if (timer == null) {
			timer = new Timer();
			localTimer.set(timer);
		}
		return timer;
	}

	private static Map<String, Timer> getTimerMap() {
		Map<String, Timer> timerMap = localTimerMap.get();
		if (timerMap == null) {
			timerMap = new HashMap<>(16);
			localTimerMap.set(timerMap);
		}
		return timerMap;
	}

	static class Timer {
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
