package com.vip.vjtools.vjkit.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ClassUtils;

/**
 * Annotation的工具类
 * 
 * 1.获得类的全部Annotation
 * 
 * 2.获取类的标注了annotation的所有属性和方法
 */
public class AnnotationUtil {

	/**
	 * 递归Class所有的Annotation，一个最彻底的实现.
	 * 
	 * 包括所有基类，所有接口的Annotation，同时支持Spring风格的Annotation继承的父Annotation，
	 */
	public static Set<Annotation> getAllAnnotations(final Class<?> cls) {
		List<Class<?>> allTypes = ClassUtil.getAllSuperclasses(cls);
		allTypes.addAll(ClassUtil.getAllInterfaces(cls));
		allTypes.add(cls);

		Set<Annotation> anns = new HashSet<>();
		for (Class<?> type : allTypes) {
			anns.addAll(Arrays.asList(type.getDeclaredAnnotations()));
		}

		Set<Annotation> superAnnotations = new HashSet<>();
		for (Annotation ann : anns) {
			getSuperAnnotations(ann.annotationType(), superAnnotations);
		}

		anns.addAll(superAnnotations);

		return anns;
	}

	private static <A extends Annotation> void getSuperAnnotations(Class<A> annotationType, Set<Annotation> visited) {
		Annotation[] anns = annotationType.getDeclaredAnnotations();

		for (Annotation ann : anns) {
			if (!ann.annotationType().getName().startsWith("java.lang") && visited.add(ann)) {
				getSuperAnnotations(ann.annotationType(), visited);
			}
		}
	}

	/**
	 * 找出所有标注了该annotation的公共属性，循环遍历父类.
	 * 
	 * 暂未支持Spring风格Annotation继承Annotation
	 * 
	 * copy from org.unitils.util.AnnotationUtils
	 */
	public static <T extends Annotation> Set<Field> getAnnotatedPublicFields(Class<? extends Object> clazz,
			Class<T> annotation) {

		if (Object.class.equals(clazz)) {
			return Collections.emptySet();
		}

		Set<Field> annotatedFields = new HashSet<>();
		Field[] fields = clazz.getFields();

		for (Field field : fields) {
			if (field.getAnnotation(annotation) != null) {
				annotatedFields.add(field);
			}
		}

		return annotatedFields;
	}

	/**
	 * 找出所有标注了该annotation的属性，循环遍历父类，包含private属性.
	 * 
	 * 暂未支持Spring风格Annotation继承Annotation
	 * 
	 * copy from org.unitils.util.AnnotationUtils
	 */
	public static <T extends Annotation> Set<Field> getAnnotatedFields(Class<? extends Object> clazz,
			Class<T> annotation) {
		if (Object.class.equals(clazz)) {
			return Collections.emptySet();
		}
		Set<Field> annotatedFields = new HashSet<>();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if (field.getAnnotation(annotation) != null) {
				annotatedFields.add(field);
			}
		}
		annotatedFields.addAll(getAnnotatedFields(clazz.getSuperclass(), annotation));
		return annotatedFields;
	}

	/**
	 * 找出所有标注了该annotation的公共方法(含父类的公共函数)，循环其接口.
	 * 
	 * 暂未支持Spring风格Annotation继承Annotation
	 * 
	 * 另，如果子类重载父类的公共函数，父类函数上的annotation不会继承，只有接口上的annotation会被继承.
	 */
	public static <T extends Annotation> Set<Method> getAnnotatedPublicMethods(Class<?> clazz, Class<T> annotation) {
		// 已递归到Objebt.class, 停止递归
		if (Object.class.equals(clazz)) {
			return Collections.emptySet();
		}

		List<Class<?>> ifcs = ClassUtils.getAllInterfaces(clazz);
		Set<Method> annotatedMethods = new HashSet<>();

		// 遍历当前类的所有公共方法
		Method[] methods = clazz.getMethods();

		for (Method method : methods) {
			// 如果当前方法有标注，或定义了该方法的所有接口有标注
			if (method.getAnnotation(annotation) != null || searchOnInterfaces(method, annotation, ifcs)) {
				annotatedMethods.add(method);
			}
		}

		return annotatedMethods;
	}

	private static <T extends Annotation> boolean searchOnInterfaces(Method method, Class<T> annotationType,
			List<Class<?>> ifcs) {
		for (Class<?> iface : ifcs) {
			try {
				Method equivalentMethod = iface.getMethod(method.getName(), method.getParameterTypes());
				if (equivalentMethod.getAnnotation(annotationType) != null) {
					return true;
				}
			} catch (NoSuchMethodException ex) { // NOSONAR
				// Skip this interface - it doesn't have the method...
			}
		}
		return false;
	}
}
