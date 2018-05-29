package com.vip.vjtools.vjkit.reflect;

import java.util.ArrayList;
import java.util.List;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;

import com.vip.vjtools.vjkit.collection.ArrayUtil;

/**
 * 实现深度的BeanOfClasssA<->BeanOfClassB复制
 * 
 * 不要是用Apache Common BeanUtils进行类复制，每次就行反射查询对象的属性列表, 非常缓慢.
 * 
 * orika性能更好，也不需要Getter函数与无参构造函数，但有潜在bug还没在社区版修复
 * 
 * Dozer有6.0版，但only for JDK8
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
		List<D> destionationList = new ArrayList<D>();
		for (S source : sourceList) {
			if (source != null) {
				destionationList.add(mapper.map(source, destinationClass));
			}
		}
		return destionationList;
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
