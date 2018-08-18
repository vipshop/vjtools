package com.vip.vjtools.vjkit.base;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

import com.vip.vjtools.vjkit.base.type.Pair;
import com.vip.vjtools.vjkit.base.type.Triple;

public class PairTest {

	@Test
	public void pairTest() {
		Pair<String, Integer> pair = Pair.of("haha", 1);
		Pair<String, Integer> pair2 = Pair.of("haha", 2);
		Pair<String, Integer> pair3 = Pair.of("kaka", 1);

		assertThat(pair.equals(pair2)).isFalse();
		assertThat(pair.equals(pair3)).isFalse();
		assertThat(pair.hashCode() != pair2.hashCode()).isTrue();
		assertThat(pair.toString()).isEqualTo("Pair [left=haha, right=1]");

		assertThat(pair.getLeft()).isEqualTo("haha");
		assertThat(pair.getRight()).isEqualTo(1);
	}

	@Test
	public void tripleTest() {
		Triple<String, String, Integer> triple = Triple.of("haha", "hehe", 1);
		Triple<String, String, Integer> triple2 = Triple.of("haha", "hehe", 2);
		Triple<String, String, Integer> triple3 = Triple.of("haha", "lala", 2);
		Triple<String, String, Integer> triple4 = Triple.of("kaka", "lala", 2);


		Pair<String, Integer> pair = Pair.of("haha", 1);
		assertThat(triple.equals(triple2)).isFalse();
		assertThat(triple.equals(triple3)).isFalse();
		assertThat(triple.equals(triple4)).isFalse();
		assertThat(triple.equals(pair)).isFalse();
		assertThat(triple.hashCode() != triple2.hashCode()).isTrue();
		assertThat(triple.toString()).isEqualTo("Triple [left=haha, middle=hehe, right=1]");

		assertThat(triple.getLeft()).isEqualTo("haha");
		assertThat(triple.getMiddle()).isEqualTo("hehe");
		assertThat(triple.getRight()).isEqualTo(1);
	}

}
