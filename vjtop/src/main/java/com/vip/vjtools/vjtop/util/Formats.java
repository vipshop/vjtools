package com.vip.vjtools.vjtop.util;

import java.util.List;
import java.util.Locale;

import com.vip.vjtools.vjtop.VMInfo.Usage;
import com.vip.vjtools.vjtop.WarningRule.DoubleWarning;
import com.vip.vjtools.vjtop.WarningRule.LongWarning;

public class Formats {

	private static final long BYTE_SIZE = 1;
	private static final long KB_SIZE = BYTE_SIZE * 1024;
	public static final long MB_SIZE = KB_SIZE * 1024;
	private static final long GB_SIZE = MB_SIZE * 1024;
	private static final long TB_SIZE = GB_SIZE * 1024;

	private static String[] RED_ANSI = new String[] { "\033[31m\033[01m", "\033[0m" };
	private static String[] YELLOW_ANSI = new String[] { "\033[33m\033[01m", "\033[0m" };
	private static final String[] NORMAL_ANSI = new String[] { "", "" };
	private static String CLEAR_TERMINAL_ANSI_CMD = new String(
			new byte[] { (byte) 0x1b, (byte) 0x5b, (byte) 0x32, (byte) 0x4a, (byte) 0x1b, (byte) 0x5b, (byte) 0x48 });

	public static boolean isWindows = System.getProperty("os.name").toLowerCase(Locale.US).contains("windows");
	{
		if (isWindows) {
			disableAnsi();
			CLEAR_TERMINAL_ANSI_CMD = "%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n";
		}
	}

	public static void disableAnsi() {
		RED_ANSI = NORMAL_ANSI;
		YELLOW_ANSI = NORMAL_ANSI;
	}

	public static void setCleanClearTerminal() {
		CLEAR_TERMINAL_ANSI_CMD = "%n%n";
	}

	public static void setTextClearTerminal() {
		CLEAR_TERMINAL_ANSI_CMD = "%n";
	}

	public static String toMBWithColor(long bytes, LongWarning warning) {
		String[] ansi = colorAnsi(bytes, warning);
		return ansi[0] + toMB(bytes) + ansi[1];
	}

	public static String toColor(long value, LongWarning warning) {
		String[] ansi = colorAnsi(value, warning);
		return ansi[0] + value + ansi[1];
	}

	public static String red(String value) {
		return RED_ANSI[0] + value + RED_ANSI[1];
	}

	public static String yellow(String value) {
		return YELLOW_ANSI[0] + value + YELLOW_ANSI[1];
	}

	/**
	 * Formats a long value containing "number of bytes" to its megabyte representation. If the value is negative, "n/a"
	 * will be returned.
	 */
	public static String toMB(long bytes) {
		if (bytes < 0) {
			return "NaN";
		}
		long mb = bytes / MB_SIZE;

		if (mb < 9999) {
			return mb + "m";
		} else {
			return toSizeUnit(bytes).trim();
		}
	}

	public static String toSizeUnitWithColor(Long size, LongWarning warning) {
		String[] ansi = colorAnsi(size, warning);
		return ansi[0] + toSizeUnit(size) + ansi[1];
	}

	public static String toSizeUnit(Long size) {
		if (size == null) {
			return "NaN";
		}
		if (size < KB_SIZE) {
			return size.toString();
		}

		if (size < MB_SIZE) {
			return (size / KB_SIZE) + "k";
		}

		if (size < GB_SIZE) {
			return (size / MB_SIZE) + "m";
		}

		if (size < TB_SIZE) {
			return (size / GB_SIZE) + "g";
		}

		return (size / TB_SIZE) + "t";
	}

	public static String toFixLengthSizeUnit(Long size) {
		if (size == null) {
			return "NaN";
		}
		if (size < KB_SIZE) {
			return String.format("%4d", size);
		}

		if (size < MB_SIZE) {
			return String.format("%4dk", size / KB_SIZE);
		}

		if (size < GB_SIZE) {
			return String.format("%4dm", size / MB_SIZE);
		}

		if (size < TB_SIZE) {
			return String.format("%4dg", size / GB_SIZE);
		}

		return String.format("%4dt", size / TB_SIZE);
	}

	public static String toTimeUnit(long millis) {
		long seconds = millis / 1000;
		if (seconds < 60) {
			return String.format("%02ds", seconds);
		}

		if (seconds < 3600) {
			return String.format("%02dm%02ds", seconds / 60, seconds % 60);
		}

		if (seconds < (24 * 3600)) {
			return String.format("%02dh%02dm", seconds / 3600, (seconds / 60) % 60);
		}

		return String.format("%dd%02dh", seconds / (3600 * 24), (seconds / 3600) % 24);
	}


	public static String formatUsage(Usage usage) {
		if (usage.committed == usage.max) {
			return String.format("%s/%s", toMB(usage.used), toMB(usage.max));
		} else {
			return String.format("%s/%s/%s", toMB(usage.used), toMB(usage.committed), toMB(usage.max));
		}
	}

	public static String formatUsageWithColor(Usage usage, LongWarning warning) {
		String[] ansi = colorAnsi(usage.used, warning);
		return ansi[0] + formatUsage(usage) + ansi[1];
	}

	public static String[] colorAnsi(long value, LongWarning warning) {
		if (value < warning.yellow) {
			return NORMAL_ANSI;
		} else if (value >= warning.red) {
			return RED_ANSI;
		} else {
			return YELLOW_ANSI;
		}
	}

	public static String[] colorAnsi(double value, DoubleWarning warning) {
		if (value < warning.yellow) {
			return NORMAL_ANSI;
		} else if (value >= warning.red) {
			return RED_ANSI;
		} else {
			return YELLOW_ANSI;
		}
	}

	/**
	 * Returns a substring of the given string, representing the 'length' most-right characters
	 */
	public static String rightStr(String str, int length) {
		return str.substring(Math.max(0, str.length() - length));
	}

	/**
	 * Returns a substring of the given string, representing the 'length' most-left characters
	 */
	public static String leftStr(String str, int length) {
		return str.substring(0, Math.min(str.length(), length));
	}

	/**
	 * shortName("123456789", 8, 3) = "12...789"
	 * 
	 * shortName("123456789", 8, 2) = "123...89"
	 */
	public static String shortName(String str, int length, int rightLength) {
		if (str.length() > length) {
			int leftIndex = length - 3 - rightLength;
			str = str.substring(0, Math.max(0, leftIndex)) + "..."
					+ str.substring(Math.max(0, str.length() - rightLength), str.length());
		}
		return str;
	}

	/**
	 * Joins the given list of strings using the given delimiter delim
	 */
	public static String join(List<String> list, String delim) {

		StringBuilder sb = new StringBuilder();

		String loopDelim = "";

		for (String s : list) {

			sb.append(loopDelim);
			sb.append(s);

			loopDelim = delim;
		}

		return sb.toString();
	}

	public static long parseFromSize(String str) {
		if (str == null || str.isEmpty()) {
			return -1;
		}

		str = str.toLowerCase();
		long fromScale = BYTE_SIZE;

		try {
			if (str.endsWith("kb")) {
				str = str.substring(0, str.length() - 2).trim();
				fromScale = KB_SIZE;
			}
			if (str.endsWith("k")) {
				str = str.substring(0, str.length() - 1).trim();
				fromScale = KB_SIZE;
			} else if (str.endsWith("mb")) {
				str = str.substring(0, str.length() - 2).trim();
				fromScale = MB_SIZE;
			} else if (str.endsWith("m")) {
				str = str.substring(0, str.length() - 1).trim();
				fromScale = MB_SIZE;
			} else if (str.endsWith("gb")) {
				str = str.substring(0, str.length() - 2).trim();
				fromScale = GB_SIZE;
			} else if (str.endsWith("g")) {
				str = str.substring(0, str.length() - 1).trim();
				fromScale = GB_SIZE;
			} else if (str.endsWith("tb")) {
				str = str.substring(0, str.length() - 2).trim();
				fromScale = TB_SIZE;
			} else if (str.endsWith("t")) {
				str = str.substring(0, str.length() - 1).trim();
				fromScale = TB_SIZE;
			} else if (str.endsWith("bytes")) {
				str = str.substring(0, str.length() - "bytes".length()).trim();
				fromScale = BYTE_SIZE;
			}

			str = str.replace(",", "");

			long value = (long) Double.parseDouble(str);
			return value * fromScale;
		} catch (Throwable ex) {
			return -1;
		}
	}

	public static void clearTerminal() {
		System.out.printf(CLEAR_TERMINAL_ANSI_CMD);
	}
}
