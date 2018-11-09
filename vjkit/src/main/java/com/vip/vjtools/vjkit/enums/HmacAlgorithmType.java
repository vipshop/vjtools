package com.vip.vjtools.vjkit.enums;

/**
 * HmacSHA1摘要算法
 * @author  haven.zhang on 2018/10/26.
 */
public enum HmacAlgorithmType {
	/**
	 * HmacSHA1摘要算法
	 */
	HMAC_SHA1("HmacSHA1"),
	HMAC_SHA224("HmacSHA224"),
	HMAC_SHA256("HmacSHA256"),
	HMAC_SHA384("HmacSHA384"),
	HMAC_SHA512("HmacSHA512"),
	HMAC_MD2("HmacMD2"),
	HMAC_MD4("HmacMD4"),
	HMAC_MD5("HmacMD5");

	public final String value;

	HmacAlgorithmType(String value) {
		this.value = value;
	}
}
