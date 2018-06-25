package com.vip.vjstar.gc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import com.vip.vjtools.vjkit.number.RandomUtil;

public class CleanUpScheduler {

	protected ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, new ThreadFactory() {
		public Thread newThread(Runnable runnable) {
			Thread thread = Executors.defaultThreadFactory().newThread(runnable);
			thread.setDaemon(true);
			return thread;
		}
	});

	public void schedule(String schedulePlan, Runnable task) {
		List<Long> times = getDelayMillsList(schedulePlan);
		for (long time : times) {
			scheduler.schedule(task, time, TimeUnit.MILLISECONDS);
		}
	}

	public void reschedule(Runnable task) {
		scheduler.schedule(task, 24, TimeUnit.HOURS);
	}

	public boolean isShutdown() {
		return scheduler.isShutdown();
	}


	/**
	 * Generate delay millis list by given plans string, separated by comma.<br/>
	 * eg, 03:00-05:00,13:00-14:00
	 */
	public static List<Long> getDelayMillsList(String schedulePlans) {
		List<Long> result = new ArrayList<>();
		String[] plans = StringUtils.split(schedulePlans, ',');
		for (String plan : plans) {
			result.add(getDelayMillis(plan));
		}
		return result;
	}

	/**
	 * Get scheduled delay for proactive gc task，cross-day setting is supported.<br/>
	 * 01:30-02:40，some time between 01:30-02:40；<br/>
	 * 180000，180 seconds later.
	 */
	public static long getDelayMillis(String time) {
		String pattern = "HH:mm";
		Date now = new Date();
		if (StringUtils.contains(time, "-")) {
			String start = time.split("-")[0];
			String end = time.split("-")[1];
			if (StringUtils.contains(start, ":") && StringUtils.contains(end, ":")) {
				Date d1 = getCurrentDateByTime(start, pattern);
				Date d2 = getCurrentDateByTime(end, pattern);
				while (d1.before(now)) {
					d1 = DateUtils.addDays(d1, 1);
				}
				while (d2.before(d1)) {
					d2 = DateUtils.addDays(d2, 1);
				}
				return RandomUtil.nextLong(d1.getTime() - now.getTime(), d2.getTime() - now.getTime());
			}
		} else if (StringUtils.isNumeric(time)) {
			return Long.parseLong(time);
		}
		
		return getDelayMillis("03:00-05:00");
	}

	/**
	 * return current date time by specified hour:minute
	 * 
	 * @param time format: hh:mm
	 */
	public static Date getCurrentDateByTime(String time, String pattern) {
		try {
			FastDateFormat format = FastDateFormat.getInstance(pattern);
			Date end = format.parse(time);
			end = DateUtils.setYears(end, (Calendar.getInstance().get(Calendar.YEAR)));
			end = DateUtils.setMonths(end, Calendar.getInstance().get(Calendar.MONTH));
			end = DateUtils.setDays(end, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
			return end;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
