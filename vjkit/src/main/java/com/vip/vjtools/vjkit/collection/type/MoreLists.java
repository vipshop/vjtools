package com.vip.vjtools.vjkit.collection.type;

import java.util.Comparator;

/**
 * 特殊的List类型
 */
public class MoreLists {

	/**
	 * 排序的ArrayList.
	 * 
	 * from Jodd的新类型，插入时排序，但指定插入index的方法如add(index,element)不支持
	 */
	@SuppressWarnings("rawtypes")
	public static <T extends Comparable> SortedArrayList<T> createSortedArrayList() {
		return new SortedArrayList<T>();
	}

	/**
	 * 排序的ArrayList.
	 * 
	 * from Jodd的新类型，插入时排序，但指定插入index的方法如add(index,element)不支持
	 */
	public static <T> SortedArrayList<T> createSortedArrayList(Comparator<? super T> c) {
		return new SortedArrayList<T>(c);
	}
}
