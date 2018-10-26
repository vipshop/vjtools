package com.vip.vjtools.vjkit.enums;

/**
 * 加密模式 
 * @author haven.zhang
 */
public enum EncryptedMode {

	/**
	 * 电码本模式（Electronic Codebook Book (ECB) 每一块都使用相同的秘钥进行加密
	 */
	ECB,
	/**
	 * 密码分组链接模式（Cipher Block Chaining (CBC)） 在第一个块中需要使用初始化向量IV
	 * 每个明文块先与前一个密文块进行异或后，再进行加密
	 */
	CBC,
	/**
	 * 填充密码块链接（PCBC，Propagating cipher-block chaining）或称为明文密码块链接（Plaintext cipher-block chaining）
	 */
	PCBC,
	/**
	 * 计算器模式（Counter (CTR)）
	 */
	CTR,

	/**
	 * 密码反馈模式（Cipher FeedBack (CFB)）
	 * CFB的加密工作分为两部分：
	 * 1.将一前段加密得到的密文再加密；
	 * 2.将第1步加密得到的数据与当前段的明文异或。
	 */
	CFB,
	/**
	 * 和 CFB 一样，但是使用 8 位模式（不推荐）
	 */
	CFB8,
	/**
	 * 输出反馈模式（Output FeedBack (OFB)）
	 */
	OFB,
	/**
	 * 和 OFB 一样，但是使用 8 位模式（不推荐）
	 */
	OFB8;
}
