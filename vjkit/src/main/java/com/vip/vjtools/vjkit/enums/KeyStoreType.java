package com.vip.vjtools.vjkit.enums;

/**
 * keystore密钥库类型
 * see https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#KeyStore
 * @author haven.zhang
 */
public enum KeyStoreType {
	/**
	 * The proprietary keystore implementation provided by the SUN provider.
	 */
	JKS("JKS"),
	/**
	 * The proprietary keystore implementation provided by the SunJCE provider.
	 */
	JCEKS("JCEKS"),
	/**
	 * Bouncycastle 支持的密钥库类型
	 */
	BKS("BKS"),
	/**
	 *Bouncycastle 支持的密钥库类型
	 */
	UBER("UBER"),
	/**
	 * The transfer syntax for personal identity information as defined in PKCS #12.
	 */
	PKCS12("PKCS12");


	private final String value;

	private KeyStoreType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
