package com.vip.vjstar.gc;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.vjtools.vjkit.number.UnitConverter;

/**
 * Detect old gen usage of current jvm periodically and trigger a cms gc if necessary.<br/>
 * In order to enable this feature, add these options to your target jvm:<br/>
 * -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=75 -XX:+ExplicitGCInvokesConcurrent<br/>
 * You can alter this class to work on a remote jvm using jmx.
 */
public class ProactiveGcTask implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(ProactiveGcTask.class);

	protected CleanUpScheduler scheduler;
	protected int oldGenOccupancyFraction;

	protected MemoryPoolMXBean oldGenMemoryPool;
	protected long maxOldGenBytes;
	protected boolean valid;

	public ProactiveGcTask(CleanUpScheduler scheduler, int oldGenOccupancyFraction) {
		this.scheduler = scheduler;
		this.oldGenOccupancyFraction = oldGenOccupancyFraction;
		this.oldGenMemoryPool = getOldGenMemoryPool();

		if (oldGenMemoryPool != null && oldGenMemoryPool.isValid()) {
			this.maxOldGenBytes = getMemoryPoolMaxOrCommitted(oldGenMemoryPool);
			this.valid = true;
		} else {
			this.valid = false;
		}
	}

	public void run() {
		if (!valid) {
			logger.warn("OldMemoryPool is not valid, task stop.");
			return;
		}

		try {
			long usedOldGenBytes = logOldGenStatus("checking oldgen status");

			if (needTriggerGc(maxOldGenBytes, usedOldGenBytes, oldGenOccupancyFraction)) {
				preGc();
				doGc();
				postGc();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			scheduler.reschedule(this);
		}
	}

	/**
	 * Determine whether or not to trigger gc.
	 */
	private boolean needTriggerGc(long capacityBytes, long usedBytes, int occupancyFraction) {
		return (occupancyFraction * capacityBytes / 100) < usedBytes;
	}

	/**
	 * Suggests gc.
	 */
	protected void doGc() {
		System.gc(); // NOSONAR
	}

	/**
	 * Stuff before gc. You can override this method to do your own stuff, for example, cache clean up, deregister from register center.
	 */
	protected void preGc() {
		logger.warn("old gen is occupied larger than occupancy fraction[{}], trying to trigger gc...",
				oldGenOccupancyFraction);
	}

	/**
	 * Stuff after gc. You can override this method to do your own stuff, for example, cache warmup, reregister to register center.
	 */
	protected void postGc() {
		logOldGenStatus("post gc");
	}

	protected long logOldGenStatus(String hints) {
		long usedOldBytes = oldGenMemoryPool.getUsage().getUsed();
		logger.info(String.format("%s, max old gen:%s, used old gen:%s, current fraction: %.2f%%, gc fraction: %d%%",
				hints, UnitConverter.toSizeUnit(maxOldGenBytes, 2), UnitConverter.toSizeUnit(usedOldBytes, 2),
				usedOldBytes * 100d / maxOldGenBytes, oldGenOccupancyFraction));
		return usedOldBytes;
	}

	private MemoryPoolMXBean getOldGenMemoryPool() {
		String OLD = "old";
		String TENURED = "tenured";

		MemoryPoolMXBean oldGenMemoryPool = null;
		List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getPlatformMXBeans(MemoryPoolMXBean.class);
		for (MemoryPoolMXBean memoryPool : memoryPoolMXBeans) {
			String name = memoryPool.getName().trim().toLowerCase();
			if (name.contains(OLD) || name.contains(TENURED)) {
				oldGenMemoryPool = memoryPool;
				break;
			}
		}

		return oldGenMemoryPool;
	}

	private long getMemoryPoolMaxOrCommitted(MemoryPoolMXBean memoryPool) {
		MemoryUsage usage = memoryPool.getUsage();
		long max = usage.getMax();
		return max < 0 ? usage.getCommitted() : max;
	}
}
