package com.vip.vjtools.vjkit.text;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Utf8;
import com.vip.vjtools.vjkit.base.annotation.Nullable;
import com.vip.vjtools.vjkit.collection.ListUtil;

/**
 * 尽量使用Common Lang StringUtils, 基本覆盖了所有类库的StringUtils
 * 
 * 本类仅补充少量额外方法, 尤其是针对char的运算
 * 
 * 1. split char/chars
 * 
 * 2. 针对char的replace first/last, startWith,endWith 等
 * 
 * @author calvin
 */
public class MoreStringUtil {

	/////////// split char 相关 ////////

	/**
	 * 高性能的Split，针对char的分隔符号，比JDK String自带的高效.
	 * 
	 * copy from Commons Lange 3.5 StringUtils 并做优化
	 * 
	 * @see #split(String, char, int)
	 */
	public static List<String> split(@Nullable final String str, final char separatorChar) {
		return split(str, separatorChar, 10);
	}

	/**
	 * 高性能的Split，针对char的分隔符号，比JDK String自带的高效.
	 * 
	 * copy from Commons Lange 3.5 StringUtils, 做如下优化:
	 * 
	 * 1. 最后不做数组转换，直接返回List.
	 * 
	 * 2. 可设定List初始大小.
	 * 
	 * 3. preserveAllTokens 取默认值false
	 * 
	 * @param expectParts 预估分割后的List大小，初始化数据更精准
	 * 
	 * @return 如果为null返回null, 如果为""返回空数组
	 */
	public static List<String> split(@Nullable final String str, final char separatorChar, int expectParts) {
		if (str == null) {
			return null;
		}

		final int len = str.length();
		if (len == 0) {
			return ListUtil.emptyList();
		}

		final List<String> list = new ArrayList<String>(expectParts);
		int i = 0;
		int start = 0;
		boolean match = false;
		while (i < len) {
			if (str.charAt(i) == separatorChar) {
				if (match) {
					list.add(str.substring(start, i));
					match = false;
				}
				start = ++i;
				continue;
			}
			match = true;
			i++;
		}
		if (match) {
			list.add(str.substring(start, i));
		}
		return list;
	}

	/**
	 * 使用多个可选的char作为分割符, 还可以设置omitEmptyStrings,trimResults等配置
	 * 
	 * 设置后的Splitter进行重用，不要每次创建
	 * 
	 * @param separatorChars 比如Unix/Windows的路径分割符 "/\\"
	 * 
	 * @see com.google.common.base.Splitter
	 */
	public static Splitter charsSplitter(final String separatorChars) {
		return Splitter.on(CharMatcher.anyOf(separatorChars));
	}

	////////// 其他 char 相关 ///////////
	/**
	 * String 有replace(char,char)，但缺少单独replace first/last的
	 */
	public static String replaceFirst(@Nullable String s, char sub, char with) {
		if (s == null) {
			return null;
		}
		int index = s.indexOf(sub);
		if (index == -1) {
			return s;
		}
		char[] str = s.toCharArray();
		str[index] = with;
		return new String(str);
	}

	/**
	 * String 有replace(char,char)替换全部char，但缺少单独replace first/last
	 */
	public static String replaceLast(@Nullable String s, char sub, char with) {
		if (s == null) {
			return null;
		}

		int index = s.lastIndexOf(sub);
		if (index == -1) {
			return s;
		}
		char[] str = s.toCharArray();
		str[index] = with;
		return new String(str);
	}

	/**
	 * 判断字符串是否以字母开头
	 * 
	 * 如果字符串为Null或空，返回false
	 */
	public static boolean startWith(@Nullable CharSequence s, char c) {
		if (StringUtils.isEmpty(s)) {
			return false;
		}
		return s.charAt(0) == c;
	}

	/**
	 * 判断字符串是否以字母结尾
	 * 
	 * 如果字符串为Null或空，返回false
	 */
	public static boolean endWith(@Nullable CharSequence s, char c) {
		if (StringUtils.isEmpty(s)) {
			return false;
		}
		return s.charAt(s.length() - 1) == c;
	}

	/**
	 * 如果结尾字符为c, 去除掉该字符.
	 */
	public static String removeEnd(final String s, final char c) {
		if (endWith(s, c)) {
			return s.substring(0, s.length() - 1);
		}
		return s;
	}

	///////////// 其他 ////////////
	/**
	 * 计算字符串被UTF8编码后的字节数 via guava
	 * 
	 * @see Utf8#encodedLength(CharSequence)
	 */
	public static int utf8EncodedLength(@Nullable CharSequence sequence) {
		if (StringUtils.isEmpty(sequence)) {
			return 0;
		}
		return Utf8.encodedLength(sequence);
	}
}
