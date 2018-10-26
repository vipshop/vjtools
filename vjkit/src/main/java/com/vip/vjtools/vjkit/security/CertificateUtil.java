package com.vip.vjtools.vjkit.security;

import com.vip.vjtools.vjkit.base.type.UncheckedException;
import com.vip.vjtools.vjkit.enums.KeyPairAlgorithms;
import com.vip.vjtools.vjkit.enums.KeyStoreType;
import com.vip.vjtools.vjkit.enums.ProviderType;
import com.vip.vjtools.vjkit.enums.SignAlgorithm;
import com.vip.vjtools.vjkit.id.IdUtil;
import com.vip.vjtools.vjkit.io.FileUtil;
import com.vip.vjtools.vjkit.io.IOUtil;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.bc.BcPEMDecryptorProvider;
import org.bouncycastle.openssl.jcajce.*;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.RSAPublicKeySpec;
import java.util.Date;
import java.util.Enumeration;

/**
 * 证书管理相关工具
 * 包括生成公开证书，从证书中读取公私钥、证书格式转换等等
 * 使用该类时，需要依赖bouncycastle maven配置
 * 并在初始化时加入BC作为安全提供者 Security.addProvider(new BouncyCastleProvider());
 * @author haven.zhang on 2018/10/22.
 */
public class CertificateUtil {
	/**
	 * x.509证书
	 */
	private static final String CERTIFICATE_TYPE = "X.509";

	/**
	 * 从keystore文件中加载公钥证书，仅用于pkcs12格式的秘钥库，例如：pfx格式的文件
	 * @param keystoreFile 文件目录
	 * @param passWord 访问密码
	 * @return Certificate 公钥证书对象
	 * @throws Exception
	 */
	public static Certificate getCertificate(String keystoreFile, final String passWord) throws Exception {
		FileInputStream fin = null;
		BufferedInputStream bin = null;
		try {
			fin = new FileInputStream(new File(keystoreFile));
			bin = new BufferedInputStream(fin);
			KeyStore ks = KeyStore.getInstance(KeyStoreType.PKCS12.getValue());
			ks.load(bin, passWord.toCharArray());
			return getCertificate(ks);
		} finally {
			IOUtil.closeQuietly(bin);
			IOUtil.closeQuietly(fin);
		}
	}

	/**
	 * 从keystore中加载公钥证书，例如：pfx格式的文件
	 * @param keystore keystore对象
	 * @return Certificate 公钥证书对象
	 * @throws Exception
	 */
	public static Certificate getCertificate(KeyStore keystore) throws Exception {
		Enumeration myEnum = keystore.aliases();
		while (myEnum.hasMoreElements()) {
			String keyAlias = (String) myEnum.nextElement();
			if (keystore.isCertificateEntry(keyAlias) || keystore.isKeyEntry(keyAlias)) {
				Certificate[] certs = keystore.getCertificateChain(keyAlias);
				return certs[0];
			}
		}
		return null;
	}

	/**
	 * 从keystore中读取公钥证书对象
	 * @param keystore 密钥库对象
	 * @param alias 保存在keystore中的证书别名
	 * @return Certificate 公开证书
	 * @throws KeyStoreException
	 */
	public static Certificate getCertificate(KeyStore keystore, String alias) throws KeyStoreException {
		return (X509Certificate) keystore.getCertificate(alias);
	}

	/**
	 * 加载公钥证书
	 * @param certFilePath x509证书文件路径，如：/apps/data/certs/xxx.cer
	 * @return X509Certificate 证书对象
	 * @throws Exception
	 */
	public static X509Certificate loadCertificate(String certFilePath) throws Exception {
		FileInputStream fin = null;
		BufferedInputStream bin = null;
		try {
			fin = new FileInputStream(certFilePath);
			bin = new BufferedInputStream(fin);
			CertificateFactory cf = CertificateFactory.getInstance(CERTIFICATE_TYPE);
			X509Certificate x509cert = (X509Certificate) cf.generateCertificate(bin);
			return x509cert;
		} finally {
			IOUtil.closeQuietly(bin);
			IOUtil.closeQuietly(fin);
		}
	}

	/**
	 * 从流中加载x509证书
	 * @param certStream 证书流
	 * @return X509Certificate 证书对象
	 * @throws Exception
	 */
	public static X509Certificate loadCertificate(InputStream certStream) throws Exception {
		CertificateFactory cf = CertificateFactory.getInstance(CERTIFICATE_TYPE);
		return (X509Certificate) cf.generateCertificate(certStream);
	}

	/**
	 * 创建keystore文件，类型为:JKS 或者 pkcs12
	 * @param outputFile 输出文件路径
	 * @param password keystore访问密码
	 * @throws Exception
	 */
	public static KeyStore createEmptyKeystore(String outputFile, String password,KeyStoreType type) throws Exception {
		FileOutputStream fout = null;
		BufferedOutputStream bout = null;
		KeyStore keyStore = null;
		try {
			fout = new FileOutputStream(new File(outputFile));
			bout = new BufferedOutputStream(fout);
			keyStore = KeyStore.getInstance(type.getValue());
			keyStore.load(null, password.toCharArray());
			keyStore.store(bout, password.toCharArray());
		} finally {
			IOUtil.closeQuietly(bout);
			IOUtil.closeQuietly(fout);
		}
		return keyStore;
	}

	/**
	 * 加载keystore文件
	 * @param keystoreFile keystore文件路径
	 * @param password keystore访问密码
	 * @param type 密钥库类型，见：KeyStoreType枚举
	 * @return KeyStore 对象
	 * @throws Exception
	 */
	public static KeyStore loadKeyStore(String keystoreFile, String password,KeyStoreType type) throws Exception {
		FileInputStream fin = null;
		BufferedInputStream bin = null;
		KeyStore keyStore;
		try {
			fin = new FileInputStream(new File(keystoreFile));
			bin = new BufferedInputStream(fin);
			keyStore = KeyStore.getInstance(type.getValue());
			keyStore.load(bin, password.toCharArray());
		} finally {
			IOUtil.closeQuietly(bin);
			IOUtil.closeQuietly(fin);
		}
		return keyStore;
	}

	/**
	 * 从流中读取keystore对象
	 * @param input 密钥库文件流
	 * @param password 密钥库的访问密码
	 * @param type 密钥库类型，见：KeyStoreType枚举
	 * @return KeyStore 格式的密钥库
	 * @throws Exception
	 */
	public static KeyStore loadKeyStore(InputStream input, String password,KeyStoreType type) throws Exception {
		KeyStore keyStore = KeyStore.getInstance(type.getValue());
		keyStore.load(input, password.toCharArray());
		return keyStore;
	}


	/**
	 * 生成pfx私钥证书文件，里面包括公钥，私钥，证书链别名
	 * @param alias 证书别名
	 * @param privKey 私钥
	 * @param password 私钥访问密码
	 * @param certChain 证书链
	 * @return out 输出流
	 * @throws Exception
	 */
	public static ByteArrayOutputStream generatePfx(String alias, PrivateKey privKey, String password,
			Certificate[] certChain) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		KeyStore outputKeyStore = KeyStore.getInstance(KeyStoreType.PKCS12.getValue(), ProviderType.BC.name());
		outputKeyStore.load(null, password.toCharArray());
		outputKeyStore.setKeyEntry(alias, privKey, password.toCharArray(), certChain);
		outputKeyStore.store(out, password.toCharArray());
		return out;
	}

	/**
	 * 生成pfx私钥证书文件，里面包括公钥，私钥，证书链别名
	 * @param alias 秘钥对别名
	 * @param privKey 私钥
	 * @param pwd 密钥库访问密码 私钥访问密码也是这个
	 * @param certChain  证书链，可以对应多个证书
	 * @throws Exception
	 */
	public static void generatePfx(String alias, PrivateKey privKey, String pwd, Certificate[] certChain,
			String outputFile) throws Exception {
		FileOutputStream out = new FileOutputStream(new File(outputFile));
		BufferedOutputStream bout = new BufferedOutputStream(out);
		try {
			KeyStore outputKeyStore = KeyStore.getInstance(KeyStoreType.PKCS12.getValue(), ProviderType.BC.name());
			outputKeyStore.load(null, pwd.toCharArray());
			outputKeyStore.setKeyEntry(alias, privKey, pwd.toCharArray(), certChain);
			outputKeyStore.store(bout, pwd.toCharArray());
		} finally {
			IOUtil.closeQuietly(bout);
			IOUtil.closeQuietly(out);
		}
	}


	/**
	 * 生成pfx私钥证书，并保存至指定目录
	 * @param alias 证书保存的别名
	 * @param privKey 私钥
	 * @param password 访问密码
	 * @param certChain 公开证书链
	 * @param outputFilePath 输出文件路径，如：/apps/data/certs/test.pfx
	 * @throws Exception
	 */
	public static void generatePfx2(String alias, PrivateKey privKey, String password, Certificate[] certChain,
			String outputFilePath) throws Exception {
		ByteArrayOutputStream out = generatePfx(alias, privKey, password, certChain);
		FileUtil.writeByteArrayToFile(new File(outputFilePath), out.toByteArray());
		IOUtil.closeQuietly(out);
	}


	/**
	 * 从pem格式的文件内容转换为公钥对象，（仅用于pem为无加密存储的方式）
	 * @param pemStr pem文件base64内容
	 * @return PublicKey 公钥
	 * @throws Exception
	 */
	public static PublicKey getPublicKey(String pemStr, KeyPairAlgorithms keyPairAlgorithms) throws Exception {
		PEMParser pemParser = new PEMParser(new StringReader(pemStr));
		PublicKey key = null;
		try {
			Object localObject = pemParser.readObject();
			if (localObject instanceof SubjectPublicKeyInfo) {
				SubjectPublicKeyInfo pkInfo = (SubjectPublicKeyInfo) localObject;
				RSAKeyParameters rsa = (RSAKeyParameters) PublicKeyFactory.createKey(pkInfo);
				RSAPublicKeySpec rsaSpec = new RSAPublicKeySpec(rsa.getModulus(), rsa.getExponent());
				KeyFactory kf = KeyFactory.getInstance(keyPairAlgorithms.name());
				key = kf.generatePublic(rsaSpec);
			}
		} finally {
			IOUtil.closeQuietly(pemParser);
		}
		return key;
	}


	/**
	 * 从pem格式的私钥证书文件内容中提取公私钥秘钥对，（仅用于pem为无加密存储的方式）
	 * @param pemStr pem文件base64内容
	 * @return KeyPair 公钥和私钥对
	 * @throws Exception
	 */
	public static KeyPair getKeyPair(String pemStr) throws Exception {
		PEMParser pemParser = new PEMParser(new StringReader(pemStr));
		KeyPair keyPair = null;
		try {
			Object localObject = pemParser.readObject();
			if (localObject instanceof PEMKeyPair) {
				keyPair = new JcaPEMKeyConverter().setProvider(ProviderType.BC.name())
						.getKeyPair((PEMKeyPair) localObject);
			}
		} finally {
			IOUtil.closeQuietly(pemParser);
		}
		return keyPair;
	}


	/**
	 * 从keystore秘钥库中读取秘钥对
	 * @param keyStore 秘钥库
	 * @param alias 秘钥对别名
	 * @param password 密钥库访问密码
	 * @return KeyPair 秘钥对（公钥和私钥）
	 * @throws Exception
	 */
	public static KeyPair getKeyPair(KeyStore keyStore, String alias, String password) throws Exception {
		Key key = keyStore.getKey(alias, password.toCharArray());
		Certificate[] chain = keyStore.getCertificateChain(alias);
		KeyPair keyPair = new KeyPair(chain[0].getPublicKey(), (PrivateKey) key);
		return keyPair;
	}

	/**
	 * 从pem格式的私钥文件内容中提取私钥，（仅用于pem为无加密存储的方式）
	 * @param pemStr pem文件base64内容
	 * @return PrivateKey 私钥
	 * @throws Exception
	 */
	public static PrivateKey getPrivateKey(String pemStr) throws Exception {
		PEMParser pemParser = new PEMParser(new StringReader(pemStr));
		PrivateKey privateKey = null;
		try {
			Object localObject = pemParser.readObject();
			if (localObject instanceof PEMKeyPair) {
				KeyPair keyPair = new JcaPEMKeyConverter().setProvider(ProviderType.BC.name())
						.getKeyPair((PEMKeyPair) localObject);
				privateKey = keyPair.getPrivate();
			}
		} finally {
			IOUtil.closeQuietly(pemParser);
		}
		return privateKey;
	}

	/**
	 * pem私钥文件转换为PrivateKey对象，需密码解密 pkcs8格式 以 -----BEGIN ENCRYPTED PRIVATE KEY----- 开头的pem文件
	 * @param pemStr pem文件内容
	 * @param passwd 访问秘钥
	 * @return pem内容
	 * @throws Exception
	 */
	public static PrivateKey getPrivateKey(String pemStr, String passwd) throws Exception {
		PEMParser pemParser = new PEMParser(new StringReader(pemStr));
		PrivateKey privateKey = null;
		try {
			InputDecryptorProvider decryptorProvider = new JceOpenSSLPKCS8DecryptorProviderBuilder()
					.setProvider(ProviderType.BC.name()).build(passwd.toCharArray());
			PKCS8EncryptedPrivateKeyInfo pKCS8EncryptedPrivateKeyInfo = (PKCS8EncryptedPrivateKeyInfo) pemParser
					.readObject();
			JcaPEMKeyConverter jcaPEMKeyConverter = new JcaPEMKeyConverter().setProvider(ProviderType.BC.name());
			privateKey = jcaPEMKeyConverter
					.getPrivateKey(pKCS8EncryptedPrivateKeyInfo.decryptPrivateKeyInfo(decryptorProvider));
		} finally {
			IOUtil.closeQuietly(pemParser);
		}
		return privateKey;
	}

	/**
	 * 生成pkcs8格式的pem文件
	 * @param priKey 私钥
	 * @param passwd 加密密码
	 * @param objectIdentifier 可选值：NISTObjectIdentifiers.id_aes256_CBC/PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC/PKCSObjectIdentifiers.
	 * pbeWithSHAAnd3_KeyTripleDES_CBC  加密算法
	 * @return pem文件字符串内容，base64编码
	 * @throws Exception
	 */
	public static String generatePKCS8Pem(PrivateKey priKey, String passwd, ASN1ObjectIdentifier objectIdentifier)
			throws Exception {
		StringWriter stringWriter = new StringWriter();
		JcaPEMWriter jcaPEMWriter = new JcaPEMWriter(stringWriter);
		try {
			JceOpenSSLPKCS8EncryptorBuilder encryptorBuilder = new JceOpenSSLPKCS8EncryptorBuilder(objectIdentifier);
			encryptorBuilder.setRandom(new SecureRandom());
			encryptorBuilder.setPasssword(passwd.toCharArray());
			OutputEncryptor oe = encryptorBuilder.build();
			JcaPKCS8Generator gen = new JcaPKCS8Generator(priKey, oe);
			PemObject obj = gen.generate();
			jcaPEMWriter.writeObject(obj);
		} finally {
			IOUtil.closeQuietly(jcaPEMWriter);
		}

		return stringWriter.toString();
	}

	/**
	 * pem私钥文件转换为公私钥秘钥对对象，需密码解密
	 * @param pemStr pem格式的证书文件内容（base64编码的字符串）
	 * @param passwd 访问密码
	 * @return KeyPair 密钥对（公钥和私钥）
	 * @throws Exception
	 */
	public static KeyPair getKeyPair(String pemStr, String passwd) throws Exception {
		PEMParser pemParser = new PEMParser(new StringReader(pemStr));
		KeyPair keyPair = null;
		try {
			Object pemObj = pemParser.readObject();
			if ((pemObj == null) || (!(pemObj instanceof PEMEncryptedKeyPair))) {
				throw new Exception("pem转换异常,pem文件格式不正常");
			}
			keyPair = new JcaPEMKeyConverter().setProvider(ProviderType.BC.name()).getKeyPair(
					((PEMEncryptedKeyPair) pemObj).decryptKeyPair(new BcPEMDecryptorProvider(passwd.toCharArray())));
		} finally {
			IOUtil.closeQuietly(pemParser);
		}
		return keyPair;
	}

	/**
	 * 产生私钥的pem文件信息（加密存储的方式）
	 * @param passwd 私钥加密密钥
	 * @param encryptAlgorithm 选值参考枚举类PemEncryptAlgoritm
	 * @return String pem格式的内容，base64编码后的字符串
	 * @throws IOException
	 */
	public static String generatPem(PrivateKey privateKey, String passwd, String encryptAlgorithm) throws IOException {
		SecureRandom random = new SecureRandom();
		StringWriter stringWriter = new StringWriter();
		JcaPEMWriter jcaPEMWriter = new JcaPEMWriter(stringWriter);
		jcaPEMWriter.writeObject(new JcaMiscPEMGenerator(privateKey,
				new JcePEMEncryptorBuilder(encryptAlgorithm).setSecureRandom(random).build(passwd.toCharArray())));
		IOUtil.closeQuietly(jcaPEMWriter);
		return stringWriter.toString();
	}

	/**
	 * 将公钥或者私钥转换成pem格式的字符串（无加密存储的方式） 输出例子如下：
	 * 公钥：
	 * -----BEGIN PUBLIC KEY-----
	 * MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCCPd+8fTSB9y/UGjqgOg9FBJP6
	 * aXnNvGuuNRCzmeo2SHopHgWSCQncyQnnK3OLgzL2tskNB9kzJ2KPOnCfa9K6yTo8
	 * 3gRjakj+mKGRKb00jBwhGXaJ75AOC1glbbOjsrIk5D44D1OwnXf6tv7cFKRIAOff
	 * LlHhDLvmYRtTSXy1XwIDAQAB
	 * -----END PUBLIC KEY-----
	 * 如果是私钥，则以-----BEGIN RSA PRIVATE KEY-----开头，以-----END RSA PRIVATE KEY-----结尾
	 * @param key,可以是公钥也可以是私钥
	 * @return String pem格式的内容 base64编码字符串
	 * @throws IOException
	 */
	public static String generatPem(Key key) throws IOException {
		StringWriter stringWriter = new StringWriter();
		JcaPEMWriter jcaPEMWriter = new JcaPEMWriter(stringWriter);
		jcaPEMWriter.writeObject(key);
		jcaPEMWriter.close();
		return stringWriter.toString();
	}

	/**
	 * der格式的公钥证书文件转换成pem格式的公钥证书，其中pem格式的公钥证书内容以下面字符串开始和结束 -----BEGIN CERTIFICATE----- -----END CERTIFICATE-----
	 * @param pemStr pem公钥文件的内容字符串（base64编码）
	 * @return X509Certificate x509证书对象
	 * @throws Exception
	 */
	public static X509Certificate getCertificate(String pemStr) throws Exception {
		StringReader rd = new StringReader(pemStr);
		PemObject pemObj;
		try (PemReader pr = new PemReader(rd)) {
			pemObj = pr.readPemObject();
		}
		ByteArrayInputStream in = new ByteArrayInputStream(pemObj.getContent());
		CertificateFactory cf = CertificateFactory.getInstance(CERTIFICATE_TYPE);
		return (X509Certificate) cf.generateCertificate(in);
	}

	/**
	 * X509Certificate 转换到pem -----BEGIN CERTIFICATE----- -----END CERTIFICATE-----
	 * @param x509Cert 证书对象
	 * @return String pem格式的公钥内容
	 * @throws IOException
	 */
	public static String generatePem(Certificate x509Cert) throws IOException {
		StringWriter sw = new StringWriter();
		try (JcaPEMWriter pw = new JcaPEMWriter(sw)) {
			pw.writeObject(x509Cert);
		}
		return sw.toString();
	}

	/**
	 * X509Certificate 证书对象转换成pem格式的字符串
	 * 实际上就是将证书信息进行base64编码
	 * @param x509cert 证书对象
	 * @return String pem文件内容
	 * @throws Exception
	 */
	public static String generatePemExt(Certificate x509cert) throws Exception {
		String begin = "-----BEGIN CERTIFICATE-----\n";
		String end = "-----END CERTIFICATE-----";
		byte[] derCert = x509cert.getEncoded();
		String pemCertPre = new String(Base64.encode(derCert), Charset.defaultCharset());
		return begin + pemCertPre + end;
	}

	/**
	 * 生成证书请求信息 将证书请求信息提供给证书颁发中心，由颁发中心进行证书签发
	 * 输出内容以-----BEGIN CERTIFICATE REQUEST-----开头，以-----END CERTIFICATE REQUEST-----结尾
	 * @param subject 如："CN=country, ST=state,L=Locality,OU=OrganizationUnit,O=Organization";
	 * @param publicKey 公钥
	 * @param privateKey 私钥
	 * @param signAlgorithm 签名算法
	 * @return String csr证书请求信息
	 * @throws Exception
	 */
	public static String generateCsr(String subject, PublicKey publicKey, PrivateKey privateKey,
			SignAlgorithm signAlgorithm) throws Exception {
		X500Name dn = new X500Name(subject);
		PKCS10CertificationRequestBuilder requestBuilder = new JcaPKCS10CertificationRequestBuilder(dn, publicKey);
		PKCS10CertificationRequest csr = requestBuilder
				.build(new JcaContentSignerBuilder(signAlgorithm.name()).setProvider(ProviderType.BC.name())
						.build(privateKey));
		String type = "CERTIFICATE REQUEST";
		byte[] content = csr.getEncoded();
		PemObject pemObject = new PemObject(type, content);
		StringWriter stringWriter = new StringWriter();
		JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter);
		pemWriter.writeObject(pemObject);
		pemWriter.close();
		return stringWriter.toString();

	}

	/**
	 * 从证书请求信息中获取公钥
	 * @param csrStr 证书请求内容字符串
	 * @return PublicKey 公钥对象
	 * @throws Exception
	 */
	public static PublicKey getPublicKeyFromCsr(String csrStr, KeyPairAlgorithms algorithms)
			throws Exception {
		csrStr = csrStr.replaceAll("-----BEGIN CERTIFICATE REQUEST-----\r\n", "");
		csrStr = csrStr.replaceAll("\r\n-----END CERTIFICATE REQUEST-----", "");
		byte[] der = Base64.decode(csrStr);
		PKCS10CertificationRequest p10CSR = new PKCS10CertificationRequest(der);

		SubjectPublicKeyInfo pkInfo = p10CSR.getSubjectPublicKeyInfo();
		RSAKeyParameters rsa = (RSAKeyParameters) PublicKeyFactory.createKey(pkInfo);
		RSAPublicKeySpec rsaSpec = new RSAPublicKeySpec(rsa.getModulus(), rsa.getExponent());
		KeyFactory kf = KeyFactory.getInstance(algorithms.name());
		PublicKey rsaPub = kf.generatePublic(rsaSpec);
		return rsaPub;
	}


	/**
	 * 颁发证书
	 * @param csrStr 证书请求信息
	 * @param priKey 颁发机构签名的私钥
	 * @param issuerStr 颁发机构信息,如："CN=country, ST=state,L=Locality,OU=OrganizationUnit,O=Organization";
	 * @param notBefore 证书生效日期
	 * @param notAfter 证书过期日期
	 * @param signAlgorithim 证书签名算法
	 * @return X509Certificate 颁发后的证书对象
	 * @throws Exception
	 */
	public static X509Certificate issueCertificate(String csrStr, PrivateKey priKey, String issuerStr, Date notBefore,
			Date notAfter, SignAlgorithm signAlgorithim) throws Exception {
		csrStr = csrStr.replaceAll("-----BEGIN CERTIFICATE REQUEST-----\r\n", "");
		csrStr = csrStr.replaceAll("\r\n-----END CERTIFICATE REQUEST-----", "");
		byte[] der = Base64.decode(csrStr);
		PKCS10CertificationRequest p10CSR = new PKCS10CertificationRequest(der);

		SubjectPublicKeyInfo pkInfo = p10CSR.getSubjectPublicKeyInfo();
		RSAKeyParameters rsa = (RSAKeyParameters) PublicKeyFactory.createKey(pkInfo);
		RSAPublicKeySpec rsaSpec = new RSAPublicKeySpec(rsa.getModulus(), rsa.getExponent());
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PublicKey publicKey = kf.generatePublic(rsaSpec);

		ContentVerifierProvider verifierProvider = new JcaContentVerifierProviderBuilder()
				.setProvider(ProviderType.BC.name()).build(publicKey);
		if (!p10CSR.isSignatureValid(verifierProvider)) {
			throw new UncheckedException("证书请求信息有误，请检查");
		}
		p10CSR.getSignatureAlgorithm().getAlgorithm().getId();
		X500Name issuer = new X500Name(issuerStr);
		X500Name subject = p10CSR.getSubject();

		JcaX509v1CertificateBuilder jcaX509v1CertificateBuilder = new JcaX509v1CertificateBuilder(issuer,
				new BigInteger(IdUtil.generateId()), notBefore, notAfter, subject, publicKey);
		X509CertificateHolder x509CertificateHolder = jcaX509v1CertificateBuilder
				.build(new JcaContentSignerBuilder(signAlgorithim.name()).setProvider(ProviderType.BC.name())
						.build(priKey));
		return new JcaX509CertificateConverter().setProvider(ProviderType.BC.name())
				.getCertificate(x509CertificateHolder);
	}

	/**
	 * 为证书签名
	 * @param caPrivateKey 签名私钥
	 * @return X509Certificate 签名后的公开证书
	 * @throws Exception
	 */
	public static X509Certificate signCertificate(PrivateKey caPrivateKey, X509Certificate cert,
			SignAlgorithm signAlgorithim) throws Exception {
		X500Name issuer = new X500Name(cert.getIssuerX500Principal().getName());
		X500Name subject = new X500Name(cert.getSubjectX500Principal().getName());
		JcaX509v1CertificateBuilder jcaX509v1CertificateBuilder = new JcaX509v1CertificateBuilder(issuer,
				cert.getSerialNumber(), cert.getNotBefore(), cert.getNotAfter(), subject, cert.getPublicKey());
		X509CertificateHolder x509CertificateHolder = jcaX509v1CertificateBuilder
				.build(new JcaContentSignerBuilder(signAlgorithim.name()).setProvider(ProviderType.BC.name())
						.build(caPrivateKey));
		return new JcaX509CertificateConverter().setProvider(ProviderType.BC.name())
				.getCertificate(x509CertificateHolder);
	}


	/**
	 * 创建版本为V1的公开证书
	 * @param caPrivateKey 证书签名私钥
	 * @param publicKey 证书公钥
	 * @param issuerBuilder 颁发者信息
	 * @param subjectBuilder 申请者信息
	 * @param signAlgorithim 签名算法
	 * @param notBefore 证书生效时间
	 * @param notAfter 证书过期时间
	 * @return X509Certificate 签名后的证书
	 * @throws Exception
	 */
	public static X509Certificate createV1Certificate(PrivateKey caPrivateKey, PublicKey publicKey,
			X500NameBuilder issuerBuilder, X500NameBuilder subjectBuilder, SignAlgorithm signAlgorithim, Date notBefore,
			Date notAfter) throws Exception {
		X500Name issuer = issuerBuilder.build();
		X500Name subject = subjectBuilder.build();
		JcaX509v1CertificateBuilder jcaX509v1CertificateBuilder = new JcaX509v1CertificateBuilder(issuer,
				BigInteger.valueOf(IdUtil.increaseId()), notBefore, notAfter, subject, publicKey);
		X509CertificateHolder x509CertificateHolder = jcaX509v1CertificateBuilder
				.build(new JcaContentSignerBuilder(signAlgorithim.name()).setProvider(ProviderType.BC.name())
						.build(caPrivateKey));
		return new JcaX509CertificateConverter().setProvider(ProviderType.BC.name())
				.getCertificate(x509CertificateHolder);
	}

	/**
	 *  创建版本为V3的公开证书
	 * @param subjectKey 证书请求者公钥
	 * @param authorityKeyPair 颁发者秘钥对（包含公钥和私钥），私钥，用于对证书进行签名
	 * @param issuerBuilder 颁发者信息
	 * @param subjectBuilder 请求主题信息
	 * @param signAlgorithim 证书签名算法
	 * @param notBefore 证书生效时间
	 * @param notAfter 证书失效时间
	 * @return X509Certificate
	 * @throws Exception
	 */
	public static X509Certificate createV3Certificate(PublicKey subjectKey, KeyPair authorityKeyPair,
			X500NameBuilder issuerBuilder, X500NameBuilder subjectBuilder, SignAlgorithm signAlgorithim, Date notBefore,
			Date notAfter) throws Exception {

		JcaX509v3CertificateBuilder jcaX509v3CertificateBuilder = new JcaX509v3CertificateBuilder(issuerBuilder.build(),
				BigInteger.valueOf(IdUtil.increaseId()), notBefore, notAfter, subjectBuilder.build(), subjectKey);
		JcaX509ExtensionUtils jcaX509ExtensionUtils = new JcaX509ExtensionUtils();
		jcaX509v3CertificateBuilder.addExtension(org.bouncycastle.asn1.x509.Extension.subjectKeyIdentifier, false,
				jcaX509ExtensionUtils.createSubjectKeyIdentifier(subjectKey));
		jcaX509v3CertificateBuilder.addExtension(org.bouncycastle.asn1.x509.Extension.authorityKeyIdentifier, false,
				jcaX509ExtensionUtils.createAuthorityKeyIdentifier(authorityKeyPair.getPublic()));
		X509CertificateHolder x509CertificateHolder = jcaX509v3CertificateBuilder
				.build(new JcaContentSignerBuilder(signAlgorithim.name()).setProvider(ProviderType.BC.name())
						.build(authorityKeyPair.getPrivate()));
		return new JcaX509CertificateConverter().setProvider(ProviderType.BC.name())
				.getCertificate(x509CertificateHolder);
	}

	/**
	 * 存储 私钥以及证书链 至 密钥库中
	 * @param keyStore 秘钥库
	 * @param alias 存储 私钥和证书链的别名 ，可以理解整个keystore就是一个类似map的数据库，key就是我们自定义的alias
	 * @param password 秘钥库和私钥访问的密码
	 * @param privateKey  私钥
	 * @param chain 证书链
	 * @param out 输出流
	 * @throws Exception
	 */
	public static void storeKeyEntry(KeyStore keyStore, String alias, String password,  PrivateKey privateKey,X509Certificate chain[], OutputStream out)
			throws Exception {
		keyStore.setKeyEntry(alias, privateKey, password.toCharArray(), chain);
		keyStore.store(out, password.toCharArray());
	}

	/**
	 * 存储 私钥以及证书链 至 密钥库中
	 * @param keyStore 秘钥库
	 * @param alias 存储 私钥和证书链的别名 ，可以理解整个keystore就是一个类似map的数据库，key就是我们自定义的alias
	 * @param password 秘钥库和私钥访问的密码
	 * @param privateKey  私钥
	 * @param chain 证书链
	 * @return KeyStore 返回新的密钥库对象
	 * @throws Exception
	 */
	public static KeyStore storeKeyEntry(KeyStore keyStore, String alias, String password,  PrivateKey privateKey,X509Certificate chain[])
			throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		keyStore.setKeyEntry(alias, privateKey, password.toCharArray(), chain);
		keyStore.store(out, password.toCharArray());

		ByteArrayInputStream bin = new ByteArrayInputStream(out.toByteArray());
		KeyStore outKeystore = KeyStore.getInstance(keyStore.getType());
		outKeystore.load(bin, password.toCharArray());
		IOUtil.closeQuietly(bin);
		IOUtil.closeQuietly(out);
		return outKeystore;
	}

	/**
	 * 存储证书到秘钥库中
	 * @param keyStore 密钥库
	 * @param alias 证书存储的别名
	 * @param cert 证书对象
	 * @param password 密钥库访问密码
	 * @param out 密钥库输出流
	 * @throws Exception
	 */
	public static void storeX509Certificate(KeyStore keyStore, String alias,X509Certificate cert, String password,OutputStream out)
			throws Exception {
		keyStore.setCertificateEntry(alias, cert);
		keyStore.store(out, password.toCharArray());
	}


	/**
	 * 存储证书到秘钥库中
	 * @param keyStore 密钥库
	 * @param alias 证书存储的别名
	 * @param cert 证书对象
	 * @param password 密钥库访问密码
	 * @return KeyStore 返回新的密钥库对象
	 * @throws Exception
	 */
	public static KeyStore storeX509Certificate(KeyStore keyStore, String alias,X509Certificate cert, String password)
			throws Exception {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		keyStore.setCertificateEntry(alias, cert);
		keyStore.store(out, password.toCharArray());

		ByteArrayInputStream bin = new ByteArrayInputStream(out.toByteArray());
		KeyStore outKeystore = KeyStore.getInstance(keyStore.getType());
		outKeystore.load(bin, password.toCharArray());
		IOUtil.closeQuietly(bin);
		IOUtil.closeQuietly(out);
		return outKeystore;
	}


}
