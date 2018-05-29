package com.vip.vjtools.vjmap.utils;

public class FormatUtils {

	public static String toFloatUnit(long size) {
		if (size < 1024) {
			return String.format("%5d", size);
		}

		if (size / 1024 < 1024) {
			return String.format("%5.1fk", size / 1024d);
		}

		if (size / (1024 * 1024) < 1024) {
			return String.format("%5.1fm", size / (1024d * 1024));
		}

		if (size / (1024 * 1024 * 1024) < 1024) {
			return String.format("%5.1fg", size / (1024d * 1024 * 1024));
		}

		return String.format("%5.1ft", size / (1024d * 1024 * 1024 * 1024));
	}
}
