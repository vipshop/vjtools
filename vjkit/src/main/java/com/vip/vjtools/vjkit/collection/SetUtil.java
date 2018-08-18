package com.vip.vjtools.vjkit.collection;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.Sets;
import com.vip.vjtools.vjkit.base.annotation.Nullable;
import com.vip.vjtools.vjkit.collection.type.ConcurrentHashSet;

/**
 * 关于Set的工具集合.
 * 
 * 1. 各种Set的创建函数, 包括ConcurrentHashSet
 * 
 * 2. 集合运算函数(交集，并集等,from guava)
 */
public class SetUtil {

	/**
	 * 根据等号左边的类型，构造类型正确的HashSet.
	 * 
	 * @see com.google.common.collect.Sets#newHashSet()
	 */
	public static <T> HashSet<T> newHashSet() {
		return new HashSet<>();
	}

	/**
	 * 根据等号左边的类型，构造类型正确的HashSet, 并初始化元素.
	 * 
	 * @see com.google.common.collect.Sets#newHashSet(Object...)
	 */
	@SuppressWarnings("unchecked")
	public static <T> HashSet<T> newHashSet(T... elements) {
		return Sets.newHashSet(elements);
	}

	/**
	 * HashSet涉及HashMap大小，因此建议在构造时传入需要初始的集合，其他如TreeSet不需要.
	 * 
	 * @see com.google.common.collect.Sets#newHashSet(Iterable)
	 */
	public static <T> HashSet<T> newHashSet(Iterable<? extends T> elements) {
		return Sets.newHashSet(elements);
	}

	/**
	 * 创建HashSet并设置初始大小，因为HashSet内部是HashMap，会计算LoadFactor后的真实大小.
	 * 
	 * @see com.google.common.collect.Sets#newHashSetWithExpectedSize(int)
	 */
	public static <T> HashSet<T> newHashSetWithCapacity(int expectedSize) {
		return Sets.newHashSetWithExpectedSize(expectedSize);
	}

	/**
	 * 根据等号左边的类型，构造类型正确的TreeSet, 通过实现了Comparable的元素自身进行排序.
	 * 
	 * @see com.google.common.collect.Sets#newTreeSet()
	 */
	@SuppressWarnings("rawtypes")
	public static <T extends Comparable> TreeSet<T> newSortedSet() {
		return new TreeSet<>();
	}

	/**
	 * 根据等号左边的类型，构造类型正确的TreeSet, 并设置comparator.
	 * 
	 * @see com.google.common.collect.Sets#newTreeSet(Comparator)
	 */
	public static <T> TreeSet<T> newSortedSet(@Nullable Comparator<? super T> comparator) {
		return Sets.newTreeSet(comparator);
	}

	/**
	 * 根据等号左边的类型，构造类型正确的ConcurrentHashSet
	 */
	public static <T> ConcurrentHashSet<T> newConcurrentHashSet() {
		return new ConcurrentHashSet<>();
	}

	///////////////// from JDK Collections的常用构造函数 ///////////////////

	/**
	 * 返回一个空的结构特殊的Set，节约空间.
	 * 
	 * 注意返回的Set不可写, 写入会抛出UnsupportedOperationException.
	 * 
	 * @see java.util.Collections#emptySet()
	 */
	public static final <T> Set<T> emptySet() {
		return Collections.emptySet();
	}

	/**
	 * 如果set为null，转化为一个安全的空Set.
	 * 
	 * 注意返回的Set不可写, 写入会抛出UnsupportedOperationException.
	 * 
	 * @see java.util.Collections#emptySet()
	 */
	public static <T> Set<T> emptySetIfNull(final Set<T> set) {
		return set == null ? (Set<T>) Collections.EMPTY_SET : set;
	}

	/**
	 * 返回只含一个元素但结构特殊的Set，节约空间.
	 * 
	 * 注意返回的Set不可写, 写入会抛出UnsupportedOperationException.
	 * 
	 * @see java.util.Collections#singleton(Object)
	 */
	public static final <T> Set<T> singletonSet(T o) {
		return Collections.singleton(o);
	}

	/**
	 * 返回包装后不可修改的Set.
	 * 
	 * 如果尝试修改，会抛出UnsupportedOperationException
	 * 
	 * @see java.util.Collections#unmodifiableSet(Set)
	 */
	public static <T> Set<T> unmodifiableSet(Set<? extends T> s) {
		return Collections.unmodifiableSet(s);
	}

	/**
	 * 从Map构造Set的大杀器, 可以用来制造各种Set
	 * 
	 * @see java.util.Collections#newSetFromMap(Map)
	 */
	public static <T> Set<T> newSetFromMap(Map<T, Boolean> map) {
		return Collections.newSetFromMap(map);
	}

	//////////////// from guava的集合运算函数/////////////
	/**
	 * set1, set2的并集（在set1或set2的对象）的只读view，不复制产生新的Set对象.
	 * 
	 * 如果尝试写入该View会抛出UnsupportedOperationException
	 */
	public static <E> Set<E> unionView(final Set<? extends E> set1, final Set<? extends E> set2) {
		return Sets.union(set1, set2);
	}

	/**
	 * set1, set2的交集（同时在set1和set2的对象）的只读view，不复制产生新的Set对象.
	 * 
	 * 如果尝试写入该View会抛出UnsupportedOperationException
	 */
	public static <E> Set<E> intersectionView(final Set<E> set1, final Set<?> set2) {
		return Sets.intersection(set1, set2);
	}

	/**
	 * set1, set2的差集（在set1，不在set2中的对象）的只读view，不复制产生新的Set对象.
	 * 
	 * 如果尝试写入该View会抛出UnsupportedOperationException
	 */
	public static <E> Set<E> differenceView(final Set<E> set1, final Set<?> set2) {
		return Sets.difference(set1, set2);
	}

	/**
	 * set1, set2的补集（在set1或set2中，但不在交集中的对象，又叫反交集）的只读view，不复制产生新的Set对象.
	 * 
	 * 如果尝试写入该View会抛出UnsupportedOperationException
	 */
	public static <E> Set<E> disjointView(final Set<? extends E> set1, final Set<? extends E> set2) {
		return Sets.symmetricDifference(set1, set2);
	}
}
