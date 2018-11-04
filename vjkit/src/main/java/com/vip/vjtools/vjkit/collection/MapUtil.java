package com.vip.vjtools.vjkit.collection;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.commons.lang3.Validate;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.vip.vjtools.vjkit.base.annotation.NotNull;
import com.vip.vjtools.vjkit.base.annotation.Nullable;

/**
 * 关于Map的工具集合，
 * 
 * 1. 常用函数(如是否为空, 两个map的Diff对比，针对value值的排序)
 * 
 * 2. 对于并发Map，增加putIfAbsent(返回最终值版), createIfAbsent这两个重要函数(from Common Lang)
 * 
 * 3. 便捷的构造函数(via guava,Java Collections，并增加了用数组，List等方式初始化Map的函数)
 * 
 * 4. JDK Collections的empty,singleton
 */
@SuppressWarnings("unchecked")
public class MapUtil {

	public static final float DEFAULT_LOAD_FACTOR = 0.75f;

	/**
	 * 判断是否为空.
	 */
	public static boolean isEmpty(final Map<?, ?> map) {
		return (map == null) || map.isEmpty();
	}

	/**
	 * 判断是否为空.
	 */
	public static boolean isNotEmpty(final Map<?, ?> map) {
		return (map != null) && !map.isEmpty();
	}

	/**
	 * ConcurrentMap的putIfAbsent()返回之前的Value，此函数封装返回最终存储在Map中的Value
	 * 
	 * @see org.apache.commons.lang3.concurrent.ConcurrentUtils#putIfAbsent(ConcurrentMap, Object, Object)
	 */
	public static <K, V> V putIfAbsentReturnLast(@NotNull final ConcurrentMap<K, V> map, final K key, final V value) {
		final V result = map.putIfAbsent(key, value);
		return result != null ? result : value;
	}

	/**
	 * 如果Key不存在则创建，返回最后存储在Map中的Value.
	 * 
	 * 如果创建Value对象有一定成本, 直接使用PutIfAbsent可能重复浪费，则使用此类，传入一个被回调的ValueCreator，Lazy创建对象。
	 * 
	 * @see org.apache.commons.lang3.concurrent.ConcurrentUtils#createIfAbsent(ConcurrentMap, Object,
	 * org.apache.commons.lang3.concurrent.ConcurrentInitializer)
	 */
	public static <K, V> V createIfAbsentReturnLast(@NotNull final ConcurrentMap<K, V> map, final K key,
			@NotNull final ValueCreator<? extends V> creator) {
		final V value = map.get(key);
		if (value == null) {
			return putIfAbsentReturnLast(map, key, creator.get());
		}
		return value;
	}

	/**
	 * Lazy创建Value值的回调类
	 * 
	 * @see MapUtil#createIfAbsentReturnLast(ConcurrentMap, Object, ValueCreator)
	 */
	public interface ValueCreator<T> {
		/**
		 * 创建对象
		 */
		T get();
	}

	///////////////// from Guava的构造函数///////////////////

	/**
	 * 根据等号左边的类型, 构造类型正确的HashMap.
	 * 
	 * 未初始化数组大小, 默认为16个桶.
	 * 
	 * @deprecated JDK7开始已经简化 
	 */
	@Deprecated
	public static <K, V> HashMap<K, V> newHashMap() {
		return new HashMap<K, V>();
	}

	/**
	 * 根据等号左边的类型, 构造类型正确的HashMap.
	 * 
	 * 注意HashMap中有0.75的加载因子的影响, 需要进行运算后才能正确初始化HashMap的大小.
	 * 
	 * 加载因子也是HashMap中减少Hash冲突的重要一环，如果读写频繁，总记录数不多的Map，可以比默认值0.75进一步降低，建议0.5
	 * 
	 * @see com.google.common.collect.Maps#newHashMap(int)
	 */
	public static <K, V> HashMap<K, V> newHashMapWithCapacity(int expectedSize, float loadFactor) {
		int finalSize = (int) (expectedSize / loadFactor + 1.0F);
		return new HashMap<K, V>(finalSize, loadFactor);
	}

	/**
	 * 根据等号左边的类型, 构造类型正确的HashMap.
	 * 
	 * 同时初始化第一个元素
	 */
	public static <K, V> HashMap<K, V> newHashMap(final K key, final V value) {
		HashMap<K, V> map = new HashMap<K, V>();
		map.put(key, value);
		return map;
	}

	/**
	 * 根据等号左边的类型, 构造类型正确的HashMap.
	 * 
	 * 同时初始化元素.
	 */
	public static <K, V> HashMap<K, V> newHashMap(@NotNull final K[] keys, @NotNull final V[] values) {
		Validate.isTrue(keys.length == values.length, "keys.length is %d but values.length is %d", keys.length,
				values.length);

		HashMap<K, V> map = new HashMap<K, V>(keys.length * 2);

		for (int i = 0; i < keys.length; i++) {
			map.put(keys[i], values[i]);
		}

		return map;
	}

	/**
	 * 根据等号左边的类型, 构造类型正确的HashMap.
	 * 
	 * 同时初始化元素.
	 */
	public static <K, V> HashMap<K, V> newHashMap(@NotNull final List<K> keys, @NotNull final List<V> values) {
		Validate.isTrue(keys.size() == values.size(), "keys.length is %s  but values.length is %s", keys.size(),
				values.size());

		HashMap<K, V> map = new HashMap<K, V>(keys.size() * 2);
		Iterator<K> keyIt = keys.iterator();
		Iterator<V> valueIt = values.iterator();

		while (keyIt.hasNext()) {
			map.put(keyIt.next(), valueIt.next());
		}

		return map;
	}

	/**
	 * 根据等号左边的类型，构造类型正确的TreeMap.
	 * 
	 * @see com.google.common.collect.Maps#newTreeMap()
	 */
	@SuppressWarnings("rawtypes")
	public static <K extends Comparable, V> TreeMap<K, V> newSortedMap() {
		return new TreeMap<K, V>();
	}

	/**
	 * 根据等号左边的类型，构造类型正确的TreeMap.
	 * 
	 * @see com.google.common.collect.Maps#newTreeMap(Comparator)
	 */
	public static <C, K extends C, V> TreeMap<K, V> newSortedMap(@Nullable Comparator<C> comparator) {
		return Maps.newTreeMap(comparator);
	}

	/**
	 * 相比HashMap，当key是枚举类时, 性能与空间占用俱佳.
	 */
	public static <K extends Enum<K>, V> EnumMap<K, V> newEnumMap(@NotNull Class<K> type) {
		return new EnumMap<K, V>(Preconditions.checkNotNull(type));
	}

	/**
	 * 根据等号左边的类型，构造类型正确的ConcurrentHashMap.
	 */
	public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap() {
		return new ConcurrentHashMap<K, V>();
	}

	/**
	 * 根据等号左边的类型，构造类型正确的ConcurrentSkipListMap.
	 */
	public static <K, V> ConcurrentSkipListMap<K, V> newConcurrentSortedMap() {
		return new ConcurrentSkipListMap<K, V>();
	}

	///////////////// from JDK Collections的常用构造函数 ///////////////////

	/**
	 * 返回一个空的结构特殊的Map，节约空间.
	 * 
	 * 注意返回的Map不可写, 写入会抛出UnsupportedOperationException.
	 * 
	 * @see java.util.Collections#emptyMap()
	 */
	public static final <K, V> Map<K, V> emptyMap() {
		return Collections.emptyMap();
	}

	/**
	 * 如果map为null，转化为一个安全的空Map.
	 * 
	 * 注意返回的Map不可写, 写入会抛出UnsupportedOperationException.
	 * 
	 * @see java.util.Collections#emptyMap()
	 */
	public static <K, V> Map<K, V> emptyMapIfNull(final Map<K, V> map) {
		return map == null ? (Map<K, V>) Collections.EMPTY_MAP : map;
	}

	/**
	 * 返回一个只含一个元素但结构特殊的Map，节约空间.
	 * 
	 * 注意返回的Map不可写, 写入会抛出UnsupportedOperationException.
	 * 
	 * @see java.util.Collections#singletonMap(Object, Object)
	 */
	public static <K, V> Map<K, V> singletonMap(final K key, final V value) {
		return Collections.singletonMap(key, value);
	}

	/**
	 * 返回包装后不可修改的Map.
	 * 
	 * 如果尝试修改，会抛出UnsupportedOperationException
	 * 
	 * @see java.util.Collections#unmodifiableMap(Map)
	 */
	public static <K, V> Map<K, V> unmodifiableMap(final Map<? extends K, ? extends V> m) {
		return Collections.unmodifiableMap(m);
	}

	/**
	 * 返回包装后不可修改的有序Map.
	 * 
	 * @see java.util.Collections#unmodifiableSortedMap(SortedMap)
	 */
	public static <K, V> SortedMap<K, V> unmodifiableSortedMap(final SortedMap<K, ? extends V> m) {
		return Collections.unmodifiableSortedMap(m);
	}

	//////// 对两个Map进行diff的操作 ///////
	/**
	 * 对两个Map进行比较，返回MapDifference，然后各种妙用.
	 * 
	 * 包括key的差集，key的交集，以及key相同但value不同的元素。
	 * 
	 * @see com.google.common.collect.MapDifference
	 */
	public static <K, V> MapDifference<K, V> difference(Map<? extends K, ? extends V> left,
			Map<? extends K, ? extends V> right) {
		return Maps.difference(left, right);
	}

	//////////// 按值排序及取TOP N的操作 /////////
	/**
	 * 对一个Map按Value进行排序，返回排序LinkedHashMap，多用于Value是Counter的情况.
	 * 
	 * @param reverse 按Value的倒序 or 正序排列
	 */
	public static <K, V extends Comparable> Map<K, V> sortByValue(Map<K, V> map, final boolean reverse) {
		return sortByValueInternal(map, reverse ? Ordering.from(new ComparableEntryValueComparator<K, V>()).reverse()
				: new ComparableEntryValueComparator<K, V>());
	}

	/**
	 * 对一个Map按Value进行排序，返回排序LinkedHashMap.
	 */
	public static <K, V> Map<K, V> sortByValue(Map<K, V> map, final Comparator<? super V> comparator) {
		return sortByValueInternal(map, new EntryValueComparator<K, V>(comparator));
	}

	private static <K, V> Map<K, V> sortByValueInternal(Map<K, V> map, Comparator<Entry<K, V>> comparator) {
		Set<Entry<K, V>> entrySet = map.entrySet();
		Entry<K, V>[] entryArray = entrySet.toArray(new Entry[0]);

		Arrays.sort(entryArray, comparator);

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Entry<K, V> entry : entryArray) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * 对一个Map按Value进行排序，返回排序LinkedHashMap，最多只返回n条，多用于Value是Counter的情况.
	 * @param reverse 按Value的倒序 or 正序排列
	 */
	public static <K, V extends Comparable> Map<K, V> topNByValue(Map<K, V> map, final boolean reverse, int n) {
		return topNByValueInternal(map, n, reverse ? Ordering.from(new ComparableEntryValueComparator<K, V>()).reverse()
				: new ComparableEntryValueComparator<K, V>());
	}

	/**
	 * 对一个Map按Value进行排序，返回排序LinkedHashMap, 最多只返回n条，多用于Value是Counter的情况.
	 */
	public static <K, V> Map<K, V> topNByValue(Map<K, V> map, final Comparator<? super V> comparator, int n) {
		return topNByValueInternal(map, n, new EntryValueComparator<K, V>(comparator));
	}

	private static <K, V> Map<K, V> topNByValueInternal(Map<K, V> map, int n, Comparator<Entry<K, V>> comparator) {
		Set<Entry<K, V>> entrySet = map.entrySet();
		Entry<K, V>[] entryArray = entrySet.toArray(new Entry[0]);
		Arrays.sort(entryArray, comparator);

		Map<K, V> result = new LinkedHashMap<K, V>();
		int size = Math.min(n, entryArray.length);
		for (int i = 0; i < size; i++) {
			Entry<K, V> entry = entryArray[i];
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	private static final class ComparableEntryValueComparator<K, V extends Comparable>
			implements Comparator<Entry<K, V>> {
		@Override
		public int compare(Entry<K, V> o1, Entry<K, V> o2) {
			return (o1.getValue()).compareTo(o2.getValue());
		}
	}

	private static final class EntryValueComparator<K, V> implements Comparator<Entry<K, V>> {
		private final Comparator<? super V> comparator;

		private EntryValueComparator(Comparator<? super V> comparator2) {
			this.comparator = comparator2;
		}

		@Override
		public int compare(Entry<K, V> o1, Entry<K, V> o2) {
			return comparator.compare(o1.getValue(), o2.getValue());
		}
	}
}
