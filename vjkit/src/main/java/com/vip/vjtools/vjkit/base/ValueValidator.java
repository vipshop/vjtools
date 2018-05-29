package com.vip.vjtools.vjkit.base;

import org.apache.commons.lang3.StringUtils;

/**
 * 配值较验器 
 * 
 * 提供对值进行较验的api，并根据较验结果取值且返回
 *
 */
public class ValueValidator {
	
	/**
	 * 对目标值进行校验，并根据校验结果取值
	 * <br>使用示例(校验目标值是否大于0, 如果小于 0 则取值为 1)
	 * <br>ValueValidator.checkAndGet(-1, 1, Validator.INTEGER_GT_ZERO_VALIDATOR)</br>
	 * @param value 校验值
	 * @param defaultValue 校验失败默认值
	 * @param v 校验器
	 * @return 经Validator校验后的返回值，校验成功返回 value, 校验失败返回 defaultValue
	 */
	public static <T> T checkAndGet(T value, T defaultValue, Validator<T> v) {
		if (v.validate(value)) {
			return value;
		}

		return defaultValue;
	}

	/**
	 * 对Properties值进行规则匹配的验证器
	 */
	public interface Validator<T> {
		
		/**
		 * 校验器: 数值配置不为null, 且大于0较验
		 */
		Validator<Integer> INTEGER_GT_ZERO_VALIDATOR = new Validator<Integer>() {
			@Override
			public boolean validate(Integer value) {
				return (value != null && value > 0);
			}
		};

		/**
		 * 校验器: 字符串不为空串较验
		 */
		Validator<String> STRING_EMPTY_VALUE_VALIDATOR = new Validator<String>() {
			@Override
			public boolean validate(String value) {
				return StringUtils.isNotEmpty(value);
			}
		};

		/**
		 * 校验器: BOOL字符串较验
		 */
		Validator<String> STRICT_BOOL_VALUE_VALIDATOR = new Validator<String>() {
			@Override
			public boolean validate(String value) {
				return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
			}
		};
		
		/**
		 * 值规则匹配方法实现
		 */
		boolean validate(T value);

	}
}
