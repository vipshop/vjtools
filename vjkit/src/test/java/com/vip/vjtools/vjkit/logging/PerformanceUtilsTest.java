package com.vip.vjtools.vjkit.logging;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class PerformanceUtilsTest {
	
	Logger logger = (Logger) LoggerFactory.getLogger(PerformanceUtilsTest.class);

	@Test
	public void test() throws InterruptedException {
		PerformanceUtil.start();
		PerformanceUtil.start("test");
		Thread.sleep(1000L);// NOSONAR
		System.out.println(Thread.currentThread().getName() + " time cost: " + PerformanceUtil.duration() + "ms");
		PerformanceUtil.end();
		PerformanceUtil.warn(logger, 0L);
		PerformanceUtil.end("test");
		System.out.println(Thread.currentThread().getName() + " time cost: " + PerformanceUtil.duration() + "ms");
		PerformanceUtil.warn(logger, "test", 0L);
	}
	
}
