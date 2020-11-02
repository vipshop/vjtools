package com.vip.vjtools.vjtop.util;

import com.vip.vjtools.vjtop.VMInfo.Usage;
import com.vip.vjtools.vjtop.WarningRule.LongWarning;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.util.ArrayList;

import static com.vip.vjtools.vjtop.util.Formats.*;
import static org.junit.Assert.*;

public class FormatsTest {

    @Rule public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testFormatUsage() {
        assertEquals("0m/0m", formatUsage(new Usage(1L, 2L, 2L)));
        assertEquals("0m/0m/0m", formatUsage(new Usage(1L, 1L, 2L)));
    }

    @Test
    public void testFormatUsageWithColor() {
        assertEquals("0m/0m",
          formatUsageWithColor(new Usage(1L, 2L, 2L), new LongWarning()));
    }

    @Test
    public void testJoin() {
        final ArrayList<String> list = new ArrayList<String>();
        final String delim = "AAAAAAAA";

        assertEquals("", join(list, delim));

        list.add("");
        assertEquals("", join(list, delim));
    }

    @Test
    public void testLeftStr() {
        assertEquals("", leftStr("!!!!!!!!", 0));

        thrown.expect(StringIndexOutOfBoundsException.class);
        leftStr("!", -536_870_911);
        // Method is not expected to return due to exception thrown
    }

    @Test
    public void testParseFromSize() {
        assertEquals(-1L, parseFromSize(null));
        assertEquals(1024L, parseFromSize("1k"));
        assertEquals(1024L, parseFromSize("1kb"));
        assertEquals(1_048_576L, parseFromSize("1mb"));
        assertEquals(1_048_576L, parseFromSize("1m"));
        assertEquals(1_073_741_824L, parseFromSize("1gb"));
        assertEquals(1_073_741_824L, parseFromSize("1g"));
        assertEquals(1_099_511_627_776L, parseFromSize("1tb"));
        assertEquals(1_099_511_627_776L, parseFromSize("1t"));
        assertEquals(44L, parseFromSize("44bytes"));
        assertEquals(-1L, parseFromSize("!!!"));
    }

    @Test
    public void testRightStr() {
        assertEquals("!!!!!!!!", rightStr("!!!!!!!!", 2_147_221_512));

        thrown.expect(StringIndexOutOfBoundsException.class);
        rightStr("!!!!!!!!", -1_048_568);
        // Method is not expected to return due to exception thrown
    }

    @Test
    public void testShortName() {
        assertEquals("!", shortName("!", 1, 6));
        assertEquals("!...", shortName("!!!!!!!!!!", 4, 0));

        thrown.expect(StringIndexOutOfBoundsException.class);
        shortName("!!!!!!!!!!", 0, -19);
        // Method is not expected to return due to exception thrown
    }

    @Test
    public void testToFixLengthSizeUnit() {
        assertEquals("NaN", toFixLengthSizeUnit(null));
        assertEquals(" 123", toFixLengthSizeUnit(123L));
        assertEquals(" 120k", toFixLengthSizeUnit(123_456L));
        assertEquals(" 117m", toFixLengthSizeUnit(123_456_789L));
        assertEquals(" 114g", toFixLengthSizeUnit(123_456_789_000L));
        assertEquals("4194304t", toFixLengthSizeUnit(4_611_686_018_427_387_906L));
    }

    @Test
    public void testToMB() {
        assertEquals("NaN", toMB(-8L));
        assertEquals("0m", toMB(8L));
        assertEquals("114g", toMB(123_456_789_000L));
    }

    @Test
    public void testToSizeUnit() {
        assertEquals("-1023", toSizeUnit(-1023L));
        assertEquals("NaN", toSizeUnit(null));
        assertEquals("44k", toSizeUnit(45_312L));
        assertEquals("117m", toSizeUnit(123_456_789L));
        assertEquals("114g", toSizeUnit(123_456_789_000L));
        assertEquals("112t", toSizeUnit(123_456_789_000_000L));
    }

    @Test
    public void testToSizeUnitWithColor() {
        assertEquals("123", toSizeUnitWithColor(123L, new LongWarning()));

        thrown.expect(NullPointerException.class);
        toSizeUnitWithColor(null, null);
        // Method is not expected to return due to exception thrown
    }

    @Test
    public void testToTimeUnit() {
        assertEquals("01s", toTimeUnit(1_000L));
        assertEquals("02m00s", toTimeUnit(120_000L));
        assertEquals("02h00m", toTimeUnit(7_200_000L));
        assertEquals("2d00h", toTimeUnit(172_800_000L));
    }

}
