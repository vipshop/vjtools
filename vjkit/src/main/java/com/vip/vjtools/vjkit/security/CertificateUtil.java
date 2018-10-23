package com.vip.vjtools.vjkit.security;

import com.vip.vjtools.vjkit.enums.KeyStoreType;
import com.vip.vjtools.vjkit.io.IOUtil;

import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

/**
 * 证书管理相关工具
 * @author  haven.zhang on 2018/10/22.
 */
public class CertificateUtil {
	/**
	 * x.509证书
	 */
	private static final String CERTIFICATE_TYPE = "X.509";

	/**
	 * 从keystore文件中加载公钥证书，例如：pfx格式的文件
	 * @param keystoreFile 文件目录
	 * @param passWord 访问密码
	 * @return Certificate 公钥证书对象
	 * @throws Exception
	 */
	public static Certificate getCertificate(String keystoreFile, final String passWord) throws Exception {
		FileInputStream fileInputStream = null;
		BufferedInputStream bin = null;
		try {
			fileInputStream = new FileInputStream(new File(keystoreFile));
			bin = new BufferedInputStream(fileInputStream);
			KeyStore ks = KeyStore.getInstance(KeyStoreType.PKCS12.getValue());
			ks.load(bin, passWord.toCharArray());
			return getCertificate(ks);
		} finally {
			IOUtil.closeQuietly(fileInputStream);
			IOUtil.closeQuietly(bin);
		}
	}

	/**
	 * 从keystore中加载公钥证书，例如：pfx格式的文件
	 * @param keystore keystore对象
	 * @return Certificate 公钥证书对象
	 * @throws Exception
	 */
	public static Certificate getCertificate(KeyStore keystore) throws KeyStoreException {
		Enumeration myEnum = keystore.aliases();
		while (myEnum.hasMoreElements()) {
			String keyAlias = (String) myEnum.nextElement();
			if (keystore.isCertificateEntry(keyAlias) || keystore.isKeyEntry(keyAlias)) {
				Certificate[] certs = (Certificate[]) keystore.getCertificateChain(keyAlias);
				return certs[0];
			}
		}
		return null;
	}

	/**
	 * 加载公钥证书
	 * @param certFilePath
	 * @return X509Certificate 证书对象
	 * @throws Exception
	 */
	public static X509Certificate loadCertificate(String certFilePath) throws Exception {
		FileInputStream fileStream = null;
		BufferedInputStream bin = null;
		try {
			fileStream = new FileInputStream(certFilePath);
			bin = new BufferedInputStream(fileStream);
			CertificateFactory cf = CertificateFactory.getInstance(CERTIFICATE_TYPE);
			X509Certificate x509cert = (X509Certificate) cf.generateCertificate(bin);
			return x509cert;
		} finally {
			IOUtil.closeQuietly(fileStream);
			IOUtil.closeQuietly(bin);
		}
	}


	/**
	 * 创建keystore文件，类型为:JKS
	 * @param outputFile 输出文件路径
	 * @param password keystore访问密码
	 * @throws Exception
	 */
	public static void createEmptyKeystore(String outputFile, String password) throws Exception {
		FileOutputStream fout = null;
		BufferedOutputStream bout = null;
		try {
			fout = new FileOutputStream(new File(outputFile));
			bout = new BufferedOutputStream(fout);
			KeyStore keyStore = KeyStore.getInstance(KeyStoreType.JKS.getValue());
			keyStore.load(null, null);
			keyStore.store(bout, password.toCharArray());
		} finally {
			IOUtil.closeQuietly(fout);
			IOUtil.closeQuietly(bout);
		}
	}

	/**
	 * 加载keystore文件
	 * @param jksfile keystore文件路径
	 * @param password keystore访问密码
	 * @return KeyStore 对象
	 * @throws Exception
	 */
	public static KeyStore loadKeyStore(String jksfile, String password) throws Exception {
		FileInputStream fin = null;
		BufferedInputStream bin = null;
		KeyStore keyStore;
		try {
			fin = new FileInputStream(new File(jksfile));
			bin = new BufferedInputStream(fin);
			keyStore = KeyStore.getInstance(KeyStoreType.JKS.getValue());
			keyStore.load(bin, password.toCharArray());
		} finally {
			IOUtil.closeQuietly(fin);
			IOUtil.closeQuietly(bin);
		}
		return keyStore;
	}

	/**
	 * 从流中读取keystore对象
	 * @param jksStream
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public static KeyStore loadKeyStore(InputStream jksStream, String password) throws Exception {
		KeyStore keyStore = KeyStore.getInstance(KeyStoreType.JKS.getValue());
		keyStore.load(jksStream, password.toCharArray());
		return keyStore;
	}

}
