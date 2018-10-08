package com.vip.vjstar.gc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.vjtools.vjkit.base.ExceptionUtil;
import com.vip.vjtools.vjkit.concurrent.threadpool.ThreadPoolUtil;
import com.vip.vjtools.vjkit.number.RandomUtil;

public class CleanUpScheduler {

	private static Logger logger = LoggerFactory.getLogger(CleanUpScheduler.class);

	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1,
			ThreadPoolUtil.buildThreadFactory("cleanup", true));

	public void schedule(String schedulePlans, Runnable task) {
		List<Long> delayTimes = getDelayMillsList(schedulePlans);
		for (long delayTime : delayTimes) {
			scheduler.schedule(task, delayTime, TimeUnit.MILLISECONDS);
		}
	}

	public void reschedule(Runnable task) {
		if (!scheduler.isShutdown()) {
			try {
				scheduler.schedule(task, 24, TimeUnit.HOURS);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	public void shutdown(){
		scheduler.shutdown();
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
				Date d1 = getCurrentDateByPlan(start, pattern);
				Date d2 = getCurrentDateByPlan(end, pattern);
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

		// default
		return getDelayMillis("02:00-05:00");
	}

	/**
	 * return current date time by specified hour:minute
	 * 
	 * @param plan format: hh:mm
	 */
	public static Date getCurrentDateByPlan(String plan, String pattern) {
		try {
			FastDateFormat format = FastDateFormat.getInstance(pattern);
			Date end = format.parse(plan);
			Calendar today = Calendar.getInstance();
			end = DateUtils.setYears(end, (today.get(Calendar.YEAR)));
			end = DateUtils.setMonths(end, today.get(Calendar.MONTH));
			end = DateUtils.setDays(end, today.get(Calendar.DAY_OF_MONTH));
			return end;
		} catch (Exception e) {
			throw ExceptionUtil.unchecked(e);
		}
	}
}
