package com.vip.vjtools.vjkit.security;

import com.vip.vjtools.vjkit.enums.KeyPairAlgorithms;
import com.vip.vjtools.vjkit.enums.KeyStoreType;
import com.vip.vjtools.vjkit.enums.SignAlgorithm;
import com.vip.vjtools.vjkit.io.FileUtil;
import com.vip.vjtools.vjkit.text.EncodeUtil;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;

import java.io.File;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CertificateUtil 测试
 * @author  haven.zhang on 2018/10/25.
 */
public class CertificateUtilTest {


	@Test
	public void createCertificateTest() throws Exception {
		try {
			Security.addProvider(new BouncyCastleProvider());

			//产生非对称密钥对
			KeyPair keyPair = KeyUtil.generateKeyPair(KeyPairAlgorithms.RSA,1024);

			//证书请求者信息以及颁发者信息
			X500NameBuilder issuer = new X500NameBuilder(BCStyle.INSTANCE);
			issuer.addRDN(BCStyle.C, "CN");
			issuer.addRDN(BCStyle.O, "vip");
			issuer.addRDN(BCStyle.OU, "vip");
			issuer.addRDN(BCStyle.EmailAddress, "xxx@163.com");
			Date notBefore=new Date();
			X500NameBuilder subject = new X500NameBuilder(BCStyle.INSTANCE);
			subject.addRDN(BCStyle.C, "CN");
			subject.addRDN(BCStyle.O, "vip");
			subject.addRDN(BCStyle.L, "guangzhou");
			subject.addRDN(BCStyle.CN, "vip");
			subject.addRDN(BCStyle.EmailAddress, "yyyy@163.com");
			Date notAfter=new Date(notBefore.getTime()+9999999999L);
			//创建v1版本的证书
			X509Certificate x509 =CertificateUtil.createV1Certificate(keyPair.getPrivate(), keyPair.getPublic(), issuer,
					subject, SignAlgorithm.SHA1withRSA, notBefore, notAfter);
			System.out.println(EncodeUtil.encodeHex(x509.getEncoded()));
			System.out.println(x509.getVersion());
			FileUtil.writeByteArrayToFile(new File("target/test.cer"), x509.getEncoded());

			//创建v3版本的证书
			X509Certificate x509Certificate1 = CertificateUtil.createV3Certificate(keyPair.getPublic(),keyPair,issuer,subject,SignAlgorithm.SHA1withRSA,notBefore,notAfter);
			System.out.println(EncodeUtil.encodeHex(x509Certificate1.getEncoded()));
			System.out.println(x509Certificate1.getVersion());
			FileUtil.writeByteArrayToFile(new File("target/test2.cer"), x509Certificate1.getEncoded());
			assertThat(x509.getPublicKey().getEncoded()).isEqualTo(x509Certificate1.getPublicKey().getEncoded());

			//从本地加载证书
			X509Certificate x509Certificate2 = CertificateUtil.loadCertificate("target/test2.cer");
			assertThat(x509Certificate1.getEncoded()).isEqualTo(x509Certificate2.getEncoded());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void certificateConvertTest() throws Exception {
		Security.addProvider(new BouncyCastleProvider());
		//产生非对称密钥对
		KeyPair keyPair = KeyUtil.generateKeyPair(KeyPairAlgorithms.RSA,1024);
		//生成证书对象
		X509Certificate x509 = generateCertificate(keyPair);

		//der格式的证书文件转换成pem格式的证书
		String pem = CertificateUtil.generatePem(x509);
		System.out.println(pem);

		//pem格式的证书文件转换成der格式的证书
		X509Certificate x509Certificate2 = CertificateUtil.getCertificate(pem);
		System.out.println(EncodeUtil.encodeHex(x509.getEncoded()));
		System.out.println(EncodeUtil.encodeHex(x509Certificate2.getEncoded()));
		assertThat(x509Certificate2.getEncoded()).isEqualTo(x509.getEncoded());

	}


	@Test
	public void privateKeyConvert2PemTest() throws Exception {
		try {
			Security.addProvider(new BouncyCastleProvider());
			//产生非对称密钥对
			KeyPair keyPair = KeyUtil.generateKeyPair(KeyPairAlgorithms.RSA, 1024);

			//生成PKCS8格式的pem私钥文件，需要密码访问
			String pemStr = CertificateUtil.generatePKCS8Pem(keyPair.getPrivate(), "111111",
					NISTObjectIdentifiers.id_aes256_CBC);
			System.out.println(pemStr);
			//从pem格式的私钥文件中读取私钥
			PrivateKey privateKey = CertificateUtil.getPrivateKey(pemStr, "111111");
			System.out.println(EncodeUtil.encodeHex(privateKey.getEncoded()));
			System.out.println(EncodeUtil.encodeHex(keyPair.getPrivate().getEncoded()));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void publicKey2pemTest() throws Exception {
		try {
			Security.addProvider(new BouncyCastleProvider());
			//产生非对称密钥对
			KeyPair keyPair = KeyUtil.generateKeyPair(KeyPairAlgorithms.RSA, 1024);

			//生成pem格式的公钥
			String publicKeypem = CertificateUtil.generatPem(keyPair.getPublic());
			System.out.println(publicKeypem);
			//生成pem格式的私钥
			String privateKeypem = CertificateUtil.generatPem(keyPair.getPrivate());
			System.out.println(privateKeypem);

			//从pem文件内容中读取公钥
			PublicKey publicKey = CertificateUtil.getPublicKey(publicKeypem, KeyPairAlgorithms.RSA);
			assertThat(publicKey.getEncoded()).isEqualTo(keyPair.getPublic().getEncoded());

			//从pem文件内容中读取私钥
			PrivateKey privateKey = CertificateUtil.getPrivateKey(privateKeypem);
			assertThat(privateKey.getEncoded()).isEqualTo(keyPair.getPrivate().getEncoded());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void csrTest() throws Exception {

		try {
			Security.addProvider(new BouncyCastleProvider());
			//产生非对称密钥对
			KeyPair keyPair = KeyUtil.generateKeyPair(KeyPairAlgorithms.RSA, 1024);
			String subject = "CN=country, ST=state,L=Locality,OU=OrganizationUnit,O=Organization";

			//生成证书请求csr
			String csr = CertificateUtil.generateCsr(subject, keyPair.getPublic(), keyPair.getPrivate(),
					SignAlgorithm.SHA1withRSA);
			System.out.println(csr);

			//从证书请求内容中获取公钥
			PublicKey publicKey = CertificateUtil.getPublicKeyFromCsr(csr, KeyPairAlgorithms.RSA);
			//		assertThat(publicKey.getEncoded()).isEqualTo(keyPair.getPublic().getEncoded());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void pfxTest() throws Exception {
		try {
			Security.addProvider(new BouncyCastleProvider());
			//产生非对称密钥对
			KeyPair keyPair = KeyUtil.generateKeyPair(KeyPairAlgorithms.RSA, 1024);
			//生成证书对象
			X509Certificate x509 = generateCertificate(keyPair);

			CertificateUtil.generatePfx("alias",keyPair.getPrivate(),"111111",new Certificate[]{x509},"target/test.pfx");
			CertificateUtil.generatePfx2("alias", keyPair.getPrivate(), "111111", new Certificate[]{x509},
					"target/test2.pfx");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Test
	public void keyStoreTest() throws Exception {
		try {
			Security.addProvider(new BouncyCastleProvider());
			//产生非对称密钥对
			KeyPair keyPair = KeyUtil.generateKeyPair(KeyPairAlgorithms.RSA, 1024);

			//生成一个空的秘钥库文件
			CertificateUtil.createEmptyKeystore("target/testKeystore.p12", "111111", KeyStoreType.PKCS12);
			//加载本地密钥库文件
			KeyStore keyStore = CertificateUtil.loadKeyStore("target/testKeystore.p12","111111", KeyStoreType.PKCS12);

			//存储证书到秘钥库
			X509Certificate x509Certificate =  generateCertificate(keyPair);
			KeyStore keyStore2 = CertificateUtil.storeX509Certificate(keyStore, "test", x509Certificate, "111111");
			//从秘钥库中读取证书
			X509Certificate x509Certificate2 = (X509Certificate) CertificateUtil.getCertificate(keyStore2,"test");
			System.out.println(EncodeUtil.encodeHex(x509Certificate2.getEncoded()));
			assertThat(x509Certificate2.getEncoded()).isEqualTo(x509Certificate.getEncoded());

			//存储私钥和证书链
			KeyStore keyStore3 = CertificateUtil.storeKeyEntry(keyStore,"test2","111111",keyPair.getPrivate(),new X509Certificate[]{x509Certificate});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * 生成用于单元测试的x509证书
	 * @return
	 * @throws Exception
	 */
	private X509Certificate generateCertificate(KeyPair keyPair) throws Exception {
		//证书请求者信息以及颁发者信息
		X500NameBuilder issuer = new X500NameBuilder(BCStyle.INSTANCE);
		issuer.addRDN(BCStyle.C, "CN");
		issuer.addRDN(BCStyle.O, "vip");
		issuer.addRDN(BCStyle.OU, "vip");
		issuer.addRDN(BCStyle.EmailAddress, "xxx@163.com");
		Date notBefore=new Date();
		X500NameBuilder subject = new X500NameBuilder(BCStyle.INSTANCE);
		subject.addRDN(BCStyle.C, "CN");
		subject.addRDN(BCStyle.O, "vip");
		subject.addRDN(BCStyle.L, "guangzhou");
		subject.addRDN(BCStyle.CN, "vip");
		subject.addRDN(BCStyle.EmailAddress, "yyyy@163.com");
		Date notAfter=new Date(notBefore.getTime()+9999999999L);
		//创建v1版本的证书
		X509Certificate x509 =CertificateUtil.createV1Certificate(keyPair.getPrivate(), keyPair.getPublic(), issuer,
				subject, SignAlgorithm.SHA1withRSA, notBefore, notAfter);
		return x509;
	}




}
