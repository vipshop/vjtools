package com.vip.vjtools.vjkit.collection;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Ordering;

public class CollectionUtilTest {

	@Test
	public void test() {
		List<String> list1 = ListUtil.newArrayList();

		List<String> list2 = ListUtil.newArrayList("a", "b", "c");
		List<String> list3 = ListUtil.newArrayList("a");

		Set<String> set1 = SetUtil.newSortedSet();
		set1.add("a");
		set1.add("b");
		set1.add("c");

		Set<String> set2 = SetUtil.newSortedSet();
		set2.add("a");

		assertThat(CollectionUtil.isEmpty(list1)).isTrue();
		assertThat(CollectionUtil.isEmpty(null)).isTrue();
		assertThat(CollectionUtil.isEmpty(list2)).isFalse();

		assertThat(CollectionUtil.isNotEmpty(list1)).isFalse();
		assertThat(CollectionUtil.isNotEmpty(null)).isFalse();
		assertThat(CollectionUtil.isNotEmpty(list2)).isTrue();

		assertThat(CollectionUtil.getFirst(list2)).isEqualTo("a");
		assertThat(CollectionUtil.getLast(list2)).isEqualTo("c");

		assertThat(CollectionUtil.getFirst(set1)).isEqualTo("a");
		assertThat(CollectionUtil.getLast(set1)).isEqualTo("c");

		assertThat(CollectionUtil.getFirst(list3)).isEqualTo("a");
		assertThat(CollectionUtil.getLast(list3)).isEqualTo("a");

		assertThat(CollectionUtil.getFirst(set2)).isEqualTo("a");
		assertThat(CollectionUtil.getLast(set2)).isEqualTo("a");

		assertThat(CollectionUtil.getFirst(list1)).isNull();
		assertThat((List<String>) CollectionUtil.getFirst(null)).isNull();
		assertThat(CollectionUtil.getLast(list1)).isNull();
		assertThat((List<String>) CollectionUtil.getLast(null)).isNull();
	}

	@Test
	public void minAndMax() {
		List<Integer> list = ListUtil.newArrayList(4, 1, 9, 100, 20, 101, 40);

		assertThat(CollectionUtil.min(list)).isEqualTo(1);
		assertThat(CollectionUtil.min(list, Ordering.natural())).isEqualTo(1);
		assertThat(CollectionUtil.max(list)).isEqualTo(101);
		assertThat(CollectionUtil.max(list, Ordering.natural())).isEqualTo(101);

		assertThat(CollectionUtil.minAndMax(list).getLeft()).isEqualTo(1);
		assertThat(CollectionUtil.minAndMax(list).getRight()).isEqualTo(101);

		assertThat(CollectionUtil.minAndMax(list, Ordering.natural()).getLeft()).isEqualTo(1);
		assertThat(CollectionUtil.minAndMax(list, Ordering.natural()).getRight()).isEqualTo(101);
	}

	@Test
	public void listCompare() {
		List<String> list1 = ArrayUtil.asList("d", "a", "c", "b", "e", "i", "g");
		List<String> list2 = ArrayUtil.asList("d", "a", "c", "b", "e", "i", "g");

		List<String> list3 = ArrayUtil.asList("d", "c", "a", "b", "e", "i", "g");
		List<String> list4 = ArrayUtil.asList("d", "a", "c", "b", "e");
		List<String> list5 = ArrayUtil.asList("d", "a", "c", "b", "e", "i", "g", "x");

		assertThat(CollectionUtil.elementsEqual(list1, list1)).isTrue();
		assertThat(CollectionUtil.elementsEqual(list1, list2)).isTrue();

		assertThat(CollectionUtil.elementsEqual(list1, list3)).isFalse();
		assertThat(CollectionUtil.elementsEqual(list1, list4)).isFalse();
		assertThat(CollectionUtil.elementsEqual(list1, list5)).isFalse();
	}

	@Test
	public void topNAndBottomN() {
		List<Integer> list = ArrayUtil.asList(3, 5, 7, 4, 2, 6, 9);

		assertThat(CollectionUtil.topN(list, 3)).containsExactly(9, 7, 6);
		assertThat(CollectionUtil.topN(list, 3, Ordering.natural().reverse())).containsExactly(2, 3, 4);
		assertThat(CollectionUtil.bottomN(list, 3)).containsExactly(2, 3, 4);
		assertThat(CollectionUtil.bottomN(list, 3, Ordering.natural().reverse())).containsExactly(9, 7, 6);
	}
}
