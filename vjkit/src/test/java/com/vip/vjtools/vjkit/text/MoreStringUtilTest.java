package com.vip.vjtools.vjkit.text;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.Test;

import com.google.common.base.Splitter;

public class MoreStringUtilTest {
	@Test
	public void split() {

		List<String> result = MoreStringUtil.split("192.168.0.1", '.', 4);
		assertThat(result).hasSize(4).containsSequence("192", "168", "0", "1");

		result = MoreStringUtil.split("192.168..1", '.', 4);
		assertThat(result).hasSize(3).containsSequence("192", "168", "1");

		result = MoreStringUtil.split("192.168.0.", '.', 4);
		assertThat(result).hasSize(3).containsSequence("192", "168", "0");

		assertThat(MoreStringUtil.split(null, '.', 4)).isNull();

		assertThat(MoreStringUtil.split("", '.', 4)).hasSize(0);

		Splitter splitter = MoreStringUtil.charsSplitter("/\\").omitEmptyStrings();
		result = splitter.splitToList("/a/b/c");
		assertThat(result).hasSize(3).containsSequence("a", "b", "c");

		result = splitter.splitToList("\\a\\b\\c");
		assertThat(result).hasSize(3).containsSequence("a", "b", "c");

	}

	@Test
	public void charMatch() {
		String str = "abc";
		assertThat(MoreStringUtil.startWith(str, 'a')).isTrue();
		assertThat(MoreStringUtil.startWith(str, 'b')).isFalse();
		assertThat(MoreStringUtil.startWith(null, 'b')).isFalse();
		assertThat(MoreStringUtil.startWith("", 'b')).isFalse();

		assertThat(MoreStringUtil.endWith(str, 'c')).isTrue();
		assertThat(MoreStringUtil.endWith(str, 'b')).isFalse();
		assertThat(MoreStringUtil.endWith(null, 'b')).isFalse();
		assertThat(MoreStringUtil.endWith("", 'b')).isFalse();

		assertThat(MoreStringUtil.replaceFirst("abbc", 'b', 'c')).isEqualTo("acbc");
		assertThat(MoreStringUtil.replaceFirst("abcc", 'c', 'c')).isEqualTo("abcc");
		assertThat(MoreStringUtil.replaceFirst("", 'c', 'c')).isEqualTo("");
		assertThat(MoreStringUtil.replaceFirst(null, 'c', 'c')).isNull();

		assertThat(MoreStringUtil.replaceLast("abbc", 'b', 'c')).isEqualTo("abcc");
		assertThat(MoreStringUtil.replaceLast("abcc", 'c', 'c')).isEqualTo("abcc");
		assertThat(MoreStringUtil.replaceLast("", 'c', 'c')).isEqualTo("");
		assertThat(MoreStringUtil.replaceLast(null, 'c', 'c')).isNull();

	}

	@Test
	public void utf8EncodedLength() {
		assertThat(MoreStringUtil.utf8EncodedLength("ab12")).isEqualTo(4);
		assertThat(MoreStringUtil.utf8EncodedLength("中文")).isEqualTo(6);
	}

}
