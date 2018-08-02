package com.vip.vjtools.vjkit.base.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * 标注参数、属性、方法不可为 Null
 */
@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD })
public @interface NotNull {

}
