package com.vip.vjtools.vjkit.enums;

/**
 * 对称加密密码类型
 * see https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyGenerator
 * @author haven.zhang on 2018/10/22.
 */
public enum KeyGeneratorType {
	RC2,
	RC4,
	HmacSHA1,
	HmacSHA256,
	HmacSHA384,
	HmacSHA512,
	HmacMD5,
	Blowfish,
//	ARCFOUR,
	AES,
	DES,
	DESede;
}
