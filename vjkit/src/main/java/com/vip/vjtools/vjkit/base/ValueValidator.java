package com.vip.vjtools.vjkit.base;

import org.apache.commons.lang3.StringUtils;

/**
 * 数值校验取值器 
 * 
 * 提供对配置值进行校验，并根据结果决定是否使用默认值。
 * 
 * Guva, Commons Lang里的Validate类用于判断并抛异常。
 * 
 * 而ValueValidator的行为是取默认值，多用于配置值的处理。
 * 
 * 除默认提供的Validator，可自行扩写。
 */
public class ValueValidator {

	/**
	 * 对目标值进行校验，并根据校验结果取值
	 * 
	 * 使用示例(校验目标值是否大于0, 如果小于 0 则取值为 1)
	 * 
	 * ValueValidator.checkAndGet(idleTime, 1, Validator.INTEGER_GT_ZERO_VALIDATOR)
	 * 
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
	 * 对值进行规则匹配的验证器
	 */
	public interface Validator<T> {
		/**
		 * 校验值是否匹配
		 */
		boolean validate(T value);

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
	}
}
