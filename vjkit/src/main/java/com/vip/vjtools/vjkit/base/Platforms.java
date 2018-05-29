package com.vip.vjtools.vjkit.base;

import java.io.File;

import org.apache.commons.lang3.SystemUtils;

/**
 * 关于系统设定，平台信息的变量(via Common Lang SystemUtils)
 */
public class Platforms {

	// 文件路径分隔符
	public static final String FILE_PATH_SEPARATOR = File.separator;
	public static final char FILE_PATH_SEPARATOR_CHAR = File.separatorChar;
	public static final char WINDOWS_FILE_PATH_SEPARATOR_CHAR = '\\';
	public static final char LINUX_FILE_PATH_SEPARATOR_CHAR = '/';

	// ClassPath分隔符
	public static final String CLASS_PATH_SEPARATOR = File.pathSeparator;
	public static final char CLASS_PATH_SEPARATOR_CHAR = File.pathSeparatorChar;

	// 换行符
	public static final String LINE_SEPARATOR = System.lineSeparator();

	// 临时目录
	public static final String TMP_DIR = SystemUtils.JAVA_IO_TMPDIR;
	// 应用的工作目录
	public static final String WORKING_DIR = SystemUtils.USER_DIR;
	// 用户 HOME目录
	public static final String USER_HOME = SystemUtils.USER_HOME;
	// Java HOME目录
	public static final String JAVA_HOME = SystemUtils.JAVA_HOME;

	// Java版本
	public static final String JAVA_SPECIFICATION_VERSION = SystemUtils.JAVA_SPECIFICATION_VERSION; // e.g. 1.8
	public static final String JAVA_VERSION = SystemUtils.JAVA_VERSION; // e.g. 1.8.0_102
	public static final boolean IS_JAVA7 = SystemUtils.IS_JAVA_1_7;
	public static final boolean IS_JAVA8 = SystemUtils.IS_JAVA_1_8;
	public static final boolean IS_ATLEASET_JAVA7 = IS_JAVA7 || IS_JAVA8;
	public static final boolean IS_ATLEASET_JAVA8 = IS_JAVA8;

	// 操作系统类型及版本
	public static final String OS_NAME = SystemUtils.OS_NAME;
	public static final String OS_VERSION = SystemUtils.OS_VERSION;
	public static final String OS_ARCH = SystemUtils.OS_ARCH; // e.g. x86_64
	public static final boolean IS_LINUX = SystemUtils.IS_OS_LINUX;
	public static final boolean IS_UNIX = SystemUtils.IS_OS_UNIX;
	public static final boolean IS_WINDOWS = SystemUtils.IS_OS_WINDOWS;
}
