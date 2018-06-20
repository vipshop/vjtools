package com.vip.vjstar.gc;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProactiveGcTaskDemo {
	public static void main(String[] args) throws IOException {
		Logger logger = LoggerFactory.getLogger(ProactiveGcTaskDemo.class);
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1,
				new DaemonThreadFactory("GcTaskScheduler"));
		List<Long> times = DateTimeUtil.getDelayMillisList4ProactiveGcTask("03:30-04:30,12:30-13:30");
		for (long time : times) {
			scheduler.schedule(new ProactiveGcTask(scheduler, logger, 50), time, TimeUnit.MILLISECONDS);
		}
		System.in.read();
	}
}
