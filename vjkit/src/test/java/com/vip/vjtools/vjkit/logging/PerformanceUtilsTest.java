package com.vip.vjtools.vjkit.logging;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class PerformanceUtilsTest {
	
	Logger logger = (Logger) LoggerFactory.getLogger(PerformanceUtilsTest.class);

	@Test
	public void test() throws InterruptedException {
		PerformanceUtils.start();
		PerformanceUtils.start("test");
		Thread.sleep(1000L);// NOSONAR
		System.out.println(Thread.currentThread().getName() + " time cost: " + PerformanceUtils.duration() + "ms");
		PerformanceUtils.end();
		PerformanceUtils.warn(logger, 0L);
		PerformanceUtils.end("test");
		System.out.println(Thread.currentThread().getName() + " time cost: " + PerformanceUtils.duration() + "ms");
		PerformanceUtils.warn(logger, "test", 0L);
	}
	
}
