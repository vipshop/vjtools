package com.vip.vjtools.vjkit.datamasking;

import com.alibaba.fastjson.JSON;
import com.vip.vjtools.vjkit.datamasking.strategy.HashMask;

/**
 * 脱敏工具类
 */
public class DataMask {

	private static DataMaskJsonFilter jsonFilter = new DataMaskJsonFilter();

	/**
	 * 设置Hash脱敏的salt
	 */
	public static void setHashSalt(String salt){
		HashMask.setSalt(salt);
	}

	/**
	 * 根据类型来脱敏
	 * @param source 源字符串
	 * @param type 类型
	 * @return 掩码后的字符串
	 */
	public static String maskByType(String source, SensitiveType type) {
		try {
			return type.getStrategy().mask(source, type.getParams());
		} catch (Exception e) {
			return source;
		}
	}

	/**
	 * 默认的脱敏方式，只显示第一个字符串
	 * @param source 源字符串
	 * @return 脱敏后的字符串
	 */
	public static String mask(String source) {
		try {
			return maskByType(source, SensitiveType.Default);
		} catch (Exception e) {
			return source;
		}
	}

	/**
	 * 自定义脱敏策略来脱敏
	 * @param source 源字符串
	 * @param strategy 策略
	 * @param param 策略参数
	 * @return 脱敏后的字符串
	 */
	public static String mask(String source,MaskStrategy strategy,int...param){
		try {
			return strategy.mask(source,param);
		} catch (Exception e) {
			return source;
		}
	}

	/**
	 * 将需要脱敏的字段先进行脱敏操作，最后转成json格式
	 * @param object 需要序列化的对象
	 * @return 脱敏后的json格式
	 */
	public static String toJSONString(Object object) {
		if (object == null) {
			return null;
		}
		try {
			return JSON.toJSONString(object, jsonFilter);
		} catch (Exception e) {
			return JSON.toJSONString(object);
		}
	}

	/**
	 * 对脱敏字段进行处理，然后调用对象原来的toString()
	 * 注意，如果需要脱敏的字段比较多，性能会被toJson差，建议优先使用toJSON
	 * @param object 需要序列化的对象
	 * @return 脱敏后的toString()格式
	 */
	public static String toString(Object object) {
		if (object == null) {
			return null;
		}
		try {
			String json = toJSONString(object);
			Object cloneObj = JSON.parseObject(json, object.getClass());
			return cloneObj.toString();
		} catch (Exception e) {
			return object.toString();
		}
	}
}
