package com.vip.vjtools.vjtop.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.vip.vjtools.vjtop.Utils;

import sun.management.counter.Counter;
import sun.management.counter.perf.PerfInstrumentation;
import sun.misc.Perf;

@SuppressWarnings("restriction")
public class PerfData {
	private final PerfInstrumentation instr;
	// PerfData中的时间相关数据以tick表示，每个tick的时长与计算机频率相关
	private final double nanosPerTick;

	public static PerfData connect(long pid) {
		try {
			return new PerfData((int) pid);
		} catch (ThreadDeath e) {
			throw e;
		} catch (OutOfMemoryError e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException("Cannot perf data for process " + pid + " - " + e.toString());
		}
	}

	private PerfData(int pid) throws IOException {
		ByteBuffer bb = Perf.getPerf().attach(pid, "r");
		instr = new PerfInstrumentation(bb);
		long hz = ((sun.management.counter.LongCounter) instr.findByPattern("sun.os.hrt.frequency").get(0)).longValue();
		nanosPerTick = ((double) TimeUnit.SECONDS.toNanos(1)) / hz;
	}

	public Counter findCounter(String pattern) {
		return instr.findByPattern(pattern).get(0);
	}

	public Map<String, Counter> getAllCounters() {
		Map<String, Counter> result = new LinkedHashMap<>();

		for (Counter c : instr.getAllCounters()) {
			result.put(c.getName(), c);
		}

		return result;
	}

	public List<Counter> findByPattern(String pattern) {
		return instr.findByPattern(pattern);
	}

	public long tickToMills(Counter tickCounter) {
		if (tickCounter.getUnits() == sun.management.counter.Units.TICKS) {
			return (long) ((nanosPerTick * (Long) tickCounter.getValue()) / Utils.NANOS_TO_MILLS);
		} else {
			throw new IllegalArgumentException(tickCounter.getName() + " is not a ticket counter");
		}
	}
}
