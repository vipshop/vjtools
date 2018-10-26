package com.vip.vjtools.vjkit.enums;

/**
 * 安全提供者
 * @author haven.zhang
 */
public enum ProviderType {

	SUN("sun.security.provider.Sun"),
	SUNRSASIGN("sun.security.rsa.SunRsaSign"),
	SUNEC("sun.security.ec.SunEC"),
	SSLPROVIDER("com.sun.net.ssl.internal.ssl.Provider"),
	SUNJCE("com.sun.crypto.provider.SunJCE"),
	SUNPROVIDER("sun.security.jgss.SunProvider"),
	SASLPROVIDER("com.sun.security.sasl.Provider"),
	XMLDSIGRI("org.jcp.xml.dsig.internal.dom.XMLDSigRI"),
	SUNPCSC("sun.security.smartcardio.SunPCSC"),
	SUNMSCAPI("sun.security.mscapi.SunMSCAPI"),
	BC("org.bouncycastle.jce.provider.BouncyCastleProvider");

	private final String value;

	private ProviderType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
