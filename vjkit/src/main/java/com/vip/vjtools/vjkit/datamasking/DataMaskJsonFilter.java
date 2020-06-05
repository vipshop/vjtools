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

			//处理字符串,只处理字符串相关类型的字段
			if (field.getType() == String.class) {
				if (((String) value).length() == 0) {
					return value;
				}

				Sensitive sensitive = field.getAnnotation(Sensitive.class);
				SensitiveType sensitiveType = getSensitiveType(field, sensitive);
				return mask((String) value, sensitive, sensitiveType);

			} else if (field.getType() == String[].class) {
				//处理数组String[]
				Sensitive sensitive = field.getAnnotation(Sensitive.class);
				SensitiveType sensitiveType = getSensitiveType(field, sensitive);

				if (sensitiveType == null) {
					return value;
				}
				String[] strArr = (String[]) value;

				for (int i = 0; i < strArr.length; i++) {
					strArr[i] = mask(strArr[i], sensitive, sensitiveType);
				}
				return strArr;
			} else if (Collection.class.isAssignableFrom(field.getType())) {
				//处理Collection<String>
				Sensitive sensitive = field.getAnnotation(Sensitive.class);
				SensitiveType sensitiveType = getSensitiveType(field, sensitive);

				if (sensitiveType == null) {
					return value;
				}

				if(!isStringCollection(field)){
					return value;
				}

				//没有set()的接口，重新构造一个
				Collection<String> newValue = (Collection<String>) value.getClass().newInstance();
				for (String item : (Collection<String>) value) {
					newValue.add(mask(item, sensitive, sensitiveType));
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
	 * 是否Collection<String>
	 * @param field
	 * @return
	 */
	private boolean isStringCollection(Field field){
		Type type = field.getGenericType();
		if (!(type instanceof ParameterizedType)) {
			return false;
		}
		Class parameterizedType = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
		if (parameterizedType != String.class) {
			return false;
		}
		return true;
	}

	/**
	 * 对字段进行mask
	 * @param value 值
	 * @return 脱敏后的值
	 */
	private String mask(String value, Sensitive sensitive, SensitiveType type) {
		if (value == null || value.isEmpty()) {
			return value;
		}
		if (sensitive == null) {
			if (type != null) {
				return DataMask.mask(value, type);
			} else {
				return value;
			}
		} else {
			return sensitive.type().getStrategy().mask(value, sensitive.keepChars());
		}
	}

	private SensitiveType getSensitiveType(Field field, Sensitive sensitive) {
		SensitiveType type = null;
		if (sensitive == null) {
			String fieldName = field.getName();
			//没有@Sensitive，但是有mapping命中
			type = MaskMapping.getMaskTypeMapping(fieldName);
		} else {
			type = sensitive.type();
		}
		return type;
	}


}
