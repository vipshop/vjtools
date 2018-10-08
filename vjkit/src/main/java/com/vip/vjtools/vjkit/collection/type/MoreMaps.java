package com.vip.vjtools.vjkit.collection.type;

import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.MapMaker;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeRangeMap;
import com.google.common.util.concurrent.AtomicLongMap;
import com.vip.vjtools.vjkit.collection.type.primitive.IntObjectHashMap;
import com.vip.vjtools.vjkit.collection.type.primitive.LongObjectHashMap;

/**
 * 来自Guava，Netty等的特殊Map类型
 */
public class MoreMaps {

	/**
	 * 创建Key为弱引用的ConcurrentMap，Key对象可被回收.
	 * 
	 * JDK没有WeakHashMap的并发实现, 由Guava提供
	 */
	public static <K, V> ConcurrentMap<K, V> createWeakKeyConcurrentMap(int initialCapacity, int concurrencyLevel) {
		return new MapMaker().weakKeys().initialCapacity(initialCapacity).concurrencyLevel(concurrencyLevel).makeMap();
	}

	/**
	 * 创建Value为弱引用的ConcurrentMap，Value对象可被回收.
	 * 
	 * JDK没有WeakHashMap的并发实现, 由Guava提供
	 */
	public static <K, V> ConcurrentMap<K, V> createWeakValueConcurrentMap(int initialCapacity, int concurrencyLevel) {
		return new MapMaker().weakValues().initialCapacity(initialCapacity).concurrencyLevel(concurrencyLevel)
				.makeMap();
	}

	/**
	 * 创建移植自Netty的key为int的优化HashMap
	 * 
	 * @param initialCapacity 默认为8
	 * @param loadFactor 默认为0.5
	 */
	public static <V> IntObjectHashMap<V> createPrimitiveIntKeyMap(int initialCapacity, float loadFactor) {
		return new IntObjectHashMap<V>(initialCapacity, loadFactor);
	}

	/**
	 * 创建移植自Netty的key为long的优化HashMap
	 * 
	 * @param initialCapacity 默认为8
	 * @param loadFactor 默认为0.5
	 */
	public static <V> LongObjectHashMap<V> createPrimitiveLongKeyMap(int initialCapacity, float loadFactor) {
		return new LongObjectHashMap<V>(initialCapacity, loadFactor);
	}

	/**
	 * 创建值为可更改的Integer的HashMap. 可更改的Integer在更改时不需要重新创建Integer对象，节约了内存
	 * 
	 * @param initialCapacity 建议为16
	 * @param loadFactor 建议为0.5
	 */
	public static <K> HashMap<K, MutableInt> createMutableIntValueMap(int initialCapacity, float loadFactor) {
		return new HashMap<K, MutableInt>(initialCapacity, loadFactor);
	}

	/**
	 * 创建值为可更改的Long的HashMap. 可更改的Long在更改时不需要重新创建Long对象，节约了内存
	 * 
	 * @param initialCapacity 建议为16
	 * @param loadFactor 建议为0.5
	 */
	public static <K> HashMap<K, MutableLong> createMutableLongValueMap(int initialCapacity, float loadFactor) {
		return new HashMap<K, MutableLong>(initialCapacity, loadFactor);
	}

	/**
	 * 以Guava的AtomicLongMap，实现线程安全的HashMap<E,AtomicLong>结构的Counter
	 */
	public static <E> AtomicLongMap<E> createConcurrentCounterMap() {
		return AtomicLongMap.create();
	}

	/**
	 * 以Guava的MultiMap，实现的HashMap<E,List<V>>结构的一个Key对应多个值的map.
	 * 
	 * 注意非线程安全, MultiMap无线程安全的实现.
	 * 
	 * 另有其他结构存储values的MultiMap，请自行参考MultimapBuilder使用.
	 * 
	 * @param expectedKeys 默认为16
	 * @param expectedValuesPerKey 默认为3
	 */
	public static <K, V> ArrayListMultimap<K, V> createListMultiValueMap(int expectedKeys, int expectedValuesPerKey) {
		return ArrayListMultimap.create(expectedKeys, expectedValuesPerKey);
	}

	/**
	 * 以Guava的MultiMap，实现的HashMap<E,TreeSet<V>>结构的一个Key对应多个值的map.
	 * 
	 * 注意非线程安全, MultiValueMap无线程安全的实现.
	 * 
	 * 另有其他结构存储values的MultiMap，请自行参考MultimapBuilder使用.
	 */
	public static <K, V extends Comparable> SortedSetMultimap<K, V> createSortedSetMultiValueMap() {
		return MultimapBuilder.hashKeys().treeSetValues().build();
	}

	/**
	 * 以Guava的MultiMap，实现的HashMap<E,TreeSet<V>>结构的一个Key对应多个值的map.
	 * 
	 * 注意非线程安全, MultiValueMap无线程安全的实现.
	 * 
	 * 另有其他结构存储values的MultiMap，请自行参考MultimapBuilder使用.
	 */
	public static <K, V> SortedSetMultimap<K, V> createSortedSetMultiValueMap(Comparator<V> comparator) {
		return (SortedSetMultimap<K, V>) MultimapBuilder.hashKeys().treeSetValues(comparator);
	}

	/**
	 * 以Guava TreeRangeMap实现的, 一段范围的Key指向同一个Value的Map
	 */
	@SuppressWarnings("rawtypes")
	public static <K extends Comparable, V> TreeRangeMap<K, V> createRangeMap() {
		return TreeRangeMap.create();
	}

}
