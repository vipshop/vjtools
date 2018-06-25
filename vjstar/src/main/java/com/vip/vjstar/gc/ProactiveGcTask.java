package com.vip.vjstar.gc;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.vjtools.vjkit.number.SizeUnit;
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
	private static Logger log = LoggerFactory.getLogger(ProactiveGcTask.class);

	private CleanUpScheduler scheduler;
	private int oldGenOccupancyFraction;
	private MemoryPoolMXBean oldMemoryPool;

	public ProactiveGcTask(CleanUpScheduler scheduler, int oldGenOccupancyFraction) {
		this.scheduler = scheduler;
		this.oldGenOccupancyFraction = oldGenOccupancyFraction;
	}

	public void run() {
		log.info("ProactiveGcTask starting, oldGenOccupancyFraction:" + oldGenOccupancyFraction + ", datetime: "
				+ new Date());
		try {
			oldMemoryPool = getOldMemoryPool();
			long maxOldBytes = getMemoryPoolMaxOrCommitted(oldMemoryPool);
			long oldUsedBytes = oldMemoryPool.getUsage().getUsed();
			log.info(String.format("max old gen: %.2f MB, used old gen: %.2f MB, available old gen: %.2f MB.",
					SizeUnit.BYTES.toMegaBytes(maxOldBytes), SizeUnit.BYTES.toMegaBytes(oldUsedBytes),
					SizeUnit.BYTES.toMegaBytes(maxOldBytes - oldUsedBytes)));
			if (needTriggerGc(maxOldBytes, oldUsedBytes, oldGenOccupancyFraction)) {
				preGc();
				doGc();
				postGc();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (!scheduler.isShutdown()) { // reschedule this task
				try {
					scheduler.reschedule(this);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}

	private MemoryPoolMXBean getOldMemoryPool() {
		MemoryPoolMXBean oldMemoryPool = null;
		List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getPlatformMXBeans(MemoryPoolMXBean.class);
		for (MemoryPoolMXBean memoryPool : memoryPoolMXBeans) {
			String name = memoryPool.getName().trim();
			String lowerCaseName = name.toLowerCase();
			if (lowerCaseName.contains(OLD) || lowerCaseName.contains(TENURED)) {
				oldMemoryPool = memoryPool;
				break;
			}
		}
		return oldMemoryPool;
	}

	private long getMemoryPoolMaxOrCommitted(MemoryPoolMXBean memoryPool) {
		MemoryUsage usage = memoryPool.getUsage();
		long max = usage.getMax();
		max = max < 0 ? usage.getCommitted() : max;
		return max;
	}

	/**
	 * Determine whether or not to trigger gc.
	 * 
	 * @param capacityBytes
	 *            old gen capacity
	 * @param usedBytes
	 *            used old gen
	 * @param occupancyFraction
	 *            old gen used fraction
	 * @return
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
		log.warn("old gen is occupied larger than occupancy fraction[{}], trying to trigger gc...",
				oldGenOccupancyFraction);
	}

	/**
	 * Stuff after gc. You can override this method to do your own stuff, for example, cache warmup, reregister to register center.
	 */
	public void postGc() {
		long maxOldBytes = getMemoryPoolMaxOrCommitted(oldMemoryPool);
		long oldUsedBytes = oldMemoryPool.getUsage().getUsed();
		log.info(String.format("max old gen: %sf, used old gen: %s, available old gen: %.2f MB, after gc.",
				UnitConverter.toSizeUnit(maxOldBytes, 1), UnitConverter.toSizeUnit(oldUsedBytes, 1),
				UnitConverter.toSizeUnit((maxOldBytes - oldUsedBytes), 1))); // NOSONAR
		oldMemoryPool = null;
	}

}
