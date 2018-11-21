package com.vip.vjtools.vjtop.util;

import com.vip.vjtools.vjtop.util.Utils;
import org.junit.Assert;
import org.junit.Test;

public class UtilsTest {

    @Test
    public void calcLoadInputNegativeZeroZeroOutputZero() {

        // Arrange
        final Long deltaCpuTime = -4_194_304L;
        final long deltaUptime = 0L;
        final long factor = 0L;

        // Act
        final double retval = Utils.calcLoad(deltaCpuTime, deltaUptime, factor);

        // Assert result
        Assert.assertEquals(0.0, retval, 0.0);
    }

    @Test
    public void calcLoadInputPositivePositiveOutputPositive() {

        // Arrange
        final long deltaCpuTime = 558_348_370L;
        final long deltaUptime = 1L;

        // Act
        final double retval = Utils.calcLoad(deltaCpuTime, deltaUptime);

        // Assert result
        Assert.assertEquals(0x1.a0008001p+35 /* 5.58348e+10 */, retval, 0.0);
    }

    @Test
    public void calcLoadInputPositivePositiveZeroOutputPositiveInfinity() {

        // Arrange
        final Long deltaCpuTime = 4_194_304L;
        final long deltaUptime = 4_611_686_018_427_387_904L;
        final long factor = 0L;

        // Act
        final double retval = Utils.calcLoad(deltaCpuTime, deltaUptime, factor);

        // Assert result
        Assert.assertEquals(Double.POSITIVE_INFINITY, retval, 0.0);
    }

    @Test
    public void calcLoadInputPositiveZeroOutputZero() {

        // Arrange
        final long deltaCpuTime = 558_348_370L;
        final long deltaUptime = 0L;

        // Act
        final double retval = Utils.calcLoad(deltaCpuTime, deltaUptime);

        // Assert result
        Assert.assertEquals(0.0, retval, 0.0);
    }

    @Test
    public void calcLoadInputPositiveZeroZeroOutputZero() {

        // Arrange
        final Long deltaCpuTime = 4_194_304L;
        final long deltaUptime = 0L;
        final long factor = 0L;

        // Act
        final double retval = Utils.calcLoad(deltaCpuTime, deltaUptime, factor);

        // Assert result
        Assert.assertEquals(0.0, retval, 0.0);
    }

    @Test
    public void calcLoadInputZeroZeroOutputZero() {

        // Arrange
        final long deltaCpuTime = 0L;
        final long deltaUptime = 0L;

        // Act
        final double retval = Utils.calcLoad(deltaCpuTime, deltaUptime);

        // Assert result
        Assert.assertEquals(0.0, retval, 0.0);
    }

    @Test
    public void calcMemoryUtilizationInputNegativePositiveOutputNegative() {

        // Arrange
        final Long threadBytes = -507_680_768_073_012_547L;
        final long totalBytes = 1_147_301_170_758_571_992L;

        // Act
        final double retval = Utils.calcMemoryUtilization(threadBytes, totalBytes);

        // Assert result
        Assert.assertEquals(-0x1.6200000024f82p+5 /* -44.25 */, retval, 0.0);
    }

    @Test
    public void calcMemoryUtilizationInputPositiveZeroOutputZero() {

        // Arrange
        final Long threadBytes = 1L;
        final long totalBytes = 0L;

        // Act
        final double retval = Utils.calcMemoryUtilization(threadBytes, totalBytes);

        // Assert result
        Assert.assertEquals(0.0, retval, 0.0);
    }

}
