package com.vip.vjtools.vjkit.datamasking.strategy;

import com.vip.vjtools.vjkit.datamasking.MaskStrategy;

/**
 * 电子邮件掩码
 * 规则：
 * '@' 前面的字符串，首尾保留1位
 */
public class EmailMask extends PartMask implements MaskStrategy {

	@Override
	public String mask(String source, int[] params) {
		if (source == null || source.length() == 0) {
			return source;
		}

		int index = source.indexOf("@");
		//不是电子邮件格式，直接回退到默认的模式
		if (index <= 0) {
			return super.mask(source, new int[]{1, 0});
		}

		return super.mask(source.substring(0, index), new int[]{1}).concat(source.substring(index));

	}
}
