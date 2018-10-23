package com.vip.vjtools.vjkit.enums;

/**
 * 加密算法枚举
 * @author haven.zhang on 2018/10/23.
 */
public enum CipherAlgorithms {
	RC2("RC2"),
	RC4("RC4"),

	DES_ECB_ZeroBytePadding("DES/ECB/ZeroBytePadding"),
	DES_ECB_ISO10126Padding("DES/ECB/ISO10126Padding"),
	DES_ECB_PKCS5Padding("DES/ECB/PKCS5Padding"),
	DES_ECB_PKCS7Padding("DES/ECB/PKCS7Padding"),
	DES_ECB_NoPadding("DES/ECB/NoPadding"),

	DES_CBC_PKCS5Padding("DES/CBC/PKCS5Padding"),
	DES_CBC_NoPadding("DES/CBC/NoPadding"),
	DES_CBC_ZeroBytePadding("DES/CBC/ZeroBytePadding"),
	DES_CBC_PKCS7Padding("DES/CBC/PKCS7Padding"),
	DES_CBC_ISO10126Padding("DES/CBC/ISO10126Padding"),

	DES_PCBC_PKCS5Padding("DES/PCBC/PKCS5Padding"),
	DES_PCBC_NoPadding("DES/PCBC/NoPadding"),
	DES_PCBC_ZeroBytePadding("DES/PCBC/ZeroBytePadding"),
	DES_PCBC_PKCS7Padding("DES/PCBC/PKCS7Padding"),
	DES_PCBC_ISO10126Padding("DES/PCBC/ISO10126Padding"),

	DES_CFB_PKCS5Padding("DES/CFB/PKCS5Padding"),
	DES_CFB_NoPadding("DES/CFB/NoPadding"),
	DES_CFB_ZeroBytePadding("DES/CFB/ZeroBytePadding"),
	DES_CFB_PKCS7Padding("DES/CFB/PKCS7Padding"),
	DES_CFB_ISO10126Padding("DES/CFB/ISO10126Padding"),

	DES_OFB_PKCS5Padding("DES/OFB/PKCS5Padding"),
	DES_OFB_NoPadding("DES/OFB/NoPadding"),
	DES_OFB_ZeroBytePadding("DES/OFB/ZeroBytePadding"),
	DES_OFB_PKCS7Padding("DES/OFB/PKCS7Padding"),
	DES_OFB_ISO10126Padding("DES/OFB/ISO10126Padding"),

	DES_CTR_PKCS5Padding("DES/CTR/PKCS5Padding"),
	DES_CTR_NoPadding("DES/CTR/NoPadding"),
	DES_CTR_ZeroBytePadding("DES/CTR/ZeroBytePadding"),
	DES_CTR_PKCS7Padding("DES/CTR/PKCS7Padding"),
	DES_CTR_ISO10126Padding("DES/CTR/ISO10126Padding"),



	Desede_ECB_ZeroBytePadding("Desede/ECB/ZeroBytePadding"),
	Desede_ECB_ISO10126Padding("Desede/ECB/ISO10126Padding"),
	Desede_ECB_PKCS7Padding("Desede/ECB/PKCS7Padding"),
	Desede_ECB_PKCS5Padding("Desede/ECB/PKCS5Padding"),
	Desede_ECB_NoPadding("Desede/ECB/NoPadding"),

	Desede_CBC_PKCS5Padding("Desede/CBC/PKCS5Padding"),
	Desede_CBC_PKCS7Padding("Desede/CBC/PKCS7Padding"),
	Desede_CBC_ZeroBytePadding("Desede/CBC/ZeroBytePadding"),
	Desede_CBC_ISO10126Padding("Desede/CBC/ISO10126Padding"),
	Desede_CBC_NoPadding("Desede/CBC/NoPadding"),

	Desede_PCBC_PKCS5Padding("Desede/PCBC/PKCS5Padding"),
	Desede_PCBC_NoPadding("Desede/PCBC/NoPadding"),
	Desede_PCBC_ZeroBytePadding("Desede/PCBC/ZeroBytePadding"),
	Desede_PCBC_PKCS7Padding("Desede/PCBC/PKCS7Padding"),
	Desede_PCBC_ISO10126Padding("Desede/PCBC/ISO10126Padding"),

	Desede_CFB_PKCS5Padding("Desede/CFB/PKCS5Padding"),
	Desede_CFB_NoPadding("Desede/CFB/NoPadding"),
	Desede_CFB_ZeroBytePadding("Desede/CFB/ZeroBytePadding"),
	Desede_CFB_PKCS7Padding("Desede/CFB/PKCS7Padding"),
	Desede_CFB_ISO10126Padding("Desede/CFB/ISO10126Padding"),

	Desede_OFB_PKCS5Padding("Desede/OFB/PKCS5Padding"),
	Desede_OFB_NoPadding("Desede/OFB/NoPadding"),
	Desede_OFB_ZeroBytePadding("DES/OFB/ZeroBytePadding"),
	Desede_OFB_PKCS7Padding("Desede/OFB/PKCS7Padding"),
	Desede_OFB_ISO10126Padding("Desede/OFB/ISO10126Padding"),

	Desede_CTR_PKCS5Padding("Desede/CTR/PKCS5Padding"),
	Desede_CTR_NoPadding("Desede/CTR/NoPadding"),
	Desede_CTR_ZeroBytePadding("Desede/CTR/ZeroBytePadding"),
	Desede_CTR_PKCS7Padding("Desede/CTR/PKCS7Padding"),
	Desede_CTR_ISO10126Padding("Desede/CTR/ISO10126Padding"),

	AES_ECB_ZeroBytePadding("AES/ECB/ZeroBytePadding"),
	AES_ECB_ISO10126Padding("AES/ECB/ISO10126Padding"),
	AES_ECB_PKCS7Padding("AES/ECB/PKCS7Padding"),
	AES_ECB_PKCS5Padding("AES/ECB/PKCS5Padding"),
	AES_ECB_NoPadding("AES/ECB/NoPadding"),

	AES_CBC_PKCS5Padding("AES/CBC/PKCS5Padding"),
	AES_CBC_PKCS7Padding("AES/CBC/PKCS7Padding"),
	AES_CBC_ZeroBytePadding("AES/CBC/ZeroBytePadding"),
	AES_CBC_ISO10126Padding("AES/CBC/ISO10126Padding"),
	AES_CBC_NoPadding("AES/CBC/NoPadding"),

	AES_PCBC_PKCS5Padding("AES/PCBC/PKCS5Padding"),
	AES_PCBC_NoPadding("AES/PCBC/NoPadding"),
	AES_PCBC_ZeroBytePadding("AES/PCBC/ZeroBytePadding"),
	AES_PCBC_PKCS7Padding("AES/PCBC/PKCS7Padding"),
	AES_PCBC_ISO10126Padding("AES/PCBC/ISO10126Padding"),

	AES_CFB_PKCS5Padding("AES/CFB/PKCS5Padding"),
	AES_CFB_NoPadding("AES/CFB/NoPadding"),
	AES_CFB_ZeroBytePadding("AES/CFB/ZeroBytePadding"),
	AES_CFB_PKCS7Padding("AES/CFB/PKCS7Padding"),
	AES_CFB_ISO10126Padding("AES/CFB/ISO10126Padding"),

	AES_OFB_PKCS5Padding("AES/OFB/PKCS5Padding"),
	AES_OFB_NoPadding("AES/OFB/NoPadding"),
	AES_OFB_ZeroBytePadding("DES/OFB/ZeroBytePadding"),
	AES_OFB_PKCS7Padding("AES/OFB/PKCS7Padding"),
	AES_OFB_ISO10126Padding("AES/OFB/ISO10126Padding"),

	AES_CTR_PKCS5Padding("AES/CTR/PKCS5Padding"),
	AES_CTR_NoPadding("AES/CTR/NoPadding"),
	AES_CTR_ZeroBytePadding("AES/CTR/ZeroBytePadding"),
	AES_CTR_PKCS7Padding("AES/CTR/PKCS7Padding"),
	AES_CTR_ISO10126Padding("AES/CTR/ISO10126Padding"),


	RSA_ECB_PKCS1Padding("RSA/ECB/PKCS1Padding"),
	RSA_ECB_OAEPWithSHA1("RSA/ECB/OAEPWithSHA-1AndMGF1Padding"),
	RSA_ECB_OAEPWithSHA256("RSA/ECB/OAEPWithSHA-256AndMGF1Padding"),
	RSA_ECB_NoPadding("RSA/ECB/NoPadding");

	private String value;

	private CipherAlgorithms(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
