package com.vip.vjtools.vjkit.text;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

public class CsvUtilTest {

	@Test
	public void toCsvString() {
		assertThat(CsvUtil.toCsvString(1, 2)).isEqualTo("1,2");

		assertThat(CsvUtil.toCsvString(1, 2, 3, 4)).isEqualTo("1,2,3,4");

		// "2" still plain as 2
		assertThat(CsvUtil.toCsvString(1, "2")).isEqualTo("1,2");

		// "A BC" still plain as A BC
		assertThat(CsvUtil.toCsvString(1, "A BC")).isEqualTo("1,A BC");

		// "A,BC" has ',' as "A,BC"
		assertThat(CsvUtil.toCsvString(1, "A,BC")).isEqualTo("1,\"A,BC\"");

		// "A"BC" has '"' as "A""BC"
		assertThat(CsvUtil.toCsvString(1, "A\"BC")).isEqualTo("1,\"A\"\"BC\"");

		// "A,B"a"C" has 2 '""' as "A,""a""BC"
		assertThat(CsvUtil.toCsvString(1, "A,\"a\"BC")).isEqualTo("1,\"A,\"\"a\"\"BC\"");
	}

	@Test
	public void fromCsvString() {
		assertThat(CsvUtil.fromCsvString("1,2")).hasSize(2).contains("1").contains("2");
		assertThat(CsvUtil.fromCsvString("1,A BC")).hasSize(2).contains("1").contains("A BC");
		assertThat(CsvUtil.fromCsvString("1,\"A,BC\"")).hasSize(2).contains("1").contains("A,BC");
		assertThat(CsvUtil.fromCsvString("1,\"A,\"\"a\"\"BC\"")).hasSize(2).contains("1").contains("A,\"a\"BC");

		// wrong format still work
		assertThat(CsvUtil.fromCsvString("1,\"A,\"a\"\"BC\"")).hasSize(2).contains("1").contains("A,\"a\"BC");
		assertThat(CsvUtil.fromCsvString("1,ABC\"")).hasSize(2).contains("1").contains("ABC\"");
	}

}
