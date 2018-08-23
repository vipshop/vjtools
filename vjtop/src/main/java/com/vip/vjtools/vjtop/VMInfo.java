package com.vip.vjtools.vjtop;

import java.io.IOException;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.Locale;
import java.util.Map;

import com.sun.management.ThreadMXBean;
import com.vip.vjtools.vjtop.data.PerfData;
import com.vip.vjtools.vjtop.data.ProcFileData;
import com.vip.vjtools.vjtop.data.jmx.JmxClient;
import com.vip.vjtools.vjtop.data.jmx.JmxMemoryPoolManager;
import com.vip.vjtools.vjtop.util.Formats;
import com.vip.vjtools.vjtop.util.Utils;

import sun.management.counter.Counter;

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

	private Counter threadLiveCounter;
	private Counter threadDaemonCounter;
	private Counter threadPeakCounter;
	private Counter threadStartedCounter;
	private Counter classUnloadCounter;
	private Counter classLoadedCounter;
	private Counter ygcCountCounter;
	private Counter ygcTimeCounter;
	private Counter fullGcCountCounter;
	private Counter fullgcTimeCounter;
	private Counter safepointCountCounter;
	private Counter safepointTimeCounter;
	private Counter safepointSyncTimeCounter;
	private Counter currentGcCauseCounter;


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
			perfCounters = perfData.getCounters();
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

		threadCpuTimeSupported = jmxClient.getThreadMXBean().isThreadCpuTimeSupported();
		threadMemoryAllocatedSupported = jmxClient.getThreadMXBean().isThreadAllocatedMemorySupported();

		processors = jmxClient.getOperatingSystemMXBean().getAvailableProcessors();
		warningRule.updateProcessor(processors);

		isLinux = System.getProperty("os.name").toLowerCase(Locale.US).contains("linux");
	}


	/**
	 * Updates all jvm metrics to the most recent remote values
	 */
	public void update() {
		if (state == VMInfoState.ERROR_DURING_ATTACH || state == VMInfoState.DETACHED) {
			return;
		}

		try {
			int lastJmxErrorCount = jmxUpdateErrorCount;
			state = VMInfoState.ATTACHED;

			// 清空JMX内部缓存
			jmxClient.flush();

			updateUpTime();

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

			// 无新异常，状态重新判定为正常
			if (jmxUpdateErrorCount - lastJmxErrorCount == 0) {
				jmxUpdateErrorCount = 0;
			}
		} catch (Throwable e) {
			// 其他非JMX异常，直接退出
			e.printStackTrace(System.out);
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

			threadActive = (Long) threadLiveCounter.getValue();
			threadDaemon = (Long) threadDaemonCounter.getValue();
			threadPeak = (Long) threadPeakCounter.getValue();
			threadNew.update((Long) threadStartedCounter.getValue());
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
			classUnLoaded = (long) classUnloadCounter.getValue();
			classLoaded.update((long) classLoadedCounter.getValue() - classUnLoaded);
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

			direct = new Usage(jmxClient.getBufferPoolManager().getDirectBufferPool().getMemoryUsed(),
					jmxClient.getBufferPoolManager().getDirectBufferPool().getTotalCapacity(), maxDirectMemorySize);

			// 取巧用法，将count 放入无用的max中。
			long mapUsed = jmxClient.getBufferPoolManager().getMappedBufferPool().getMemoryUsed();
			map = new Usage(mapUsed, jmxClient.getBufferPoolManager().getMappedBufferPool().getTotalCapacity(),
					mapUsed == 0 ? 0 : jmxClient.getBufferPoolManager().getMappedBufferPool().getCount());

		} catch (Exception e) {
			handleJmxFetchDataError(e);
		}
	}

	private void updateGC() {
		if (perfDataSupport) {
			ygcCount.update((Long) ygcCountCounter.getValue());
			ygcTimeMills.update(perfData.tickToMills(ygcTimeCounter));
			fullgcCount.update((Long) fullGcCountCounter.getValue());
			fullgcTimeMills.update(perfData.tickToMills(fullgcTimeCounter));
		} else if (isJmxStateOk()) {
			try {
				ygcCount.update(jmxClient.getYoungCollector().getCollectionCount());
				ygcTimeMills.update(jmxClient.getYoungCollector().getCollectionTime());
				fullgcCount.update(jmxClient.getFullCollector().getCollectionCount());
				fullgcTimeMills.update(jmxClient.getFullCollector().getCollectionTime());
			} catch (Exception e) {
				handleJmxFetchDataError(e);
			}
		}
	}

	private void updateSafepoint() {
		if (!perfDataSupport) {
			return;
		}
		safepointCount.update((Long) safepointCountCounter.getValue());
		safepointTimeMills.update(perfData.tickToMills(safepointTimeCounter));
		safepointSyncTimeMills.update(perfData.tickToMills(safepointSyncTimeCounter));

		currentGcCause = (String) currentGcCauseCounter.getValue();
	}

	public ThreadMXBean getThreadMXBean() throws IOException {
		return jmxClient.getThreadMXBean();
	}

	private void initPerfCounters(Map<String, Counter> perfCounters) {
		threadLiveCounter = perfCounters.get("java.threads.live");
		threadDaemonCounter = perfCounters.get("java.threads.daemon");
		threadPeakCounter = perfCounters.get("java.threads.livePeak");
		threadStartedCounter = perfCounters.get("java.threads.started");
		classUnloadCounter = perfCounters.get("java.cls.unloadedClasses");
		classLoadedCounter = perfCounters.get("java.cls.loadedClasses");
		ygcCountCounter = perfCounters.get("sun.gc.collector.0.invocations");
		ygcTimeCounter = perfCounters.get("sun.gc.collector.0.time");

		fullGcCountCounter = perfCounters.get("sun.gc.collector.1.invocations");
		fullgcTimeCounter = perfCounters.get("sun.gc.collector.1.time");

		safepointCountCounter = perfCounters.get("sun.rt.safepoints");
		safepointTimeCounter = perfCounters.get("sun.rt.safepointTime");
		safepointSyncTimeCounter = perfCounters.get("sun.rt.safepointSyncTime");
		currentGcCauseCounter = perfCounters.get("sun.gc.cause");
	}


	private void handleJmxFetchDataError(Throwable e) {
		e.printStackTrace(System.out);
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