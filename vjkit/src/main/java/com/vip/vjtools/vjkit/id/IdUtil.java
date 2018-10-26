package com.vip.vjtools.vjkit.id;

import com.google.common.collect.Maps;
import com.vip.vjtools.vjkit.concurrent.jsr166e.LongAdder;
import com.vip.vjtools.vjkit.net.NetUtil;
import org.apache.commons.lang3.StringUtils;

import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class IdUtil {
	/**
	 * 进程号
	 */
	private static String pid;
	/**
	 * 本机ip后两段，如：10.168.10.129 => 10129
	 */
	private static String ipSuffix;
	/**
	 * 累计器初始值
	 */
	private static long initValue = 0;

	static {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		pid = name.split("@")[0];
		String[] values = NetUtil.getLocalHost().split("\\.");
		ipSuffix = values[2] + values[3];
	}

	/**
	 * 默认的累加器key
	 */
	private static final String DEFAULT_BUS_KEY = "default_key";
	/**
	 * 计数器
	 * 每个业务对应一个累加器
	 */
	private static Map<String, LongAdder> counterMap = Maps.newConcurrentMap();

	/**
	 * 返回使用ThreadLocalRandm的UUID，比默认的UUID性能更优
	 */
	public static UUID fastUUID() {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		return new UUID(random.nextLong(), random.nextLong());
	}

	/**
	 * 生成唯一id，计算速度要高于UUID
	 * 如果只是想要得到一个唯一的id，比如链路id，可以采用该方法
	 * @return String id
	 */
	public static String generateId() {
		return generateId("");
	}

	/**
	 * 生成唯一id，在高并发的情况下，计算速度要高于UUID
	 * 如果只是想要得到一个唯一的id，比如链路id，可以采用该方法
	 * @param salt 加入盐计算
	 * @return String id
	 */
	public static String generateId(String salt) {
		StringBuilder idString = new StringBuilder(30);
		idString.append(StringUtils.trimToEmpty(salt)).append(ipSuffix).append(pid)
				.append(Thread.currentThread().getId()).append(System.currentTimeMillis());
		return idString.toString();
	}

	/**
	 * 产生线性递增的id
	 * @return long id值
	 */
	public static long increaseId() {

		return increaseId(DEFAULT_BUS_KEY);
	}

	/**
	 * 产生线性递增的id
	 * @param busKey 业务key，每一个业务都应该有一个独立的key，粒度越细也好
	 * @return long id值
	 */
	public static long increaseId(String busKey) {

		if (!counterMap.containsKey(busKey)) {
			synchronized (new String(busKey).intern()) {
				if (!counterMap.containsKey(busKey)) {
					System.out.println("lock " + busKey);
					counterMap.put(busKey, new LongAdder());
					counterMap.get(busKey).add(initValue);
				}
			}
		}
		counterMap.get(busKey).increment();
		return counterMap.get(busKey).longValue();
	}

	/**
	 * 初始化累加器起始值
	 * @param value 起始值
	 */
	public static void setStartValue(long value) {
		initValue = value;
	}
}
