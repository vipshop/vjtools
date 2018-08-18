package com.vip.vjtools.vjkit.collection;

import static org.assertj.core.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import com.google.common.collect.Ordering;
import com.vip.vjtools.vjkit.collection.type.ConcurrentHashSet;

public class SetUtilTest {

	@Test
	public void guavaBuildSet() {
		HashSet<String> set1 = SetUtil.newHashSet();

		HashSet<String> set2 = SetUtil.newHashSetWithCapacity(10);

		HashSet<String> set3 = SetUtil.newHashSet("1", "2", "2");

		assertThat(set3).hasSize(2).contains("1", "2");

		HashSet<String> set4 = SetUtil.newHashSet(ListUtil.newArrayList("1", "2", "2"));
		assertThat(set4).hasSize(2).contains("1", "2");

		TreeSet<String> set5 = SetUtil.newSortedSet();

		TreeSet<String> set6 = SetUtil.newSortedSet(Ordering.natural());

		ConcurrentHashSet set7 = SetUtil.newConcurrentHashSet();
	}

	@Test
	public void jdkBuildSet() {
		Set<String> set1 = SetUtil.emptySet();
		assertThat(set1).hasSize(0);

		Set<String> set2 = SetUtil.emptySetIfNull(null);
		assertThat(set2).isNotNull().hasSize(0);

		Set<String> set3 = SetUtil.emptySetIfNull(set1);
		assertThat(set3).isSameAs(set1);

		try {
			set1.add("a");
			fail("should fail before");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(UnsupportedOperationException.class);
		}

		Set<String> set4 = SetUtil.singletonSet("1");
		assertThat(set4).hasSize(1).contains("1");
		try {
			set4.add("a");
			fail("should fail before");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(UnsupportedOperationException.class);
		}

		Set<String> set5 = SetUtil.newHashSet();
		Set<String> set6 = SetUtil.unmodifiableSet(set5);

		try {
			set6.add("a");
			fail("should fail before");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(UnsupportedOperationException.class);
		}

		Set<String> set7 = SetUtil.newSetFromMap(MapUtil.<String, Boolean>newConcurrentSortedMap());
	}

	@Test
	public void collectionCaculate() {
		HashSet<String> set1 = SetUtil.newHashSet("1", "2", "3", "6");
		HashSet<String> set2 = SetUtil.newHashSet("4", "5", "6", "7");

		Set<String> set3 = SetUtil.unionView(set1, set2);
		assertThat(set3).hasSize(7).contains("1", "2", "3", "4", "5", "6", "7");

		Set<String> set4 = SetUtil.intersectionView(set1, set2);
		assertThat(set4).hasSize(1).contains("6");

		Set<String> set5 = SetUtil.differenceView(set1, set2);
		assertThat(set5).hasSize(3).contains("1", "2", "3");

		Set<String> set6 = SetUtil.disjointView(set1, set2);
		assertThat(set6).hasSize(6).contains("1", "2", "3", "4", "5", "7");

		try {
			set6.add("a");
			fail("should fail before");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(UnsupportedOperationException.class);
		}
	}

}
