package com.vip.vjtools.vjtop;

import java.io.IOException;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.rmi.ConnectException;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.management.ThreadMXBean;
import com.sun.tools.attach.AttachNotSupportedException;
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
	private long lastRchar = -1;
	private long lastWchar = -1;
	private long lastReadBytes = -1;
	private long lastWriteBytes = -1;
	public long deltaRchar = -1;
	public long deltaWchar = -1;
	public long deltaReadBytes = -1;
	public long deltaWriteBytes = -1;

	public long lastUpTimeMills = -1;
	public long lastCPUTimeNanos = -1;
	public long deltaUptimeMills = 0;
	private long deltaCpuTimeNanos = 0;

	public double cpuLoad = 0.0;
	public double singleCoreCpuLoad = 0.0;

	private long lastYgcCount = -1;
	public long deltaYgcCount;

	private long lastFullgcCount = -1;
	public long deltaFullgcCount;

	private long lastYgcTimeMills;
	public long deltaYgcTimeMills;

	private long lastFullgcTimeMills;
	public long deltaFullgcTimeMills;

	public long rss;
	public long swap;

	public long threadActive;
	public long threadDaemon;
	public long threadPeak;
	public long threadStarted;

	public long classLoaded;
	public long classUnLoaded;

	private long lastSafepointCount = -1;
	public long deltaSafepointCount;
	private long lastSafepointTimeMills;
	public long deltaSafepointTimeMills;
	private long lastSafepointSyncTimeMills;
	public long deltaSafepointSyncTimeMills;

	public long edenUsedBytes;
	public long edenMaxBytes;
	public long surUsedBytes;
	public long surMaxBytes;
	public long oldUsedBytes;
	public long oldMaxBytes;
	public long permUsedBytes;
	public long permMaxBytes;
	public long codeCacheUsedBytes;
	public long codeCacheMaxBytes;
	public long ccsUsedBytes;
	public long ccsMaxBytes;

	public long directUsedBytes;
	public long directMaxBytes;
	public long mapUsedBytes;
	public long mapMaxBytes;

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

			if (!jmxClient.isConnected()) {
				Logger.getLogger("vjtop").log(Level.SEVERE, "connection refused (PID=" + pid + ")");
				return createDeadVM(pid, VMInfoState.ERROR_DURING_ATTACH);
			}

			// 注册JMXClient注销的钩子
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				@Override
				public void run() {
					jmxClient.disconnect();
				}
			}));

			return new VMInfo(jmxClient, pid);
		} catch (ConnectException rmiE) {
			if (rmiE.getMessage().contains("refused")) {
				Logger.getLogger("vjtop").log(Level.SEVERE, "connection refused (PID=" + pid + ")", rmiE);
				return createDeadVM(pid, VMInfoState.CONNECTION_REFUSED);
			}
			rmiE.printStackTrace(System.err);
		} catch (IOException e) {
			if ((e.getCause() != null && e.getCause() instanceof AttachNotSupportedException)
					|| e.getMessage().contains("Permission denied")) {
				Logger.getLogger("vjtop").log(Level.SEVERE, "could not attach (PID=" + pid + ")", e);
				return createDeadVM(pid, VMInfoState.CONNECTION_REFUSED);
			}
			e.printStackTrace(System.err);
		} catch (Exception e) {
			Logger.getLogger("vjtop").log(Level.SEVERE, "could not attach (PID=" + pid + ")", e);
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

		vmArgs = Utils.join(jmxClient.getRuntimeMXBean().getInputArguments(), " ");

		Map<String, String> systemProperties_ = jmxClient.getRuntimeMXBean().getSystemProperties();
		osUser = systemProperties_.get("user.name");
		jvmVersion = systemProperties_.get("java.version");
		jvmMajorVersion = getJavaMajorVersion();

		permGenName = jmxClient.getMemoryPoolManager().getPermMemoryPool().getName().toLowerCase();

		threadCpuTimeSupported = jmxClient.getThreadMXBean().isThreadCpuTimeSupported();
		threadMemoryAllocatedSupported = jmxClient.getThreadMXBean().isThreadAllocatedMemorySupported();

		processors = jmxClient.getOperatingSystemMXBean().getAvailableProcessors();
		isLinux = jmxClient.getOperatingSystemMXBean().getName().toLowerCase(Locale.US).contains("linux");
	}

	/**
	 * Updates all jvm metrics to the most recent remote values
	 */
	public void update() throws Exception {
		if (state == VMInfoState.ERROR_DURING_ATTACH || state == VMInfoState.DETACHED
				|| state == VMInfoState.CONNECTION_REFUSED) {
			return;
		}

		try {
			if (perfDataSupport) {
				perfCounters = perfData.getAllCounters();
			}

			jmxClient.flush();

			updateIO();
			updateCpu();
			updateThreads();
			updateClassLoad();
			updateMemoryPool();
			updateGC();
			updateSafepoint();
		} catch (Throwable e) {
			handleFetchDataError(e);
		}
	}

	private void updateIO() {

		Map<String, String> procIo = ProcFileData.getProcIO(pid);
		long rchar = Utils.parseFromSize(procIo.get("rchar"));
		long wchar = Utils.parseFromSize(procIo.get("wchar"));
		long readBytes = Utils.parseFromSize(procIo.get("read_bytes"));
		long writeBytes = Utils.parseFromSize(procIo.get("write_bytes"));

		if (lastRchar > 0 || lastWchar > 0 || lastReadBytes > 0 || lastWriteBytes > 0) {
			deltaRchar = rchar - lastRchar;
			deltaWchar = wchar - lastWchar;
			deltaReadBytes = readBytes - lastReadBytes;
			deltaWriteBytes = writeBytes - lastWriteBytes;
		}
		lastRchar = rchar;
		lastWchar = wchar;
		lastReadBytes = readBytes;
		lastWriteBytes = writeBytes;
	}

	private void updateCpu() throws Exception {
		long uptimeMills = jmxClient.getRuntimeMXBean().getUptime();
		long cpuTimeNanos = jmxClient.getOperatingSystemMXBean().getProcessCpuTime();

		if (lastUpTimeMills > 0 && lastCPUTimeNanos > 0) {
			deltaUptimeMills = uptimeMills - lastUpTimeMills;
			deltaCpuTimeNanos = (cpuTimeNanos - lastCPUTimeNanos);
			cpuLoad = Utils.calcLoad(deltaUptimeMills, deltaCpuTimeNanos / (Utils.NANOS_TO_MILLS * 1D), processors);
			singleCoreCpuLoad = Utils.calcLoad(deltaUptimeMills, deltaCpuTimeNanos / (Utils.NANOS_TO_MILLS * 1D), 1);
		}
		lastUpTimeMills = uptimeMills;
		lastCPUTimeNanos = cpuTimeNanos;

		Map<String, String> procStatus = ProcFileData.getProcStatus(pid);
		rss = Utils.parseFromSize(procStatus.get("VmRSS"));
		swap = Utils.parseFromSize(procStatus.get("VmSwap"));
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

		// eden
		edenUsedBytes = memoryPoolManager.getEdenMemoryPool().getUsage().getUsed();
		edenMaxBytes = getMemoryPoolMaxOrCommited(memoryPoolManager.getEdenMemoryPool());

		// old gen
		oldUsedBytes = memoryPoolManager.getOldMemoryPool().getUsage().getUsed();
		oldMaxBytes = getMemoryPoolMaxOrCommited(memoryPoolManager.getOldMemoryPool());

		// survivor
		MemoryPoolMXBean survivorMemoryPool = memoryPoolManager.getSurvivorMemoryPool();
		if (survivorMemoryPool != null) {
			surUsedBytes = survivorMemoryPool.getUsage().getUsed();
			surMaxBytes = getMemoryPoolMaxOrCommited(survivorMemoryPool);
		}

		// perm gen
		permUsedBytes = memoryPoolManager.getPermMemoryPool().getUsage().getUsed();
		permMaxBytes = getMemoryPoolMaxOrCommited(memoryPoolManager.getPermMemoryPool());

		// compressed class space
		if (jvmMajorVersion >= 8) {
			MemoryPoolMXBean compressedClassSpaceMemoryPool = memoryPoolManager.getCompressedClassSpaceMemoryPool();
			if (compressedClassSpaceMemoryPool != null) {
				ccsUsedBytes = compressedClassSpaceMemoryPool.getUsage().getUsed();
				ccsMaxBytes = getMemoryPoolMaxOrCommited(compressedClassSpaceMemoryPool);
			}
		}

		// code cache
		codeCacheUsedBytes = memoryPoolManager.getCodeCacheMemoryPool().getUsage().getUsed();
		codeCacheMaxBytes = getMemoryPoolMaxOrCommited(memoryPoolManager.getCodeCacheMemoryPool());

		// direct
		directUsedBytes = jmxClient.getBufferPoolManager().getDirectBufferPool().getMemoryUsed();
		directMaxBytes = jmxClient.getBufferPoolManager().getDirectBufferPool().getTotalCapacity();

		// map
		mapUsedBytes = jmxClient.getBufferPoolManager().getMappedBufferPool().getMemoryUsed();
		mapMaxBytes = jmxClient.getBufferPoolManager().getMappedBufferPool().getTotalCapacity();
	}

	private void updateGC() throws IOException {
		long youngGcCount = 0;
		long youngGcTimeMills = 0;
		long fullGcCount = 0;
		long fullGcTimeMills = 0;

		if (perfDataSupport) {
			youngGcCount = (Long) perfCounters.get("sun.gc.collector.0.invocations").getValue();
			youngGcTimeMills = perfData.tickToMills(perfCounters.get("sun.gc.collector.0.time"));
			fullGcCount = (Long) perfCounters.get("sun.gc.collector.1.invocations").getValue();
			fullGcTimeMills = perfData.tickToMills(perfCounters.get("sun.gc.collector.1.time"));
		} else {
			youngGcCount = jmxClient.getYoungCollector().getCollectionCount();
			youngGcTimeMills = jmxClient.getYoungCollector().getCollectionTime();
			fullGcCount = jmxClient.getFullCollector().getCollectionCount();
			fullGcTimeMills = jmxClient.getFullCollector().getCollectionTime();
		}

		if (lastYgcCount > 0) {
			deltaYgcTimeMills = youngGcTimeMills - lastYgcTimeMills;
			deltaYgcCount = youngGcCount - lastYgcCount;
		}

		if (lastFullgcCount > 0) {
			deltaFullgcTimeMills = fullGcTimeMills - lastFullgcTimeMills;
			deltaFullgcCount = fullGcCount - lastFullgcCount;
		}

		lastYgcTimeMills = youngGcTimeMills;
		lastYgcCount = youngGcCount;
		lastFullgcTimeMills = fullGcTimeMills;
		lastFullgcCount = fullGcCount;
	}

	private void updateSafepoint() {
		if (!perfDataSupport) {
			return;
		}

		long safepointCount = (Long) perfCounters.get("sun.rt.safepoints").getValue();
		long safepointTimeMills = perfData.tickToMills(perfCounters.get("sun.rt.safepointTime"));
		long safepointSyncTimeMills = perfData.tickToMills(perfCounters.get("sun.rt.safepointTime"));

		if (lastSafepointCount > 0) {
			deltaSafepointCount = safepointCount - lastSafepointCount;
			deltaSafepointTimeMills = safepointTimeMills - lastSafepointTimeMills;
			deltaSafepointSyncTimeMills = safepointSyncTimeMills - lastSafepointSyncTimeMills;
		}

		lastSafepointCount = safepointCount;
		lastSafepointTimeMills = safepointTimeMills;
		lastSafepointSyncTimeMills = safepointSyncTimeMills;
	}

	public ThreadMXBean getThreadMXBean() throws IOException {
		return jmxClient.getThreadMXBean();
	}

	private void handleFetchDataError(Throwable e) {
		Logger.getLogger("vjtop").log(Level.INFO, "error during update", e);
		updateErrorCount++;
		if (updateErrorCount > 10) {
			state = VMInfoState.DETACHED;
		} else {
			state = VMInfoState.ATTACHED_UPDATE_ERROR;
		}
	}

	private long getMemoryPoolMaxOrCommited(MemoryPoolMXBean memoryPool) {
		MemoryUsage usage = memoryPool.getUsage();
		long max = usage.getMax();
		max = max < 0 ? usage.getCommitted() : max;
		return max;
	}

	private int getJavaMajorVersion() {
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
		INIT, ERROR_DURING_ATTACH, ATTACHED, ATTACHED_UPDATE_ERROR, DETACHED, CONNECTION_REFUSED
	}
}