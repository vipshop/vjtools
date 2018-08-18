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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.vip.vjtools.vjtop.Utils;

import sun.management.counter.perf.PerfInstrumentation;
import sun.misc.Perf;

/**
 * Wraps {@link PerfInstrumentation} class. Its purpose is to shield warnings
 * and {@link NoClassDefFoundError}s.
 * 
 * @author Alexey Ragozin (alexey.ragozin@gmail.com)
 */
@SuppressWarnings("restriction")
public abstract class PerfData {

	public static PerfData connect(long pid) {
		try {
			return new PerfIntr((int) pid);
		} catch (ThreadDeath e) {
			throw e;
		} catch (OutOfMemoryError e) {
			throw e;
		} catch (Error e) {
			throw new RuntimeException("Cannot perf data for process " + pid
					+ " - " + e.toString());
		} catch (Exception e) {
			throw new RuntimeException("Cannot perf data for process " + pid
					+ " - " + e.toString());
		}
	}

	public abstract int getMajorVersion();

	public abstract int getMinorVersion();

	public abstract long getModificationTimeStamp();

	public abstract Map<String, Counter<?>> getAllCounters();

	public abstract List<Counter<?>> findByPattern(String pattern);

	public enum Units {

		INVALID, NONE, BYTES, TICKS, EVENTS, STRING, HERTZ,
	}

	public enum Variability {

		INVALID, CONSTANT, MONOTONIC, VARIABLE,
	}

	public interface Counter<T> {

		public String getName();

		public Units getUnits();

		public Variability getVariability();

		public T getValue();

	}

	public interface StringCounter extends Counter<String> {

		public String getString();

	}

	public interface LongCounter extends Counter<Long> {

		public long getLong();

	}

	public interface TickCounter extends LongCounter {

		public long getTicks();

		double getTick();

		public long getMills();

	}

	private static class PerfIntr extends PerfData {

		private static sun.management.counter.Units U_TICKS = sun.management.counter.Units.TICKS;

		private static Map<Object, Units> UNIT_MAP = new HashMap<>();
		private static Map<Object, Variability> VARIABILITY_MAP = new HashMap<>();

		static {
			UNIT_MAP.put(sun.management.counter.Units.INVALID, Units.INVALID);
			UNIT_MAP.put(sun.management.counter.Units.NONE, Units.NONE);
			UNIT_MAP.put(sun.management.counter.Units.BYTES, Units.BYTES);
			UNIT_MAP.put(sun.management.counter.Units.TICKS, Units.TICKS);
			UNIT_MAP.put(sun.management.counter.Units.EVENTS, Units.EVENTS);
			UNIT_MAP.put(sun.management.counter.Units.STRING, Units.STRING);
			UNIT_MAP.put(sun.management.counter.Units.HERTZ, Units.HERTZ);

			VARIABILITY_MAP.put(sun.management.counter.Variability.INVALID,
					Variability.INVALID);
			VARIABILITY_MAP.put(sun.management.counter.Variability.CONSTANT,
					Variability.CONSTANT);
			VARIABILITY_MAP.put(sun.management.counter.Variability.MONOTONIC,
					Variability.MONOTONIC);
			VARIABILITY_MAP.put(sun.management.counter.Variability.VARIABLE,
					Variability.VARIABLE);
		}

		final PerfInstrumentation instr;
		final double tick;

		public PerfIntr(int pid) throws IllegalArgumentException, IOException {
			ByteBuffer bb = Perf.getPerf().attach(pid, "r");
			instr = new PerfInstrumentation(bb);
			long hz = ((sun.management.counter.LongCounter) instr
					.findByPattern("sun.os.hrt.frequency").get(0)).longValue();
			tick = ((double) TimeUnit.SECONDS.toNanos(1)) / hz;
		}

		@Override
		public int getMajorVersion() {
			return instr.getMajorVersion();
		}

		@Override
		public int getMinorVersion() {
			return instr.getMinorVersion();
		}

		@Override
		public long getModificationTimeStamp() {
			return instr.getModificationTimeStamp();
		}

		@Override
		public Map<String, Counter<?>> getAllCounters() {
			Map<String, Counter<?>> result = new LinkedHashMap<>();

			for (Object c : instr.getAllCounters()) {
				Counter<?> cc = convert(c);
				result.put(cc.getName(), cc);
			}

			return result;
		}

		@Override
		public List<Counter<?>> findByPattern(String pattern) {
			return convert(instr.findByPattern(pattern));
		}

		@SuppressWarnings("rawtypes")
		private List<Counter<?>> convert(List list) {
			List<Counter<?>> cl = new ArrayList<>(list.size());
			for (Object c : list) {
				cl.add(convert(c));
			}
			return cl;
		}

		@SuppressWarnings("rawtypes")
		private Counter<?> convert(Object c) {
			if (c instanceof sun.management.counter.LongCounter) {
				sun.management.counter.LongCounter lc = (sun.management.counter.LongCounter) c;
				if (U_TICKS.equals(lc.getUnits())) {
					return new TickWrapper(tick, lc);
				} else {
					return new LongWrapper(lc);
				}
			} else if (c instanceof sun.management.counter.StringCounter) {
				sun.management.counter.StringCounter lc = (sun.management.counter.StringCounter) c;
				return new StringWrapper(lc);
			} else if (c instanceof sun.management.counter.LongArrayCounter) {
				sun.management.counter.LongArrayCounter lc = (sun.management.counter.LongArrayCounter) c;
				return new CounterWrapper(lc);
			} else if (c instanceof sun.management.counter.ByteArrayCounter) {
				sun.management.counter.ByteArrayCounter lc = (sun.management.counter.ByteArrayCounter) c;
				return new ByteArrayWrapper(lc);
			} else {
				return new CounterWrapper((sun.management.counter.Counter) c);
			}
		}

		private static class CounterWrapper<T> implements Counter<T> {

			protected final sun.management.counter.Counter counter;

			public CounterWrapper(sun.management.counter.Counter counter) {
				this.counter = counter;
			}

			@Override
			public String getName() {
				return counter.getName();
			}

			@Override
			public Units getUnits() {
				Units u = UNIT_MAP.get(counter.getUnits());
				return u == null ? Units.INVALID : u;
			}

			@Override
			public Variability getVariability() {
				Variability v = VARIABILITY_MAP.get(counter.getVariability());
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

		private static class ByteArrayWrapper extends CounterWrapper<byte[]>
				implements Counter<byte[]> {

			public ByteArrayWrapper(
					sun.management.counter.ByteArrayCounter counter) {
				super(counter);
			}
		}

		private static class StringWrapper extends CounterWrapper<String>
				implements StringCounter {

			public StringWrapper(sun.management.counter.StringCounter counter) {
				super(counter);
			}

			@Override
			public String getString() {
				return trim(((sun.management.counter.StringCounter) counter)
						.stringValue());
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

		private static class LongWrapper extends CounterWrapper<Long> implements
				LongCounter {

			public LongWrapper(sun.management.counter.LongCounter counter) {
				super(counter);
			}

			@Override
			public long getLong() {
				return ((sun.management.counter.LongCounter) counter)
						.longValue();
			}
		}

		private static class TickWrapper extends LongWrapper implements
				TickCounter {

			private final double tick;

			public TickWrapper(double tick,
					sun.management.counter.LongCounter counter) {
				super(counter);
				this.tick = tick;
			}

			@Override
			public double getTick() {
				return tick;
			}

			@Override
			public long getTicks() {
				return getLong();
			}

			@Override
			public long getMills() {
				return (long) ((tick * getLong())/Utils.NANOS_TO_MILLS);
			}
		}
	}
}
