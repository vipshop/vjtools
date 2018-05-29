package com.vip.vjtools.vjkit.collection.type;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

import com.vip.vjtools.vjkit.collection.SetUtil;

public class ConcurrentHashSetTest {

	@Test
	public void concurrentHashSet() {
		ConcurrentHashSet<String> conrrentHashSet = SetUtil.newConcurrentHashSet();
		conrrentHashSet.add("a");
		conrrentHashSet.add("b");
		conrrentHashSet.add("c");

		assertThat(conrrentHashSet.isEmpty()).isFalse();
		assertThat(conrrentHashSet.contains("a")).isTrue();
		assertThat(conrrentHashSet.contains("d")).isFalse();

		assertThat(conrrentHashSet).hasSize(3).contains("a", "b", "c");

		for (String key : conrrentHashSet) {
			System.out.print(key + ",");
		}

		conrrentHashSet.remove("c");
		assertThat(conrrentHashSet).hasSize(2);

		Object[] strings = conrrentHashSet.toArray();
		assertThat(strings).hasSize(2).contains("a", "b");

		conrrentHashSet.toArray(new String[conrrentHashSet.size()]);
		conrrentHashSet.hashCode();
		conrrentHashSet.toString();

		ConcurrentHashSet<String> conrrentHashSet2 = SetUtil.newConcurrentHashSet();
		conrrentHashSet2.add("a");

		assertThat(conrrentHashSet.equals(conrrentHashSet)).isTrue();
		assertThat(conrrentHashSet.equals(conrrentHashSet2)).isFalse();

		assertThat(conrrentHashSet.containsAll(conrrentHashSet2)).isTrue();

		conrrentHashSet.retainAll(conrrentHashSet2);
		assertThat(conrrentHashSet).hasSize(1).contains("a");
		assertThat(conrrentHashSet.equals(conrrentHashSet2)).isTrue();

		conrrentHashSet.removeAll(conrrentHashSet2);
		assertThat(conrrentHashSet.isEmpty()).isTrue();

		conrrentHashSet2.clear();
		assertThat(conrrentHashSet2.isEmpty()).isTrue();
	}

}
