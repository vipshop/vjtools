package com.vip.vjtools.vjtop.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.vip.vjtools.vjtop.util.Utils;

import sun.management.counter.Counter;
import sun.management.counter.LongCounter;
import sun.management.counter.perf.PerfInstrumentation;
import sun.misc.Perf;

@SuppressWarnings("restriction")
public class PerfData {
	private final PerfInstrumentation instr;
	// PerfData中的时间相关数据以tick表示，每个tick的时长与计算机频率相关
	private final double nanosPerTick;

	private final Map<String, Counter> counters;

	public static PerfData connect(int pid) {
		try {
			return new PerfData(pid);
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
		counters = buildAllCounters();

		long hz = (Long) counters.get("sun.os.hrt.frequency").getValue();
		nanosPerTick = ((double) TimeUnit.SECONDS.toNanos(1)) / hz;
	}

	private Map<String, Counter> buildAllCounters() {
		Map<String, Counter> result = new HashMap<>(512);

		for (Counter c : instr.getAllCounters()) {
			result.put(c.getName(), c);
		}

		return result;
	}

	public Map<String, Counter> getAllCounters() {
		return counters;
	}

	public Counter findCounter(String counterName) {
		return counters.get(counterName);
	}

	public long tickToMills(LongCounter tickCounter) {
		if (tickCounter.getUnits() == sun.management.counter.Units.TICKS) {
			return (long) ((nanosPerTick * tickCounter.longValue()) / Utils.NANOS_TO_MILLS);
		} else {
			throw new IllegalArgumentException(tickCounter.getName() + " is not a ticket counter");
		}
	}
}
