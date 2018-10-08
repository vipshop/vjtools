package com.vip.vjtools.vjkit.time;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.time.FastDateFormat;

/**
 * DateFormat.format()消耗较大，如果时间戳是递增的，而且同一单位内有多次format()，使用用本类减少重复调用.
 * 
 * copy from Log4j2 DatePatternConverter，进行了优化，根据输出格式是否毫秒级，决定缓存在秒级还是毫秒级.
 * 
 * 注意如果输出格式为毫秒级的话，根据QPS决定性价比
 * 
 * see https://github.com/apache/logging-log4j2/blob/master/log4j-core/src/main/java/org/apache/logging/log4j/core/pattern/DatePatternConverter.java#L272
 * 
 * @author calvin
 */
public class CachingDateFormatter {
	private FastDateFormat fastDateFormat;
	private AtomicReference<CachedTime> cachedTime;
	private boolean onSecond;// 根据时间格式，决定缓存在秒级还是毫秒级

	public CachingDateFormatter(String pattern) {
		this(FastDateFormat.getInstance(pattern));
	}

	public CachingDateFormatter(FastDateFormat fastDateFormat) {
		this.fastDateFormat = fastDateFormat;
		onSecond = fastDateFormat.getPattern().indexOf("SSS") == -1;

		long current = System.currentTimeMillis();
		this.cachedTime = new AtomicReference<CachedTime>(new CachedTime(current, fastDateFormat.format(current)));
	}

	public String format(final long timestampMillis) {
		CachedTime cached = cachedTime.get();

		long timestamp = onSecond ? timestampMillis / 1000 : timestampMillis;

		if (timestamp != cached.timestamp) {
			final CachedTime newCachedTime = new CachedTime(timestamp, fastDateFormat.format(timestampMillis));
			// 尝试放入cachedTime
			cachedTime.compareAndSet(cached, newCachedTime);
			// 与log4j2做法不同，无论是否放入成功，都使用自己的值
			cached = newCachedTime;
		}

		return cached.formatted;
	}

	static final class CachedTime {
		public long timestamp;
		public String formatted;

		public CachedTime(final long timestamp, String formatted) {
			this.timestamp = timestamp;
			this.formatted = formatted;
		}
	}
}
