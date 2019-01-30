package com.vip.vjtools.vjkit.mapper;

import java.util.ArrayList;
import java.util.List;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;

import com.vip.vjtools.vjkit.collection.ArrayUtil;

/**
 * 实现深度的BeanOfClasssA<->BeanOfClassB复制
 * 
 * 不要使用Apache Common BeanUtils进行类复制，每次就行反射查询对象的属性列表, 非常缓慢.
 * 
 * orika性能比Dozer快近十倍，也不需要Getter函数与无参构造函数
 * 
 * 但我们内部修复了的bug，社区版没有修复: https://github.com/orika-mapper/orika/issues/252 
 * 
 * 如果应用启动时有并发流量进入，可能导致两个不同类型的同名属性间(如Order的User user属性，与OrderVO的UserVO user)的复制失败，只有重启才能解决。
 * 
 * 因此安全起见，在vjkit的开源版本中仍然使用Dozer。
 * 
 * Dozer最新是6.x版，但only for JDK8，为兼容JDK7这里仍使用5.x版本。
 * 
 * 注意: 需要参考POM文件，显式引用Dozer.
 */
public class BeanMapper {

	private static Mapper mapper = new DozerBeanMapper();

	/**
	 * 简单的复制出新类型对象.
	 */
	public static <S, D> D map(S source, Class<D> destinationClass) {
		return mapper.map(source, destinationClass);
	}

	/**
	 * 简单的复制出新对象ArrayList
	 */
	public static <S, D> List<D> mapList(Iterable<S> sourceList, Class<D> destinationClass) {
		List<D> destinationList = new ArrayList<D>();
		for (S source : sourceList) {
			if (source != null) {
				destinationList.add(mapper.map(source, destinationClass));
			}
		}
		return destinationList;
	}

	/**
	 * 简单复制出新对象数组
	 */
	public static <S, D> D[] mapArray(final S[] sourceArray, final Class<D> destinationClass) {
		D[] destinationArray = ArrayUtil.newArray(destinationClass, sourceArray.length);

		int i = 0;
		for (S source : sourceArray) {
			if (source != null) {
				destinationArray[i] = mapper.map(sourceArray[i], destinationClass);
				i++;
			}
		}

		return destinationArray;
	}
}
