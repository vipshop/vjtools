package com.vip.vjtools.vjtop.util;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.util.ArrayList;

import static com.vip.vjtools.vjtop.util.Formats.*;

public class FormatsTest {

    @Rule public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testJoin() {
        final ArrayList<String> list = new ArrayList<String>();
        final String delim = "AAAAAAAA";

        Assert.assertEquals("", join(list, delim));

        list.add("");
        Assert.assertEquals("", join(list, delim));
    }

    @Test
    public void testLeftStr() {
        Assert.assertEquals("", leftStr("!!!!!!!!", 0));

        thrown.expect(StringIndexOutOfBoundsException.class);
        leftStr("!", -536_870_911);
        // Method is not expected to return due to exception thrown
    }

    @Test
    public void testRightStr() {
        Assert.assertEquals("!!!!!!!!", rightStr("!!!!!!!!", 2_147_221_512));

        thrown.expect(StringIndexOutOfBoundsException.class);
        rightStr("!!!!!!!!", -1_048_568);
        // Method is not expected to return due to exception thrown
    }

    @Test
    public void testShortName() {
        Assert.assertEquals("!", shortName("!", 1, 6));
        Assert.assertEquals("!...", shortName("!!!!!!!!!!", 4, 0));

        thrown.expect(StringIndexOutOfBoundsException.class);
        shortName("!!!!!!!!!!", 0, -19);
        // Method is not expected to return due to exception thrown
    }

    @Test
    public void testToFixLengthSizeUnit() {
        Assert.assertEquals("NaN", toFixLengthSizeUnit(null));
        Assert.assertEquals("4194304t",
          toFixLengthSizeUnit(4_611_686_018_427_387_906L));
    }

    @Test
    public void testToMB() {
        Assert.assertEquals("NaN", toMB(-8L));
        Assert.assertEquals("0m", toMB(8L));
    }

    @Test
    public void testToSizeUnit() {
        Assert.assertEquals("-1023", toSizeUnit(-1023L));
        Assert.assertEquals("NaN", toSizeUnit(null));
        Assert.assertEquals("44k", toSizeUnit(45_312L));
    }

    @Test
    public void testToSizeUnitWithColor() {
        thrown.expect(NullPointerException.class);
        toSizeUnitWithColor(null, null);
        // Method is not expected to return due to exception thrown
    }

}
