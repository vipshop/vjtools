package com.vip.vjtools.vjkit.io;

import org.apache.commons.lang3.StringUtils;

import com.google.common.io.Files;
import com.vip.vjtools.vjkit.base.Platforms;
import com.vip.vjtools.vjkit.text.MoreStringUtil;

/**
 * 关于文件路径的工具集. 这个类只适合处理纯字符串的路径，如果是File对象或者Path对象的路径处理，建议直接使用Path类的方法。
 * 
 * @see {@link java.nio.file.Path}
 */
public class FilePathUtil {

	/**
	 * 在Windows环境里，兼容Windows上的路径分割符，将 '/' 转回 '\'
	 */
	public static String normalizePath(String path) {
		if (Platforms.FILE_PATH_SEPARATOR_CHAR == Platforms.WINDOWS_FILE_PATH_SEPARATOR_CHAR
				&& StringUtils.indexOf(path, Platforms.LINUX_FILE_PATH_SEPARATOR_CHAR) != -1) {
			return StringUtils.replaceChars(path, Platforms.LINUX_FILE_PATH_SEPARATOR_CHAR,
					Platforms.WINDOWS_FILE_PATH_SEPARATOR_CHAR);
		}
		return path;

	}

	/**
	 * 将路径整理，如 "a/../b"，整理成 "b"
	 */
	public static String simplifyPath(String path) {
		return Files.simplifyPath(path);
	}

	/**
	 * 以拼接路径名
	 */
	public static String concat(String baseName, String... appendName) {
		if (appendName.length == 0) {
			return baseName;
		}

		StringBuilder concatName = new StringBuilder();
		if (MoreStringUtil.endWith(baseName, Platforms.FILE_PATH_SEPARATOR_CHAR)) {
			concatName.append(baseName).append(appendName[0]);
		} else {
			concatName.append(baseName).append(Platforms.FILE_PATH_SEPARATOR_CHAR).append(appendName[0]);
		}

		if (appendName.length > 1) {
			for (int i = 1; i < appendName.length; i++) {
				concatName.append(Platforms.FILE_PATH_SEPARATOR_CHAR).append(appendName[i]);
			}
		}

		return concatName.toString();
	}

	/**
	 * 获得上层目录的路径
	 */
	public static String getParentPath(String path) {
		String parentPath = path;

		if (Platforms.FILE_PATH_SEPARATOR.equals(parentPath)) {
			return parentPath;
		}

		parentPath = MoreStringUtil.removeEnd(parentPath, Platforms.FILE_PATH_SEPARATOR_CHAR);

		int idx = parentPath.lastIndexOf(Platforms.FILE_PATH_SEPARATOR_CHAR);
		if (idx >= 0) {
			parentPath = parentPath.substring(0, idx + 1);
		} else {
			parentPath = Platforms.FILE_PATH_SEPARATOR;
		}

		return parentPath;
	}

	/**
	 * 获得参数clazz所在的Jar文件的绝对路径
	 */
	public static String getJarPath(Class<?> clazz) {
		return clazz.getProtectionDomain().getCodeSource().getLocation().getFile();
	}
}
