package com.vip.vjtools.vjkit.id;

import com.vip.vjtools.vjkit.net.NetUtil;
import org.apache.commons.lang3.StringUtils;

import java.lang.management.ManagementFactory;
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

	static {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		pid = name.split("@")[0];
		String[] values = NetUtil.getLocalHost().split("\\.");
		ipSuffix = values[2]+values[3];
	}

	/*
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
	public static String generateId(){
		return generateId("");
	}

	/**
	 * 生成唯一id，在高并发的情况下，计算速度要高于UUID
	 * 如果只是想要得到一个唯一的id，比如链路id，可以采用该方法
	 * @param salt 加入盐计算
	 * @return String id
	 */
	public static String generateId(String salt){
		StringBuilder idString = new StringBuilder(30);
		idString.append(StringUtils.trimToEmpty(salt)).append(ipSuffix).append(pid).append(Thread.currentThread().getId())
				.append(System.currentTimeMillis());
		return idString.toString();
	}
}
