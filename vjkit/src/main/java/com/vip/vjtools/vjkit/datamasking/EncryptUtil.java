package com.vip.vjtools.vjkit.datamasking;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 加密算法工具类
 */
public class EncryptUtil {

	private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f'};

	/**
	 * sha1加密算法，在性能和加密强度考虑，选择了这个算法
	 */
	public static String sha1(String text) {
		MessageDigest msgDigest = null;
		try {
			msgDigest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("System doesn't support SHA-1 algorithm.");
		}
		try {
			//注意改接口是按照utf-8编码形式加密
			msgDigest.update(text.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("System doesn't support your  EncodingException.");
		}
		byte[] bytes = msgDigest.digest();
		return new String(encodeHex(bytes));
	}

	public static char[] encodeHex(byte[] data) {
		int l = data.length;
		char[] out = new char[l << 1];
		for (int i = 0, j = 0; i < l; i++) {
			out[j++] = DIGITS[(0xF0 & data[i]) >>> 4];
			out[j++] = DIGITS[0x0F & data[i]];
		}
		return out;
	}
}
