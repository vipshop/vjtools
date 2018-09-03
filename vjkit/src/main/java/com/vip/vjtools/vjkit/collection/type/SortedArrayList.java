// Copyright (c) 2003-present, Jodd Team (http://jodd.org)
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.
package com.vip.vjtools.vjkit.collection.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * 从Jodd整体复制，部分指定了index的操作不支持，如 add(index, element)
 * 
 * 修改包括：改进Comparator泛型定义，findInsertionPoint的位移改进
 * 
 * https://github.com/oblac/jodd/blob/master/jodd-core/src/main/java/jodd/util/collection/SortedArrayList.java
 * 
 * An extension of <code>ArrayList</code> that insures that all of the items
 * added are sorted. <b>This breaks original list contract!</b>.
 * A binary search method is used to provide a quick way to
 * auto sort this list.Note: Not all methods for adding and
 * removing elements are supported.
 */
public final class SortedArrayList<E> extends ArrayList<E> {

	private static final long serialVersionUID = -8301136559614447593L;

	protected final Comparator<? super E> comparator;

	/**
	 * Constructs a new <code>SortedArrayList</code>.
	 */
	public SortedArrayList(Comparator<? super E> c) {
		comparator = c;
	}

	/**
	 * Constructs a new <code>SortedArrayList</code> expecting
	 * elements are comparable.
	 */
	public SortedArrayList() {
		comparator = null;
	}

	/**
	 * Constructs a new <code>SortedArrayList</code> expecting
	 * elements are comparable.
	 */
	public SortedArrayList(Collection<? extends E> c) {
		comparator = null;
		addAll(c);
	}

	/**
	 * Returns comparator assigned to this collection, if such exist.
	 */
	public Comparator getComparator() {
		return comparator;
	}

	// ---------------------------------------------------------------- override

	/**
	 * Adds an Object to sorted list. Object is inserted at correct place, found
	 * using binary search. If the same item exist, it will be put to the end of
	 * the range.
	 * <p>
	 * This method breaks original list contract since objects are not
	 * added at the list end, but in sorted manner.
	 */
	@Override
	public boolean add(E o) {
		int idx = 0;
		if (!isEmpty()) {
			idx = findInsertionPoint(o);
		}
		super.add(idx, o);
		return true;
	}

	/**
	 * Add all of the elements in the given collection to this list.
	 */
	@Override
	public boolean addAll(Collection<? extends E> c) {
		Iterator<? extends E> i = c.iterator();
		boolean changed = false;
		while (i.hasNext()) {
			boolean ret = add(i.next());
			if (!changed) {
				changed = ret;
			}
		}
		return changed;
	}

	/**
	 * Finds the index at which object should be inserted.
	 */
	public int findInsertionPoint(E o) {
		return findInsertionPoint(o, 0, size() - 1);
	}

	// ---------------------------------------------------------------- unsupported methods

	/**
	 * @throws UnsupportedOperationException This method not supported.
	 */
	@Override
	@Deprecated
	public void add(int index, E element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException This method not supported.
	 */
	@Override
	@Deprecated
	public E set(int index, E element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException This method not supported.
	 */
	@Override
	@Deprecated
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}


	// ---------------------------------------------------------------- sorting

	/**
	 * Compares two keys using the correct comparison method for this
	 * collection.
	 */
	@SuppressWarnings({ "unchecked" })
	protected int compare(E k1, E k2) {
		if (comparator == null) {
			return ((Comparable) k1).compareTo(k2);
		}
		return comparator.compare(k1, k2);
	}

	/**
	 * Conducts a binary search to find the index where Object
	 * should be inserted.
	 */
	protected int findInsertionPoint(E o, int originalLow, int originalHigh) {
		int low = originalLow;
		int high = originalHigh;
		while (low <= high) {
			int mid = low + ((high - low) >>> 1);
			int delta = compare(get(mid), o);

			if (delta > 0) {
				high = mid - 1;
			} else {
				low = mid + 1;
			}
		}

		return low;
	}

}
