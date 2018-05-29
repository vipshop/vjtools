package com.vip.vjtools.vjkit.reflect;

public class ClassLoaderUtil {

	/**
	 * Copy from Spring, 按顺序获取默认ClassLoader
	 * 
	 * 1. Thread.currentThread().getContextClassLoader()
	 * 
	 * 2. ClassLoaderUtil的加载ClassLoader
	 * 
	 * 3. SystemClassLoader
	 */
	public static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		} catch (Throwable ex) { // NOSONAR
			// Cannot access thread context ClassLoader - falling back...
		}
		if (cl == null) {
			// No thread context class loader -> use class loader of this class.
			cl = ClassLoaderUtil.class.getClassLoader();
			if (cl == null) {
				// getClassLoader() returning null indicates the bootstrap ClassLoader
				try {
					cl = ClassLoader.getSystemClassLoader();
				} catch (Throwable ex) { // NOSONAR
					// Cannot access system ClassLoader - oh well, maybe the caller can live with null...
				}
			}
		}
		return cl;
	}

	/**
	 * 探测类是否存在classpath中
	 */
	public static boolean isPresent(String className, ClassLoader classLoader) {
		try {
			classLoader.loadClass(className);
			return true;
		} catch (Throwable ex) { // NOSONAR
			// Class or one of its dependencies is not present...
			return false;
		}
	}

}
