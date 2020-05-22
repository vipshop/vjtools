package com.vip.vjtools.vjkit.datamasking.strategy;

import com.vip.vjtools.vjkit.datamasking.MaskStrategy;

import java.util.HashMap;
import java.util.Map;

/**
 * 部分掩码
 * 可以支持前后保留n个字符串
 */
public class PartMask implements MaskStrategy {

	private static Map<Integer, String> maskCodes = new HashMap<>();

	static {
		//预生成指定长度的掩码星号
		for (int i = 1; i <= 10; i++) {
			maskCodes.put(i, getMaskCodeByNum(i));
		}
	}

	/**
	 * 生成指定长度的*
	 */
	private static String getMaskCodeByNum(int num) {
		String maskCode = maskCodes.get(num);
		if (maskCode != null) {
			return maskCode;
		}
		StringBuilder mask = new StringBuilder();
		for (int j = 0; j < num; j++) {
			mask.append('*');
		}
		return mask.toString();
	}

	@Override
	public String mask(String source, int[] params) {
		//空数据直接返回
		if (source == null || source.isEmpty()) {
			return source;
		}

		if (source.length() == 1) {
			return "*";
		}

		int leftKeep = Math.max(0, params[0]);
		int rightKeep = leftKeep;
		//左右保留不一样，可以配置两个参数
		if (params.length > 1) {
			rightKeep = Math.max(0, params[1]);
		}

		//各种不符合条件的(leftKeep,rightKeep过界），都回退到只保留第一位的情况
		boolean useDefault =
				leftKeep >= source.length() || rightKeep >= source.length() || (leftKeep + rightKeep) >= source
						.length();

		if (useDefault) {
			return source.substring(0, 1).concat(getMaskCodeByNum(source.length() - 1));
		}

		return source.substring(0, leftKeep).concat(getMaskCodeByNum(source.length() - leftKeep - rightKeep))
				.concat(source.substring(source.length() - rightKeep, source.length()));
	}
}
