package com.vip.vjtools.vjmap.utils;

/**
 * 最多等待15分钟，超时后打印当前结果并退出，建议用户使用live参数
 */
public class TimeController {

	long start = System.currentTimeMillis();

	// 15 minutes
	long maxTime = Long.parseLong(System.getProperty("vjmap.timeout", String.valueOf(60 * 1000 * 15)));

	public void checkTimedOut() {
		if (System.currentTimeMillis() - start > maxTime) {
			throw new TimeoutException();
		}
	}

	public static class TimeoutException extends RuntimeException {

	}
}
