package com.vip.vjtools.vjkit.number;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

public class UnitConverterTest {

	@Test
	public void convertDurationMillis() {
		assertThat(UnitConverter.toDurationMillis("12345")).isEqualTo(12345);
		assertThat(UnitConverter.toDurationMillis("12S")).isEqualTo(12000);
		assertThat(UnitConverter.toDurationMillis("12s")).isEqualTo(12000);
		assertThat(UnitConverter.toDurationMillis("12ms")).isEqualTo(12);
		assertThat(UnitConverter.toDurationMillis("12m")).isEqualTo(12 * 60 * 1000);
		assertThat(UnitConverter.toDurationMillis("12h")).isEqualTo(12l * 60 * 60 * 1000);
		assertThat(UnitConverter.toDurationMillis("12d")).isEqualTo(12l * 24 * 60 * 60 * 1000);

		try {
			assertThat(UnitConverter.toDurationMillis("12a")).isEqualTo(12 * 60 * 1000);
			fail("should fail");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(IllegalArgumentException.class);
		}

		try {
			assertThat(UnitConverter.toDurationMillis("a12")).isEqualTo(12 * 60 * 1000);
			fail("should fail");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(IllegalArgumentException.class);
		}
	}

	@Test
	public void convertSizeBytes() {
		assertThat(UnitConverter.toBytes("12345")).isEqualTo(12345);
		assertThat(UnitConverter.toBytes("12b")).isEqualTo(12);
		assertThat(UnitConverter.toBytes("12k")).isEqualTo(12 * 1024);
		assertThat(UnitConverter.toBytes("12M")).isEqualTo(12 * 1024 * 1024);

		assertThat(UnitConverter.toBytes("12G")).isEqualTo(12l * 1024 * 1024 * 1024);
		assertThat(UnitConverter.toBytes("12T")).isEqualTo(12l * 1024 * 1024 * 1024 * 1024);

		try {
			UnitConverter.toBytes("12x");
			fail("should fail");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(IllegalArgumentException.class);
		}

		try {
			UnitConverter.toBytes("a12");
			fail("should fail");
		} catch (Throwable t) {
			assertThat(t).isInstanceOf(IllegalArgumentException.class);
		}
	}

	@Test
	public void convertToSizeUnit() {
		assertThat(UnitConverter.toSizeUnit(966L, 0)).isEqualTo(" 966");

		assertThat(UnitConverter.toSizeUnit(1522L, 0)).isEqualTo("   1k");
		assertThat(UnitConverter.toSizeUnit(1522L, 1)).isEqualTo("   1.5k");

		assertThat(UnitConverter.toSizeUnit(1024L * 1024 * 2 + 1024 * 200, 0)).isEqualTo("   2m");
		assertThat(UnitConverter.toSizeUnit(1024L * 1024 * 2 + 1024 * 600, 0)).isEqualTo("   3m");

		assertThat(UnitConverter.toSizeUnit(1024L * 1024 * 2 + 1024 * 140, 1)).isEqualTo("   2.1m");
		assertThat(UnitConverter.toSizeUnit(1024L * 1024 * 2 + 1024 * 160, 1)).isEqualTo("   2.2m");

		assertThat(UnitConverter.toSizeUnit(1024L * 1024 * 1024 * 2 + 1024 * 1024 * 200, 0)).isEqualTo("   2g");
		assertThat(UnitConverter.toSizeUnit(1024L * 1024 * 1024 * 2 + 1024 * 1024 * 200, 1)).isEqualTo("   2.2g");

		assertThat(UnitConverter.toSizeUnit(1024L * 1024 * 1024 * 1024 * 2 + 1024L * 1024 * 1024 * 200, 0))
				.isEqualTo("   2t");
		assertThat(UnitConverter.toSizeUnit(1024L * 1024 * 1024 * 1024 * 2 + 1024L * 1024 * 1024 * 200, 1))
				.isEqualTo("   2.2t");

	}

	@Test
	public void convertToTimeUnit() {
		assertThat(UnitConverter.toTimeUnit(1322L, 0)).isEqualTo(" 1s");
		assertThat(UnitConverter.toTimeUnit(1322L, 1)).isEqualTo(" 1.3s");

		assertThat(UnitConverter.toTimeUnit(1000L * 62, 0)).isEqualTo(" 1m");
		assertThat(UnitConverter.toTimeUnit(1000L * 90, 0)).isEqualTo(" 2m");

		assertThat(UnitConverter.toTimeUnit(1000L * 90, 1)).isEqualTo(" 1.5m");

		assertThat(UnitConverter.toTimeUnit(1000L * 60 * 70, 1)).isEqualTo(" 1.2h");

		assertThat(UnitConverter.toTimeUnit(1000L * 60 * 60 * 28, 1)).isEqualTo(" 1.2d");
	}

}
