package com.vip.vjtools.vjkit.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.google.common.io.Resources;
import com.vip.vjtools.vjkit.collection.ListUtil;
import com.vip.vjtools.vjkit.reflect.ClassLoaderUtil;
import com.vip.vjtools.vjkit.text.Charsets;

/**
 * 针对Jar包内的文件的工具类.
 * <p>
 * 1.ClassLoader
 * 
 * 不指定contextClass时，优先使用Thread.getContextClassLoader()， 如果ContextClassLoader未设置则使用Guava Resources类的ClassLoader
 * 
 * 指定contextClass时，则直接使用该contextClass的ClassLoader.
 * <p>
 * 2.路径
 * 
 * 不指定contextClass时，按URLClassLoader的实现, 从jar file中查找resourceName，
 * 
 * 所以resourceName无需以"/"打头即表示jar file中的根目录，带了"/" 反而导致JarFile.getEntry(resouceName)时没有返回.
 * 
 * 指定contextClass时，class.getResource()会先对name进行处理再交给classLoader，打头的"/"的会被去除，不以"/"打头则表示与该contextClass package的相对路径,
 * 会先转为绝对路径.
 * <p>
 * 3.同名资源
 * 
 * 如果有多个同名资源，除非调用getResources()获取全部资源，否则在URLClassLoader中按ClassPath顺序打开第一个命中的Jar文件.
 */
public class ResourceUtil {

	// 打开单个文件////
	/**
	 * 读取规则见本类注释.
	 */
	public static URL asUrl(String resourceName) {
		return Resources.getResource(resourceName);
	}

	/**
	 * 读取规则见本类注释.
	 */
	public static URL asUrl(Class<?> contextClass, String resourceName) {
		return Resources.getResource(contextClass, resourceName);
	}

	/**
	 * 读取规则见本类注释.
	 */
	public static InputStream asStream(String resourceName) throws IOException {
		return Resources.getResource(resourceName).openStream();
	}

	/**
	 * 读取文件的每一行，读取规则见本类注释.
	 */
	public static InputStream asStream(Class<?> contextClass, String resourceName) throws IOException {
		return Resources.getResource(contextClass, resourceName).openStream();
	}

	////// 读取单个文件内容／／／／／

	/**
	 * 读取文件的每一行，读取规则见本类注释.
	 */
	public static String toString(String resourceName) throws IOException {
		return Resources.toString(Resources.getResource(resourceName), Charsets.UTF_8);
	}

	/**
	 * 读取文件的每一行，读取规则见本类注释.
	 */
	public static String toString(Class<?> contextClass, String resourceName) throws IOException {
		return Resources.toString(Resources.getResource(contextClass, resourceName), Charsets.UTF_8);
	}

	/**
	 * 读取文件的每一行，读取规则见本类注释.
	 */
	public static List<String> toLines(String resourceName) throws IOException {
		return Resources.readLines(Resources.getResource(resourceName), Charsets.UTF_8);
	}

	/**
	 * 读取文件的每一行，读取规则见本类注释.
	 */
	public static List<String> toLines(Class<?> contextClass, String resourceName) throws IOException {
		return Resources.readLines(Resources.getResource(contextClass, resourceName), Charsets.UTF_8);
	}

	///////////// 打开所有同名文件///////

	public static List<URL> getResourcesQuietly(String resourceName) {
		return getResourcesQuietly(resourceName, ClassLoaderUtil.getDefaultClassLoader());
	}

	public static List<URL> getResourcesQuietly(String resourceName, ClassLoader contextClassLoader) {
		try {
			Enumeration<URL> urls = contextClassLoader.getResources(resourceName);
			List<URL> list = new ArrayList<URL>(10);
			while (urls.hasMoreElements()) {
				list.add(urls.nextElement());
			}
			return list;
		} catch (IOException e) {// NOSONAR
			return ListUtil.emptyList();
		}
	}
}
