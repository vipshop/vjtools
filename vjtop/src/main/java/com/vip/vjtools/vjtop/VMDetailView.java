package com.vip.vjtools.vjtop;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.sun.management.OperatingSystemMXBean;
import com.vip.vjtools.vjtop.VMInfo.VMInfoState;

@SuppressWarnings("restriction")
public class VMDetailView {

	private static final int DEFAULT_WIDTH = 100;
	private static final int MIN_WIDTH = 80;

	volatile public DetailMode mode;
	volatile public int threadLimit = 10;
	volatile private int interval;
	private int width;
	volatile public String threadNameFilter = null;

	volatile private long minDeltaCpuTime;
	volatile private long minDeltaMemory;

	public VMInfo vmInfo;
	private WarningRule warning;

	// 纪录vjtop进程本身的消耗
	private boolean isDebug = false;
	private long lastCpu = 0;

	private boolean shouldExit = false;
	private boolean firstTime = true;
	public boolean displayCommandHints = false;
	volatile public boolean collectingData = true;

	private Map<Long, Long> lastThreadCpuTotalTimes = new HashMap<Long, Long>();
	private Map<Long, Long> lastThreadSysCpuTotalTimes = new HashMap<Long, Long>();
	private Map<Long, Long> lastThreadMemoryTotalBytes = new HashMap<Long, Long>();

	public VMDetailView(VMInfo vmInfo, DetailMode mode, Integer width, Integer interval) throws Exception {
		this.vmInfo = vmInfo;
		this.warning = vmInfo.warning;
		this.mode = mode;
		setWidth(width);
		updateInterval(interval);
	}

	public void printView() throws Exception {
		long iterationStartTime = 0;
		long iterationStartCpu = 0;
		if (isDebug) {
			iterationStartTime = System.currentTimeMillis();
			iterationStartCpu = ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean())
					.getProcessCpuTime();
		}

		vmInfo.update();

		if (!checkState()) {
			return;
		}

		// 打印进程级别内容
		printJvmInfo();

		// 打印线程级别内容
		if (mode.isCpuMode) {
			printTopCpuThreads(mode);
		} else {
			printTopMemoryThreads(mode);
		}

		if (isDebug) {
			// 打印vjtop自身消耗
			printIterationCost(iterationStartTime, iterationStartCpu);
		}
		if (displayCommandHints) {
			System.out.print(" Input command (h for help):");
		}
	}

	private boolean checkState() {
		if (vmInfo.state == VMInfoState.ATTACHED_UPDATE_ERROR) {
			System.out.println("ERROR: Could not fetch data - Process terminated?");
			return false;
		}

		if (vmInfo.state != VMInfo.VMInfoState.ATTACHED) {
			System.out.println("ERROR: Could not attach to process. ");
			exit();
			return false;
		}

		return true;
	}

	private void printJvmInfo() {
		System.out.printf(" PID: %s - %8tT JVM: %s USER: %s UPTIME: %s%n", vmInfo.pid, new Date(), vmInfo.jvmVersion,
				vmInfo.osUser, Utils.toTimeUnit(vmInfo.upTimeMills.current));


		String[] cpuLoadAnsi = Utils.colorAnsi(vmInfo.cpuLoad, warning.cpu);

		System.out.printf(" PROCESS: %5.2f%% cpu(%s%5.2f%%%s of %d core)", vmInfo.singleCoreCpuLoad, cpuLoadAnsi[0],
				vmInfo.cpuLoad, cpuLoadAnsi[1], vmInfo.processors);

		if (vmInfo.isLinux) {
			System.out.printf(", %s thread%n", Utils.toColor(vmInfo.osThreads, warning.thread));

			System.out.printf(" MEMORY: %s rss, %s peak, %s swap |", Utils.toMB(vmInfo.rss), Utils.toMB(vmInfo.peakRss),
					Utils.toMBWithColor(vmInfo.swap, warning.swap));

			if (vmInfo.ioDataSupport) {
				System.out.printf(" DISK: %sB read, %sB write",
						Utils.toSizeUnitWithColor(vmInfo.readBytes.ratePerSecond, warning.io),
						Utils.toSizeUnitWithColor(vmInfo.writeBytes.ratePerSecond, warning.io));
			}
		}
		System.out.println();

		System.out.printf(" THREAD: %s active, %d daemon, %s peak, %s new",
				Utils.toColor(vmInfo.threadActive, warning.thread), vmInfo.threadDaemon, vmInfo.threadPeak,
				Utils.toColor(vmInfo.threadNew.delta, warning.newThread));

		System.out.printf(" | CLASS: %s loaded, %d unloaded, %s new%n",
				Utils.toColor(vmInfo.classLoaded.current, warning.loadClass), vmInfo.classUnLoaded,
				Utils.toColor(vmInfo.classLoaded.delta, warning.newClass));

		System.out.printf(" HEAP: %s eden, %s sur, %s old%n", Utils.formatUsage(vmInfo.eden),
				Utils.formatUsage(vmInfo.sur), Utils.formatUsageWithColor(vmInfo.old, warning.old));

		System.out.printf(" NON-HEAP: %s %s, %s codeCache", Utils.formatUsageWithColor(vmInfo.perm, warning.perm),
				vmInfo.permGenName, Utils.formatUsageWithColor(vmInfo.codeCache, warning.codeCache));
		if (vmInfo.jvmMajorVersion >= 8) {
			System.out.printf(", %s ccs", Utils.formatUsage(vmInfo.ccs));
		}
		System.out.println("");

		System.out.printf(" OFF-HEAP: %s/%s direct(max=%s), %s/%s map(count=%d), %s threadStack%n",
				Utils.toMB(vmInfo.direct.used), Utils.toMB(vmInfo.direct.committed), Utils.toMB(vmInfo.direct.max),
				Utils.toMB(vmInfo.map.used), Utils.toMB(vmInfo.map.committed), vmInfo.map.max,
				Utils.toMB(vmInfo.threadStackSize * vmInfo.threadActive));

		long ygcCount = vmInfo.ygcCount.delta;
		long ygcTime = vmInfo.ygcTimeMills.delta;
		long avgYgcTime = ygcCount == 0 ? 0 : ygcTime / ygcCount;
		long fgcCount = vmInfo.fullgcCount.delta;
		System.out.printf(" GC: %s/%sms/%sms ygc, %s/%dms fgc", Utils.toColor(ygcCount, warning.ygcCount),
				Utils.toColor(ygcTime, warning.ygcTime), Utils.toColor(avgYgcTime, warning.ygcAvgTime),
				Utils.toColor(fgcCount, warning.fullgcCount), vmInfo.fullgcTimeMills.delta);

		if (vmInfo.perfDataSupport) {
			System.out.printf(" | SAFE-POINT: %s count, %sms time, %dms syncTime",
					Utils.toColor(vmInfo.safepointCount.delta, warning.safepointCount),
					Utils.toColor(vmInfo.safepointTimeMills.delta, warning.safepointTime),
					vmInfo.safepointSyncTimeMills.delta);
		}
		System.out.println("");

	}

	private void printTopCpuThreads(DetailMode mode) throws IOException {
		if (!vmInfo.threadCpuTimeSupported) {
			System.out.printf("%n -Thread CPU telemetries are not available on the monitored jvm/platform-%n");
			return;
		}

		Map<Long, Long> threadCpuTotalTimes = new HashMap<Long, Long>();
		Map<Long, Long> threadCpuDeltaTimes = new HashMap<Long, Long>();
		Map<Long, Long> threadSysCpuTotalTimes = new HashMap<Long, Long>();
		Map<Long, Long> threadSysCpuDeltaTimes = new HashMap<Long, Long>();

		long threadsHaveValue = 0;

		long tids[] = vmInfo.getThreadMXBean().getAllThreadIds();

		// 批量获取CPU times，性能大幅提高。
		// 两次获取之间有间隔，在低流量下可能造成负数
		long[] threadCpuTotalTimeArray = vmInfo.getThreadMXBean().getThreadCpuTime(tids);
		long[] threadUserCpuTotalTimeArray = vmInfo.getThreadMXBean().getThreadUserTime(tids);

		long deltaAllThreadCpu = 0;
		long deltaAllThreadSysCpu = 0;

		// 计算本次CPU Time
		// 此算法第一次不会显示任何数据，保证每次显示都只显示区间内数据
		for (int i = 0; i < tids.length; i++) {
			Long tid = tids[i];
			long threadCpuTotalTime = threadCpuTotalTimeArray[i];
			threadCpuTotalTimes.put(tid, threadCpuTotalTime);

			Long lastTime = lastThreadCpuTotalTimes.get(tid);
			if (lastTime != null) {
				long deltaThreadCpuTime = threadCpuTotalTime - lastTime;
				if (deltaThreadCpuTime >= minDeltaCpuTime) {
					threadCpuDeltaTimes.put(tid, deltaThreadCpuTime);
					deltaAllThreadCpu += deltaThreadCpuTime;
				}
			}
		}

		// 计算本次SYSCPU Time
		for (int i = 0; i < tids.length; i++) {
			Long tid = tids[i];
			// 因为totalTime 与 userTime 的获取时间有先后，实际sys接近0时，后取的userTime可能比前一时刻的totalTime高，计算出来的sysTime可为负数
			long threadSysCpuTotalTime = Math.max(0, threadCpuTotalTimeArray[i] - threadUserCpuTotalTimeArray[i]);
			threadSysCpuTotalTimes.put(tid, threadSysCpuTotalTime);

			Long lastTime = lastThreadSysCpuTotalTimes.get(tid);
			if (lastTime != null) {
				long deltaThreadSysCpuTime = Math.max(0, threadSysCpuTotalTime - lastTime);
				if (deltaThreadSysCpuTime >= minDeltaCpuTime) {
					threadSysCpuDeltaTimes.put(tid, deltaThreadSysCpuTime);
					deltaAllThreadSysCpu += deltaThreadSysCpuTime;
				}
			}
		}

		// 第一次无数据时跳过
		if (lastThreadCpuTotalTimes.isEmpty()) {
			lastThreadCpuTotalTimes = threadCpuTotalTimes;
			lastThreadSysCpuTotalTimes = threadSysCpuTotalTimes;
			printWelcome();
			return;
		}

		collectingData = false;

		// 打印线程view的页头
		String titleFormat = " %6s %-" + getThreadNameWidth() + "s %10s %6s %6s %6s %6s%n";
		String dataFormat = " %6d %-" + getThreadNameWidth() + "s %10s %s%5.2f%%%s %s%5.2f%%%s %5.2f%% %5.2f%%%n";
		System.out.printf("%n%n" + titleFormat, "TID", "NAME  ", "STATE", "CPU", "SYSCPU", " TOTAL", "TOLSYS");

		// 按不同类型排序,过滤
		long[] topTidArray;
		if (mode == DetailMode.cpu) {
			topTidArray = Utils.sortAndFilterThreadIdsByValue(threadCpuDeltaTimes, threadLimit);
			threadsHaveValue = threadCpuDeltaTimes.size();
		} else if (mode == DetailMode.syscpu) {
			topTidArray = Utils.sortAndFilterThreadIdsByValue(threadSysCpuDeltaTimes, threadLimit);
			threadsHaveValue = threadSysCpuDeltaTimes.size();
		} else if (mode == DetailMode.totalcpu) {
			topTidArray = Utils.sortAndFilterThreadIdsByValue(threadCpuTotalTimes, threadLimit);
			threadsHaveValue = threadCpuTotalTimes.size();
		} else if (mode == DetailMode.totalsyscpu) {
			topTidArray = Utils.sortAndFilterThreadIdsByValue(threadSysCpuTotalTimes, threadLimit);
			threadsHaveValue = threadSysCpuTotalTimes.size();
		} else {
			throw new RuntimeException("unkown mode");
		}

		// 获得threadInfo
		ThreadInfo[] threadInfos = vmInfo.getThreadMXBean().getThreadInfo(topTidArray);

		// 打印线程Detail
		for (ThreadInfo info : threadInfos) {
			Long tid = info.getThreadId();
			if (info != null) {
				String threadName = Utils.shortName(info.getThreadName(), getThreadNameWidth(), 20);
				// 过滤threadName
				if (threadNameFilter != null && !threadName.contains(threadNameFilter)) {
					continue;
				}
				// 刷新间隔里，所使用的单核CPU比例
				double cpu = Utils.calcLoad(threadCpuDeltaTimes.get(tid), vmInfo.upTimeMills.delta,
						Utils.NANOS_TO_MILLS);
				String[] cpuAnsi = Utils.colorAnsi(cpu, warning.cpu);

				double syscpu = Utils.calcLoad(threadSysCpuDeltaTimes.get(tid), vmInfo.upTimeMills.delta,
						Utils.NANOS_TO_MILLS);

				String[] syscpuAnsi = Utils.colorAnsi(syscpu, warning.syscpu);

				// 在进程所有消耗的CPU里，本线程的比例
				double totalcpuPercent = Utils.calcLoad(threadCpuTotalTimes.get(tid), vmInfo.cpuTimeNanos.current, 1);

				double totalsysPercent = Utils.calcLoad(threadSysCpuTotalTimes.get(tid), vmInfo.cpuTimeNanos.current,
						1);

				System.out.printf(dataFormat, tid, threadName, Utils.leftStr(info.getThreadState().toString(), 10),
						cpuAnsi[0], cpu, cpuAnsi[1], syscpuAnsi[0], syscpu, syscpuAnsi[1], totalcpuPercent,
						totalsysPercent);
			}
		}

		// 打印线程汇总
		double deltaAllThreadCpuLoad = Utils.calcLoad(deltaAllThreadCpu / Utils.NANOS_TO_MILLS,
				vmInfo.upTimeMills.delta);
		double deltaAllThreadSysCpuLoad = Utils.calcLoad(deltaAllThreadSysCpu / Utils.NANOS_TO_MILLS,
				vmInfo.upTimeMills.delta);

		System.out.printf("%n Total cpu: %5.2f%%(user=%5.2f%%, sys=%5.2f%%), %d threads used at least 0.1%% cpu%n",
				deltaAllThreadCpuLoad, deltaAllThreadCpuLoad - deltaAllThreadSysCpuLoad, deltaAllThreadSysCpuLoad,
				threadsHaveValue);

		System.out.printf(" Setting  : top %d threads order by %s%s, flush every %ds%n", threadLimit,
				mode.toString().toUpperCase(), threadNameFilter == null ? "" : " filter by " + threadNameFilter,
				interval);

		lastThreadCpuTotalTimes = threadCpuTotalTimes;
		lastThreadSysCpuTotalTimes = threadSysCpuTotalTimes;
	}


	private void printTopMemoryThreads(DetailMode mode) throws IOException {

		if (!vmInfo.threadMemoryAllocatedSupported) {
			System.out.printf(
					"%n -Thread Memory Allocated telemetries are not available on the monitored jvm/platform-%n");
			return;
		}

		long tids[] = vmInfo.getThreadMXBean().getAllThreadIds();

		Map<Long, Long> threadMemoryTotalBytesMap = new HashMap<Long, Long>();
		Map<Long, Long> threadMemoryDeltaBytesMap = new HashMap<Long, Long>();

		long totalDeltaBytes = 0;
		long totalBytes = 0;

		long threadsHaveValue = 0;

		// 批量获取内存分配
		long[] threadMemoryTotalBytesArray = vmInfo.getThreadMXBean().getThreadAllocatedBytes(tids);

		// 此算法第一次不会显示任何数据，保证每次显示都只显示区间内数据
		for (int i = 0; i < tids.length; i++) {
			Long tid = tids[i];
			long threadMemoryTotalBytes = threadMemoryTotalBytesArray[i];
			threadMemoryTotalBytesMap.put(tid, threadMemoryTotalBytes);
			totalBytes += threadMemoryTotalBytes;

			long threadMemoryDeltaBytes = 0;
			Long lastBytes = lastThreadMemoryTotalBytes.get(tid);

			if (lastBytes != null) {
				threadMemoryDeltaBytes = threadMemoryTotalBytes - lastBytes;
				if (threadMemoryDeltaBytes >= minDeltaMemory) {
					threadMemoryDeltaBytesMap.put(tid, threadMemoryDeltaBytes);
					totalDeltaBytes += threadMemoryDeltaBytes;
				}
			}
		}

		// 第一次无数据跳过
		if (lastThreadMemoryTotalBytes.size() == 0) {
			lastThreadMemoryTotalBytes = threadMemoryTotalBytesMap;
			printWelcome();
			return;
		}

		collectingData = false;

		// 打印线程View的页头
		String titleFormat = " %6s %-" + getThreadNameWidth() + "s %10s %14s %18s%n";
		String dataFormat = " %6d %-" + getThreadNameWidth() + "s %10s %5s/s(%5.2f%%) %10s(%5.2f%%)%n";
		System.out.printf("%n%n" + titleFormat, "TID", "NAME  ", "STATE", "MEMORY", "TOTAL-ALLOCATED");

		// 线程排序
		long[] topTidArray;
		if (mode == DetailMode.memory) {
			topTidArray = Utils.sortAndFilterThreadIdsByValue(threadMemoryDeltaBytesMap, threadLimit);
			threadsHaveValue = threadMemoryDeltaBytesMap.size();
		} else {
			topTidArray = Utils.sortAndFilterThreadIdsByValue(threadMemoryTotalBytesMap, threadLimit);
			threadsHaveValue = threadMemoryTotalBytesMap.size();
		}

		ThreadInfo[] threadInfos = vmInfo.getThreadMXBean().getThreadInfo(topTidArray);

		// 打印线程Detail
		for (ThreadInfo info : threadInfos) {
			Long tid = info.getThreadId();
			String threadName = Utils.shortName(info.getThreadName(), getThreadNameWidth(), 12);

			// 过滤threadName
			if (threadNameFilter != null && !threadName.contains(threadNameFilter)) {
				continue;
			}

			Long threadDelta = threadMemoryDeltaBytesMap.get(tid);
			long allocationRate = threadDelta == null ? 0 : (threadDelta * 1000) / vmInfo.upTimeMills.delta;
			System.out.printf(dataFormat, tid, threadName, Utils.leftStr(info.getThreadState().toString(), 10),
					Utils.toFixLengthSizeUnit(allocationRate),
					getThreadMemoryUtilization(threadMemoryDeltaBytesMap.get(tid), totalDeltaBytes),
					Utils.toFixLengthSizeUnit(threadMemoryTotalBytesMap.get(tid)),
					getThreadMemoryUtilization(threadMemoryTotalBytesMap.get(tid), totalBytes));
		}

		// 打印线程汇总信息，这里因为最后单位是精确到秒，所以bytes除以毫秒以后要乘以1000才是按秒统计
		System.out.printf("%n Total memory allocate: %5s/s, %d threads allocated at least 1k/s%n",
				Utils.toFixLengthSizeUnit((totalDeltaBytes * 1000) / vmInfo.upTimeMills.delta), threadsHaveValue);

		System.out.printf(" Setting  : top %d threads order by %s%s, flush every %ds%n", threadLimit,
				mode.toString().toUpperCase(), threadNameFilter == null ? "" : " filter by " + threadNameFilter,
				interval);


		lastThreadMemoryTotalBytes = threadMemoryTotalBytesMap;
	}

	public void printIterationCost(long iterationStartTime, long iterationStartCpu) {
		long currentCpu = ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getProcessCpuTime();
		long deltaIterationTime = System.currentTimeMillis() - iterationStartTime;

		long deltaIterationCpuTime = (currentCpu - iterationStartCpu) / Utils.NANOS_TO_MILLS;
		long deltaOtherCpuTime = (iterationStartCpu - lastCpu) / Utils.NANOS_TO_MILLS;
		long deltaTotalCpuTime = deltaIterationCpuTime + deltaOtherCpuTime;
		lastCpu = currentCpu;

		System.out.printf(" Cost %5.2f%% cpu in %dms, other is %dms, total is %dms%n",
				deltaIterationCpuTime * 100d / deltaIterationTime, deltaIterationTime, deltaOtherCpuTime,
				deltaTotalCpuTime);
	}

	private void printWelcome() {
		if (firstTime) {
			if (!vmInfo.isLinux) {
				System.out.printf("%n OS isn't linux, Process's MEMORY, THREAD, DISK data will be skipped.%n");
			}

			if (!vmInfo.ioDataSupport) {
				System.out.printf("%n /proc/%s/io is not readable, Process's DISK data will be skipped.%n", vmInfo.pid);
			}

			if (!vmInfo.perfDataSupport) {
				System.out.printf("%n Perfdata doesn't support, SAFE-POINT data will be skipped.%n");
			}

			System.out.printf("%n VMARGS: %s%n%n", vmInfo.vmArgs);

			firstTime = false;

		}
		System.out.printf("%n Collecting data, please wait ......%n%n");
		collectingData = true;
	}

	/**
	 * 打印单条线程的stack strace，不造成停顿
	 */
	public void printStack(long tid) throws IOException {
		ThreadInfo info = vmInfo.getThreadMXBean().getThreadInfo(tid, 20);
		if (info == null) {
			System.err.println(" TID not exist:" + tid);
			return;
		}
		StackTraceElement[] trace = info.getStackTrace();
		System.out.println(" " + info.getThreadId() + ":" + info.getThreadName());
		for (StackTraceElement traceElement : trace) {
			System.out.println("\tat " + traceElement);
		}
		System.out.flush();
	}

	/**
	 * 打印所有线程，只获取名称不获取stack，不造成停顿
	 */
	public void printAllThreads() throws IOException {
		long tids[] = vmInfo.getThreadMXBean().getAllThreadIds();
		ThreadInfo[] threadInfos = vmInfo.getThreadMXBean().getThreadInfo(tids);
		for (ThreadInfo info : threadInfos) {
			String threadName = info.getThreadName();
			if (threadNameFilter != null && !threadName.contains(threadNameFilter)) {
				continue;
			}
			System.out.println(" " + info.getThreadId() + "\t:" + threadName);
		}
		if (threadNameFilter != null) {
			System.out.println(" Thread name filter is:" + threadNameFilter);
		}
		System.out.flush();
	}

	public void cleanupThreadsHistory() {
		this.lastThreadCpuTotalTimes.clear();
		this.lastThreadSysCpuTotalTimes.clear();
		this.lastThreadMemoryTotalBytes.clear();
	}

	private static double getThreadMemoryUtilization(Long threadBytes, long totalBytes) {
		if (threadBytes == null || totalBytes == 0) {
			return 0;
		}

		return (threadBytes * 100d) / totalBytes;// 这里因为最后单位是百分比%，所以bytes除以totalBytes以后要乘以100，才可以再加上单位%
	}

	public boolean shouldExit() {
		return shouldExit;
	}

	/**
	 * Requests the disposal of this view - it should be called again.
	 */
	public void exit() {
		shouldExit = true;
	}

	private void setWidth(Integer width) {
		if (width == null) {
			this.width = DEFAULT_WIDTH;
		} else if (width < MIN_WIDTH) {
			this.width = MIN_WIDTH;
		} else {
			this.width = width;
		}
	}

	private int getThreadNameWidth() {
		return this.width - 48;
	}

	public void updateInterval(int interval) {
		minDeltaCpuTime = interval * Utils.NANOS_TO_MILLS;
		minDeltaMemory = interval * 1024;
		this.interval = interval;
		warning.updateInterval(interval);
	}


	public enum DetailMode {
		cpu(true), totalcpu(true), syscpu(true), totalsyscpu(true), memory(false), totalmemory(false);

		public boolean isCpuMode;

		private DetailMode(boolean isCpuMode) {
			this.isCpuMode = isCpuMode;
		}

		public static DetailMode parse(String mode) {
			switch (mode) {
				case "1":
					return cpu;
				case "2":
					return syscpu;
				case "3":
					return totalcpu;
				case "4":
					return totalsyscpu;
				case "5":
					return memory;
				case "6":
					return totalmemory;
				default:
					return null;
			}
		}
	}
}
