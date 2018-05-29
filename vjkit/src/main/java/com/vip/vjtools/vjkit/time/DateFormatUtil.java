package com.vip.vjtools.vjkit.time;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import com.vip.vjtools.vjkit.base.annotation.NotNull;

/**
 * Date的parse()与format(), 采用Apache Common Lang中线程安全, 性能更佳的FastDateFormat
 * 
 * 注意Common Lang版本，3.5版才使用StringBuilder，3.4及以前使用StringBuffer.
 * 
 * 1. 常用格式的FastDateFormat定义, 常用格式直接使用这些FastDateFormat
 * 
 * 2. 日期格式不固定时的String<->Date 转换函数.
 * 
 * 3. 打印时间间隔，如"01:10:10"，以及用户友好的版本，比如"刚刚"，"10分钟前"
 * 
 * @see FastDateFormat#parse(String)
 * @see FastDateFormat#format(java.util.Date)
 * @see FastDateFormat#format(long)
 */
public class DateFormatUtil {

	// 以T分隔日期和时间，并带时区信息，符合ISO8601规范
	public static final String PATTERN_ISO = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";
	public static final String PATTERN_ISO_ON_SECOND = "yyyy-MM-dd'T'HH:mm:ssZZ";
	public static final String PATTERN_ISO_ON_DATE = "yyyy-MM-dd";

	// 以空格分隔日期和时间，不带时区信息
	public static final String PATTERN_DEFAULT = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final String PATTERN_DEFAULT_ON_SECOND = "yyyy-MM-dd HH:mm:ss";

	// 使用工厂方法FastDateFormat.getInstance(), 从缓存中获取实例

	// 以T分隔日期和时间，并带时区信息，符合ISO8601规范
	public static final FastDateFormat ISO_FORMAT = FastDateFormat.getInstance(PATTERN_ISO);
	public static final FastDateFormat ISO_ON_SECOND_FORMAT = FastDateFormat.getInstance(PATTERN_ISO_ON_SECOND);
	public static final FastDateFormat ISO_ON_DATE_FORMAT = FastDateFormat.getInstance(PATTERN_ISO_ON_DATE);

	// 以空格分隔日期和时间，不带时区信息
	public static final FastDateFormat DEFAULT_FORMAT = FastDateFormat.getInstance(PATTERN_DEFAULT);
	public static final FastDateFormat DEFAULT_ON_SECOND_FORMAT = FastDateFormat.getInstance(PATTERN_DEFAULT_ON_SECOND);

	/**
	 * 分析日期字符串, 仅用于pattern不固定的情况.
	 * 
	 * 否则直接使用DateFormats中封装好的FastDateFormat.
	 * 
	 * FastDateFormat.getInstance()已经做了缓存，不会每次创建对象，但直接使用对象仍然能减少在缓存中的查找.
	 */
	public static Date parseDate(@NotNull String pattern, @NotNull String dateString) throws ParseException {
		return FastDateFormat.getInstance(pattern).parse(dateString);
	}

	/**
	 * 格式化日期, 仅用于pattern不固定的情况.
	 * 
	 * 否则直接使用本类中封装好的FastDateFormat.
	 * 
	 * FastDateFormat.getInstance()已经做了缓存，不会每次创建对象，但直接使用对象仍然能减少在缓存中的查找.
	 */
	public static String formatDate(@NotNull String pattern, @NotNull Date date) {
		return FastDateFormat.getInstance(pattern).format(date);
	}

	/**
	 * 格式化日期, 仅用于不固定pattern不固定的情况.
	 * 
	 * 否否则直接使用本类中封装好的FastDateFormat.
	 * 
	 * FastDateFormat.getInstance()已经做了缓存，不会每次创建对象，但直接使用对象仍然能减少在缓存中的查找.
	 */
	public static String formatDate(@NotNull String pattern, long date) {
		return FastDateFormat.getInstance(pattern).format(date);
	}

	/////// 格式化间隔时间/////////
	/**
	 * 按HH:mm:ss.SSS格式，格式化时间间隔.
	 * 
	 * endDate必须大于startDate，间隔可大于1天，
	 * 
	 * @see DurationFormatUtils
	 */
	public static String formatDuration(@NotNull Date startDate, @NotNull Date endDate) {
		return DurationFormatUtils.formatDurationHMS(endDate.getTime() - startDate.getTime());
	}

	/**
	 * 按HH:mm:ss.SSS格式，格式化时间间隔
	 * 
	 * 单位为毫秒，必须大于0，可大于1天
	 * 
	 * @see DurationFormatUtils
	 */
	public static String formatDuration(long durationMillis) {
		return DurationFormatUtils.formatDurationHMS(durationMillis);
	}

	/**
	 * 按HH:mm:ss格式，格式化时间间隔
	 * 
	 * endDate必须大于startDate，间隔可大于1天
	 * 
	 * @see DurationFormatUtils
	 */
	public static String formatDurationOnSecond(@NotNull Date startDate, @NotNull Date endDate) {
		return DurationFormatUtils.formatDuration(endDate.getTime() - startDate.getTime(), "HH:mm:ss");
	}

	/**
	 * 按HH:mm:ss格式，格式化时间间隔
	 * 
	 * 单位为毫秒，必须大于0，可大于1天
	 * 
	 * @see DurationFormatUtils
	 */
	public static String formatDurationOnSecond(long durationMillis) {
		return DurationFormatUtils.formatDuration(durationMillis, "HH:mm:ss");
	}

	//////// 打印用于页面显示的用户友好，与当前时间比的时间差
	/**
	 * 打印用户友好的，与当前时间相比的时间差，如刚刚，5分钟前，今天XXX，昨天XXX
	 * 
	 * copy from AndroidUtilCode
	 */
	public static String formatFriendlyTimeSpanByNow(@NotNull Date date) {
		return formatFriendlyTimeSpanByNow(date.getTime());
	}

	/**
	 * 打印用户友好的，与当前时间相比的时间差，如刚刚，5分钟前，今天XXX，昨天XXX
	 * 
	 * copy from AndroidUtilCode
	 */
	public static String formatFriendlyTimeSpanByNow(long timeStampMillis) {
		long now = ClockUtil.currentTimeMillis();
		long span = now - timeStampMillis;
		if (span < 0) {
			// 'c' 日期和时间，被格式化为 "%ta %tb %td %tT %tZ %tY"，例如 "Sun Jul 20 16:17:00 EDT 1969"。
			return String.format("%tc", timeStampMillis);
		}
		if (span < DateUtil.MILLIS_PER_SECOND) {
			return "刚刚";
		} else if (span < DateUtil.MILLIS_PER_MINUTE) {
			return String.format("%d秒前", span / DateUtil.MILLIS_PER_SECOND);
		} else if (span < DateUtil.MILLIS_PER_HOUR) {
			return String.format("%d分钟前", span / DateUtil.MILLIS_PER_MINUTE);
		}
		// 获取当天00:00
		long wee = DateUtil.beginOfDate(new Date(now)).getTime();
		if (timeStampMillis >= wee) {
			// 'R' 24 小时制的时间，被格式化为 "%tH:%tM"
			return String.format("今天%tR", timeStampMillis);
		} else if (timeStampMillis >= wee - DateUtil.MILLIS_PER_DAY) {
			return String.format("昨天%tR", timeStampMillis);
		} else {
			// 'F' ISO 8601 格式的完整日期，被格式化为 "%tY-%tm-%td"。
			return String.format("%tF", timeStampMillis);
		}
	}
}
