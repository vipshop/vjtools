package com.vip.vjtools.vjkit.datamasking;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 掩码类型标记
 * 在Java类字段中标记，对象在被DataMask序列化的时候，就可以自动被掩码
 */
@Target(ElementType.FIELD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Sensitive {

	SensitiveType type() default SensitiveType.Default;

	/**
	 * 如果首尾保留数量一样的，可以只用一个数字 {2} 表示首尾各保留2个字符 {1,2} 表示头部保留1个字符串，尾部保留2个字符串
	 */
	int[] keepChars() default {1, 0};

}
