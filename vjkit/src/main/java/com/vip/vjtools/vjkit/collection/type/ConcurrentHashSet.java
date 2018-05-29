package com.vip.vjtools.vjkit.collection.type;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JDK并没有提供ConcurrenHashSet，考虑到JDK的HashSet也是基于HashMap实现的，因此ConcurrenHashSet也由ConcurrenHashMap完成。
 * 
 * 虽然也可以通过Collections.newSetFromMap(new ConcurrentHashMap())，
 * 
 * 但声明一个单独的类型，阅读代码时能更清晰的知道set的并发友好性，代码来自JDK的SetFromMap，去除JDK8接口.
 */
public class ConcurrentHashSet<E> extends AbstractSet<E> implements Set<E>, java.io.Serializable {

	private static final long serialVersionUID = -8672117787651310382L;

	private final Map<E, Boolean> m;

	private transient Set<E> s; // Its keySet

	public ConcurrentHashSet() {
		m = new ConcurrentHashMap<E, Boolean>();
		s = m.keySet();
	}

	public void clear() {
		m.clear();
	}

	public int size() {
		return m.size();
	}

	public boolean isEmpty() {
		return m.isEmpty();
	}

	public boolean contains(Object o) {
		return m.containsKey(o);
	}

	public boolean remove(Object o) {
		return m.remove(o) != null;
	}

	public boolean add(E e) {
		return m.put(e, Boolean.TRUE) == null;
	}

	public Iterator<E> iterator() {
		return s.iterator();
	}

	public Object[] toArray() {
		return s.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return s.toArray(a);
	}

	@Override
	public String toString() {
		return s.toString();
	}

	public int hashCode() {
		return s.hashCode();
	}

	public boolean equals(Object o) {
		return o == this || s.equals(o);
	}

	public boolean containsAll(Collection<?> c) {
		return s.containsAll(c);
	}

	public boolean removeAll(Collection<?> c) {
		return s.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return s.retainAll(c);
	}
}
