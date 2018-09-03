package com.vip.vjtools.vjkit.base;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

public class RuntimeUtilTest {
	@Test
	public void testRuntime() {
		System.out.println("pid:" + RuntimeUtil.getPid());
		assertThat(RuntimeUtil.getPid()).isNotEqualTo(-1);
		System.out.println("vmargs:" + RuntimeUtil.getVmArguments());
		RuntimeUtil.addShutdownHook(new Runnable() {
			@Override
			public void run() {
				System.out.println("systemShutdowning");
			}
		});

		assertThat(RuntimeUtil.getCores()).isGreaterThan(1);

		System.out.println("uptime" + RuntimeUtil.getUpTime());
	}
}
