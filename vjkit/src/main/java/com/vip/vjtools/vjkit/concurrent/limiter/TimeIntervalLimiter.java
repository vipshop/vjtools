package com.vip.vjtools.vjkit.concurrent.limiter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class TimeIntervalLimiter {

	private final AtomicLong lastTimeAtom = new AtomicLong(0);

	private long windowSizeMillis;

	public TimeIntervalLimiter(long interval, TimeUnit timeUnit) {
		this.windowSizeMillis = timeUnit.toMillis(interval);
	}

	public boolean tryAcquire() {
		long currentTime = System.currentTimeMillis();
		long lastTime = lastTimeAtom.get();
		return currentTime - lastTime >= windowSizeMillis && lastTimeAtom.compareAndSet(lastTime, currentTime);
	}
}
