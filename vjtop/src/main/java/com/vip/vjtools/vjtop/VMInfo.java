package com.vip.vjtools.vjtop;

import java.io.IOException;
import java.lang.management.BufferPoolMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.Locale;
import java.util.Map;

import com.sun.management.ThreadMXBean;
import com.vip.vjtools.vjtop.data.PerfData;
import com.vip.vjtools.vjtop.data.ProcFileData;
import com.vip.vjtools.vjtop.data.jmx.JmxClient;
import com.vip.vjtools.vjtop.data.jmx.JmxMemoryPoolManager;

import sun.management.counter.Counter;

@SuppressWarnings("restriction")
public class VMInfo {
	private JmxClient jmxClient = null;

	private PerfData perfData = null;
	private Map<String, Counter> perfCounters;
	public boolean perfDataSupport = false;

	public VMInfoState state = VMInfoState.INIT;
	public String pid;
	private int updateErrorCount;

	// 静态数据//
	public String osUser;
	public String vmArgs = "";
	public String jvmVersion = "";
	public int jvmMajorVersion;
	public String permGenName;

	public int processors;
	public boolean isLinux;

	public boolean threadCpuTimeSupported;
	public boolean threadMemoryAllocatedSupported;

	// 动态数据//
	public long rss;
	public long swap;

	public Rate rchar = new Rate();
	public Rate wchar = new Rate();
	public Rate readBytes = new Rate();
	public Rate writeBytes = new Rate();

	public Rate upTimeMills = new Rate();
	public Rate cpuTimeNanos = new Rate();

	public double cpuLoad = 0.0;
	public double singleCoreCpuLoad = 0.0;

	public Rate ygcCount = new Rate();
	public Rate fullgcCount = new Rate();

	public Rate ygcTimeMills = new Rate();
	public Rate fullgcTimeMills = new Rate();


	public long threadActive;
	public long threadDaemon;
	public long threadPeak;
	public long threadStarted;

	public long classLoaded;
	public long classUnLoaded;

	public Rate safepointCount = new Rate();
	public Rate safepointTimeMills = new Rate();
	public Rate safepointSyncTimeMills = new Rate();

	public Usage eden;
	public Usage sur;
	public Usage old;

	public Usage perm;
	public Usage codeCache;
	public Usage ccs;

	public Usage direct;
	public Usage map;


	public VMInfo(JmxClient jmxClient, String vmId) throws Exception {
		this.jmxClient = jmxClient;
		this.state = VMInfoState.ATTACHED;
		this.pid = vmId;

		init();
	}

	private VMInfo() {
	}

	/**
	 * 创建JMX连接并构造VMInfo实例
	 */
	public static VMInfo processNewVM(String pid) {
		try {
			final JmxClient jmxClient = new JmxClient(pid);
			jmxClient.connect();

			// 注册JMXClient注销的钩子
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				@Override
				public void run() {
					jmxClient.disconnect();
				}
			}));

			return new VMInfo(jmxClient, pid);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}

		return createDeadVM(pid, VMInfoState.ERROR_DURING_ATTACH);
	}

	/**
	 * Creates a dead VMInfo, representing a jvm in a given state which cannot
	 * be attached or other monitoring issues occurred.
	 */
	public static VMInfo createDeadVM(String pid, VMInfoState state) {
		VMInfo vmInfo = new VMInfo();
		vmInfo.state = state;
		vmInfo.pid = pid;
		return vmInfo;
	}

	/**
	 * 初始化静态数据
	 */
	private void init() throws IOException {
		try {
			perfData = PerfData.connect(Integer.parseInt(pid));
			perfDataSupport = true;
		} catch (Exception e) {
			System.err.println("PerfData not support");
		}

		if (perfDataSupport) {
			vmArgs = (String) perfData.findCounter("java.rt.vmArgs").getValue();
		} else {
			vmArgs = Utils.join(jmxClient.getRuntimeMXBean().getInputArguments(), " ");
		}

		Map<String, String> systemProperties_ = jmxClient.getRuntimeMXBean().getSystemProperties();
		osUser = systemProperties_.get("user.name");
		jvmVersion = systemProperties_.get("java.version");
		jvmMajorVersion = getJavaMajorVersion(jvmVersion);

		permGenName = jvmMajorVersion >= 8 ? "metaspace" : "perm";

		threadCpuTimeSupported = jmxClient.getThreadMXBean().isThreadCpuTimeSupported();
		threadMemoryAllocatedSupported = jmxClient.getThreadMXBean().isThreadAllocatedMemorySupported();

		processors = jmxClient.getOperatingSystemMXBean().getAvailableProcessors();
		isLinux = jmxClient.getOperatingSystemMXBean().getName().toLowerCase(Locale.US).contains("linux");
	}

	/**
	 * Updates all jvm metrics to the most recent remote values
	 */
	public void update() throws Exception {
		if (state == VMInfoState.ERROR_DURING_ATTACH || state == VMInfoState.DETACHED) {
			return;
		}

		try {
			if (perfDataSupport) {
				perfCounters = perfData.getAllCounters();
			}

			jmxClient.flush();

			updateCpu();

			if (isLinux) {
				updateMemory();
				updateIO();
			}

			updateThreads();
			updateClassLoad();
			updateMemoryPool();
			updateGC();
			updateSafepoint();
		} catch (Throwable e) {
			handleFetchDataError(e);
		}
	}

	private void updateCpu() throws IOException {
		upTimeMills.current = jmxClient.getRuntimeMXBean().getUptime();
		cpuTimeNanos.current = jmxClient.getOperatingSystemMXBean().getProcessCpuTime();

		cpuTimeNanos.update();
		upTimeMills.update();
		cpuLoad = Utils.calcLoad(upTimeMills.delta, cpuTimeNanos.delta / (Utils.NANOS_TO_MILLS * 1D), processors);
		singleCoreCpuLoad = Utils.calcLoad(upTimeMills.delta, cpuTimeNanos.delta / (Utils.NANOS_TO_MILLS * 1D), 1);
	}

	private void updateMemory() {
		Map<String, String> procStatus = ProcFileData.getProcStatus(pid);
		rss = Utils.parseFromSize(procStatus.get("VmRSS"));
		swap = Utils.parseFromSize(procStatus.get("VmSwap"));
	}

	private void updateIO() {
		Map<String, String> procIo = ProcFileData.getProcIO(pid);
		rchar.current = Utils.parseFromSize(procIo.get("rchar"));
		wchar.current = Utils.parseFromSize(procIo.get("wchar"));
		readBytes.current = Utils.parseFromSize(procIo.get("read_bytes"));
		writeBytes.current = Utils.parseFromSize(procIo.get("write_bytes"));

		rchar.update();
		wchar.update();
		readBytes.update();
		writeBytes.update();
	}


	private void updateThreads() throws IOException {
		if (perfDataSupport) {
			threadActive = (Long) perfCounters.get("java.threads.live").getValue();
			threadDaemon = (Long) perfCounters.get("java.threads.daemon").getValue();
			threadPeak = (Long) perfCounters.get("java.threads.livePeak").getValue();
			threadStarted = (Long) perfCounters.get("java.threads.started").getValue();
		} else {
			threadActive = jmxClient.getThreadMXBean().getThreadCount();
			threadDaemon = jmxClient.getThreadMXBean().getDaemonThreadCount();
			threadPeak = jmxClient.getThreadMXBean().getPeakThreadCount();
			threadStarted = jmxClient.getThreadMXBean().getTotalStartedThreadCount();
		}
	}

	private void updateClassLoad() throws IOException {
		// 优先从perfData取值
		if (perfDataSupport) {
			classLoaded = (long) perfCounters.get("java.cls.loadedClasses").getValue();
			classUnLoaded = (long) perfCounters.get("java.cls.unloadedClasses").getValue();
		} else {
			classLoaded = jmxClient.getClassLoadingMXBean().getLoadedClassCount();
			classUnLoaded = jmxClient.getClassLoadingMXBean().getUnloadedClassCount();
		}
	}

	private void updateMemoryPool() throws IOException {
		JmxMemoryPoolManager memoryPoolManager = jmxClient.getMemoryPoolManager();

		if (perfDataSupport) {
			eden = new Usage((long) perfCounters.get("sun.gc.generation.0.space.0.used").getValue(),
					(long) perfCounters.get("sun.gc.generation.0.space.0.capacity").getValue(),
					(long) perfCounters.get("sun.gc.generation.0.space.0.maxCapacity").getValue());

			old = new Usage((long) perfCounters.get("sun.gc.generation.1.space.0.used").getValue(),
					(long) perfCounters.get("sun.gc.generation.1.space.0.capacity").getValue(),
					(long) perfCounters.get("sun.gc.generation.1.space.0.maxCapacity").getValue());

			sur = new Usage(
					(long) perfCounters.get("sun.gc.generation.0.space.1.used").getValue()
							+ (long) perfCounters.get("sun.gc.generation.0.space.2.used").getValue(),
					(long) perfCounters.get("sun.gc.generation.0.space.1.capacity").getValue()
							+ (long) perfCounters.get("sun.gc.generation.0.space.2.capacity").getValue(),
					(long) perfCounters.get("sun.gc.generation.0.space.1.maxCapacity").getValue()
							+ (long) perfCounters.get("sun.gc.generation.0.space.2.maxCapacity").getValue());

			if (jvmMajorVersion >= 8) {
				perm = new Usage((long) perfCounters.get("sun.gc.metaspace.used").getValue(),
						(long) perfCounters.get("sun.gc.metaspace.capacity").getValue(),
						(long) perfCounters.get("sun.gc.metaspace.maxCapacity").getValue());

				ccs = new Usage((long) perfCounters.get("sun.gc.compressedclassspace.used").getValue(),
						(long) perfCounters.get("sun.gc.compressedclassspace.capacity").getValue(),
						(long) perfCounters.get("sun.gc.compressedclassspace.maxCapacity").getValue());
			} else {
				perm = new Usage((long) perfCounters.get("sun.gc.generation.2.space.0.used").getValue(),
						(long) perfCounters.get("sun.gc.generation.2.space.0.capacity").getValue(),
						(long) perfCounters.get("sun.gc.generation.2.space.0.maxCapacity").getValue());
			}
		} else {
			eden = new Usage(memoryPoolManager.getEdenMemoryPool().getUsage());

			old = new Usage(memoryPoolManager.getOldMemoryPool().getUsage());

			MemoryPoolMXBean survivorMemoryPool = memoryPoolManager.getSurvivorMemoryPool();
			if (survivorMemoryPool != null) {
				sur = new Usage(survivorMemoryPool.getUsage());
			} else {
				sur = new Usage();
			}

			perm = new Usage(memoryPoolManager.getPermMemoryPool().getUsage());

			if (jvmMajorVersion >= 8) {
				MemoryPoolMXBean compressedClassSpaceMemoryPool = memoryPoolManager.getCompressedClassSpaceMemoryPool();
				if (compressedClassSpaceMemoryPool != null) {
					ccs = new Usage(compressedClassSpaceMemoryPool.getUsage());
				}
			}
		}

		codeCache = new Usage(memoryPoolManager.getCodeCacheMemoryPool().getUsage());

		direct = new Usage(jmxClient.getBufferPoolManager().getDirectBufferPool());

		map = new Usage(jmxClient.getBufferPoolManager().getMappedBufferPool());
	}

	private void updateGC() throws IOException {
		if (perfDataSupport) {
			ygcCount.current = (Long) perfCounters.get("sun.gc.collector.0.invocations").getValue();
			ygcTimeMills.current = perfData.tickToMills(perfCounters.get("sun.gc.collector.0.time"));
			fullgcCount.current = (Long) perfCounters.get("sun.gc.collector.1.invocations").getValue();
			fullgcTimeMills.current = perfData.tickToMills(perfCounters.get("sun.gc.collector.1.time"));
		} else {
			ygcCount.current = jmxClient.getYoungCollector().getCollectionCount();
			ygcTimeMills.current = jmxClient.getYoungCollector().getCollectionTime();
			fullgcCount.current = jmxClient.getFullCollector().getCollectionCount();
			fullgcTimeMills.current = jmxClient.getFullCollector().getCollectionTime();
		}

		ygcTimeMills.update();
		ygcCount.update();
		fullgcTimeMills.update();
		fullgcCount.update();
	}

	private void updateSafepoint() {
		if (!perfDataSupport) {
			return;
		}

		safepointCount.current = (Long) perfCounters.get("sun.rt.safepoints").getValue();
		safepointTimeMills.current = perfData.tickToMills(perfCounters.get("sun.rt.safepointTime"));
		safepointSyncTimeMills.current = perfData.tickToMills(perfCounters.get("sun.rt.safepointTime"));

		safepointCount.update();
		safepointTimeMills.update();
		safepointSyncTimeMills.update();
	}

	public ThreadMXBean getThreadMXBean() throws IOException {
		return jmxClient.getThreadMXBean();
	}

	private void handleFetchDataError(Throwable e) {
		e.printStackTrace(System.out);
		updateErrorCount++;

		if (updateErrorCount > 3) {
			state = VMInfoState.DETACHED;
		} else {
			state = VMInfoState.ATTACHED_UPDATE_ERROR;
		}
	}


	private static int getJavaMajorVersion(String jvmVersion) {
		if (jvmVersion.startsWith("1.8")) {
			return 8;
		} else if (jvmVersion.startsWith("1.7")) {
			return 7;
		} else if (jvmVersion.startsWith("1.6")) {
			return 6;
		} else {
			return 0;
		}
	}

	public enum VMInfoState {
		INIT, ERROR_DURING_ATTACH, ATTACHED, ATTACHED_UPDATE_ERROR, DETACHED
	}

	public class Rate {
		public long last = -1;
		public long current = -1;
		public long delta = -1;

		public void update() {
			if (last != -1) {
				delta = current - last;
			}
			last = current;
		}

	}

	public class Usage {
		public long used = -1;
		public long committed = -1;
		public long max = -1;

		public Usage() {
		}

		public Usage(long used, long committed, long max) {
			this.used = used;
			this.committed = committed;
			this.max = max;
		}

		public Usage(MemoryUsage jmxUsage) {
			this(jmxUsage.getUsed(), jmxUsage.getCommitted(), jmxUsage.getMax());
		}

		public Usage(BufferPoolMXBean bufferPoolUsage) {
			this(bufferPoolUsage.getMemoryUsed(), -1, bufferPoolUsage.getTotalCapacity());
		}
	}
}