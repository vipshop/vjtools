package com.vip.vjtools.vjkit.text;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 
 * 尽量使用Charsets.UTF8而不是"UTF-8"，减少JDK里的Charset查找消耗.
 * 
 * 使用JDK7的StandardCharsets，同时留了标准名称的字符串
 * 
 * @author calvin
 */
public class Charsets {

	public static final Charset UTF_8 = StandardCharsets.UTF_8;
	public static final Charset US_ASCII = StandardCharsets.US_ASCII;
	public static final Charset ISO_8859_1 = StandardCharsets.ISO_8859_1;

	public static final String UTF_8_NAME = StandardCharsets.UTF_8.name();
	public static final String ASCII_NAME = StandardCharsets.US_ASCII.name();
	public static final String ISO_8859_1_NAME = StandardCharsets.ISO_8859_1.name();

}
