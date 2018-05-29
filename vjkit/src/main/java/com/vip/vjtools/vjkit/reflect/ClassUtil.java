package com.vip.vjtools.vjkit.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 获取Class信息的工具类
 * 
 * 1. 获取类名，包名，循环向上的全部父类，全部接口
 * 
 * 2. 其他便捷函数
 */
public class ClassUtil {

	private static final String CGLIB_CLASS_SEPARATOR = "$$";

	private static Logger logger = LoggerFactory.getLogger(ClassUtil.class);

	////// Short class and Package Name ////...
	/**
	 * 返回短Class名, 不包含PackageName.
	 * 
	 * 内部类的话，返回"主类.内部类"
	 */
	public static String getShortClassName(final Class<?> cls) {
		return ClassUtils.getShortClassName(cls);
	}

	/**
	 * 返回Class名，不包含PackageName
	 * 
	 * 内部类的话，返回"主类.内部类"
	 */
	public static String getShortClassName(final String className) {
		return ClassUtils.getShortClassName(className);
	}

	/**
	 * 返回PackageName
	 */
	public static String getPackageName(final Class<?> cls) {
		return ClassUtils.getPackageName(cls);
	}

	/**
	 * 返回PackageName
	 */
	public static String getPackageName(final String className) {
		return ClassUtils.getPackageName(className);
	}

	////////// 获取全部父类，全部接口//////////
	/**
	 * 递归返回所有的SupperClasses，包含Object.class
	 */
	public static List<Class<?>> getAllSuperclasses(final Class<?> cls) {
		return ClassUtils.getAllSuperclasses(cls);
	}

	/**
	 * 递归返回本类及所有基类继承的接口，及接口继承的接口，比Spring中的相同实现完整
	 */
	public static List<Class<?>> getAllInterfaces(final Class<?> cls) {
		return ClassUtils.getAllInterfaces(cls);
	}

	/////////// 杂项 /////////

	/**
	 * https://github.com/linkedin/linkedin-utils/blob/master/org.linkedin.util-core/src/main/java/org/linkedin/util/reflect/ReflectUtils.java
	 * 
	 * The purpose of this method is somewhat to provide a better naming / documentation than the javadoc of
	 * <code>Class.isAssignableFrom</code> method.
	 * 
	 * @return <code>true</code> if subclass is a subclass or sub interface of superclass
	 */
	public static boolean isSubClassOrInterfaceOf(Class subclass, Class superclass) {
		return superclass.isAssignableFrom(subclass);
	}

	/**
	 * 获取CGLib处理过后的实体的原Class.
	 */
	public static Class<?> unwrapCglib(Object instance) {
		Validate.notNull(instance, "Instance must not be null");
		Class<?> clazz = instance.getClass();
		if ((clazz != null) && clazz.getName().contains(CGLIB_CLASS_SEPARATOR)) {
			Class<?> superClass = clazz.getSuperclass();
			if ((superClass != null) && !Object.class.equals(superClass)) {
				return superClass;
			}
		}
		return clazz;
	}

	/**
	 * 通过反射, 获得Class定义中声明的泛型参数的类型,
	 * 
	 * 注意泛型必须定义在父类处. 这是唯一可以通过反射从泛型获得Class实例的地方.
	 * 
	 * 如无法找到, 返回Object.class.
	 * 
	 * eg. public UserDao extends HibernateDao<User>
	 * 
	 * @param clazz The class to introspect
	 * @return the first generic declaration, or Object.class if cannot be determined
	 */
	public static <T> Class<T> getClassGenericType(final Class clazz) {
		return getClassGenericType(clazz, 0);
	}

	/**
	 * 通过反射, 获得Class定义中声明的父类的泛型参数的类型.
	 * 
	 * 注意泛型必须定义在父类处. 这是唯一可以通过反射从泛型获得Class实例的地方.
	 * 
	 * 如无法找到, 返回Object.class.
	 * 
	 * 如public UserDao extends HibernateDao<User,Long>
	 * 
	 * @param clazz clazz The class to introspect
	 * @param index the Index of the generic declaration, start from 0.
	 * @return the index generic declaration, or Object.class if cannot be determined
	 */
	public static Class getClassGenericType(final Class clazz, final int index) {

		Type genType = clazz.getGenericSuperclass();

		if (!(genType instanceof ParameterizedType)) {
			logger.warn(clazz.getSimpleName() + "'s superclass not ParameterizedType");
			return Object.class;
		}

		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

		if ((index >= params.length) || (index < 0)) {
			logger.warn("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: "
					+ params.length);
			return Object.class;
		}
		if (!(params[index] instanceof Class)) {
			logger.warn(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
			return Object.class;
		}

		return (Class) params[index];
	}

}
