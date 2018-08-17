package com.vip.vjtools.vjtop.data;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// See http://man7.org/linux/man-pages/man5/proc.5.html for /proc file details
public class ProcFileData {

	private static final String PROC_SELF_STATUS_FILE_TPL = "/proc/%s/status";
	private static final String PROC_SELF_IO_FILE_TPL = "/proc/%s/io";

	private static final String VALUE_SEPARATOR = ":";

	public static Map<String, String> getProcStatus(String pid) {
		return getProcFileAsMap(String.format(PROC_SELF_STATUS_FILE_TPL, pid));
	}

	public static Map<String, String> getProcIO(String pid) {
		return getProcFileAsMap(String.format(PROC_SELF_IO_FILE_TPL, pid));
	}

	public static Map<String, String> getProcFileAsMap(String filePath) {
		try {
			File file = new File(filePath);
			if (!file.exists() || file.isDirectory() || !file.canRead()) {
				return Collections.emptyMap();
			}

			List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
			Map<String, String> result = new HashMap<>(lines.size() * 2);

			for (String line : lines) {
				int index = line.indexOf(VALUE_SEPARATOR);
				if (index <= 0 || index >= line.length() - 1) {
					continue;
				}
				String key = line.substring(0, index).trim();
				String value = line.substring(index + 1).trim();
				result.put(key, value);
			}
			return result;
		} catch (Throwable ex) {
			ex.printStackTrace();
			return Collections.emptyMap();
		}
	}
}
