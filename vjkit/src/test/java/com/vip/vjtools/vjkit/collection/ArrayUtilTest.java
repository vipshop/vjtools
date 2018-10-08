package com.vip.vjtools.vjkit.collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.vip.vjtools.vjkit.number.RandomUtil;

public class ArrayUtilTest {

	@Test
	public void shuffle() {
		String[] arrays = new String[] { "d", "a", "c", "b", "e", "i", "g" };
		String[] arraysClone = Arrays.copyOf(arrays, arrays.length);
		Arrays.sort(arrays);
		assertThat(arrays).containsExactly("a", "b", "c", "d", "e", "g", "i");
		ArrayUtil.shuffle(arrays);
		assertFalse("should not be equal to origin array", Arrays.equals(arrays, arraysClone));
		// System.out.println(Arrays.toString(arrays));
		Arrays.sort(arrays);
		ArrayUtil.shuffle(arrays, RandomUtil.secureRandom());
		assertFalse("should not be equal to origin array", Arrays.equals(arrays, arraysClone));
	}

	@Test
	public void asList() {
		List<String> list = ArrayUtil.asList("d", "a", "c", "b", "e", "i", "g");
		assertThat(list).hasSize(7).containsExactly("d", "a", "c", "b", "e", "i", "g");

		try {
			list.add("a");
			fail("should fail before");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(UnsupportedOperationException.class);
		}


		List<Integer> list3 = ArrayUtil.intAsList(1, 2, 3);
		assertThat(list3).hasSize(3).containsExactly(1, 2, 3);

		List<Long> list4 = ArrayUtil.longAsList(1L, 2L, 3L);
		assertThat(list4).hasSize(3).containsExactly(1L, 2L, 3L);

		List<Double> list5 = ArrayUtil.doubleAsList(1.1d, 2.2d, 3.3d);
		assertThat(list5).hasSize(3).containsExactly(1.1d, 2.2d, 3.3d);
	}

	@Test
	public void contact() {
		String[] array = new String[] { "d", "a", "c" };
		assertThat(ArrayUtil.concat("z", array)).containsExactly("z", "d", "a", "c");
		assertThat(ArrayUtil.concat(array, "z")).containsExactly("d", "a", "c", "z");
	}

}
