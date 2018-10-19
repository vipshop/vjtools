package com.vip.vjtools.vjkit.security;

import com.vip.vjtools.vjkit.base.annotation.NotNull;
import org.bouncycastle.crypto.digests.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;

/**
 * ��ϢժҪ���
 * ע��: ��Ҫ�ο���ģ���POM�ļ�����ʽ����bouncycastle.
 * @author haven.zhang
 * */
public class DigestUtils {

	/**
	 * �㷨����
	 */
	enum HmacAlgorithmType {
		/**
		 * HmacSHA1ժҪ�㷨
		 */
		HMAC_SHA1("HmacSHA1"),
		HMAC_SHA224("HmacSHA224"),
		HMAC_SHA256("HmacSHA256"),
		HMAC_SHA384("HmacSHA384"),
		HMAC_SHA512("HmacSHA512"),
		HMAC_MD2("HmacMD2"),
		HMAC_MD4("HmacMD4"),
		HMAC_MD5("HmacMD5");
		String value;

		HmacAlgorithmType(String value) {
			this.value = value;
		}
	}

	/**
	 * ����HmacMD5����Կ
	 * @return byte[] ��Կ
	 *
	 * */
	public static byte[] generateHmacMD5Key() throws Exception {
		//����BouncyCastleProvider��֧��
		Security.addProvider(new BouncyCastleProvider());
		//��ʼ��KeyGenerator
		KeyGenerator keyGenerator = KeyGenerator.getInstance(HmacAlgorithmType.HMAC_MD5.value);
		//������Կ
		SecretKey secretKey = keyGenerator.generateKey();
		//��ȡ��Կ
		return secretKey.getEncoded();
	}

	/**
	 * HmacMD5��ϢժҪ
	 * @param data ����ժҪ���������
	 * @param key ��Կ
	 * @return byte[] ��ϢժҪ
	 * */
	public static byte[] hmacMD5(byte[] data, byte[] key) throws Exception {
		//����BouncyCastleProvider��֧��
		Security.addProvider(new BouncyCastleProvider());
		//��ԭ��Կ����Ϊ��Կ����byte��ʽΪ��Ϣ�����㷨��ӵ��
		SecretKey secretKey = new SecretKeySpec(key, HmacAlgorithmType.HMAC_MD5.value);
		//ʵ����Mac
		Mac mac = Mac.getInstance(secretKey.getAlgorithm());
		//��ʼ��Mac
		mac.init(secretKey);
		//ִ����ϢժҪ����
		return mac.doFinal(data);
	}

	/**
	 * HmacMD5Hex��ϢժҪ
	 * @param data ������ϢժҪ���������
	 * @param key ��Կ
	 * @return String ��ϢժҪ ʮ�������ַ���
	 * */
	public static String hmacMD5Hex(byte[] data, byte[] key) throws Exception {
		//ִ����ϢժҪ����
		byte[] b = hmacMD5(data, key);
		//��ʮ������ת��
		return new String(Hex.encode(b));
	}


	/**
	 * ����HmacSHA1����Կ
	 * @return byte[] ��Կ
	 *
	 * */
	public static byte[] generateHmacSHAKey() throws Exception {
		//��ʼ��KeyGenerator
		KeyGenerator keyGenerator = KeyGenerator.getInstance(HmacAlgorithmType.HMAC_SHA1.value);
		//������Կ
		SecretKey secretKey = keyGenerator.generateKey();
		//��ȡ��Կ
		return secretKey.getEncoded();
	}

	/**
	 * HmacSHA1��ϢժҪ
	 * @param data ����ժҪ���������
	 * @param key ��Կ
	 * @return byte[] ��ϢժҪ
	 * */
	public static byte[] hmacSHA(byte[] data, byte[] key) throws Exception {
		//��ԭ��Կ����Ϊ��Կ����byte��ʽΪ��Ϣ�����㷨��ӵ��
		SecretKey secretKey = new SecretKeySpec(key, HmacAlgorithmType.HMAC_SHA1.value);
		//ʵ����Mac
		Mac mac = Mac.getInstance(secretKey.getAlgorithm());
		//��ʼ��Mac
		mac.init(secretKey);
		//ִ����ϢժҪ����
		return mac.doFinal(data);
	}

	/**
	 * HmacSHA1��ϢժҪ
	 * @param data ����ժҪ���������
	 * @param key ��Կ
	 * @return String ��ϢժҪ ʮ�������ַ���
	 * */
	public static String hmacSHAHex(byte[] data, byte[] key) throws Exception {
		//ִ����ϢժҪ����
		byte[] b = hmacSHA(data, key);
		//��ʮ������ת��
		return new String(Hex.encode(b));
	}

	/**
	 * ��ʼ��HmacSHA256����Կ
	 * @return byte[] ��Կ
	 *
	 * */
	public static byte[] generateHmacSHA256Key() throws Exception {
		Security.addProvider(new BouncyCastleProvider());
		//��ʼ��KeyGenerator
		KeyGenerator keyGenerator = KeyGenerator.getInstance(HmacAlgorithmType.HMAC_SHA256.value);
		//������Կ
		SecretKey secretKey = keyGenerator.generateKey();
		//��ȡ��Կ
		return secretKey.getEncoded();
	}

	/**
	 * HmacSHA256��ϢժҪ
	 * @param data ����ժҪ���������
	 * @param key ��Կ
	 * @return byte[] ��ϢժҪ
	 * */
	public static byte[] hmacSHA256(byte[] data, byte[] key) throws Exception {
		//����BouncyCastleProvider��֧��
		Security.addProvider(new BouncyCastleProvider());
		//��ԭ��Կ����Ϊ��Կ����byte��ʽΪ��Ϣ�����㷨��ӵ��
		SecretKey secretKey = new SecretKeySpec(key, HmacAlgorithmType.HMAC_SHA256.value);
		//ʵ����Mac
		Mac mac = Mac.getInstance(secretKey.getAlgorithm());
		//��ʼ��Mac
		mac.init(secretKey);
		//ִ����ϢժҪ����
		return mac.doFinal(data);
	}

	/**
	 * HmacSHA256��ϢժҪ
	 * @param data ����ժҪ���������
	 * @param key ��Կ
	 * @return String ��ϢժҪ ʮ�������ַ���
	 * */
	public static String hmacSHA256Hex(byte[] data, byte[] key) throws Exception {
		//ִ����ϢժҪ����
		byte[] b = hmacSHA256(data, key);
		//��ʮ������ת��
		return new String(Hex.encode(b));
	}

	/**
	 * ��ʼ��HmacSHA384����Կ
	 * @return byte[] ��Կ
	 *
	 * */
	public static byte[] generateHmacSHA384Key() throws Exception {
		Security.addProvider(new BouncyCastleProvider());
		//��ʼ��KeyGenerator
		KeyGenerator keyGenerator = KeyGenerator.getInstance(HmacAlgorithmType.HMAC_SHA384.value);
		//������Կ
		SecretKey secretKey = keyGenerator.generateKey();
		//��ȡ��Կ
		return secretKey.getEncoded();
	}

	/**
	 * HmacSHA384��ϢժҪ
	 * @param data ����ժҪ���������
	 * @param key ��Կ
	 * @return byte[] ��ϢժҪ
	 * */
	public static byte[] hmacSHA384(byte[] data, byte[] key) throws Exception {
		Security.addProvider(new BouncyCastleProvider());
		//��ԭ��Կ����Ϊ��Կ����byte��ʽΪ��Ϣ�����㷨��ӵ��
		SecretKey secretKey = new SecretKeySpec(key, HmacAlgorithmType.HMAC_SHA384.value);
		//ʵ����Mac
		Mac mac = Mac.getInstance(secretKey.getAlgorithm());
		//��ʼ��Mac
		mac.init(secretKey);
		//ִ����ϢժҪ����
		return mac.doFinal(data);
	}

	/**
	 * HmacSHA384��ϢժҪ
	 * @param data ����ժҪ���������
	 * @param key ��Կ
	 * @return String ��ϢժҪ ʮ�������ַ���
	 * */
	public static String hmacSHA384Hex(byte[] data, byte[] key) throws Exception {
		//ִ����ϢժҪ����
		byte[] b = hmacSHA384(data, key);
		//��ʮ������ת��
		return new String(Hex.encode(b));
	}

	/**
	 * ��ʼ��HmacSHA512����Կ
	 * @return byte[] ��Կ
	 *
	 * */
	public static byte[] generateHmacSHA512Key() throws Exception {
		//��ʼ��KeyGenerator
		KeyGenerator keyGenerator = KeyGenerator.getInstance(HmacAlgorithmType.HMAC_SHA512.value);
		//������Կ
		SecretKey secretKey = keyGenerator.generateKey();
		//��ȡ��Կ
		return secretKey.getEncoded();
	}

	/**
	 * HmacSHA512��ϢժҪ
	 * @param data ����ժҪ���������
	 * @param key ��Կ
	 * @return byte[] ��ϢժҪ
	 * */
	public static byte[] hmacSHA512(byte[] data, byte[] key) throws Exception {
		//��ԭ��Կ����Ϊ��Կ����byte��ʽΪ��Ϣ�����㷨��ӵ��
		SecretKey secretKey = new SecretKeySpec(key, HmacAlgorithmType.HMAC_SHA512.value);
		//ʵ����Mac
		Mac mac = Mac.getInstance(secretKey.getAlgorithm());
		//��ʼ��Mac
		mac.init(secretKey);
		//ִ����ϢժҪ����
		return mac.doFinal(data);
	}

	/**
	 * HmacSHA512��ϢժҪ
	 * @param data ����ժҪ���������
	 * @param key ��Կ
	 * @return String ��ϢժҪ ʮ�������ַ���
	 * */
	public static String hmacSHA512Hex(byte[] data, byte[] key) throws Exception {
		//ִ����ϢժҪ����
		byte[] b = hmacSHA512(data, key);
		//��ʮ������ת��
		return new String(Hex.encode(b));
	}

	/**
	 * ��ʼ��HmacMD2����Կ
	 * @return byte[] ��Կ
	 * */
	public static byte[] generateHmacMD2Key() throws Exception {

		//����BouncyCastleProvider��֧��
		Security.addProvider(new BouncyCastleProvider());
		//��ʼ��KeyGenerator
		KeyGenerator keyGenerator = KeyGenerator.getInstance(HmacAlgorithmType.HMAC_MD2.value);
		//������Կ
		SecretKey secretKey = keyGenerator.generateKey();
		//��ȡ��Կ
		return secretKey.getEncoded();
	}

	/**
	 * HmacMD2��ϢժҪ
	 * @param data ����ժҪ���������
	 * @param key ��Կ
	 * @return byte[] ��ϢժҪ
	 * */
	public static byte[] hmacMD2(byte[] data, byte[] key) throws Exception {
		//����BouncyCastleProvider��֧��
		Security.addProvider(new BouncyCastleProvider());
		//��ԭ��Կ����Ϊ��Կ����byte��ʽΪ��Ϣ�����㷨��ӵ��
		SecretKey secretKey = new SecretKeySpec(key, HmacAlgorithmType.HMAC_MD2.value);
		//ʵ����Mac
		Mac mac = Mac.getInstance(secretKey.getAlgorithm());
		//��ʼ��Mac
		mac.init(secretKey);
		//ִ����ϢժҪ����
		return mac.doFinal(data);
	}

	/**
	 * HmacMD2��ϢժҪ
	 * @param data ����ժҪ���������
	 * @param key ��Կ
	 * @return String ��ϢժҪ ʮ�������ַ���
	 * */
	public static String hmacMD2Hex(byte[] data, byte[] key) throws Exception {
		//ִ����ϢժҪ����
		byte[] b = hmacMD2(data, key);
		//��ʮ������ת��
		return new String(Hex.encode(b));
	}


	/**
	 * ��ʼ��HmacMD2����Կ
	 * @return byte[] ��Կ
	 * */
	public static byte[] generateHmacMD4Key() throws Exception {

		//����BouncyCastleProvider��֧��
		Security.addProvider(new BouncyCastleProvider());
		//��ʼ��KeyGenerator
		KeyGenerator keyGenerator = KeyGenerator.getInstance(HmacAlgorithmType.HMAC_MD4.value);
		//������Կ
		SecretKey secretKey = keyGenerator.generateKey();
		//��ȡ��Կ
		return secretKey.getEncoded();
	}

	/**
	 * HmacMD4��ϢժҪ
	 * @param data ����ժҪ���������
	 * @param key ��Կ
	 * @return byte[] ��ϢժҪ
	 * */
	public static byte[] hmacMD4(byte[] data, byte[] key) throws Exception {
		//����BouncyCastleProvider��֧��
		Security.addProvider(new BouncyCastleProvider());
		//��ԭ��Կ����Ϊ��Կ����byte��ʽΪ��Ϣ�����㷨��ӵ��
		SecretKey secretKey = new SecretKeySpec(key, HmacAlgorithmType.HMAC_MD4.value);
		//ʵ����Mac
		Mac mac = Mac.getInstance(secretKey.getAlgorithm());
		//��ʼ��Mac
		mac.init(secretKey);
		//ִ����ϢժҪ����
		return mac.doFinal(data);
	}

	/**
	 * HmacMD4Hex��ϢժҪ
	 * @param data ������ϢժҪ���������
	 * @param key ��Կ
	 * @return byte[] ��ϢժҪ
	 * */
	public static String hmacMD4Hex(byte[] data, byte[] key) throws Exception {
		//ִ����ϢժҪ����
		byte[] b = hmacMD4(data, key);
		//��ʮ������ת��
		return new String(Hex.encode(b));
	}

	/**
	 * ��ʼ��HmacSHA224����Կ
	 * @return byte[] ��Կ
	 * */
	public static byte[] generateHmacSHA224Key() throws Exception {

		//����BouncyCastleProvider��֧��
		Security.addProvider(new BouncyCastleProvider());
		//��ʼ��KeyGenerator
		KeyGenerator keyGenerator = KeyGenerator.getInstance(HmacAlgorithmType.HMAC_SHA224.value);
		//������Կ
		SecretKey secretKey = keyGenerator.generateKey();
		//��ȡ��Կ
		return secretKey.getEncoded();
	}

	/**
	 * HmacSHA224��ϢժҪ
	 * @param data ����ժҪ���������
	 * @param key ��Կ
	 * @return byte[] ��ϢժҪ
	 * */
	public static byte[] hmacSHA224(byte[] data, byte[] key) throws Exception {
		//����BouncyCastleProvider��֧��
		Security.addProvider(new BouncyCastleProvider());
		//��ԭ��Կ����Ϊ��Կ����byte��ʽΪ��Ϣ�����㷨��ӵ��
		SecretKey secretKey = new SecretKeySpec(key, HmacAlgorithmType.HMAC_SHA224.value);
		//ʵ����Mac
		Mac mac = Mac.getInstance(secretKey.getAlgorithm());
		//��ʼ��Mac
		mac.init(secretKey);
		//ִ����ϢժҪ����
		return mac.doFinal(data);
	}

	/**
	 * HmacSHA224Hex��ϢժҪ
	 * @param data ������ϢժҪ���������
	 * @param key ��Կ
	 * @return byte[] ��ϢժҪ
	 * */
	public static String hmacSHA224Hex(byte[] data, byte[] key) throws Exception {
		//ִ����ϢժҪ����
		byte[] b = hmacSHA224(data, key);
		//��ʮ������ת��
		return new String(Hex.encode(b));
	}

	/**
	 * sha224 ժҪ����
	 * @param data
	 * @return byte[] ��ϢժҪ
	 */
	public static byte[] sha224(@NotNull byte[] data) {
		SHA224Digest dis244 = new SHA224Digest();
		dis244.update(data, 0, data.length);
		byte[] dis = new byte[dis244.getDigestSize()];
		dis244.doFinal(dis, 0);
		return dis;
	}

	/**
	 * sha224 ժҪ����
	 * @param data
	 * @return String ��ϢժҪ
	 */
	public static String sha224Hex(@NotNull byte[] data) {
		//ִ����ϢժҪ����
		byte[] b = sha224(data);
		//��ʮ������ת��
		return new String(Hex.encode(b));
	}

	/**
	 * sha256 ժҪ����
	 * @param data
	 * @return byte[] ��ϢժҪ
	 */
	public static byte[] sha256(@NotNull byte[] data) {
		SHA256Digest dis256 = new SHA256Digest();
		dis256.update(data, 0, data.length);
		byte[] dis = new byte[dis256.getDigestSize()];
		dis256.doFinal(dis, 0);
		return dis;
	}

	/**
	 * sha256 ժҪ����
	 * @param data
	 * @return String ��ϢժҪ
	 */
	public static String sha256Hex(@NotNull byte[] data) {
		//ִ����ϢժҪ����
		byte[] b = sha256(data);
		//��ʮ������ת��
		return new String(Hex.encode(b));
	}

	/**
	 * sha384 ժҪ����
	 * @param data
	 * @return byte[] ��ϢժҪ
	 */
	public static byte[] sha384(@NotNull byte[] data) {
		SHA384Digest dis384 = new SHA384Digest();
		dis384.update(data, 0, data.length);
		byte[] dis = new byte[dis384.getDigestSize()];
		dis384.doFinal(dis, 0);
		return dis;
	}

	/**
	 * sha384 ժҪ����
	 * @param data
	 * @return String ��ϢժҪ
	 */
	public static String sha384Hex(@NotNull byte[] data) {
		//ִ����ϢժҪ����
		byte[] b = sha384(data);
		//��ʮ������ת��
		return new String(Hex.encode(b));
	}

	/**
	 * sha512 ժҪ����
	 * @param data
	 * @return byte[] ��ϢժҪ
	 */
	public static byte[] sha512(@NotNull byte[] data) {
		SHA512Digest dis512 = new SHA512Digest();
		dis512.update(data, 0, data.length);
		byte[] dis = new byte[dis512.getDigestSize()];
		dis512.doFinal(dis, 0);
		return dis;
	}

	/**
	 * sha512 ժҪ����
	 * @param data
	 * @return String ��ϢժҪ
	 */
	public static String sha512Hex(@NotNull byte[] data) {
		//ִ����ϢժҪ����
		byte[] b = sha512(data);
		//��ʮ������ת��
		return new String(Hex.encode(b));
	}

	/**
	 * sha1 ժҪ����
	 * @param data
	 * @return byte[] ��ϢժҪ
	 */
	public static byte[] sha1(@NotNull byte[] data) {
		SHA1Digest dis1 = new SHA1Digest();
		dis1.update(data, 0, data.length);
		byte[] dis = new byte[dis1.getDigestSize()];
		dis1.doFinal(dis, 0);
		return dis;
	}

	/**
	 * sha1 ժҪ����
	 * @param data
	 * @return String ��ϢժҪ
	 */
	public static String sha1Hex(@NotNull byte[] data) {
		//ִ����ϢժҪ����
		byte[] b = sha1(data);
		//��ʮ������ת��
		return new String(Hex.encode(b));
	}

	/**
	 * md2 ժҪ����
	 * @param data
	 * @return byte[] ��ϢժҪ
	 */
	public static byte[] md2(@NotNull byte[] data) {
		MD2Digest md2 = new MD2Digest();
		md2.update(data, 0, data.length);
		byte[] dis = new byte[md2.getDigestSize()];
		md2.doFinal(dis, 0);
		return dis;
	}

	/**
	 * md2 ժҪ����
	 * @param data
	 * @return String ��ϢժҪ
	 */
	public static String md2Hex(@NotNull byte[] data) {
		//ִ����ϢժҪ����
		byte[] b = md2(data);
		//��ʮ������ת��
		return new String(Hex.encode(b));
	}

	/**
	 * md4 ժҪ����
	 * @param data
	 * @return byte[] ��ϢժҪ
	 */
	public static byte[] md4(@NotNull byte[] data) {
		MD4Digest md4 = new MD4Digest();
		md4.update(data, 0, data.length);
		byte[] dis = new byte[md4.getDigestSize()];
		md4.doFinal(dis, 0);
		return dis;
	}

	/**
	 * md4 ժҪ����
	 * @param data
	 * @return String ��ϢժҪ
	 */
	public static String md4Hex(@NotNull byte[] data) {
		//ִ����ϢժҪ����
		byte[] b = md4(data);
		//��ʮ������ת��
		return new String(Hex.encode(b));
	}

	/**
	 * md5 ժҪ����
	 * @param data
	 * @return byte[] ��ϢժҪ
	 */
	public static byte[] md5(@NotNull byte[] data) {
		MD5Digest md5 = new MD5Digest();
		md5.update(data, 0, data.length);
		byte[] dis = new byte[md5.getDigestSize()];
		md5.doFinal(dis, 0);
		return dis;
	}

	/**
	 * md5 ժҪ����
	 * @param data
	 * @return String ��ϢժҪ
	 */
	public static String md5Hex(@NotNull byte[] data) {
		//ִ����ϢժҪ����
		byte[] b = md5(data);
		//��ʮ������ת��
		return new String(Hex.encode(b));
	}
}
