package com.vip.vjstar.gc;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import com.vip.vjtools.vjkit.number.RandomUtil;

public class DateTimeUtil {

	/**
	 * Generate delay millis list by given time string, separated by comma.<br/>
	 * eg, 03:00-05:00,13:00-14:00
	 * 
	 * @see #getDelayMillis4ProactiveGcTask(String)
	 * @param time
	 * @return
	 * @throws Exception
	 */
	public static List<Long> getDelayMillisList4ProactiveGcTask(String time) {
		List<Long> result = new ArrayList<>();
		String[] arr = StringUtils.split(time, ",");
		for (String t : arr) {
			result.add(getDelayMillis4ProactiveGcTask(t));
		}
		return result;
	}

	/**
	 * Get scheduled delay for proactive gc task，cross-day setting is supported.<br/>
	 * 01:30-02:40，some time between 01:30-02:40；<br/>
	 * 180000，180 seconds later.
	 * 
	 * @return
	 * @throws Exception
	 */
	public static long getDelayMillis4ProactiveGcTask(String time) {
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
		return getDelayMillis4ProactiveGcTask("03:00-05:00");
	}

	/**
	 * return current date time by specified hour:minute
	 * 
	 * @param time,
	 *            format: hh:mm
	 * @return
	 * @throws ParseException
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
