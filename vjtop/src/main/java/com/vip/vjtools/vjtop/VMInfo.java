package com.vip.vjtools.vjtop;

import java.io.IOException;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadInfo;
import java.util.Locale;
import java.util.Map;

import com.vip.vjtools.vjtop.data.PerfData;
import com.vip.vjtools.vjtop.data.ProcFileData;
import com.vip.vjtools.vjtop.data.jmx.JmxClient;
import com.vip.vjtools.vjtop.data.jmx.JmxMemoryPoolManager;
import com.vip.vjtools.vjtop.util.Formats;
import com.vip.vjtools.vjtop.util.Utils;

import sun.management.counter.Counter;
import sun.management.counter.LongCounter;
import sun.management.counter.StringCounter;

@SuppressWarnings("restriction")
public class VMInfo {
	private JmxClient jmxClient = null;

	private PerfData perfData = null;
	public boolean perfDataSupport = false;

	public VMInfoState state = VMInfoState.INIT;
	public String pid;
	private int jmxUpdateErrorCount;

	// 静态数据//
	private long startTime = 0;
	public String osUser;
	public String vmArgs = "";
	public String jvmVersion = "";
	public int jvmMajorVersion;

	public String permGenName;
	public long threadStackSize;
	public long maxDirectMemorySize;

	public int processors;
	public boolean isLinux;
	public boolean ioDataSupport = true;// 不是同一个用户，不能读/proc/PID/io
	public boolean processDataSupport = true;
	public boolean threadCpuTimeSupported;
	public boolean threadMemoryAllocatedSupported;
	public boolean threadContentionMonitoringSupported;

	public WarningRule warningRule = new WarningRule();

	// 动态数据//
	public Rate upTimeMills = new Rate();
	public Rate cpuTimeNanos = new Rate();

	public long rss;
	public long peakRss;
	public long swap;
	public long osThreads;

	public Rate readBytes = new Rate();
	public Rate writeBytes = new Rate();

	public double cpuLoad = 0.0;
	public double singleCoreCpuLoad = 0.0;

	public Rate ygcCount = new Rate();
	public Rate ygcTimeMills = new Rate();
	public Rate fullgcCount = new Rate();
	public Rate fullgcTimeMills = new Rate();
	public String currentGcCause = "";

	public long threadActive;
	public long threadDaemon;
	public long threadPeak;
	public Rate threadNew = new Rate();

	public Rate classLoaded = new Rate();
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

	private LongCounter threadLiveCounter;
	private LongCounter threadDaemonCounter;
	private LongCounter threadPeakCounter;
	private LongCounter threadStartedCounter;
	private LongCounter classUnloadCounter;
	private LongCounter classLoadedCounter;
	private LongCounter ygcCountCounter;
	private LongCounter ygcTimeCounter;
	private LongCounter fullGcCountCounter;
	private LongCounter fullgcTimeCounter;
	private LongCounter safepointCountCounter;
	private LongCounter safepointTimeCounter;
	private LongCounter safepointSyncTimeCounter;
	private StringCounter currentGcCauseCounter;

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
	public static VMInfo processNewVM(String pid, String jmxHostAndPort) {
		try {
			final JmxClient jmxClient = new JmxClient();
			jmxClient.connect(pid, jmxHostAndPort);

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
		Map<String, Counter> perfCounters = null;
		try {
			perfData = PerfData.connect(Integer.parseInt(pid));
			perfCounters = perfData.getAllCounters();
			initPerfCounters(perfCounters);
			perfDataSupport = true;
		} catch (Throwable ignored) {
		}

		if (perfDataSupport) {
			vmArgs = (String) perfCounters.get("java.rt.vmArgs").getValue();
		} else {
			vmArgs = Formats.join(jmxClient.getRuntimeMXBean().getInputArguments(), " ");
		}

		startTime = jmxClient.getRuntimeMXBean().getStartTime();

		Map<String, String> taregetVMSystemProperties = jmxClient.getRuntimeMXBean().getSystemProperties();
		osUser = taregetVMSystemProperties.get("user.name");
		jvmVersion = taregetVMSystemProperties.get("java.version");
		jvmMajorVersion = getJavaMajorVersion(jvmVersion);
		permGenName = jvmMajorVersion >= 8 ? "metaspace" : "perm";

		threadStackSize = 1024
				* Long.parseLong(jmxClient.getHotSpotDiagnosticMXBean().getVMOption("ThreadStackSize").getValue());
		maxDirectMemorySize = Long
				.parseLong(jmxClient.getHotSpotDiagnosticMXBean().getVMOption("MaxDirectMemorySize").getValue());
		maxDirectMemorySize = maxDirectMemorySize == 0 ? -1 : maxDirectMemorySize;

		processors = jmxClient.getOperatingSystemMXBean().getAvailableProcessors();
		warningRule.updateProcessor(processors);

		isLinux = System.getProperty("os.name").toLowerCase(Locale.US).contains("linux");
	}

	public void initThreadInfoAbility() throws IOException {
		threadCpuTimeSupported = jmxClient.getThreadMXBean().isThreadCpuTimeSupported();
		threadMemoryAllocatedSupported = jmxClient.getThreadMXBean().isThreadAllocatedMemorySupported();
		threadContentionMonitoringSupported = jmxClient.getThreadMXBean().isThreadContentionMonitoringEnabled();
	}

	/**
	 * Updates all jvm metrics to the most recent remote values
	 */
	public void update(boolean needJvmInfo) {
		if (state == VMInfoState.ERROR_DURING_ATTACH || state == VMInfoState.DETACHED) {
			return;
		}

		try {
			int lastJmxErrorCount = jmxUpdateErrorCount;
			// 将UPDTATE_ERROR重置开始新一轮循环
			state = VMInfoState.ATTACHED;

			// 清空JMX内部缓存
			jmxClient.flush();

			updateUpTime();

			if (needJvmInfo) {
				if (isLinux) {
					updateProcessStatus();
					updateIO();
				}

				updateCpu();
				updateThreads();
				updateClassLoader();
				updateMemoryPool();
				updateGC();
				updateSafepoint();
			}

			// 无新异常，状态重新判定为正常
			if (jmxUpdateErrorCount == lastJmxErrorCount) {
				jmxUpdateErrorCount = 0;
			}
		} catch (Throwable e) {
			// 其他非JMX异常，直接退出
			e.printStackTrace();
			System.out.flush();
			state = VMInfoState.DETACHED;
		}
	}

	private void updateUpTime() {
		upTimeMills.update(System.currentTimeMillis() - startTime);
		warningRule.updateInterval(Math.max(1, upTimeMills.delta / 1000));
	}

	private void updateProcessStatus() {
		if (!processDataSupport) {
			return;
		}

		Map<String, String> procStatus = ProcFileData.getProcStatus(pid);
		if (procStatus.isEmpty()) {
			processDataSupport = false;
			return;
		}
		rss = Formats.parseFromSize(procStatus.get("VmRSS"));
		peakRss = Formats.parseFromSize(procStatus.get("VmHWM"));
		swap = Formats.parseFromSize(procStatus.get("VmSwap"));
		osThreads = Long.parseLong(procStatus.get("Threads"));
	}

	private void updateIO() {
		if (!ioDataSupport) {
			return;
		}

		Map<String, String> procIo = ProcFileData.getProcIO(pid);

		if (procIo.isEmpty()) {
			ioDataSupport = false;
			return;
		}

		readBytes.update(Formats.parseFromSize(procIo.get("read_bytes")));
		writeBytes.update(Formats.parseFromSize(procIo.get("write_bytes")));

		readBytes.caculateRatePerSecond(upTimeMills.delta);
		writeBytes.caculateRatePerSecond(upTimeMills.delta);
	}

	private void updateCpu() {
		if (!isJmxStateOk()) {
			return;
		}
		try {
			cpuTimeNanos.update(jmxClient.getOperatingSystemMXBean().getProcessCpuTime());
			singleCoreCpuLoad = Utils.calcLoad(cpuTimeNanos.delta / Utils.NANOS_TO_MILLS, upTimeMills.delta);
			cpuLoad = singleCoreCpuLoad / processors;
		} catch (Exception e) {
			handleJmxFetchDataError(e);
		}
	}

	private void updateThreads() {
		if (perfDataSupport) {
			threadActive = threadLiveCounter.longValue();
			threadDaemon = threadDaemonCounter.longValue();
			threadPeak = threadPeakCounter.longValue();
			threadNew.update(threadStartedCounter.longValue());
		} else if (isJmxStateOk()) {
			try {
				threadActive = jmxClient.getThreadMXBean().getThreadCount();
				threadDaemon = jmxClient.getThreadMXBean().getDaemonThreadCount();
				threadPeak = jmxClient.getThreadMXBean().getPeakThreadCount();
				threadNew.update(jmxClient.getThreadMXBean().getTotalStartedThreadCount());
			} catch (Exception e) {
				handleJmxFetchDataError(e);
			}
		}
	}

	private void updateClassLoader() {
		// 优先从perfData取值，注意此处loadedClasses 等于JMX的TotalLoadedClassCount
		if (perfDataSupport) {
			classUnLoaded = classUnloadCounter.longValue();
			classLoaded.update(classLoadedCounter.longValue() - classUnLoaded);
		} else if (isJmxStateOk()) {
			try {
				classUnLoaded = jmxClient.getClassLoadingMXBean().getUnloadedClassCount();
				classLoaded.update(jmxClient.getClassLoadingMXBean().getLoadedClassCount());
			} catch (Exception e) {
				handleJmxFetchDataError(e);
			}
		}
	}

	private void updateMemoryPool() {
		if (!isJmxStateOk()) {
			return;
		}

		try {
			JmxMemoryPoolManager memoryPoolManager = jmxClient.getMemoryPoolManager();
			eden = new Usage(memoryPoolManager.getEdenMemoryPool().getUsage());
			old = new Usage(memoryPoolManager.getOldMemoryPool().getUsage());
			warningRule.updateOld(old.max);

			MemoryPoolMXBean survivorMemoryPool = memoryPoolManager.getSurvivorMemoryPool();
			if (survivorMemoryPool != null) {
				sur = new Usage(survivorMemoryPool.getUsage());
			} else {
				sur = new Usage();
			}

			perm = new Usage(memoryPoolManager.getPermMemoryPool().getUsage());
			warningRule.updatePerm(perm.max);

			if (jvmMajorVersion >= 8) {
				MemoryPoolMXBean compressedClassSpaceMemoryPool = memoryPoolManager.getCompressedClassSpaceMemoryPool();
				if (compressedClassSpaceMemoryPool != null) {
					ccs = new Usage(compressedClassSpaceMemoryPool.getUsage());
				} else {
					ccs = new Usage();
				}
			}

			codeCache = new Usage(memoryPoolManager.getCodeCacheMemoryPool().getUsage());

			direct = new Usage(jmxClient.getBufferPoolManager().getDirectBufferPoolUsed(),
					jmxClient.getBufferPoolManager().getDirectBufferPoolCapacity(), maxDirectMemorySize);

			// 取巧用法，将count 放入无用的max中。
			long mapUsed = jmxClient.getBufferPoolManager().getMappedBufferPoolUsed();
			map = new Usage(mapUsed, jmxClient.getBufferPoolManager().getMappedBufferPoolCapacity(),
					mapUsed == 0 ? 0 : jmxClient.getBufferPoolManager().getMappedBufferPoolCount());

		} catch (Exception e) {
			handleJmxFetchDataError(e);
		}
	}

	private void updateGC() {
		if (perfDataSupport) {
			ygcCount.update(ygcCountCounter.longValue());
			ygcTimeMills.update(perfData.tickToMills(ygcTimeCounter));
			if (fullGcCountCounter != null) {
				fullgcCount.update(fullGcCountCounter.longValue());
				fullgcTimeMills.update(perfData.tickToMills(fullgcTimeCounter));
			}
		} else if (isJmxStateOk()) {
			try {
				ygcCount.update(jmxClient.getGarbageCollectorManager().getYoungCollector().getCollectionCount());
				ygcTimeMills.update(jmxClient.getGarbageCollectorManager().getYoungCollector().getCollectionTime());

				if (jmxClient.getGarbageCollectorManager().getFullCollector() != null) {
					fullgcCount.update(jmxClient.getGarbageCollectorManager().getFullCollector().getCollectionCount());
					fullgcTimeMills
							.update(jmxClient.getGarbageCollectorManager().getFullCollector().getCollectionTime());
				}
			} catch (Exception e) {
				handleJmxFetchDataError(e);
			}
		}
	}

	private void updateSafepoint() {
		if (!perfDataSupport) {
			return;
		}
		safepointCount.update(safepointCountCounter.longValue());
		safepointTimeMills.update(perfData.tickToMills(safepointTimeCounter));
		safepointSyncTimeMills.update(perfData.tickToMills(safepointSyncTimeCounter));

		currentGcCause = (String) currentGcCauseCounter.getValue();
	}

	public long[] getAllThreadIds() throws IOException {
		return jmxClient.getThreadMXBean().getAllThreadIds();
	}

	public long[] getThreadCpuTime(long[] tids) throws IOException {
		return jmxClient.getThreadMXBean().getThreadCpuTime(tids);
	}

	public long[] getThreadUserTime(long[] tids) throws IOException {
		return jmxClient.getThreadMXBean().getThreadUserTime(tids);
	}

	public ThreadInfo[] getThreadInfo(long[] tids) throws IOException {
		return jmxClient.getThreadMXBean().getThreadInfo(tids);
	}

	public ThreadInfo getThreadInfo(long tid, int maxDepth) throws IOException {
		return jmxClient.getThreadMXBean().getThreadInfo(tid, maxDepth);
	}

	public ThreadInfo[] getThreadInfo(long[] tids, int maxDepth) throws IOException {
		return jmxClient.getThreadMXBean().getThreadInfo(tids, maxDepth);
	}

	public ThreadInfo[] getAllThreadInfo() throws IOException {
		return jmxClient.getThreadMXBean().dumpAllThreads(false, false);
	}

	public long[] getThreadAllocatedBytes(long[] tids) throws IOException {
		return jmxClient.getThreadMXBean().getThreadAllocatedBytes(tids);
	}

	private void initPerfCounters(Map<String, Counter> perfCounters) {
		threadLiveCounter = (LongCounter) perfCounters.get("java.threads.live");
		threadDaemonCounter = (LongCounter) perfCounters.get("java.threads.daemon");
		threadPeakCounter = (LongCounter) perfCounters.get("java.threads.livePeak");
		threadStartedCounter = (LongCounter) perfCounters.get("java.threads.started");

		classUnloadCounter = (LongCounter) perfCounters.get("java.cls.unloadedClasses");
		classLoadedCounter = (LongCounter) perfCounters.get("java.cls.loadedClasses");

		ygcCountCounter = (LongCounter) perfCounters.get("sun.gc.collector.0.invocations");
		ygcTimeCounter = (LongCounter) perfCounters.get("sun.gc.collector.0.time");
		fullGcCountCounter = (LongCounter) perfCounters.get("sun.gc.collector.1.invocations");
		fullgcTimeCounter = (LongCounter) perfCounters.get("sun.gc.collector.1.time");

		safepointCountCounter = (LongCounter) perfCounters.get("sun.rt.safepoints");
		safepointTimeCounter = (LongCounter) perfCounters.get("sun.rt.safepointTime");
		safepointSyncTimeCounter = (LongCounter) perfCounters.get("sun.rt.safepointSyncTime");
		currentGcCauseCounter = (StringCounter) perfCounters.get("sun.gc.cause");
	}

	public void handleJmxFetchDataError(Throwable e) {
		System.out.println("");
		e.printStackTrace();
		System.out.flush();
		jmxUpdateErrorCount++;

		// 连续三次刷新周期JMX 获取数据失败则退出
		if (jmxUpdateErrorCount > 3) {
			state = VMInfoState.DETACHED;
		} else {
			state = VMInfoState.ATTACHED_UPDATE_ERROR;
		}
	}

	public boolean isJmxStateOk() {
		return state != VMInfoState.ATTACHED_UPDATE_ERROR && state != VMInfoState.DETACHED;
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

	public static class Rate {
		private long last = -1;
		public long current = -1;
		public long delta = 0;
		public long ratePerSecond = 0;

		public void update(long current) {
			this.current = current;
			if (last != -1) {
				delta = current - last;
			}
			last = current;
		}

		public void caculateRatePerSecond(long deltaTimeMills) {
			if (delta != 0) {
				ratePerSecond = delta * 1000 / deltaTimeMills;
			}
		}
	}

	public static class Usage {
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
	}
}