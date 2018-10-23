package com.vip.vjtools.vjkit.enums;

/**
 * The algorithm names in this section can be specified when generating an instance of KeyPairGenerator.
 * @author haven.zhang on 2018/10/22.
 */
public enum KeyPairAlgorithms {
	DiffieHellman,
	DSA,//DSA密钥长度均为512～65536(64的整数倍)，默认长度为1024
	RSA,//RSA密钥长度均为512～65536(64的整数倍)，默认长度为1024
	EC;//EC秘钥长度为112-571，ECDSA：椭圆曲线数字签名算法  默认长度：256 特点：速度快，强度高，签名短。
}
