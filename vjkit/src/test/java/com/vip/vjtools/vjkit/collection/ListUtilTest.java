package com.vip.vjtools.vjkit.collection;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Random;

import org.junit.Test;

import com.google.common.collect.Ordering;

public class ListUtilTest {
	@Test
	public void guavaBuildList() {
		List<String> list1 = ListUtil.newArrayList();

		List<String> list2 = ListUtil.newArrayList("a", "b");
		assertThat(list2).hasSize(2).containsExactly("a", "b");

		List<String> list3 = ListUtil.newArrayList(SetUtil.newHashSet("a", "b"));
		assertThat(list2).hasSize(2).containsExactly("a", "b");

		List<String> list4 = ListUtil.newArrayListWithCapacity(10);

		List<String> list5 = ListUtil.newCopyOnWriteArrayList();

		List<String> list6 = ListUtil.newCopyOnWriteArrayList("a", "b");
		assertThat(list6).hasSize(2).containsExactly("a", "b");

		List<String> list7 = ListUtil.newLinkedList();
	}

	@Test
	public void jdkBuild() {
		List<String> list1 = ListUtil.emptyList();

		assertThat(list1).hasSize(0);

		List<String> list2 = ListUtil.emptyListIfNull(null);
		assertThat(list2).isNotNull().hasSize(0);

		List<String> list3 = ListUtil.emptyListIfNull(list1);
		assertThat(list3).isSameAs(list1);

		try {
			list1.add("a");
			fail("should fail before");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(UnsupportedOperationException.class);
		}

		List<String> list4 = ListUtil.singletonList("1");
		assertThat(list4).hasSize(1).contains("1");
		try {
			list4.add("a");
			fail("should fail before");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(UnsupportedOperationException.class);
		}

		List<String> list5 = ListUtil.newArrayList();
		List<String> list6 = ListUtil.unmodifiableList(list5);

		try {
			list6.add("a");
			fail("should fail before");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(UnsupportedOperationException.class);
		}

		List<String> list7 = ListUtil.synchronizedList(list6);
	}

	@Test
	public void general() {
		List<String> list1 = ListUtil.newArrayList();

		List<String> list2 = ListUtil.newArrayList("a", "b", "c");
		List<String> list3 = ListUtil.newArrayList("a");

		assertThat(ListUtil.isEmpty(list1)).isTrue();
		assertThat(ListUtil.isEmpty(null)).isTrue();
		assertThat(ListUtil.isEmpty(list2)).isFalse();

		assertThat(ListUtil.isNotEmpty(list1)).isFalse();
		assertThat(ListUtil.isNotEmpty(null)).isFalse();
		assertThat(ListUtil.isNotEmpty(list2)).isTrue();

		assertThat(ListUtil.getFirst(list2)).isEqualTo("a");
		assertThat(ListUtil.getLast(list2)).isEqualTo("c");

		assertThat(ListUtil.getFirst(list3)).isEqualTo("a");
		assertThat(ListUtil.getLast(list3)).isEqualTo("a");

		assertThat(ListUtil.getFirst(list1)).isNull();
		assertThat((List<String>) ListUtil.getFirst(null)).isNull();
	}

	@Test
	public void sortAndSearch() {

		List<String> list = ListUtil.newArrayList("d", "a", "c", "b", "e", "i", "g");
		ListUtil.sort(list);

		assertThat(list).hasSize(7).containsExactly("a", "b", "c", "d", "e", "g", "i");

		ListUtil.shuffle(list);
		ListUtil.shuffle(list, new Random());
		System.out.println("shuffle list:" + list);

		ListUtil.sort(list, Ordering.natural());

		assertThat(list).hasSize(7).containsExactly("a", "b", "c", "d", "e", "g", "i");

		assertThat(ListUtil.binarySearch(list, "b")).isEqualTo(1);
		assertThat(ListUtil.binarySearch(list, "b", Ordering.natural())).isEqualTo(1);
		assertThat(ListUtil.binarySearch(list, "x")).isEqualTo(-8);

		// reverse
		List list8 = ListUtil.reverse(list);
		assertThat(list8).hasSize(7).containsExactly("i", "g", "e", "d", "c", "b", "a");

		// sortReverse
		ListUtil.shuffle(list8);
		ListUtil.sortReverse(list8);
		assertThat(list8).hasSize(7).containsExactly("i", "g", "e", "d", "c", "b", "a");

		ListUtil.shuffle(list8);
		ListUtil.sortReverse(list8, Ordering.natural());
		assertThat(list8).hasSize(7).containsExactly("i", "g", "e", "d", "c", "b", "a");
	}


	@Test
	public void collectionCalc() {
		List<String> list1 = ListUtil.newArrayList("1", "2", "3", "6", "6");
		List<String> list2 = ListUtil.newArrayList("4", "5", "6", "7", "6", "6");

		List<String> result = ListUtil.union(list1, list2);
		assertThat(result).containsExactly("1", "2", "3", "6", "6", "4", "5", "6", "7", "6", "6");

		List<String> result2 = ListUtil.intersection(list1, list2);
		assertThat(result2).containsExactly("6", "6");

		List<String> result3 = ListUtil.difference(list2, list1);
		assertThat(result3).containsExactly("4", "5", "7", "6");

		List<String> result4 = ListUtil.disjoint(list1, list2);
		assertThat(result4).containsExactly("1", "2", "3", "4", "5", "7", "6");
	}
}
