package com.vip.vjtools.vjkit.concurrent.type;

import java.util.HashMap;
import java.util.Map;

/**
 * 存储于ThreadLocal的Map, 用于存储上下文.<br/>
 * 
 * 但HashMap<String,Object>的存储其实较为低效，在高性能场景下可改为EnumMap<br/>
 * 
 * 1.先定义枚举类，列举所有可能的Key<br/>
 * 2.替换contextMap的创建语句，见下例<br/>
 * 3.修改put()/get()中key的类型<br/>
 * 
 * <pre>
 * private static ThreadLocal<Map<MyEnum, Object>> contextMap = new ThreadLocal<Map<MyEnum, Object>>() {
 * 	&#64;Override
 * 	protected Map<MyEnum, Object> initialValue() {
 * 		return new EnumMap<MyEnum, Object>(MyEnum.class);
 * 	}
 * };
 * </pre>
 */
public class ThreadLocalContext {

	private static ThreadLocal<Map<String, Object>> contextMap = new ThreadLocal<Map<String, Object>>() {
		@Override
		protected Map<String, Object> initialValue() {
			// 降低loadFactory减少冲突
			return new HashMap<>(16, 0.5f);
		}
	};

	/**
	 * 放入ThreadLocal的上下文信息.
	 */
	public static void put(String key, Object value) {
		contextMap.get().put(key, value);
	}

	/**
	 * 取出ThreadLocal的上下文信息.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T get(String key) {
		return (T) (contextMap.get().get(key));
	}

	/**
	 * 清理ThreadLocal的Context内容.
	 */
	public static void reset() {
		contextMap.get().clear();
	}
}
