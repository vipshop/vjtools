package com.vip.vjtools.vjkit.base.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>代表这个只是为了测试用例才开放的，</p>
 * <p>例如：<br>
 * 1.某个字段或者方法本来应该是private，结果是protected的；<br>
 * 2.某个构造函数本来不应该出现的；<br>
 * 3.某个类主要就是测试用的</p>
 *
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR })
public @interface JustForTest {
}