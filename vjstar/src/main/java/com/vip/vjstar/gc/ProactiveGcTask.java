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
	private static final String OLD = "old";
	private static final String TENURED = "tenured";
	private static Logger logger = LoggerFactory.getLogger(ProactiveGcTask.class);

	private CleanUpScheduler scheduler;
	private int oldGenOccupancyFraction;

	private MemoryPoolMXBean oldMemoryPool;
	private long maxOldBytes;

	public ProactiveGcTask(CleanUpScheduler scheduler, int oldGenOccupancyFraction) {
		this.scheduler = scheduler;
		this.oldGenOccupancyFraction = oldGenOccupancyFraction;
		this.oldMemoryPool = getOldGenMemoryPool();
		this.maxOldBytes = getMemoryPoolMaxOrCommitted(oldMemoryPool);
	}

	public void run() {
		logger.info("ProactiveGcTask starting, oldGenOccupancyFraction:" + oldGenOccupancyFraction);
		try {
			long usedOldBytes = logOldGenStatus();

			if (needTriggerGc(maxOldBytes, usedOldBytes, oldGenOccupancyFraction)) {
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

	public long logOldGenStatus() {
		long usedOldBytes = oldMemoryPool.getUsage().getUsed();
		logger.info(String.format("max old gen: %s, used old gen: %s, available old gen: %s.",
				UnitConverter.toSizeUnit(maxOldBytes, 2), UnitConverter.toSizeUnit(usedOldBytes, 2),
				UnitConverter.toSizeUnit(maxOldBytes - usedOldBytes, 2)));
		return usedOldBytes;
	}

	private MemoryPoolMXBean getOldGenMemoryPool() {
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

	/**
	 * Determine whether or not to trigger gc.
	 */
	private boolean needTriggerGc(long capacityBytes, long usedBytes, int occupancyFraction) {
		return (occupancyFraction / 100.0 * capacityBytes) < usedBytes;
	}

	/**
	 * Suggests gc.
	 */
	public void doGc() {
		System.gc(); // NOSONAR
	}

	/**
	 * Stuff before gc. You can override this method to do your own stuff, for example, cache clean up, deregister from register center.
	 */
	public void preGc() {
		logger.warn("old gen is occupied larger than occupancy fraction[{}], trying to trigger gc...",
				oldGenOccupancyFraction);
	}

	/**
	 * Stuff after gc. You can override this method to do your own stuff, for example, cache warmup, reregister to register center.
	 */
	public void postGc() {
		long usedOldBytes = oldMemoryPool.getUsage().getUsed();
		logger.info(String.format("max old gen: %s, used old gen: %s, available old gen: %s, after gc.",
				UnitConverter.toSizeUnit(maxOldBytes, 2), UnitConverter.toSizeUnit(usedOldBytes, 2),
				UnitConverter.toSizeUnit((maxOldBytes - usedOldBytes), 2))); // NOSONAR
	}

}
