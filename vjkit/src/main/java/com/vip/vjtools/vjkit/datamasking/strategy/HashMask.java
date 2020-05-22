package com.vip.vjtools.vjkit.datamasking.strategy;

import com.vip.vjtools.vjkit.datamasking.EncryptUtil;
import com.vip.vjtools.vjkit.datamasking.MaskStrategy;

/**
 * Hash掩码，主要用于一些敏感信息掩码后查询。
 * 算法：sha1(source+salt)
 */
public class HashMask implements MaskStrategy {

	private static String salt="default_salt";


	@Override
	public String mask(String source, int[] params) {
		if (source == null || source.isEmpty()) {
			return source;
		}
		return EncryptUtil.sha1(source + getSalt());
	}

	/**
	 * 设置salt
	 */
	public static void setSalt(String salt){
		HashMask.salt = salt;
	}

	/**
	 * 默认用 default_salt
	 * 可以通过DataMask来设置
	 */
	public static String getSalt() {
		return salt;
	}
}
