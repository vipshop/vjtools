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
		final Enchanter enchanter = new Enchanter();
		enchanter.makeGarbage("50000000");
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1,
				new DaemonThreadFactory("GcTaskScheduler"));
		List<Long> times = DateTimeUtil.getDelayMillisList4ProactiveGcTask("03:30-04:30");
		for (long time : times) {
			// Schedule gc task at the specified random delay, and repeat it every other day.
			ProactiveGcTask task = new ProactiveGcTask(scheduler, 50, 48, TimeUnit.HOURS) {
				@Override
				public void preGc() {
					super.preGc();
					enchanter.clearGarbage();
				}
				@Override
				public void postGc() {
					super.postGc();
					System.exit(1);
				}
			};
			scheduler.schedule(task, time, TimeUnit.MILLISECONDS);
		}
		System.in.read();
	}
}
