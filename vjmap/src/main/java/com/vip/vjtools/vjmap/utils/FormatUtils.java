package com.vip.vjtools.vjmap.utils;

public class FormatUtils {

	private static final long BYTE_UNIT_KILO = 1024;
	private static final long BYTE_UNIT_MEGA = BYTE_UNIT_KILO * 1024;
	private static final long BYTE_UNIT_GIGA = BYTE_UNIT_MEGA * 1024;
	private static final long BYTE_UNIT_TERA = BYTE_UNIT_GIGA * 1024;

	/**
	 * 转换成带单位的字符串，转换时保留一位小数
	 */
	public static String toFloatUnit(long size) {
		if (size < BYTE_UNIT_KILO) {
			return String.format("%5d", size);
		}

		if (size < BYTE_UNIT_MEGA) {
			return String.format("%5.1fk", size / (1d * BYTE_UNIT_KILO));
		}

		if (size < BYTE_UNIT_GIGA) {
			return String.format("%5.1fm", size / (1d * BYTE_UNIT_MEGA));
		}

		if (size < BYTE_UNIT_TERA) {
			return String.format("%5.1fg", size / (1d * BYTE_UNIT_GIGA));
		}

		return String.format("%5.1ft", size / (1d * BYTE_UNIT_TERA));
	}
}