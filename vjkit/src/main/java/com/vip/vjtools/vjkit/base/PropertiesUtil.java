package com.vip.vjtools.vjkit.base;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.vjtools.vjkit.io.URLResourceUtil;
import com.vip.vjtools.vjkit.number.NumberUtil;
import com.vip.vjtools.vjkit.text.Charsets;

/**
 * 关于Properties的工具类
 * 
 * 1. 统一风格读取Properties到各种数据类型
 * 
 * 2. 从文件或字符串装载Properties
 */
public class PropertiesUtil {

	private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

	/////////////////// 读取Properties ////////////////////

	public static Boolean getBoolean(Properties p, String name, Boolean defaultValue) {
		return BooleanUtil.toBooleanObject(p.getProperty(name), defaultValue);
	}

	public static Integer getInt(Properties p, String name, Integer defaultValue) {
		return NumberUtil.toIntObject(p.getProperty(name), defaultValue);
	}

	public static Long getLong(Properties p, String name, Long defaultValue) {
		return NumberUtil.toLongObject(p.getProperty(name), defaultValue);
	}

	public static Double getDouble(Properties p, String name, Double defaultValue) {
		return NumberUtil.toDoubleObject(p.getProperty(name), defaultValue);
	}

	public static String getString(Properties p, String name, String defaultValue) {
		return p.getProperty(name, defaultValue);
	}

	/////////// 加载Properties////////
	/**
	 * 从文件路径加载properties. 默认使用utf-8编码解析文件
	 * 
	 * 路径支持从外部文件或resources文件加载, "file://"或无前缀代表外部文件, "classpath:"代表resources
	 */
	public static Properties loadFromFile(String generalPath) {
		Properties p = new Properties();
		try (Reader reader = new InputStreamReader(URLResourceUtil.asStream(generalPath), Charsets.UTF_8)) {
			p.load(reader);
		} catch (IOException e) {
			logger.warn("Load property from " + generalPath + " failed", e);
		}
		return p;
	}

	/**
	 * 从字符串内容加载Properties
	 */
	public static Properties loadFromString(String content) {
		Properties p = new Properties();
		try (Reader reader = new StringReader(content)) {
			p.load(reader);
		} catch (IOException ignored) { // NOSONAR
		}
		return p;
	}

}
