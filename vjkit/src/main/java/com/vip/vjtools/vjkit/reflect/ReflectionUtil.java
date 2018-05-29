package com.vip.vjtools.vjkit.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import com.vip.vjtools.vjkit.base.ExceptionUtil;
import com.vip.vjtools.vjkit.base.ObjectUtil;
import com.vip.vjtools.vjkit.base.type.UncheckedException;

/**
 * 反射工具类.
 * 
 * 所有反射均无视modifier的范围限制，同时将反射的Checked异常转为UnChecked异常。
 * 
 * 需要平衡性能较差的一次性调用，以及高性能的基于预先获取的Method/Filed对象反复调用两种用法
 * 
 * 1. 获取方法与属性 (兼容了原始类型/接口/抽象类的参数, 并默认将方法与属性设为可访问)
 * 
 * 2. 方法调用.
 * 
 * 3. 构造函数.
 */
@SuppressWarnings("unchecked")
public class ReflectionUtil {

	private static final String SETTER_PREFIX = "set";
	private static final String GETTER_PREFIX = "get";
	private static final String IS_PREFIX = "is";

	///////// 获取方法对象 ////////
	/**
	 * 循环遍历，按属性名获取前缀为set的函数，并设为可访问
	 */
	public static Method getSetterMethod(Class<?> clazz, String propertyName, Class<?> parameterType) {
		String setterMethodName = SETTER_PREFIX + StringUtils.capitalize(propertyName);
		return getMethod(clazz, setterMethodName, parameterType);
	}

	/**
	 * 循环遍历，按属性名获取前缀为get或is的函数，并设为可访问
	 */
	public static Method getGetterMethod(Class<?> clazz, String propertyName) {
		String getterMethodName = GETTER_PREFIX + StringUtils.capitalize(propertyName);

		Method method = getMethod(clazz, getterMethodName);

		// retry on another name
		if (method == null) {
			getterMethodName = IS_PREFIX + StringUtils.capitalize(propertyName);
			method = getMethod(clazz, getterMethodName);
		}
		return method;
	}

	/**
	 * 循环向上转型, 获取对象的DeclaredMethod, 并强制设置为可访问.
	 * 
	 * 如向上转型到Object仍无法找到, 返回null.
	 * 
	 * 匹配函数名+参数类型.
	 * 
	 * 方法需要被多次调用时，先使用本函数先取得Method，然后调用Method.invoke(Object obj, Object... args)
	 * 
	 * 因为getMethod() 不能获取父类的private函数, 因此采用循环向上的getDeclaredMethod();
	 */
	public static Method getMethod(final Class<?> clazz, final String methodName, Class<?>... parameterTypes) {
		Method method = MethodUtils.getMatchingMethod(clazz, methodName, parameterTypes);
		if (method != null) {
			makeAccessible(method);
		}
		return method;
	}

	/**
	 * 循环向上转型, 获取对象的DeclaredMethod,并强制设置为可访问.
	 * 
	 * 如向上转型到Object仍无法找到, 返回null.
	 * 
	 * 只匹配函数名, 如果有多个同名函数返回第一个
	 * 
	 * 方法需要被多次调用时，先使用本函数先取得Method，然后调用Method.invoke(Object obj, Object... args)
	 * 
	 * 因为getMethod() 不能获取父类的private函数, 因此采用循环向上的getDeclaredMethods()
	 */
	public static Method getAccessibleMethodByName(final Class clazz, final String methodName) {
		Validate.notNull(clazz, "clazz can't be null");
		Validate.notEmpty(methodName, "methodName can't be blank");

		for (Class<?> searchType = clazz; searchType != Object.class; searchType = searchType.getSuperclass()) {
			Method[] methods = searchType.getDeclaredMethods();
			for (Method method : methods) {
				if (method.getName().equals(methodName)) {
					makeAccessible(method);
					return method;
				}
			}
		}
		return null;
	}

	//////////// 获取Field对象///////////
	/**
	 * 循环向上转型, 获取对象的DeclaredField, 并强制设置为可访问.
	 * 
	 * 如向上转型到Object仍无法找到, 返回null.
	 * 
	 * 因为getFiled()不能获取父类的private属性, 因此采用循环向上的getDeclaredField();
	 */
	public static Field getField(final Class clazz, final String fieldName) {
		Validate.notNull(clazz, "clazz can't be null");
		Validate.notEmpty(fieldName, "fieldName can't be blank");
		for (Class<?> superClass = clazz; superClass != Object.class; superClass = superClass.getSuperclass()) {
			try {
				Field field = superClass.getDeclaredField(fieldName);
				makeAccessible(field);
				return field;
			} catch (NoSuchFieldException e) {// NOSONAR
				// Field不在当前类定义,继续向上转型
			}
		}
		return null;
	}

	/////////// 获取或设置属性相关函数 ///////////
	/**
	 * 调用Getter方法, 无视private/protected修饰符.
	 * 
	 * 性能较差, 用于单次调用的场景
	 */
	public static <T> T invokeGetter(Object obj, String propertyName) {
		Method method = getGetterMethod(obj.getClass(), propertyName);
		if (method == null) {
			throw new IllegalArgumentException(
					"Could not find getter method [" + propertyName + "] on target [" + obj + ']');
		}
		return invokeMethod(obj, method);
	}

	/**
	 * 调用Setter方法, 无视private/protected修饰符, 按传入value的类型匹配函数.
	 * 
	 * 性能较差, 用于单次调用的场景
	 */
	public static void invokeSetter(Object obj, String propertyName, Object value) {
		Method method = getSetterMethod(obj.getClass(), propertyName, value.getClass());
		if (method == null) {
			throw new IllegalArgumentException(
					"Could not find getter method [" + propertyName + "] on target [" + obj + ']');
		}
		invokeMethod(obj, method, value);
	}

	/**
	 * 直接读取对象属性值, 无视private/protected修饰符, 不经过getter函数.
	 * 
	 * 性能较差, 用于单次调用的场景
	 */
	public static <T> T getFieldValue(final Object obj, final String fieldName) {
		Field field = getField(obj.getClass(), fieldName);
		if (field == null) {
			throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + obj + ']');
		}
		return getFieldValue(obj, field);
	}

	/**
	 * 使用已获取的Field, 直接读取对象属性值, 不经过getter函数.
	 * 
	 * 用于反复调用的场景.
	 */
	public static <T> T getFieldValue(final Object obj, final Field field) {
		try {
			return (T) field.get(obj);
		} catch (Exception e) {
			throw convertReflectionExceptionToUnchecked(e);
		}
	}

	/**
	 * 直接设置对象属性值, 无视private/protected修饰符, 不经过setter函数.
	 * 
	 * 性能较差, 用于单次调用的场景
	 */
	public static void setFieldValue(final Object obj, final String fieldName, final Object value) {
		Field field = getField(obj.getClass(), fieldName);
		if (field == null) {
			throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + obj + ']');
		}
		setField(obj, field, value);
	}

	/**
	 * 使用预先获取的Field, 直接读取对象属性值, 不经过setter函数.
	 * 
	 * 用于反复调用的场景.
	 */
	public static void setField(final Object obj, Field field, final Object value) {
		try {
			field.set(obj, value);
		} catch (Exception e) {
			throw convertReflectionExceptionToUnchecked(e);
		}
	}

	/**
	 * 先尝试用Getter函数读取, 如果不存在则直接读取变量.
	 * 
	 * 性能较差, 用于单次调用的场景
	 */
	public static <T> T getProperty(Object obj, String propertyName) {
		Method method = getGetterMethod(obj.getClass(), propertyName);
		if (method != null) {
			return invokeMethod(obj, method);
		} else {
			return getFieldValue(obj, propertyName);
		}
	}

	/**
	 * 先尝试用Setter函数写入, 如果不存在则直接写入变量, 按传入value的类型匹配函数.
	 * 
	 * 性能较差, 用于单次调用的场景
	 */
	public static void setProperty(Object obj, String propertyName, final Object value) {
		Method method = getSetterMethod(obj.getClass(), propertyName, value.getClass());
		if (method != null) {
			invokeMethod(obj, method, value);
		} else {
			setFieldValue(obj, propertyName, value);
		}
	}

	/////////// 方法相关函数 ////////////
	/**
	 * 反射调用对象方法, 无视private/protected修饰符.
	 * 
	 * 根据传入参数的实际类型进行匹配, 支持方法参数定义是接口，父类，原子类型等情况
	 * 
	 * 性能较差，仅用于单次调用.
	 */
	public static <T> T invokeMethod(Object obj, String methodName, Object... args) {
		Object[] theArgs = ArrayUtils.nullToEmpty(args);
		final Class<?>[] parameterTypes = ClassUtils.toClass(theArgs);
		return invokeMethod(obj, methodName, theArgs, parameterTypes);
	}

	/**
	 * 反射调用对象方法, 无视private/protected修饰符.
	 * 
	 * 根据参数类型参数进行匹配, 支持方法参数定义是接口，父类，原子类型等情况
	 * 
	 * 性能较低，仅用于单次调用.
	 */
	public static <T> T invokeMethod(final Object obj, final String methodName, final Object[] args,
			final Class<?>[] parameterTypes) {
		Method method = getMethod(obj.getClass(), methodName, parameterTypes);
		if (method == null) {
			throw new IllegalArgumentException("Could not find method [" + methodName + "] with parameter types:"
					+ ObjectUtil.toPrettyString(parameterTypes) + " on class [" + obj.getClass() + ']');
		}
		return invokeMethod(obj, method, args);
	}

	/**
	 * 反射调用对象方法, 无视private/protected修饰符
	 * 
	 * 只匹配函数名，如果有多个同名函数调用第一个. 用于确信只有一个同名函数, 但参数类型不确定的情况.
	 * 
	 * 性能较低，仅用于单次调用.
	 */
	public static <T> T invokeMethodByName(final Object obj, final String methodName, final Object[] args) {
		Method method = getAccessibleMethodByName(obj.getClass(), methodName);
		if (method == null) {
			throw new IllegalArgumentException(
					"Could not find method [" + methodName + "] on class [" + obj.getClass() + ']');
		}
		return invokeMethod(obj, method, args);
	}

	/**
	 * 调用预先获取的Method，用于反复调用的场景
	 */
	public static <T> T invokeMethod(final Object obj, Method method, Object... args) {
		try {
			return (T) method.invoke(obj, args);
		} catch (Exception e) {
			throw ExceptionUtil.unwrapAndUnchecked(e);
		}
	}

	////////// 构造函数 ////////
	// TODO:更多函数的封装
	/**
	 * 调用构造函数.
	 */
	public static <T> T invokeConstructor(final Class<T> cls, Object... args) {
		try {
			return ConstructorUtils.invokeConstructor(cls, args);
		} catch (Exception e) {
			throw ExceptionUtil.unwrapAndUnchecked(e);
		}
	}

	/////// 辅助函数 ////////

	/**
	 * 改变private/protected的方法为可访问，尽量不进行改变，避免JDK的SecurityManager抱怨。
	 */
	public static void makeAccessible(Method method) {
		if (!method.isAccessible() && (!Modifier.isPublic(method.getModifiers())
				|| !Modifier.isPublic(method.getDeclaringClass().getModifiers()))) {
			method.setAccessible(true);
		}
	}

	/**
	 * 改变private/protected的成员变量为可访问，尽量不进行改变，避免JDK的SecurityManager抱怨。
	 */
	public static void makeAccessible(Field field) {
		if (!field.isAccessible() && (!Modifier.isPublic(field.getModifiers())
				|| !Modifier.isPublic(field.getDeclaringClass().getModifiers())
				|| Modifier.isFinal(field.getModifiers()))) {
			field.setAccessible(true);
		}
	}

	/**
	 * 将反射时的checked exception转换为unchecked exception.
	 */
	public static RuntimeException convertReflectionExceptionToUnchecked(Exception e) {
		if ((e instanceof IllegalAccessException) || (e instanceof NoSuchMethodException)) {
			return new IllegalArgumentException(e);
		} else if (e instanceof InvocationTargetException) {
			return new RuntimeException(((InvocationTargetException) e).getTargetException());
		} else if (e instanceof RuntimeException) {
			return (RuntimeException) e;
		}
		return new UncheckedException(e);
	}
}
