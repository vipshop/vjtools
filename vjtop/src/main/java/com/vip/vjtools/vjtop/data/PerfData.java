/**
 * Copyright 2015 Alexey Ragozin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vip.vjtools.vjtop.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.vip.vjtools.vjtop.Utils;

import sun.management.counter.Units;
import sun.management.counter.Variability;
import sun.management.counter.perf.PerfInstrumentation;
import sun.misc.Perf;

/**
 * Wraps {@link PerfInstrumentation} class. Its purpose is to shield warnings
 * and {@link NoClassDefFoundError}s.
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
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
		} catch (Error e) {
			throw new RuntimeException("Cannot perf data for process " + pid + " - " + e.toString());
		} catch (Exception e) {
			throw new RuntimeException("Cannot perf data for process " + pid + " - " + e.toString());
		}
	}

	private PerfData(int pid) throws IOException {
		ByteBuffer bb = Perf.getPerf().attach(pid, "r");
		instr = new PerfInstrumentation(bb);
		long hz = ((sun.management.counter.LongCounter) instr.findByPattern("sun.os.hrt.frequency").get(0)).longValue();
		nanosPerTick = ((double) TimeUnit.SECONDS.toNanos(1)) / hz;
	}

	public long getModificationTimeStamp() {
		return instr.getModificationTimeStamp();
	}

	public Map<String, Counter<?>> getAllCounters() {
		Map<String, Counter<?>> result = new LinkedHashMap<String, PerfData.Counter<?>>();

		for (Object c : instr.getAllCounters()) {
			Counter<?> cc = convert(c);
			result.put(cc.getName(), cc);
		}

		return result;
	}

	public List<Counter<?>> findByPattern(String pattern) {
		return convert(instr.findByPattern(pattern));
	}

	@SuppressWarnings("rawtypes")
	private List<Counter<?>> convert(List list) {
		List<Counter<?>> cl = new ArrayList<Counter<?>>(list.size());
		for (Object c : list) {
			cl.add(convert(c));
		}
		return cl;
	}

	@SuppressWarnings("rawtypes")
	private Counter<?> convert(Object c) {
		if (c instanceof sun.management.counter.LongCounter) {
			sun.management.counter.LongCounter lc = (sun.management.counter.LongCounter) c;
			if (sun.management.counter.Units.TICKS.equals(lc.getUnits())) {
				return new TickCounter(nanosPerTick, lc);
			} else {
				return new LongCounter(lc);
			}
		} else if (c instanceof sun.management.counter.StringCounter) {
			sun.management.counter.StringCounter lc = (sun.management.counter.StringCounter) c;
			return new StringCounter(lc);
		} else {
			return new CounterWrapper((sun.management.counter.Counter) c);
		}
	}

	public interface Counter<T> {

		public String getName();

		public Units getUnits();

		public Variability getVariability();

		public T getValue();
	}

	public static class CounterWrapper<T> implements Counter<T> {

		protected final sun.management.counter.Counter counter;

		public CounterWrapper(sun.management.counter.Counter counter) {
			this.counter = counter;
		}

		@Override
		public String getName() {
			return counter.getName();
		}

		@Override
		public sun.management.counter.Units getUnits() {
			sun.management.counter.Units u = counter.getUnits();
			return u == null ? sun.management.counter.Units.INVALID : u;
		}

		@Override
		public Variability getVariability() {
			Variability v = counter.getVariability();
			return v == null ? Variability.INVALID : v;
		}

		@Override
		@SuppressWarnings("unchecked")
		public T getValue() {
			return (T) counter.getValue();
		}

		@Override
		public String toString() {
			return counter.toString().replace((char) 0, ' ');
		}
	}

	public static class StringCounter extends CounterWrapper<String> {
		public StringCounter(sun.management.counter.StringCounter counter) {
			super(counter);
		}

		public String getString() {
			return trim(((sun.management.counter.StringCounter) counter).stringValue());
		}

		private String trim(String value) {
			int n = value.indexOf(0);
			if (n >= 0) {
				return value.substring(0, n);
			} else {
				return value;
			}
		}
	}

	public static class LongCounter extends CounterWrapper<Long> {

		public LongCounter(sun.management.counter.LongCounter counter) {
			super(counter);
		}

		public long getLong() {
			return ((sun.management.counter.LongCounter) counter).longValue();
		}
	}

	public static class TickCounter extends LongCounter {

		private final double nanosPerTick;

		public TickCounter(double nanosPerTick, sun.management.counter.LongCounter counter) {
			super(counter);
			this.nanosPerTick = nanosPerTick;
		}
		
		public long getTicks() {
			return getLong();
		}

		public long getMills() {
			return (long) ((nanosPerTick * getLong()) / Utils.NANOS_TO_MILLS);
		}
	}
}
