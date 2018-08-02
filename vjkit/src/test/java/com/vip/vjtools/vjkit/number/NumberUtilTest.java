package com.vip.vjtools.vjkit.number;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

public class NumberUtilTest {

	@Test
	public void equalsWithin() {
		float f = 0.15f;
		float f2 = 0.45f / 3;
		float f3 = 0.46f / 3;
		assertThat(NumberUtil.equalsWithin(f, f2)).isTrue();
		assertThat(NumberUtil.equalsWithin(f, f3)).isFalse();
		assertThat(NumberUtil.equalsWithin(f, f2, 0.0000001)).isTrue();
	}

	@Test
	public void toBytes() {
		byte[] bytes = NumberUtil.toBytes(1);
		assertThat(bytes).hasSize(4).containsSequence((byte) 0, (byte) 0, (byte) 0, (byte) 1);

		bytes = NumberUtil.toBytes(257);
		assertThat(bytes).containsSequence((byte) 0, (byte) 0, (byte) 1, (byte) 1);
		assertThat(NumberUtil.toInt(bytes)).isEqualTo(257);

		// long
		byte[] bytes2 = NumberUtil.toBytes(1L);
		assertThat(bytes2).hasSize(8);

		bytes2 = NumberUtil.toBytes(257L);
		assertThat(bytes2).containsSequence((byte) 0, (byte) 0, (byte) 1, (byte) 1);
		assertThat(NumberUtil.toLong(bytes2)).isEqualTo(257L);

		// dobule
		byte[] bytes3 = NumberUtil.toBytes(1.123d);
		assertThat(NumberUtil.toDouble(bytes3)).isEqualTo(1.123d);

		// toInt32
		assertThat(NumberUtil.toInt32(123l)).isEqualTo(123);

		try {
			NumberUtil.toInt32(Long.valueOf(Integer.MAX_VALUE + 1l));
			fail("should fail here");
		} catch (Exception e) {
			assertThat(e).isInstanceOf(IllegalArgumentException.class);
		}
	}

	@Test
	public void isNumber() {
		assertThat(NumberUtil.isNumber("123")).isTrue();
		assertThat(NumberUtil.isNumber("-123.1")).isTrue();
		assertThat(NumberUtil.isNumber("-1a3.1")).isFalse();

		assertThat(NumberUtil.isHexNumber("0x12F")).isTrue();
		assertThat(NumberUtil.isHexNumber("-0x12A3")).isTrue();
		assertThat(NumberUtil.isHexNumber("12A3")).isFalse();
	}

	@Test
	public void toNumber() {
		assertThat(NumberUtil.toInt("122")).isEqualTo(122);
		try {
			NumberUtil.toInt("12A");
			fail("shoud fail here");
		} catch (NumberFormatException e) {

		}
		try {
			NumberUtil.toInt((String) null);
			fail("shoud fail here");
		} catch (NumberFormatException e) {

		}
		assertThat(NumberUtil.toInt("12A", 123)).isEqualTo(123);

		assertThat(NumberUtil.toLong("122")).isEqualTo(122L);
		try {
			NumberUtil.toLong("12A");
			fail("shoud fail here");
		} catch (

		NumberFormatException e) {

		}
		try {
			NumberUtil.toLong((String) null);
			fail("shoud fail here");
		} catch (NumberFormatException e) {

		}
		assertThat(NumberUtil.toLong("12A", 123)).isEqualTo(123L);

		assertThat(NumberUtil.toDouble("122.1")).isEqualTo(122.1);

		try {
			NumberUtil.toDouble("12A");
			fail("shoud fail here");
		} catch (NumberFormatException e) {

		}

		try {
			NumberUtil.toDouble((String) null);
			fail("shoud fail here");
		} catch (NumberFormatException e) {

		}
		assertThat(NumberUtil.toDouble("12A", 123.1)).isEqualTo(123.1);

		assertThat(NumberUtil.toIntObject("122")).isEqualTo(122);
		try {
			NumberUtil.toIntObject("12A");
			fail("shoud fail here");
		} catch (NumberFormatException e) {

		}

		assertThat(NumberUtil.toIntObject("12A", 123)).isEqualTo(123);
		assertThat(NumberUtil.toIntObject(null, 123)).isEqualTo(123);
		assertThat(NumberUtil.toIntObject("", 123)).isEqualTo(123);

		assertThat(NumberUtil.toLongObject("122")).isEqualTo(122L);
		try {
			NumberUtil.toLongObject("12A");
			fail("shoud fail here");
		} catch (NumberFormatException e) {

		}
		assertThat(NumberUtil.toLongObject("12A", 123L)).isEqualTo(123L);
		assertThat(NumberUtil.toLongObject(null, 123L)).isEqualTo(123L);

		assertThat(NumberUtil.toDoubleObject("122.1")).isEqualTo(122.1);
		try {
			NumberUtil.toDoubleObject("12A");
			fail("shoud fail here");
		} catch (NumberFormatException e) {

		}
		assertThat(NumberUtil.toDoubleObject("12A", 123.1)).isEqualTo(123.1);

		assertThat(NumberUtil.hexToIntObject("0x10")).isEqualTo(16);
		assertThat(NumberUtil.hexToIntObject("0X100")).isEqualTo(256);
		assertThat(NumberUtil.hexToIntObject("-0x100")).isEqualTo(-256);
		try {
			NumberUtil.hexToIntObject("0xHI");
			fail("shoud fail here");
		} catch (NumberFormatException e) {

		}

		try {
			NumberUtil.hexToIntObject(null);
			fail("shoud fail here");
		} catch (NumberFormatException e) {

		}
		assertThat(NumberUtil.hexToIntObject("0xHI", 123)).isEqualTo(123);

		assertThat(NumberUtil.hexToLongObject("0x10")).isEqualTo(16L);
		assertThat(NumberUtil.hexToLongObject("0X100")).isEqualTo(256L);
		assertThat(NumberUtil.hexToLongObject("-0x100")).isEqualTo(-256L);
		try {
			NumberUtil.hexToLongObject("0xHI");
			fail("shoud fail here");
		} catch (NumberFormatException e) {

		}
		try {
			NumberUtil.hexToLongObject(null);
			fail("shoud fail here");
		} catch (NumberFormatException e) {

		}
		assertThat(NumberUtil.hexToLongObject("0xHI", 123L)).isEqualTo(123L);

	}

	@Test
	public void toStringTest() {
		assertThat(NumberUtil.toString(23)).isEqualTo("23");
		assertThat(NumberUtil.toString(new Integer(23))).isEqualTo("23");
		assertThat(NumberUtil.toString(23l)).isEqualTo("23");
		assertThat(NumberUtil.toString(new Long(23))).isEqualTo("23");
		assertThat(NumberUtil.toString(23l)).isEqualTo("23");

		assertThat(NumberUtil.toString(new Double(23.112d))).isEqualTo("23.112");
		assertThat(NumberUtil.toString(23.112d)).isEqualTo("23.112");
		assertThat(NumberUtil.to2DigitString(23.112d)).isEqualTo("23.11");
		assertThat(NumberUtil.to2DigitString(23.116d)).isEqualTo("23.12");

	}
}
