package com.vip.vjtools.vjkit.datamasking;

import com.alibaba.fastjson.serializer.BeanContext;
import com.alibaba.fastjson.serializer.ContextValueFilter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * 对每个json字段进行脱敏处理
 * 处理String,String[] Collection<String>
 */
public class DataMaskJsonFilter implements ContextValueFilter {
	@Override
	public Object process(BeanContext context, Object object, String name, Object value) {
		if (null == value) {
			return null;
		}
		try {
			Field field = context.getField();

			//处理字符串
			if (field.getType() == String.class) {
				if (((String) value).length() == 0) {
					return value;
				}
				return mask(field, (String) value);
			} else if (field.getType() == String[].class) {//处理数组String[]
				if (!needMask(field)) {
					return value;
				}
				String[] strArr = (String[]) value;

				for (int i = 0; i < strArr.length; i++) {
					strArr[i] = mask(field, strArr[i]);
				}
				return strArr;
			} else if (Collection.class.isAssignableFrom(field.getType())) {
				//处理Collection<String>,没有set()的接口，重新构造一个
				if (!needMask(field)) {
					return value;
				}

				Type type = field.getGenericType();
				if (!(type instanceof ParameterizedType)) {
					return value;
				}
				Class parameterizedType = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
				if (parameterizedType != String.class) {
					return value;
				}
				Collection<String> newValue = (Collection<String>) value.getClass().newInstance();

				for (String item : (Collection<String>) value) {
					newValue.add(mask(field, item));
				}

				return newValue;

			} else {
				return value;
			}
		} catch (Exception e) {
			return value;
		}
	}

	/**
	 * 对字段进行mask
	 * @param field 字段
	 * @param value 值
	 * @return 脱敏后的值
	 */
	private String mask(Field field, String value) {
		if (value == null || value.isEmpty()) {
			return value;
		}

		Sensitive sensitive = field.getAnnotation(Sensitive.class);
		if (sensitive == null) {
			//名称有默认的mapping配置
			String fieldName = field.getName();
			SensitiveType type = MaskMapping.getMaskTypeMapping(fieldName);
			if (type != null) {
				return DataMask.mask(value, type);
			} else {
				return value;
			}
		} else {
			return sensitive.type().getStrategy().mask(value, sensitive.keepChars());
		}
	}

	/**
	 * 先判断是否需要Mask，过滤不需要的反射操作
	 */
	private boolean needMask(Field field) {
		Sensitive sensitive = field.getAnnotation(Sensitive.class);
		if (sensitive == null) {
			String fieldName = field.getName();
			SensitiveType type = MaskMapping.getMaskTypeMapping(fieldName);
			return type != null;
		} else {
			return true;
		}
	}


}
