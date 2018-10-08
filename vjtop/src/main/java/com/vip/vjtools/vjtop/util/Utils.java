package com.vip.vjtools.vjtop.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Utils {

	public static long NANOS_TO_MILLS = 1000 * 1000;

	/**
	 * Sorts a Map by its values, using natural ordering.
	 */
	public static long[] sortAndFilterThreadIdsByValue(LongObjectMap map, int threadLimit) {
		int max = Math.min(threadLimit, map.size());
		List<Map.Entry> list = new LinkedList(map.entrySet());
		Collections.sort(list, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o2)).getValue()).compareTo(((Map.Entry) (o1)).getValue());
			}
		});

		long[] topTidArray = new long[max];
		int i = 0;
		for (Map.Entry entry : list) {
			topTidArray[i] = (Long) entry.getKey();
			if (++i >= max) {
				break;
			}
		}

		return topTidArray;
	}

	/**
	 * calculates a "load", given on two deltas
	 */
	public static double calcLoad(long deltaCpuTime, long deltaUptime) {
		if (deltaCpuTime <= 0 || deltaUptime == 0) {
			return 0.0;
		}
		return deltaCpuTime * 100d / deltaUptime;
	}

	/**
	 * calculates a "load", given on two deltas
	 */
	public static double calcLoad(Long deltaCpuTime, long deltaUptime, long factor) {
		if (deltaCpuTime == null || deltaCpuTime <= 0 || deltaUptime == 0) {
			return 0.0;
		}
		return deltaCpuTime * 100d / factor / deltaUptime;
	}

	public static double calcMemoryUtilization(Long threadBytes, long totalBytes) {
		if (threadBytes == null || totalBytes == 0) {
			return 0;
		}

		return (threadBytes * 100d) / totalBytes;// 这里因为最后单位是百分比%，所以bytes除以totalBytes以后要乘以100，才可以再加上单位%
	}


	public static void sleep(long mills) {
		try {
			Thread.sleep(mills);
		} catch (InterruptedException e) {
		}
	}
}
