package com.vip.vjtools.vjkit.collection;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.google.common.collect.ObjectArrays;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.vip.vjtools.vjkit.base.annotation.Nullable;

/**
 * 数组工具类.
 * 
 * 1. 创建Array的函数
 * 
 * 2. 数组的乱序与contact相加
 * 
 * 3. 从Array转换到Guava的底层为原子类型的List
 * 
 * JDK Arrays的其他函数，如sort(), toString() 请直接调用
 * 
 * Common Lang ArrayUtils的其他函数，如subarray(),reverse(),indexOf(), 请直接调用
 */
public class ArrayUtil {

	/**
	 * 传入类型与大小创建数组.
	 * 
	 * Array.newInstance()的性能并不差
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] newArray(Class<T> type, int length) {
		return (T[]) Array.newInstance(type, length);
	}

	/**
	 * 从collection转为Array, 以 list.toArray(new String[0]); 最快 不需要创建list.size()的数组.
	 * 
	 * 本函数等价于list.toArray(new String[0]); 用户也可以直接用后者.
	 * 
	 * https://shipilev.net/blog/2016/arrays-wisdom-ancients/
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(Collection<T> col, Class<T> type) {
		return col.toArray((T[]) Array.newInstance(type, 0));
	}

	/**
	 * Swaps the two specified elements in the specified array.
	 */
	private static void swap(Object[] arr, int i, int j) {
		Object tmp = arr[i];
		arr[i] = arr[j];
		arr[j] = tmp;
	}

	/**
	 * 将传入的数组乱序
	 */
	public static <T> T[] shuffle(T[] array) {
		if (array != null && array.length > 1) {
			Random rand = new Random();
			return shuffle(array, rand);
		} else {
			return array;
		}
	}

	/**
	 * 将传入的数组乱序
	 */
	public static <T> T[] shuffle(T[] array, Random random) {
		if (array != null && array.length > 1 && random != null) {
			for (int i = array.length; i > 1; i--) {
				swap(array, i - 1, random.nextInt(i));
			}
		}
		return array;
	}

	/**
	 * 添加元素到数组头.
	 */
	public static <T> T[] concat(@Nullable T element, T[] array) {
		return ObjectArrays.concat(element, array);
	}

	/**
	 * 添加元素到数组末尾.
	 */
	public static <T> T[] concat(T[] array, @Nullable T element) {
		return ObjectArrays.concat(array, element);
	}

	////////////////// guava Array 转换为底层为原子类型的List ///////////
	/**
	 * 原版将数组转换为List.
	 * 
	 * 注意转换后的List不能写入, 否则抛出UnsupportedOperationException
	 * 
	 * @see java.util.Arrays#asList(Object...)
	 */
	public static <T> List<T> asList(T... a) {
		return Arrays.asList(a);
	}

	/**
	 * Arrays.asList()的加强版, 返回一个底层为原始类型int的List
	 * 
	 * 与保存Integer相比节约空间，同时只在读取数据时AutoBoxing.
	 * 
	 * @see java.util.Arrays#asList(Object...)
	 * @see com.google.common.primitives.Ints#asList(int...)
	 * 
	 */
	public static List<Integer> intAsList(int... backingArray) {
		return Ints.asList(backingArray);
	}

	/**
	 * Arrays.asList()的加强版, 返回一个底层为原始类型long的List
	 * 
	 * 与保存Long相比节约空间，同时只在读取数据时AutoBoxing.
	 * 
	 * @see java.util.Arrays#asList(Object...)
	 * @see com.google.common.primitives.Longs#asList(long...)
	 */
	public static List<Long> longAsList(long... backingArray) {
		return Longs.asList(backingArray);
	}

	/**
	 * Arrays.asList()的加强版, 返回一个底层为原始类型double的List
	 * 
	 * 与保存Double相比节约空间，同时也避免了AutoBoxing.
	 * 
	 * @see java.util.Arrays#asList(Object...)
	 * @see com.google.common.primitives.Doubles#asList(double...)
	 */
	public static List<Double> doubleAsList(double... backingArray) {
		return Doubles.asList(backingArray);
	}

}
