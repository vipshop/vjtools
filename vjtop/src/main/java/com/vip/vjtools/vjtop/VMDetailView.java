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

	// 按线程CPU or 分配内存模式
	volatile public DetailMode mode;
	volatile public int threadLimit = 10;
	volatile public boolean collectingData = true;

	public VMInfo vmInfo;
	public WarningRule warning;

	// 纪录vjtop进程本身的消耗
	private OperatingSystemMXBean operatingSystemMXBean;
	private long lastCpu = 0;

	private int width;
	private boolean shouldExit;

	private boolean firstTime = true;
	public boolean displayCommandHints = false;

	private Map<Long, Long> lastThreadCpuTotalTimes = new HashMap<Long, Long>();
	private Map<Long, Long> lastThreadSysCpuTotalTimes = new HashMap<Long, Long>();
	private Map<Long, Long> lastThreadMemoryTotalBytes = new HashMap<Long, Long>();

	public VMDetailView(VMInfo vmInfo, DetailMode mode, Integer width) throws Exception {
		this.vmInfo = vmInfo;
		this.warning = vmInfo.warning;
		this.mode = mode;
		setWidth(width);
		operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
	}

	public void printView() throws Exception {
		long iterationStartTime = System.currentTimeMillis();

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

		// 打印vjtop自身消耗
		printIterationCost(iterationStartTime);

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
				vmInfo.osUser, Utils.toTimeUnit(vmInfo.upTimeMills.getCurrent()));

		double cpuLoad = vmInfo.cpuLoad * 100;
		String[] cpuLoadAnsi = Utils.colorAnsi(cpuLoad, warning.cpu);

		System.out.printf(" PROCESS: %5.2f%% cpu(%s%5.2f%%%s of %d core)", vmInfo.singleCoreCpuLoad * 100,
				cpuLoadAnsi[0], cpuLoad, cpuLoadAnsi[1], vmInfo.processors);

		if (vmInfo.isLinux) {
			System.out.printf(", %s thread%n", Utils.toColor(vmInfo.processThreads, warning.thread));

			System.out.printf(" MEMORY: %s rss, %s swap |", Utils.toMB(vmInfo.rss),
					Utils.toMBWithColor(vmInfo.swap, warning.swap));

			if (vmInfo.ioDataSupport) {
				System.out.printf(" DISK: %sB read, %sB write%n",
						Utils.toSizeUnitWithColor(vmInfo.readBytes.getRate(), warning.io),
						Utils.toSizeUnitWithColor(vmInfo.writeBytes.getRate(), warning.io));
			}
		}
		System.out.println();

		System.out.printf(" THREAD: %s active, %d daemon, %s peak, %s new | CLASS: %d loaded, %d unloaded, %s new%n",
				Utils.toColor(vmInfo.threadActive, warning.thread), vmInfo.threadDaemon, vmInfo.threadPeak,
				Utils.toColor(vmInfo.threadNew.getDelta(), warning.newThread), vmInfo.classLoaded.getCurrent(),
				vmInfo.classUnLoaded, Utils.toColor(vmInfo.classLoaded.getDelta(), warning.newClass));

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

		long ygcCount = vmInfo.ygcCount.getDelta();
		long ygcTime = vmInfo.ygcTimeMills.getDelta();
		long avgYgcTime = ygcCount == 0 ? 0 : ygcTime / ygcCount;
		long fgcCount = vmInfo.fullgcCount.getDelta();
		System.out.printf(" GC: %s/%sms/%sms ygc, %s/%dms fgc", Utils.toColor(ygcCount, warning.ygcCount),
				Utils.toColor(ygcTime, warning.ygcTime), Utils.toColor(avgYgcTime, warning.ygcAvgTime),
				Utils.toColor(fgcCount, warning.fullgcCount), vmInfo.fullgcTimeMills.getDelta());

		if (vmInfo.perfDataSupport) {
			System.out.printf(" | SAFE-POINT: %s count, %sms time, %dms syncTime",
					Utils.toColor(vmInfo.safepointCount.getDelta(), warning.safepointCount),
					Utils.toColor(vmInfo.safepointTimeMills.getDelta(), warning.ygcTime),
					vmInfo.safepointSyncTimeMills.getDelta());
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
				threadCpuDeltaTimes.put(tid, deltaThreadCpuTime);
				deltaAllThreadCpu += deltaThreadCpuTime;
			}
		}

		// 计算本次SYSCPU Time
		for (int i = 0; i < tids.length; i++) {
			Long tid = tids[i];
			// 要处理cpuTime 获取时间有先后，sys本身接近0时，造成sysTime为负数的场景,
			long threadSysCpuTotalTime = Math.max(0, threadCpuTotalTimeArray[i] - threadUserCpuTotalTimeArray[i]);
			threadSysCpuTotalTimes.put(tid, threadSysCpuTotalTime);

			Long lastTime = lastThreadSysCpuTotalTimes.get(tid);
			if (lastTime != null) {
				long deltaThreadSysCpuTime = Math.max(0, threadSysCpuTotalTime - lastTime);
				threadSysCpuDeltaTimes.put(tid, deltaThreadSysCpuTime);
				deltaAllThreadSysCpu += deltaThreadSysCpuTime;
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
		} else if (mode == DetailMode.syscpu) {
			topTidArray = Utils.sortAndFilterThreadIdsByValue(threadSysCpuDeltaTimes, threadLimit);
		} else if (mode == DetailMode.totalcpu) {
			topTidArray = Utils.sortAndFilterThreadIdsByValue(threadCpuTotalTimes, threadLimit);
		} else if (mode == DetailMode.totalsyscpu) {
			topTidArray = Utils.sortAndFilterThreadIdsByValue(threadSysCpuTotalTimes, threadLimit);
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


				double cpu = getThreadCPUUtilization(threadCpuDeltaTimes.get(tid), vmInfo.upTimeMills.getDelta(),
						Utils.NANOS_TO_MILLS);
				String[] cpuAnsi = Utils.colorAnsi(cpu, warning.cpu);

				double syscpu = getThreadCPUUtilization(threadSysCpuDeltaTimes.get(tid), vmInfo.upTimeMills.getDelta(),
						Utils.NANOS_TO_MILLS);
				String[] syscpuAnsi = Utils.colorAnsi(syscpu, warning.syscpu);

				double totalcpu = getThreadCPUUtilization(threadCpuTotalTimes.get(tid),
						vmInfo.cpuTimeNanos.getCurrent(), 1);

				double totalsys = getThreadCPUUtilization(threadSysCpuTotalTimes.get(tid),
						vmInfo.cpuTimeNanos.getCurrent(), 1);

				System.out.printf(dataFormat, tid, threadName, Utils.leftStr(info.getThreadState().toString(), 10),
						cpuAnsi[0], cpu, cpuAnsi[1], syscpuAnsi[0], syscpu, syscpuAnsi[1], totalcpu, totalsys);
			}
		}

		// 打印线程汇总
		double deltaAllThreadCpuLoad = Utils.calcLoad((deltaAllThreadCpu * 100) / (Utils.NANOS_TO_MILLS * 1D),
				vmInfo.upTimeMills.getDelta(), 1);
		double deltaAllThreadSysCpuLoad = Utils.calcLoad((deltaAllThreadSysCpu * 100) / (Utils.NANOS_TO_MILLS * 1D),
				vmInfo.upTimeMills.getDelta(), 1);

		System.out.printf("%n Total cpu: %5.2f%% (user=%5.2f%%, sys=%5.2f%%)", deltaAllThreadCpuLoad,
				deltaAllThreadCpuLoad - deltaAllThreadSysCpuLoad, deltaAllThreadSysCpuLoad);

		if (threadCpuTotalTimes.size() > threadLimit) {
			System.out.printf(", top %d threads are shown, order by %s%n", threadLimit, mode.toString().toUpperCase());
		} else {
			System.out.printf(", all %d threads are shown, order by %s%n", threadCpuTotalTimes.size(),
					mode.toString().toUpperCase());
		}

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
				threadMemoryDeltaBytesMap.put(tid, threadMemoryDeltaBytes);
				totalDeltaBytes += threadMemoryDeltaBytes;
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
		} else {
			topTidArray = Utils.sortAndFilterThreadIdsByValue(threadMemoryTotalBytesMap, threadLimit);
		}

		ThreadInfo[] threadInfos = vmInfo.getThreadMXBean().getThreadInfo(topTidArray);

		// 打印线程Detail
		for (ThreadInfo info : threadInfos) {
			Long tid = info.getThreadId();
			String threadName = Utils.shortName(info.getThreadName(), getThreadNameWidth(), 12);

			System.out.printf(dataFormat, tid, threadName, Utils.leftStr(info.getThreadState().toString(), 10),
					Utils.toFixLengthSizeUnit(
							(threadMemoryDeltaBytesMap.get(tid) * 1000) / vmInfo.upTimeMills.getDelta()),
					getThreadMemoryUtilization(threadMemoryDeltaBytesMap.get(tid), totalDeltaBytes),
					Utils.toFixLengthSizeUnit(threadMemoryTotalBytesMap.get(tid)),
					getThreadMemoryUtilization(threadMemoryTotalBytesMap.get(tid), totalBytes));
		}

		// 打印线程汇总信息，这里因为最后单位是精确到秒，所以bytes除以毫秒以后要乘以1000才是按秒统计
		System.out.printf("%n Total memory allocate rate : %5s/s",
				Utils.toFixLengthSizeUnit((totalDeltaBytes * 1000) / vmInfo.upTimeMills.getDelta()));

		if (threadMemoryTotalBytesMap.size() > threadLimit) {
			System.out.printf(", top %d threads are shown, order by %s%n", threadLimit, mode.toString().toUpperCase());
		} else {
			System.out.printf(", all %d threads are shown, order by %s%n", threadMemoryTotalBytesMap.size(),
					mode.toString().toUpperCase());
		}

		lastThreadMemoryTotalBytes = threadMemoryTotalBytesMap;
	}

	public void printIterationCost(long iterationStartTime) {
		long deltaTime = System.currentTimeMillis() - iterationStartTime;

		long currentCpu = operatingSystemMXBean.getProcessCpuTime();
		long deltaCpuTime = (currentCpu - lastCpu) / Utils.NANOS_TO_MILLS;
		lastCpu = currentCpu;
		System.out.printf(" Cost time: %3dms, CPU time: %3dms%n", deltaTime, deltaCpuTime);
	}

	private void printWelcome() {
		if (firstTime) {
			if (!vmInfo.isLinux) {
				System.out.printf("%n OS isn't linux, Process's MEMORY, THREAD, DISK data will be skipped.%n");
			}

			if (!vmInfo.ioDataSupport) {
				System.out.printf("%n /proc/%s/io is not readable, Process's IO, DISK data will be skipped.%n",
						vmInfo.pid);
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
			System.out.println(" " + info.getThreadId() + "\t:" + info.getThreadName());
		}
		System.out.flush();
	}

	public void cleanupThreadsHistory() {
		this.lastThreadCpuTotalTimes.clear();
		this.lastThreadSysCpuTotalTimes.clear();
		this.lastThreadMemoryTotalBytes.clear();
	}

	private static double getThreadCPUUtilization(Long deltaThreadCpuTime, long totalTime, double factor) {
		if (deltaThreadCpuTime == null) {
			return 0;
		}
		if (totalTime == 0) {
			return 0;
		}
		return deltaThreadCpuTime * 100d / factor / totalTime;// 这里因为最后单位是百分比%，所以cpu time除以total cpu
		// time以后要乘以100，才可以再加上单位%
	}


	private static double getThreadMemoryUtilization(Long threadBytes, long totalBytes) {
		if (threadBytes == null) {
			return 0;
		}
		if (totalBytes == 0) {
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
