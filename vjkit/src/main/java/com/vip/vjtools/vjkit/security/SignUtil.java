package com.vip.vjtools.vjkit.security;

import com.vip.vjtools.vjkit.enums.KeyFactoryAlgorithms;
import com.vip.vjtools.vjkit.enums.SignAlgorithm;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

/**
 * 签名工具类，提供常见的算法：
 * MD2withRSA,
 * MD5withRSA,
 * SHA1withRSA,
 * SHA224withRSA,
 * SHA256withRSA,
 * SHA384withRSA,
 * SHA512withRSA,
 * RIPEMD128withRSA,
 * RIPEMD160withRSA,
 * SHA1withDSA,
 * SHA224withDSA,
 * SHA256withDSA,
 * SHA384withDSA,
 * SHA512withDSA,
 * NONEwithECDSA,
 * RIPEMD160withECDSA,
 * SHA1withECDSA,
 * SHA256withECDSA,
 * SHA384withECDSA,
 * SHA512withECDSA;
 * @author haven.zhang on 2018/10/20.
 */
public class SignUtil {


	/**
	 * 使用私钥对消息进行签名
	 * @param privateKey 私钥
	 * @param msgBytes 原文
	 * @param signAlgorithm 签名算法
	 * @return byte[] 签名结果
	 * @throws Exception
	 */
	public static byte[] sign(PrivateKey privateKey, byte[] msgBytes, SignAlgorithm signAlgorithm) throws Exception {
		Signature sign = Signature.getInstance(signAlgorithm.name());
		sign.initSign(privateKey);
		sign.update(msgBytes);
		byte signed[] = sign.sign();
		return signed;

	}

	/**
	 * 验证签名
	 * @param publicKey 公钥
	 * @param signedBytes 签名信息
	 * @param orgMsgBytes 签名原文
	 * @param signAlgorithm 签名算法
	 * @return 验证结果，true通过，fasle 不通过
	 * @throws Exception
	 */
	public static boolean verify(PublicKey publicKey, byte[] signedBytes, byte[] orgMsgBytes, SignAlgorithm signAlgorithm)
			throws Exception {
		Signature signature = Signature.getInstance(signAlgorithm.name());
		signature.initVerify(publicKey);
		signature.update(orgMsgBytes);
		return signature.verify(signedBytes);
	}

	/**
	 * 签名
	 * @param msgBytes 待签名数据
	 * @param privateKey 密钥
	 * @return byte[] 数字签名
	 * */
	public static byte[] sign(byte[] msgBytes, byte[] privateKey, SignAlgorithm signAlgorithm) throws Exception {
		//生成私钥
		PrivateKey priKey = KeyUtil.getPrivateKey(privateKey, convert2KeyAlgorithm(signAlgorithm));
		//实例化Signature
		Signature signature = Signature.getInstance(signAlgorithm.name());
		//初始化Signature
		signature.initSign(priKey);
		//更新
		signature.update(msgBytes);
		byte signed[] = signature.sign();
		return signed;
	}

	/**
	 * 校验数字签名
	 * @param data 待校验数据
	 * @param publicKey 公钥
	 * @param sign 数字签名
	 * @return boolean 校验成功返回true，失败返回false
	 * */
	public static boolean verify(byte[] data, byte[] publicKey, byte[] sign, SignAlgorithm signAlgorithm) throws Exception {

		//产生公钥
		PublicKey pubKey = KeyUtil.getPublicKey(publicKey, convert2KeyAlgorithm(signAlgorithm));
		//实例化Signature
		Signature signature = Signature.getInstance(signAlgorithm.name());
		//初始化Signature
		signature.initVerify(pubKey);
		//更新
		signature.update(data);
		//验证
		return signature.verify(sign);
	}

	/**
	 * 根据签名算法 获取秘钥算法
	 * @param signAlgorithm 签名算法
	 * @return KeyFactoryAlgorithms
	 */
	public static KeyFactoryAlgorithms convert2KeyAlgorithm(SignAlgorithm signAlgorithm){
		KeyFactoryAlgorithms keyFactoryAlgorithms = null;
		switch (signAlgorithm.name()){
			case "RSA":
				keyFactoryAlgorithms = KeyFactoryAlgorithms.RSA;
				break;
			case "DSA":
				keyFactoryAlgorithms = KeyFactoryAlgorithms.DSA;
				break;
			case "ECDSA":
				keyFactoryAlgorithms = KeyFactoryAlgorithms.EC;
				break;
			default:
				keyFactoryAlgorithms = KeyFactoryAlgorithms.RSA;
				break;
		}

		return keyFactoryAlgorithms;
	}
}
