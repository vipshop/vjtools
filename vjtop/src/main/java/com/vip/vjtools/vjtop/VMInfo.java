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
	public long threadStackSize;
	public long maxDirectMemorySize;

	public int processors;
	public boolean isLinux;
	public boolean ioDataSupport = true;// 不是同一个用户，不能读/proc/PID/io
	public boolean processDataSupport = true;
	public boolean threadCpuTimeSupported;
	public boolean threadMemoryAllocatedSupported;

	public WarningRule warning = new WarningRule();

	// 动态数据//
	public Rate upTimeMills = new Rate();
	public Rate cpuTimeNanos = new Rate();

	public long rss;
	public long peakRss;
	public long swap;
	public long osThreads;
	public Rate voluntaryCtxtSwitch = new Rate();
	public Rate nonvoluntaryCtxtSwitch = new Rate();

	public Rate readBytes = new Rate();
	public Rate writeBytes = new Rate();

	public double cpuLoad = 0.0;
	public double singleCoreCpuLoad = 0.0;

	public Rate ygcCount = new Rate();
	public Rate ygcTimeMills = new Rate();
	public Rate fullgcCount = new Rate();
	public Rate fullgcTimeMills = new Rate();

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
		try {
			perfData = PerfData.connect(Integer.parseInt(pid));
			perfDataSupport = true;
		} catch (Throwable ignored) {
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

		threadStackSize = 1024
				* Long.parseLong(jmxClient.getHotSpotDiagnosticMXBean().getVMOption("ThreadStackSize").getValue());
		maxDirectMemorySize = Long
				.parseLong(jmxClient.getHotSpotDiagnosticMXBean().getVMOption("MaxDirectMemorySize").getValue());
		maxDirectMemorySize = maxDirectMemorySize == 0 ? -1 : maxDirectMemorySize;

		permGenName = jvmMajorVersion >= 8 ? "metaspace" : "perm";

		threadCpuTimeSupported = jmxClient.getThreadMXBean().isThreadCpuTimeSupported();
		threadMemoryAllocatedSupported = jmxClient.getThreadMXBean().isThreadAllocatedMemorySupported();

		processors = jmxClient.getOperatingSystemMXBean().getAvailableProcessors();
		warning.updateProcessor(processors);

		isLinux = System.getProperty("os.name").toLowerCase(Locale.US).contains("linux");

	}

	/**
	 * Updates all jvm metrics to the most recent remote values
	 */
	public void update() throws IOException {
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
				updateProcessStatus();
				updateIO();
			}

			updateThreads();
			updateClassLoader();
			updateMemoryPool();
			updateGC();
			updateSafepoint();
		} catch (Throwable e) {
			handleFetchDataError(e);
		}
	}

	private void updateCpu() throws IOException {
		upTimeMills.update(jmxClient.getRuntimeMXBean().getUptime());
		cpuTimeNanos.update(jmxClient.getOperatingSystemMXBean().getProcessCpuTime());

		singleCoreCpuLoad = Utils.calcLoad(cpuTimeNanos.delta / Utils.NANOS_TO_MILLS, upTimeMills.delta);
		cpuLoad = singleCoreCpuLoad / processors;
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
		rss = Utils.parseFromSize(procStatus.get("VmRSS"));
		peakRss = Utils.parseFromSize(procStatus.get("VmHWM"));
		swap = Utils.parseFromSize(procStatus.get("VmSwap"));
		osThreads = Long.parseLong(procStatus.get("Threads"));

		voluntaryCtxtSwitch.update(Long.parseLong(procStatus.get("voluntary_ctxt_switches")));
		nonvoluntaryCtxtSwitch.update(Long.parseLong(procStatus.get("nonvoluntary_ctxt_switches")));
		voluntaryCtxtSwitch.caculateRatePerSecond(upTimeMills.delta);
		nonvoluntaryCtxtSwitch.caculateRatePerSecond(upTimeMills.delta);
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

		readBytes.update(Utils.parseFromSize(procIo.get("read_bytes")));
		writeBytes.update(Utils.parseFromSize(procIo.get("write_bytes")));

		readBytes.caculateRatePerSecond(upTimeMills.delta);
		writeBytes.caculateRatePerSecond(upTimeMills.delta);
	}

	private void updateThreads() throws IOException {
		if (perfDataSupport) {
			threadActive = (Long) perfCounters.get("java.threads.live").getValue();
			threadDaemon = (Long) perfCounters.get("java.threads.daemon").getValue();
			threadPeak = (Long) perfCounters.get("java.threads.livePeak").getValue();
			threadNew.update((Long) perfCounters.get("java.threads.started").getValue());
		} else {
			threadActive = jmxClient.getThreadMXBean().getThreadCount();
			threadDaemon = jmxClient.getThreadMXBean().getDaemonThreadCount();
			threadPeak = jmxClient.getThreadMXBean().getPeakThreadCount();
			threadNew.update(jmxClient.getThreadMXBean().getTotalStartedThreadCount());
		}
	}

	private void updateClassLoader() throws IOException {
		// 优先从perfData取值，注意此处loadedClasses 等于JMX的TotalLoadedClassCount
		if (perfDataSupport) {
			classUnLoaded = (long) perfCounters.get("java.cls.unloadedClasses").getValue();
			classLoaded.update((long) perfCounters.get("java.cls.loadedClasses").getValue() - classUnLoaded);
		} else {
			classUnLoaded = jmxClient.getClassLoadingMXBean().getUnloadedClassCount();
			classLoaded.update(jmxClient.getClassLoadingMXBean().getLoadedClassCount());
		}
	}

	private void updateMemoryPool() throws IOException {
		JmxMemoryPoolManager memoryPoolManager = jmxClient.getMemoryPoolManager();

		eden = new Usage(memoryPoolManager.getEdenMemoryPool().getUsage());
		old = new Usage(memoryPoolManager.getOldMemoryPool().getUsage());
		warning.updateOld(old.max);

		MemoryPoolMXBean survivorMemoryPool = memoryPoolManager.getSurvivorMemoryPool();
		if (survivorMemoryPool != null) {
			sur = new Usage(survivorMemoryPool.getUsage());
		} else {
			sur = new Usage();
		}

		perm = new Usage(memoryPoolManager.getPermMemoryPool().getUsage());
		warning.updatePerm(perm.max);

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
	}

	private void updateGC() throws IOException {
		if (perfDataSupport) {
			ygcCount.update((Long) perfCounters.get("sun.gc.collector.0.invocations").getValue());
			ygcTimeMills.update(perfData.tickToMills(perfCounters.get("sun.gc.collector.0.time")));
			fullgcCount.update((Long) perfCounters.get("sun.gc.collector.1.invocations").getValue());
			fullgcTimeMills.update(perfData.tickToMills(perfCounters.get("sun.gc.collector.1.time")));
		} else {
			ygcCount.update(jmxClient.getYoungCollector().getCollectionCount());
			ygcTimeMills.update(jmxClient.getYoungCollector().getCollectionTime());
			fullgcCount.update(jmxClient.getFullCollector().getCollectionCount());
			fullgcTimeMills.update(jmxClient.getFullCollector().getCollectionTime());
		}
	}

	private void updateSafepoint() {
		if (!perfDataSupport) {
			return;
		}

		safepointCount.update((Long) perfCounters.get("sun.rt.safepoints").getValue());
		safepointTimeMills.update(perfData.tickToMills(perfCounters.get("sun.rt.safepointTime")));
		safepointSyncTimeMills.update(perfData.tickToMills(perfCounters.get("sun.rt.safepointTime")));
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