package com.vip.vjtools.vjkit.datamasking;

import com.vip.vjtools.vjkit.datamasking.strategy.EmailMask;
import com.vip.vjtools.vjkit.datamasking.strategy.HashMask;
import com.vip.vjtools.vjkit.datamasking.strategy.NameMask;
import com.vip.vjtools.vjkit.datamasking.strategy.PartMask;

/**
 * 脱敏类型
 */
public enum SensitiveType {

	Name(new NameMask()),//中文名
	Phone(new PartMask(), 3),//电话
	IDCard(new PartMask(), 5, 2),//身份证号
	BankCard(new PartMask(), 4, 2),//银行卡号
	Address(new PartMask(), 9, 0),//地址
	Email(new EmailMask()),//电子邮件
	Captcha(new PartMask(), 1),//验证码
	Passport(new PartMask(), 2),//护照/军官证
	Account(new PartMask(), 1),//账号
	Password(new PartMask(), 0),//密码
	/**
	 * 散列，这种掩码方式，用户可以手工计算Hash值来精确查询日志。
	 */
	Hash(new HashMask()),

	Default(new PartMask(), 1, 0); //缺省,只显示第一个字符串

	private MaskStrategy strategy;
	private int[] params;

	SensitiveType(MaskStrategy strategy, int... params) {
		this.strategy = strategy;
		this.params = params;
	}

	public MaskStrategy getStrategy() {
		return strategy;
	}


	public int[] getParams() {
		return params;
	}
}
