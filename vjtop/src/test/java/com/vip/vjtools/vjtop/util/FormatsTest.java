package com.vip.vjtools.vjtop.util;

import com.vip.vjtools.vjtop.WarningRule.LongWarning;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.util.ArrayList;

public class FormatsTest {

    @Rule public ExpectedException thrown = ExpectedException.none();

    @Test
    public void joinInput0NotNullOutputNotNull() {

        // Arrange
        final ArrayList<String> list = new ArrayList<String>();
        final String delim = "AAAAAAAA";

        // Act
        final String retval = Formats.join(list, delim);

        // Assert result
        Assert.assertEquals("", retval);
    }

    @Test
    public void joinInput1NullOutputNotNull() {

        // Arrange
        final ArrayList<String> list = new ArrayList<String>();
        list.add("");
        final String delim = null;

        // Act
        final String retval = Formats.join(list, delim);

        // Assert result
        Assert.assertEquals("", retval);
    }

    @Test
    public void leftStrInputNotNullNegativeOutputStringIndexOutOfBoundsException() {

        // Arrange
        final String str = "!";
        final int length = -536_870_911;

        // Act
        thrown.expect(StringIndexOutOfBoundsException.class);
        Formats.leftStr(str, length);

        // Method is not expected to return due to exception thrown
    }

    @Test
    public void leftStrInputNotNullZeroOutputNotNull() {

        // Arrange
        final String str = "!!!!!!!!";
        final int length = 0;

        // Act
        final String retval = Formats.leftStr(str, length);

        // Assert result
        Assert.assertEquals("", retval);
    }

    @Test
    public void rightStrInputNotNullNegativeOutputStringIndexOutOfBoundsException() {

        // Arrange
        final String str = "!!!!!!!!";
        final int length = -1_048_568;

        // Act
        thrown.expect(StringIndexOutOfBoundsException.class);
        Formats.rightStr(str, length);

        // Method is not expected to return due to exception thrown
    }

    @Test
    public void rightStrInputNotNullPositiveOutputNotNull() {

        // Arrange
        final String str = "!!!!!!!!";
        final int length = 2_147_221_512;

        // Act
        final String retval = Formats.rightStr(str, length);

        // Assert result
        Assert.assertEquals("!!!!!!!!", retval);
    }

    @Test
    public void shortNameInputNotNullPositivePositiveOutputNotNull() {

        // Arrange
        final String str = "!";
        final int length = 1;
        final int rightLength = 6;

        // Act
        final String retval = Formats.shortName(str, length, rightLength);

        // Assert result
        Assert.assertEquals("!", retval);
    }

    @Test
    public void shortNameInputNotNullPositiveZeroOutputNotNull() {

        // Arrange
        final String str = "!!!!!!!!!!";
        final int length = 4;
        final int rightLength = 0;

        // Act
        final String retval = Formats.shortName(str, length, rightLength);

        // Assert result
        Assert.assertEquals("!...", retval);
    }

    @Test
    public void shortNameInputNotNullZeroNegativeOutputStringIndexOutOfBoundsException() {

        // Arrange
        final String str = "!!!!!!!!!!";
        final int length = 0;
        final int rightLength = -19;

        // Act
        thrown.expect(StringIndexOutOfBoundsException.class);
        Formats.shortName(str, length, rightLength);

        // Method is not expected to return due to exception thrown
    }

    @Test
    public void toFixLengthSizeUnitInputNullOutputNotNull() {

        // Arrange
        final Long size = null;

        // Act
        final String retval = Formats.toFixLengthSizeUnit(size);

        // Assert result
        Assert.assertEquals("NaN", retval);
    }

    @Test
    public void toFixLengthSizeUnitInputPositiveOutputNotNull() {

        // Arrange
        final Long size = 4_611_686_018_427_387_906L;

        // Act
        final String retval = Formats.toFixLengthSizeUnit(size);

        // Assert result
        Assert.assertEquals("4194304t", retval);
    }

    @Test
    public void toMBInputNegativeOutputNotNull() {

        // Arrange
        final long bytes = -8L;

        // Act
        final String retval = Formats.toMB(bytes);

        // Assert result
        Assert.assertEquals("NaN", retval);
    }

    @Test
    public void toMBInputPositiveOutputNotNull() {

        // Arrange
        final long bytes = 8L;

        // Act
        final String retval = Formats.toMB(bytes);

        // Assert result
        Assert.assertEquals("0m", retval);
    }

    @Test
    public void toSizeUnitInputNegativeOutputNotNull() {

        // Arrange
        final Long size = -1023L;

        // Act
        final String retval = Formats.toSizeUnit(size);

        // Assert result
        Assert.assertEquals("-1023", retval);
    }

    @Test
    public void toSizeUnitInputNullOutputNotNull() {

        // Arrange
        final Long size = null;

        // Act
        final String retval = Formats.toSizeUnit(size);

        // Assert result
        Assert.assertEquals("NaN", retval);
    }

    @Test
    public void toSizeUnitInputPositiveOutputNotNull() {

        // Arrange
        final Long size = 45_312L;

        // Act
        final String retval = Formats.toSizeUnit(size);

        // Assert result
        Assert.assertEquals("44k", retval);
    }

    @Test
    public void toSizeUnitWithColorInputNullNullOutputNullPointerException() {

        // Arrange
        final Long size = null;
        final LongWarning warning = null;

        // Act
        thrown.expect(NullPointerException.class);
        Formats.toSizeUnitWithColor(size, warning);

        // Method is not expected to return due to exception thrown
    }

}
