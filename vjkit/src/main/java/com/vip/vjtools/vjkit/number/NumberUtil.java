package com.vip.vjtools.vjkit.number;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.vip.vjtools.vjkit.base.annotation.NotNull;
import com.vip.vjtools.vjkit.base.annotation.Nullable;

/**
 * 数字的工具类.
 * 
 * 1.原始类型数字与byte[]的双向转换(via Guava)
 * 
 * 2.判断字符串是否数字, 是否16进制字符串(via Common Lang)
 * 
 * 3.10机制/16进制字符串 与 原始类型数字/数字对象 的双向转换(参考Common Lang自写)
 */
public class NumberUtil {

	private static final double DEFAULT_DOUBLE_EPSILON = 0.00001d;

	/**
	 * 因为double的精度问题, 允许两个double在0.00001内的误差为相等。
	 */
	public static boolean equalsWithin(double d1, double d2) {
		return Math.abs(d1 - d2) < DEFAULT_DOUBLE_EPSILON;
	}

	/**
	 * 因为double的精度问题, 允许两个double在epsilon内的误差为相等
	 */
	public static boolean equalsWithin(double d1, double d2, double epsilon) {
		return Math.abs(d1 - d2) < epsilon;
	}

	///////////// bytes[] 与原始类型数字转换 ///////

	public static byte[] toBytes(int value) {
		return Ints.toByteArray(value);
	}

	public static byte[] toBytes(long value) {
		return Longs.toByteArray(value);
	}

	/**
	 * copy from ElasticSearch Numbers
	 */
	public static byte[] toBytes(double val) {
		return toBytes(Double.doubleToRawLongBits(val));
	}

	public static int toInt(byte[] bytes) {
		return Ints.fromByteArray(bytes);
	}

	public static long toLong(byte[] bytes) {
		return Longs.fromByteArray(bytes);
	}

	/**
	 * copy from ElasticSearch Numbers
	 */
	public static double toDouble(byte[] bytes) {
		return Double.longBitsToDouble(toLong(bytes));
	}

	/////// 判断字符串类型//////////
	/**
	 * 判断字符串是否合法数字
	 */
	public static boolean isNumber(@Nullable String str) {
		return NumberUtils.isCreatable(str);
	}

	/**
	 * 判断字符串是否16进制
	 */
	public static boolean isHexNumber(@Nullable String value) {
		if (StringUtils.isEmpty(value)) {
			return false;
		}

		int index = value.startsWith("-") ? 1 : 0;
		return value.startsWith("0x", index) || value.startsWith("0X", index) || value.startsWith("#", index);
	}

	/////////// 将字符串转化为原始类型数字/////////

	/**
	 * 将10进制的String转化为int.
	 * 
	 * 当str为空或非数字字符串时抛NumberFormatException
	 */
	public static int toInt(@NotNull String str) {
		return Integer.parseInt(str);
	}

	/**
	 * 将10进制的String安全的转化为int.
	 * 
	 * 当str为空或非数字字符串时，返回default值.
	 */
	public static int toInt(@Nullable String str, int defaultValue) {
		return NumberUtils.toInt(str, defaultValue);
	}

	/**
	 * 将10进制的String安全的转化为long.
	 * 
	 * 当str或非数字字符串时抛NumberFormatException
	 */
	public static long toLong(@NotNull String str) {
		return Long.parseLong(str);
	}

	/**
	 * 将10进制的String安全的转化为long.
	 * 
	 * 当str为空或非数字字符串时，返回default值
	 */
	public static long toLong(@Nullable String str, long defaultValue) {
		return NumberUtils.toLong(str, defaultValue);
	}

	/**
	 * 将10进制的String安全的转化为double.
	 * 
	 * 当str为空或非数字字符串时抛NumberFormatException
	 */
	public static double toDouble(@NotNull String str) {
		// 统一行为，不要有时候抛NPE，有时候抛NumberFormatException
		if (str == null) {
			throw new NumberFormatException("null");
		}
		return Double.parseDouble(str);
	}

	/**
	 * 将10进制的String安全的转化为double.
	 * 
	 * 当str为空或非数字字符串时，返回default值
	 */
	public static double toDouble(@Nullable String str, double defaultValue) {
		return NumberUtils.toDouble(str, defaultValue);
	}

	////////////// 10进制字符串 转换对象类型数字/////////////
	/**
	 * 将10进制的String安全的转化为Integer.
	 * 
	 * 当str为空或非数字字符串时抛NumberFormatException
	 */
	public static Integer toIntObject(@NotNull String str) {
		return Integer.valueOf(str);
	}

	/**
	 * 将10进制的String安全的转化为Integer.
	 * 
	 * 当str为空或非数字字符串时，返回default值
	 */
	public static Integer toIntObject(@Nullable String str, Integer defaultValue) {
		if (StringUtils.isEmpty(str)) {
			return defaultValue;
		}
		try {
			return Integer.valueOf(str);
		} catch (final NumberFormatException nfe) {
			return defaultValue;
		}
	}

	/**
	 * 将10进制的String安全的转化为Long.
	 * 
	 * 当str为空或非数字字符串时抛NumberFormatException
	 */
	public static Long toLongObject(@NotNull String str) {
		return Long.valueOf(str);
	}

	/**
	 * 将10进制的String安全的转化为Long.
	 * 
	 * 当str为空或非数字字符串时，返回default值
	 */
	public static Long toLongObject(@Nullable String str, Long defaultValue) {
		if (StringUtils.isEmpty(str)) {
			return defaultValue;
		}
		try {
			return Long.valueOf(str);
		} catch (final NumberFormatException nfe) {
			return defaultValue;
		}
	}

	/**
	 * 将10进制的String安全的转化为Double.
	 * 
	 * 当str为空或非数字字符串时抛NumberFormatException
	 */
	public static Double toDoubleObject(@NotNull String str) {
		// 统一行为，不要有时候抛NPE，有时候抛NumberFormatException
		if (str == null) {
			throw new NumberFormatException("null");
		}
		return Double.valueOf(str);
	}

	/**
	 * 将10进制的String安全的转化为Long.
	 * 
	 * 当str为空或非数字字符串时，返回default值
	 */
	public static Double toDoubleObject(@Nullable String str, Double defaultValue) {
		if (StringUtils.isEmpty(str)) {
			return defaultValue;
		}
		try {
			return Double.valueOf(str);
		} catch (final NumberFormatException nfe) {
			return defaultValue;
		}
	}

	//////////// 16进制 字符串转换为数字对象//////////

	/**
	 * 将16进制的String转化为Integer.
	 * 
	 * 当str为空或非数字字符串时抛NumberFormatException
	 */
	public static Integer hexToIntObject(@NotNull String str) {
		// 统一行为，不要有时候抛NPE，有时候抛NumberFormatException
		if (str == null) {
			throw new NumberFormatException("null");
		}
		return Integer.decode(str);
	}

	/**
	 * 将16进制的String转化为Integer，出错时返回默认值.
	 */
	public static Integer hexToIntObject(@Nullable String str, Integer defaultValue) {
		if (StringUtils.isEmpty(str)) {
			return defaultValue;
		}
		try {
			return Integer.decode(str);
		} catch (final NumberFormatException nfe) {
			return defaultValue;
		}
	}

	/**
	 * 将16进制的String转化为Long
	 * 
	 * 当str为空或非数字字符串时抛NumberFormatException
	 */
	public static Long hexToLongObject(@NotNull String str) {
		// 统一行为，不要有时候抛NPE，有时候抛NumberFormatException
		if (str == null) {
			throw new NumberFormatException("null");
		}
		return Long.decode(str);
	}

	/**
	 * 将16进制的String转化为Long，出错时返回默认值.
	 */
	public static Long hexToLongObject(@Nullable String str, Long defaultValue) {
		if (StringUtils.isEmpty(str)) {
			return defaultValue;
		}
		try {
			return Long.decode(str);
		} catch (final NumberFormatException nfe) {
			return defaultValue;
		}
	}

	/////// toString ///////
	// 定义了原子类型与对象类型的参数，保证不会用错函数会导致额外AutoBoxing转换//

	public static String toString(int i) {
		return Integer.toString(i);
	}

	public static String toString(@NotNull Integer i) {
		return i.toString();
	}

	public static String toString(long l) {
		return Long.toString(l);
	}

	public static String toString(@NotNull Long l) {
		return l.toString();
	}

	public static String toString(double d) {
		return Double.toString(d);
	}

	public static String toString(@NotNull Double d) {
		return d.toString();
	}

	/**
	 * 输出格式化为小数后两位的double字符串
	 */
	public static String to2DigitString(double d) {
		return String.format(Locale.ROOT, "%.2f", d);
	}

	/////////// 杂项 ///////

	/**
	 * 安全的将小于Integer.MAX的long转为int，否则抛出IllegalArgumentException异常
	 */
	public static int toInt32(long x) {
		if ((int) x == x) {
			return (int) x;
		}
		throw new IllegalArgumentException("Int " + x + " out of range");
	}

	/**
	 * 人民币金额单位转换，分转换成元，例如：100 => 1
	 * 取两位小数,四舍五入
	 * @param num
	 * @return
	 */
	public static BigDecimal fen2yuan(BigDecimal num) {
		return num.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
	}

	/**
	 * 人民币金额单位转换，分转换成元，例如：100 => 1
	 * 取两位小数,四舍五入
	 * @param num
	 * @return
	 */
	public static BigDecimal fen2yuan(long num) {
		return fen2yuan(new BigDecimal(num));
	}

	/**
	 * 人民币金额单位转换，分转换成元，例如：100 => 1
	 * 取两位小数,四舍五入
	 * @param num
	 * @return
	 */
	public static BigDecimal fen2yuan(String num) {
		if (StringUtils.isEmpty(num)) {
			num = "0";
		}
		return fen2yuan(new BigDecimal(num));
	}

	/**
	 * 人民币金额单位转换，元转换成分，例如：1 => 100
	 * @param y
	 * @return
	 */
	public static BigDecimal yuan2fen(String y) {
		return new BigDecimal(Math.round(new BigDecimal(y).multiply(new BigDecimal(100)).doubleValue()));
	}

	/**
	 * 人民币金额单位转换，元转换成分，例如：1 => 100
	 * @param y
	 * @return
	 */
	public static BigDecimal yuan2fen(double y) {
		return yuan2fen(String.valueOf(y));
	}

	/**
	 * 人民币金额单位转换，元转换成分，例如：1 => 100
	 * @param y
	 * @return
	 */
	public static BigDecimal yuan2fen(BigDecimal y) {
		if (y != null) {
			return yuan2fen(y.toString());
		} else {
			return new BigDecimal(0);
		}
	}

	/**
	 * 格式化金额，例如：1=>1.00
	 * 输出字符串
	 * @param number
	 * @return
	 */
	public static String formatNumber(BigDecimal number) {
		return formatNumber(number.doubleValue());
	}

	/**
	 * 格式化金额，例如：1=>1.00
	 * 输出字符串
	 * 默认格式：00.0
	 * @param number
	 * @return
	 */
	public static String formatNumber(double number) {
		DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
		df.applyPattern("0.00");
		return df.format(number);
	}

	/**
	 * 格式化金额，例如：1=>1.00
	 * 输出字符串
	 * 例如：pattern 等于 #,##0.00，输入： 33999999932.3333d 输出：33,999,999,932.33
	 * @param number 金额
	 * @param pattern 输出字符串金额格式 ，如：#,##0.00
	 * @return
	 */
	public static String formatNumber(BigDecimal number, String pattern) {
		return formatNumber(number.doubleValue(), pattern);
	}

	/**
	 * 格式化金额，例如：1=>1.00
	 * 输出字符串
	 * 例如：pattern 等于 #,##0.00，输入： 33999999932.3333d 输出：33,999,999,932.33
	 * @param number 金额
	 * @param pattern 输出字符串金额格式 ，如：#,##0.00
	 * @return
	 */
	public static String formatNumber(double number, String pattern) {
		DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
		if (StringUtils.isEmpty(pattern)) {
			pattern = "#,##0.00";
		}
		df.applyPattern(pattern);
		return df.format(number);
	}

	/**
	 * 格式化金额，例如：1=>1.00
	 * 输出字符串
	 * 例如：pattern 等于 #,##0.00，输入： 33999999932.3333d 输出：33,999,999,932.33
	 * @param numberStr 金额字符串，如：33,999,999,932.33
	 * @param pattern 输出字符串金额格式 ，如：#,##0.00
	 * @return
	 */
	public static BigDecimal parseString(String numberStr, String pattern) throws ParseException {
		DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
		if (StringUtils.isEmpty(pattern)) {
			pattern = "#,##0.00";
		}
		df.applyPattern(pattern);
		return new BigDecimal(df.parse(numberStr).doubleValue());
	}
}
