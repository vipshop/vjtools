package com.vip.vjtools.vjkit.security;

import com.vip.vjtools.vjkit.base.annotation.NotNull;
import com.vip.vjtools.vjkit.enums.HmacAlgorithmType;
import org.bouncycastle.crypto.digests.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;

/**
 * 消息摘要组件
 * 注意: 需要参考本模块的POM文件，显式引用bouncycastle.
 * 并在初始化时加入BC作为安全提供者 Security.addProvider(new BouncyCastleProvider());
 * @author haven.zhang
 * */
public class DigestUtils {


	/**
	 * 生成HmacMD5的密钥
	 * @return byte[] 密钥
	 *
	 * */
	public static byte[] generateHmacMD5Key() throws Exception {
		//加入BouncyCastleProvider的支持
		Security.addProvider(new BouncyCastleProvider());
		//初始化KeyGenerator
		KeyGenerator keyGenerator = KeyGenerator.getInstance(HmacAlgorithmType.HMAC_MD5.value);
		//产生密钥
		SecretKey secretKey = keyGenerator.generateKey();
		//获取密钥
		return secretKey.getEncoded();
	}

	/**
	 * HmacMD5消息摘要
	 * @param data 待做摘要处理的数据
	 * @param key 密钥
	 * @return byte[] 消息摘要
	 * */
	public static byte[] hmacMD5(byte[] data, byte[] key) throws Exception {
		//加入BouncyCastleProvider的支持
		Security.addProvider(new BouncyCastleProvider());
		//还原密钥，因为密钥是以byte形式为消息传递算法所拥有
		SecretKey secretKey = new SecretKeySpec(key, HmacAlgorithmType.HMAC_MD5.value);
		//实例化Mac
		Mac mac = Mac.getInstance(secretKey.getAlgorithm());
		//初始化Mac
		mac.init(secretKey);
		//执行消息摘要处理
		return mac.doFinal(data);
	}

	/**
	 * HmacMD5Hex消息摘要
	 * @param data 待做消息摘要处理的数据
	 * @param key 密钥
	 * @return String 消息摘要 十六进制字符串
	 * */
	public static String hmacMD5Hex(byte[] data, byte[] key) throws Exception {
		//执行消息摘要处理
		byte[] b = hmacMD5(data, key);
		//做十六进制转换
		return new String(Hex.encode(b));
	}


	/**
	 * 产生HmacSHA1的密钥
	 * @return byte[] 密钥
	 *
	 * */
	public static byte[] generateHmacSHAKey() throws Exception {
		//初始化KeyGenerator
		KeyGenerator keyGenerator = KeyGenerator.getInstance(HmacAlgorithmType.HMAC_SHA1.value);
		//产生密钥
		SecretKey secretKey = keyGenerator.generateKey();
		//获取密钥
		return secretKey.getEncoded();
	}

	/**
	 * HmacSHA1消息摘要
	 * @param data 待做摘要处理的数据
	 * @param key 密钥
	 * @return byte[] 消息摘要
	 * */
	public static byte[] hmacSHA(byte[] data, byte[] key) throws Exception {
		//还原密钥，因为密钥是以byte形式为消息传递算法所拥有
		SecretKey secretKey = new SecretKeySpec(key, HmacAlgorithmType.HMAC_SHA1.value);
		//实例化Mac
		Mac mac = Mac.getInstance(secretKey.getAlgorithm());
		//初始化Mac
		mac.init(secretKey);
		//执行消息摘要处理
		return mac.doFinal(data);
	}

	/**
	 * HmacSHA1消息摘要
	 * @param data 待做摘要处理的数据
	 * @param key 密钥
	 * @return String 消息摘要 十六进制字符串
	 * */
	public static String hmacSHAHex(byte[] data, byte[] key) throws Exception {
		//执行消息摘要处理
		byte[] b = hmacSHA(data, key);
		//做十六进制转换
		return new String(Hex.encode(b));
	}

	/**
	 * 初始化HmacSHA256的密钥
	 * @return byte[] 密钥
	 *
	 * */
	public static byte[] generateHmacSHA256Key() throws Exception {
		Security.addProvider(new BouncyCastleProvider());
		//初始化KeyGenerator
		KeyGenerator keyGenerator = KeyGenerator.getInstance(HmacAlgorithmType.HMAC_SHA256.value);
		//产生密钥
		SecretKey secretKey = keyGenerator.generateKey();
		//获取密钥
		return secretKey.getEncoded();
	}

	/**
	 * HmacSHA256消息摘要
	 * @param data 待做摘要处理的数据
	 * @param key 密钥
	 * @return byte[] 消息摘要
	 * */
	public static byte[] hmacSHA256(byte[] data, byte[] key) throws Exception {
		//加入BouncyCastleProvider的支持
		Security.addProvider(new BouncyCastleProvider());
		//还原密钥，因为密钥是以byte形式为消息传递算法所拥有
		SecretKey secretKey = new SecretKeySpec(key, HmacAlgorithmType.HMAC_SHA256.value);
		//实例化Mac
		Mac mac = Mac.getInstance(secretKey.getAlgorithm());
		//初始化Mac
		mac.init(secretKey);
		//执行消息摘要处理
		return mac.doFinal(data);
	}

	/**
	 * HmacSHA256消息摘要
	 * @param data 待做摘要处理的数据
	 * @param key 密钥
	 * @return String 消息摘要 十六进制字符串
	 * */
	public static String hmacSHA256Hex(byte[] data, byte[] key) throws Exception {
		//执行消息摘要处理
		byte[] b = hmacSHA256(data, key);
		//做十六进制转换
		return new String(Hex.encode(b));
	}

	/**
	 * 初始化HmacSHA384的密钥
	 * @return byte[] 密钥
	 *
	 * */
	public static byte[] generateHmacSHA384Key() throws Exception {
		Security.addProvider(new BouncyCastleProvider());
		//初始化KeyGenerator
		KeyGenerator keyGenerator = KeyGenerator.getInstance(HmacAlgorithmType.HMAC_SHA384.value);
		//产生密钥
		SecretKey secretKey = keyGenerator.generateKey();
		//获取密钥
		return secretKey.getEncoded();
	}

	/**
	 * HmacSHA384消息摘要
	 * @param data 待做摘要处理的数据
	 * @param key 密钥
	 * @return byte[] 消息摘要
	 * */
	public static byte[] hmacSHA384(byte[] data, byte[] key) throws Exception {
		Security.addProvider(new BouncyCastleProvider());
		//还原密钥，因为密钥是以byte形式为消息传递算法所拥有
		SecretKey secretKey = new SecretKeySpec(key, HmacAlgorithmType.HMAC_SHA384.value);
		//实例化Mac
		Mac mac = Mac.getInstance(secretKey.getAlgorithm());
		//初始化Mac
		mac.init(secretKey);
		//执行消息摘要处理
		return mac.doFinal(data);
	}

	/**
	 * HmacSHA384消息摘要
	 * @param data 待做摘要处理的数据
	 * @param key 密钥
	 * @return String 消息摘要 十六进制字符串
	 * */
	public static String hmacSHA384Hex(byte[] data, byte[] key) throws Exception {
		//执行消息摘要处理
		byte[] b = hmacSHA384(data, key);
		//做十六进制转换
		return new String(Hex.encode(b));
	}

	/**
	 * 初始化HmacSHA512的密钥
	 * @return byte[] 密钥
	 * */
	public static byte[] generateHmacSHA512Key() throws Exception {
		//初始化KeyGenerator
		KeyGenerator keyGenerator = KeyGenerator.getInstance(HmacAlgorithmType.HMAC_SHA512.value);
		//产生密钥
		SecretKey secretKey = keyGenerator.generateKey();
		//获取密钥
		return secretKey.getEncoded();
	}

	/**
	 * HmacSHA512消息摘要
	 * @param data 待做摘要处理的数据
	 * @param key 密钥
	 * @return byte[] 消息摘要
	 * */
	public static byte[] hmacSHA512(byte[] data, byte[] key) throws Exception {
		//还原密钥，因为密钥是以byte形式为消息传递算法所拥有
		SecretKey secretKey = new SecretKeySpec(key, HmacAlgorithmType.HMAC_SHA512.value);
		//实例化Mac
		Mac mac = Mac.getInstance(secretKey.getAlgorithm());
		//初始化Mac
		mac.init(secretKey);
		//执行消息摘要处理
		return mac.doFinal(data);
	}

	/**
	 * HmacSHA512消息摘要
	 * @param data 待做摘要处理的数据
	 * @param key 密钥
	 * @return String 消息摘要 十六进制字符串
	 * */
	public static String hmacSHA512Hex(byte[] data, byte[] key) throws Exception {
		//执行消息摘要处理
		byte[] b = hmacSHA512(data, key);
		//做十六进制转换
		return new String(Hex.encode(b));
	}

	/**
	 * 初始化HmacMD2的密钥
	 * @return byte[] 密钥
	 * */
	public static byte[] generateHmacMD2Key() throws Exception {

		//加入BouncyCastleProvider的支持
		Security.addProvider(new BouncyCastleProvider());
		//初始化KeyGenerator
		KeyGenerator keyGenerator = KeyGenerator.getInstance(HmacAlgorithmType.HMAC_MD2.value);
		//产生密钥
		SecretKey secretKey = keyGenerator.generateKey();
		//获取密钥
		return secretKey.getEncoded();
	}

	/**
	 * HmacMD2消息摘要
	 * @param data 待做摘要处理的数据
	 * @param key 密钥
	 * @return byte[] 消息摘要
	 * */
	public static byte[] hmacMD2(byte[] data, byte[] key) throws Exception {
		//加入BouncyCastleProvider的支持
		Security.addProvider(new BouncyCastleProvider());
		//还原密钥，因为密钥是以byte形式为消息传递算法所拥有
		SecretKey secretKey = new SecretKeySpec(key, HmacAlgorithmType.HMAC_MD2.value);
		//实例化Mac
		Mac mac = Mac.getInstance(secretKey.getAlgorithm());
		//初始化Mac
		mac.init(secretKey);
		//执行消息摘要处理
		return mac.doFinal(data);
	}

	/**
	 * HmacMD2消息摘要
	 * @param data 待做摘要处理的数据
	 * @param key 密钥
	 * @return String 消息摘要 十六进制字符串
	 * */
	public static String hmacMD2Hex(byte[] data, byte[] key) throws Exception {
		//执行消息摘要处理
		byte[] b = hmacMD2(data, key);
		//做十六进制转换
		return new String(Hex.encode(b));
	}


	/**
	 * 初始化HmacMD2的密钥
	 * @return byte[] 密钥
	 * */
	public static byte[] generateHmacMD4Key() throws Exception {

		//加入BouncyCastleProvider的支持
		Security.addProvider(new BouncyCastleProvider());
		//初始化KeyGenerator
		KeyGenerator keyGenerator = KeyGenerator.getInstance(HmacAlgorithmType.HMAC_MD4.value);
		//产生密钥
		SecretKey secretKey = keyGenerator.generateKey();
		//获取密钥
		return secretKey.getEncoded();
	}

	/**
	 * HmacMD4消息摘要
	 * @param data 待做摘要处理的数据
	 * @param key 密钥
	 * @return byte[] 消息摘要
	 * */
	public static byte[] hmacMD4(byte[] data, byte[] key) throws Exception {
		//加入BouncyCastleProvider的支持
		Security.addProvider(new BouncyCastleProvider());
		//还原密钥，因为密钥是以byte形式为消息传递算法所拥有
		SecretKey secretKey = new SecretKeySpec(key, HmacAlgorithmType.HMAC_MD4.value);
		//实例化Mac
		Mac mac = Mac.getInstance(secretKey.getAlgorithm());
		//初始化Mac
		mac.init(secretKey);
		//执行消息摘要处理
		return mac.doFinal(data);
	}

	/**
	 * HmacMD4Hex消息摘要
	 * @param data 待做消息摘要处理的数据
	 * @param key 密钥
	 * @return byte[] 消息摘要
	 * */
	public static String hmacMD4Hex(byte[] data, byte[] key) throws Exception {
		//执行消息摘要处理
		byte[] b = hmacMD4(data, key);
		//做十六进制转换
		return new String(Hex.encode(b));
	}

	/**
	 * 初始化HmacSHA224的密钥
	 * @return byte[] 密钥
	 * */
	public static byte[] generateHmacSHA224Key() throws Exception {

		//加入BouncyCastleProvider的支持
		Security.addProvider(new BouncyCastleProvider());
		//初始化KeyGenerator
		KeyGenerator keyGenerator = KeyGenerator.getInstance(HmacAlgorithmType.HMAC_SHA224.value);
		//产生密钥
		SecretKey secretKey = keyGenerator.generateKey();
		//获取密钥
		return secretKey.getEncoded();
	}

	/**
	 * HmacSHA224消息摘要
	 * @param data 待做摘要处理的数据
	 * @param key 密钥
	 * @return byte[] 消息摘要
	 * */
	public static byte[] hmacSHA224(byte[] data, byte[] key) throws Exception {
		//加入BouncyCastleProvider的支持
		Security.addProvider(new BouncyCastleProvider());
		//还原密钥，因为密钥是以byte形式为消息传递算法所拥有
		SecretKey secretKey = new SecretKeySpec(key, HmacAlgorithmType.HMAC_SHA224.value);
		//实例化Mac
		Mac mac = Mac.getInstance(secretKey.getAlgorithm());
		//初始化Mac
		mac.init(secretKey);
		//执行消息摘要处理
		return mac.doFinal(data);
	}

	/**
	 * HmacSHA224Hex消息摘要
	 * @param data 待做消息摘要处理的数据
	 * @param key 密钥
	 * @return byte[] 消息摘要
	 * */
	public static String hmacSHA224Hex(byte[] data, byte[] key) throws Exception {
		//执行消息摘要处理
		byte[] b = hmacSHA224(data, key);
		//做十六进制转换
		return new String(Hex.encode(b));
	}

	/**
	 * sha224 摘要计算
	 * @param data 计算原文
	 * @return byte[] 消息摘要
	 */
	public static byte[] sha224(@NotNull byte[] data) {
		SHA224Digest dis244 = new SHA224Digest();
		dis244.update(data, 0, data.length);
		byte[] dis = new byte[dis244.getDigestSize()];
		dis244.doFinal(dis, 0);
		return dis;
	}

	/**
	 * sha224 摘要计算
	 * @param data 计算原文
	 * @return String 消息摘要
	 */
	public static String sha224Hex(@NotNull byte[] data) {
		//执行消息摘要处理
		byte[] b = sha224(data);
		//做十六进制转换
		return new String(Hex.encode(b));
	}

	/**
	 * sha256 摘要计算
	 * @param data 计算原文
	 * @return byte[] 消息摘要
	 */
	public static byte[] sha256(@NotNull byte[] data) {
		SHA256Digest dis256 = new SHA256Digest();
		dis256.update(data, 0, data.length);
		byte[] dis = new byte[dis256.getDigestSize()];
		dis256.doFinal(dis, 0);
		return dis;
	}

	/**
	 * sha256 摘要计算
	 * @param data 计算原文
	 * @return String 消息摘要
	 */
	public static String sha256Hex(@NotNull byte[] data) {
		//执行消息摘要处理
		byte[] b = sha256(data);
		//做十六进制转换
		return new String(Hex.encode(b));
	}

	/**
	 * sha384 摘要计算
	 * @param data 计算原文
	 * @return byte[] 消息摘要
	 */
	public static byte[] sha384(@NotNull byte[] data) {
		SHA384Digest dis384 = new SHA384Digest();
		dis384.update(data, 0, data.length);
		byte[] dis = new byte[dis384.getDigestSize()];
		dis384.doFinal(dis, 0);
		return dis;
	}

	/**
	 * sha384 摘要计算
	 * @param data 计算原文
	 * @return String 消息摘要
	 */
	public static String sha384Hex(@NotNull byte[] data) {
		//执行消息摘要处理
		byte[] b = sha384(data);
		//做十六进制转换
		return new String(Hex.encode(b));
	}

	/**
	 * sha512 摘要计算
	 * @param data 计算原文
	 * @return byte[] 消息摘要
	 */
	public static byte[] sha512(@NotNull byte[] data) {
		SHA512Digest dis512 = new SHA512Digest();
		dis512.update(data, 0, data.length);
		byte[] dis = new byte[dis512.getDigestSize()];
		dis512.doFinal(dis, 0);
		return dis;
	}

	/**
	 * sha512 摘要计算
	 * @param data 计算原文
	 * @return String 消息摘要
	 */
	public static String sha512Hex(@NotNull byte[] data) {
		//执行消息摘要处理
		byte[] b = sha512(data);
		//做十六进制转换
		return new String(Hex.encode(b));
	}

	/**
	 * sha1 摘要计算
	 * @param data 计算原文
	 * @return byte[] 消息摘要
	 */
	public static byte[] sha1(@NotNull byte[] data) {
		SHA1Digest dis1 = new SHA1Digest();
		dis1.update(data, 0, data.length);
		byte[] dis = new byte[dis1.getDigestSize()];
		dis1.doFinal(dis, 0);
		return dis;
	}

	/**
	 * sha1 摘要计算
	 * @param data 计算原文
	 * @return String 消息摘要
	 */
	public static String sha1Hex(@NotNull byte[] data) {
		//执行消息摘要处理
		byte[] b = sha1(data);
		//做十六进制转换
		return new String(Hex.encode(b));
	}

	/**
	 * md2 摘要计算
	 * @param data 计算原文
	 * @return byte[] 消息摘要
	 */
	public static byte[] md2(@NotNull byte[] data) {
		MD2Digest md2 = new MD2Digest();
		md2.update(data, 0, data.length);
		byte[] dis = new byte[md2.getDigestSize()];
		md2.doFinal(dis, 0);
		return dis;
	}

	/**
	 * md2 摘要计算
	 * @param data 计算原文
	 * @return String 消息摘要
	 */
	public static String md2Hex(@NotNull byte[] data) {
		//执行消息摘要处理
		byte[] b = md2(data);
		//做十六进制转换
		return new String(Hex.encode(b));
	}

	/**
	 * md4 摘要计算
	 * @param data 计算原文
	 * @return byte[] 消息摘要
	 */
	public static byte[] md4(@NotNull byte[] data) {
		MD4Digest md4 = new MD4Digest();
		md4.update(data, 0, data.length);
		byte[] dis = new byte[md4.getDigestSize()];
		md4.doFinal(dis, 0);
		return dis;
	}

	/**
	 * md4 摘要计算
	 * @param data 计算原文
	 * @return String 消息摘要
	 */
	public static String md4Hex(@NotNull byte[] data) {
		//执行消息摘要处理
		byte[] b = md4(data);
		//做十六进制转换
		return new String(Hex.encode(b));
	}

	/**
	 * md5 摘要计算
	 * @param data 计算原文
	 * @return byte[] 消息摘要
	 */
	public static byte[] md5(@NotNull byte[] data) {
		MD5Digest md5 = new MD5Digest();
		md5.update(data, 0, data.length);
		byte[] dis = new byte[md5.getDigestSize()];
		md5.doFinal(dis, 0);
		return dis;
	}

	/**
	 * md5 摘要计算
	 * @param data 计算原文
	 * @return String 消息摘要
	 */
	public static String md5Hex(@NotNull byte[] data) {
		//执行消息摘要处理
		byte[] b = md5(data);
		//做十六进制转换
		return new String(Hex.encode(b));
	}
}
