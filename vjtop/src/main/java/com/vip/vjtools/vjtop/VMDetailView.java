package com.vip.vjtools.vjtop;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.Date;

import com.sun.management.OperatingSystemMXBean;
import com.vip.vjtools.vjtop.TopThread.TopCpuResult;
import com.vip.vjtools.vjtop.TopThread.TopMemoryResult;
import com.vip.vjtools.vjtop.util.Formats;
import com.vip.vjtools.vjtop.util.Utils;

@SuppressWarnings("restriction")
public class VMDetailView {

	private static final int DEFAULT_WIDTH = 100;
	private static final int MIN_WIDTH = 80;

	public DetailMode mode;
	public int threadLimit = 10;
	public int interval;
	public String threadNameFilter = null;

	private int width;

	private VMInfo vmInfo;
	private TopThread topThread;
	private WarningRule warning;

	// 纪录vjtop进程本身的消耗
	private boolean isDebugCost = false;
	private long lastCpu = 0;

	private boolean shouldExit = false;
	private boolean firstTime = true;
	public boolean displayCommandHints = false;

	public VMDetailView(VMInfo vmInfo, DetailMode mode, Integer width, Integer interval) throws Exception {
		this.vmInfo = vmInfo;
		this.topThread = new TopThread(vmInfo);
		this.warning = vmInfo.warningRule;
		this.mode = mode;
		this.interval = interval;
		setWidth(width);
	}

	public void printView() throws Exception {
		Formats.clearTerminal();

		long iterationStartTime = 0;
		long iterationStartCpu = 0;

		if (isDebugCost) {
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

		// JMX更新失败，不打印后续一定需要JMX获取的数据
		if (!vmInfo.isJmxStateOk()) {
			System.out.println("vminfo.state:" + vmInfo.state);
			printJmxError();
			return;
		}

		// 打印线程级别内容
		try {
			if (mode.isCpuMode) {
				printTopCpuThreads(mode);
			} else {
				printTopMemoryThreads(mode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("\n" + Formats.RED_ANSI[0]
					+ "ERROR: Exception happen when fetch thread information via JMX" + Formats.RED_ANSI[1]);
		}

		// 打印vjtop自身消耗
		if (isDebugCost) {
			printIterationCost(iterationStartTime, iterationStartCpu);
		}

		if (displayCommandHints) {
			System.out.print(" Input command (h for help):");
		}
	}


	private boolean checkState() {
		if (vmInfo.state != VMInfo.VMInfoState.ATTACHED && vmInfo.state != VMInfo.VMInfoState.ATTACHED_UPDATE_ERROR) {
			System.out.println(
					"\n" + Formats.RED_ANSI[0] + "ERROR: Could not attach to process, exit now." + Formats.RED_ANSI[1]);
			exit();
			return false;
		}
		return true;
	}

	private void printJvmInfo() {
		System.out.printf(" PID: %s - %8tT JVM: %s USER: %s UPTIME: %s%n", vmInfo.pid, new Date(), vmInfo.jvmVersion,
				vmInfo.osUser, Formats.toTimeUnit(vmInfo.upTimeMills.current));

		String[] cpuLoadAnsi = Formats.colorAnsi(vmInfo.cpuLoad, warning.cpu);

		System.out.printf(" PROCESS: %5.2f%% cpu(%s%5.2f%%%s of %d core)", vmInfo.singleCoreCpuLoad, cpuLoadAnsi[0],
				vmInfo.cpuLoad, cpuLoadAnsi[1], vmInfo.processors);

		if (vmInfo.isLinux) {
			System.out.printf(", %s thread%n", Formats.toColor(vmInfo.osThreads, warning.thread));

			System.out.printf(" MEMORY: %s rss, %s peak, %s swap |", Formats.toMB(vmInfo.rss),
					Formats.toMB(vmInfo.peakRss), Formats.toMBWithColor(vmInfo.swap, warning.swap));

			if (vmInfo.ioDataSupport) {
				System.out.printf(" DISK: %sB read, %sB write",
						Formats.toSizeUnitWithColor(vmInfo.readBytes.ratePerSecond, warning.io),
						Formats.toSizeUnitWithColor(vmInfo.writeBytes.ratePerSecond, warning.io));
			}
		}
		System.out.println();

		System.out.printf(" THREAD: %s live, %d daemon, %s peak, %s new",
				Formats.toColor(vmInfo.threadActive, warning.thread), vmInfo.threadDaemon, vmInfo.threadPeak,
				Formats.toColor(vmInfo.threadNew.delta, warning.newThread));

		System.out.printf(" | CLASS: %s loaded, %d unloaded, %s new%n",
				Formats.toColor(vmInfo.classLoaded.current, warning.loadClass), vmInfo.classUnLoaded,
				Formats.toColor(vmInfo.classLoaded.delta, warning.newClass));

		System.out.printf(" HEAP: %s eden, %s sur, %s old%n", Formats.formatUsage(vmInfo.eden),
				Formats.formatUsage(vmInfo.sur), Formats.formatUsageWithColor(vmInfo.old, warning.old));

		System.out.printf(" NON-HEAP: %s %s, %s codeCache", Formats.formatUsageWithColor(vmInfo.perm, warning.perm),
				vmInfo.permGenName, Formats.formatUsageWithColor(vmInfo.codeCache, warning.codeCache));
		if (vmInfo.jvmMajorVersion >= 8) {
			System.out.printf(", %s ccs", Formats.formatUsage(vmInfo.ccs));
		}
		System.out.println("");

		System.out.printf(" OFF-HEAP: %s/%s direct(max=%s), %s/%s map(count=%d), %s threadStack%n",
				Formats.toMB(vmInfo.direct.used), Formats.toMB(vmInfo.direct.committed),
				Formats.toMB(vmInfo.direct.max), Formats.toMB(vmInfo.map.used), Formats.toMB(vmInfo.map.committed),
				vmInfo.map.max, Formats.toMB(vmInfo.threadStackSize * vmInfo.threadActive));

		long ygcCount = vmInfo.ygcCount.delta;
		long ygcTime = vmInfo.ygcTimeMills.delta;
		long avgYgcTime = ygcCount == 0 ? 0 : ygcTime / ygcCount;
		long fgcCount = vmInfo.fullgcCount.delta;
		System.out.printf(" GC: %s/%sms/%sms ygc, %s/%dms fgc", Formats.toColor(ygcCount, warning.ygcCount),
				Formats.toColor(ygcTime, warning.ygcTime), Formats.toColor(avgYgcTime, warning.ygcAvgTime),
				Formats.toColor(fgcCount, warning.fullgcCount), vmInfo.fullgcTimeMills.delta);

		if (vmInfo.perfDataSupport) {
			System.out.printf(" | SAFE-POINT: %s count, %sms time, %dms syncTime",
					Formats.toColor(vmInfo.safepointCount.delta, warning.safepointCount),
					Formats.toColor(vmInfo.safepointTimeMills.delta, warning.safepointTime),
					vmInfo.safepointSyncTimeMills.delta);
		}
		System.out.println("");
	}

	private void printTopCpuThreads(DetailMode mode) throws IOException {
		if (!vmInfo.threadCpuTimeSupported) {
			System.out.printf("%n -Thread CPU telemetries are not available on the monitored jvm/platform-%n");
			return;
		}

		TopCpuResult result = topThread.topCpuThreads(mode, threadLimit);

		// 第一次无数据时跳过
		if (!result.ready) {
			printWelcome();
			return;
		}

		// 打印线程view的页头
		String titleFormat = " %6s %-" + getThreadNameWidth() + "s %10s %6s %6s %6s %6s%n";
		String dataFormat = " %6d %-" + getThreadNameWidth() + "s %10s %s%5.2f%%%s %s%5.2f%%%s %5.2f%% %5.2f%%%n";
		System.out.printf("%n%n" + titleFormat, "TID", "NAME  ", "STATE", "CPU", "SYSCPU", " TOTAL", "TOLSYS");

		// 打印线程Detail
		for (ThreadInfo info : result.threadInfos) {
			if (info == null) {
				continue;
			}
			Long tid = info.getThreadId();
			String threadName = Formats.shortName(info.getThreadName(), getThreadNameWidth(), 20);
			// 过滤threadName
			if (threadNameFilter != null && !threadName.toLowerCase().contains(threadNameFilter)) {
				continue;
			}
			// 刷新间隔里，所使用的单核CPU比例
			double cpu = Utils.calcLoad(result.threadCpuDeltaTimes.get(tid), vmInfo.upTimeMills.delta,
					Utils.NANOS_TO_MILLS);
			String[] cpuAnsi = Formats.colorAnsi(cpu, warning.cpu);

			double syscpu = Utils.calcLoad(result.threadSysCpuDeltaTimes.get(tid), vmInfo.upTimeMills.delta,
					Utils.NANOS_TO_MILLS);
			String[] syscpuAnsi = Formats.colorAnsi(syscpu, warning.syscpu);

			// 在进程所有消耗的CPU里，本线程的比例
			double totalcpuPercent = Utils.calcLoad(result.threadCpuTotalTimes.get(tid), vmInfo.cpuTimeNanos.current,
					1);

			double totalsysPercent = Utils.calcLoad(result.threadSysCpuTotalTimes.get(tid), vmInfo.cpuTimeNanos.current,
					1);

			System.out.printf(dataFormat, tid, threadName, Formats.leftStr(info.getThreadState().toString(), 10),
					cpuAnsi[0], cpu, cpuAnsi[1], syscpuAnsi[0], syscpu, syscpuAnsi[1], totalcpuPercent,
					totalsysPercent);

		}

		// 打印线程汇总
		double deltaAllThreadCpuLoad = Utils.calcLoad(result.deltaAllThreadCpu / Utils.NANOS_TO_MILLS,
				vmInfo.upTimeMills.delta);
		double deltaAllThreadSysCpuLoad = Utils.calcLoad(result.deltaAllThreadSysCpu / Utils.NANOS_TO_MILLS,
				vmInfo.upTimeMills.delta);

		System.out.printf("%n Total  : %5.2f%% cpu(user=%5.2f%%, sys=%5.2f%%) by %d active threads(which cpu>0.05%%)%n",
				deltaAllThreadCpuLoad, deltaAllThreadCpuLoad - deltaAllThreadSysCpuLoad, deltaAllThreadSysCpuLoad,
				result.noteableThreads);

		System.out.printf(" Setting: top %d threads order by %s%s, flush every %ds%n", threadLimit,
				mode.toString().toUpperCase(), threadNameFilter == null ? "" : " filter by " + threadNameFilter,
				interval);
	}

	private void printTopMemoryThreads(DetailMode mode) throws IOException {
		if (!vmInfo.threadMemoryAllocatedSupported) {
			System.out.printf(
					"%n -Thread Memory Allocated telemetries are not available on the monitored jvm/platform-%n");
			return;
		}

		TopMemoryResult result = topThread.topMemoryThreads(mode, threadLimit);

		// 第一次无数据跳过
		if (!result.ready) {
			printWelcome();
			return;
		}

		// 打印线程View的页头
		String titleFormat = " %6s %-" + getThreadNameWidth() + "s %10s %14s %18s%n";
		String dataFormat = " %6d %-" + getThreadNameWidth() + "s %10s %5s/s(%5.2f%%) %10s(%5.2f%%)%n";
		System.out.printf("%n%n" + titleFormat, "TID", "NAME  ", "STATE", "MEMORY", "TOTAL-ALLOCATED");


		// 打印线程Detail
		for (ThreadInfo info : result.threadInfos) {
			if (info == null) {
				continue;
			}
			Long tid = info.getThreadId();
			String threadName = Formats.shortName(info.getThreadName(), getThreadNameWidth(), 12);

			// 过滤threadName
			if (threadNameFilter != null && !threadName.toLowerCase().contains(threadNameFilter)) {
				continue;
			}

			Long threadDelta = result.threadMemoryDeltaBytesMap.get(tid);
			long allocationRate = threadDelta == null ? 0 : (threadDelta * 1000) / vmInfo.upTimeMills.delta;
			System.out.printf(dataFormat, tid, threadName, Formats.leftStr(info.getThreadState().toString(), 10),
					Formats.toFixLengthSizeUnit(allocationRate),
					getMemoryUtilization(result.threadMemoryDeltaBytesMap.get(tid), result.deltaAllThreadBytes),
					Formats.toFixLengthSizeUnit(result.threadMemoryTotalBytesMap.get(tid)),
					getMemoryUtilization(result.threadMemoryTotalBytesMap.get(tid), result.totalAllThreadBytes));
		}

		// 打印线程汇总信息，这里因为最后单位是精确到秒，所以bytes除以毫秒以后要乘以1000才是按秒统计
		System.out.printf("%n Total  : %5s/s memory allocated by %d active threads(which >1k/s)%n",
				Formats.toFixLengthSizeUnit((result.deltaAllThreadBytes * 1000) / vmInfo.upTimeMills.delta),
				result.noteableThreads);

		System.out.printf(" Setting: top %d threads order by %s%s, flush every %ds%n", threadLimit,
				mode.toString().toUpperCase(), threadNameFilter == null ? "" : " filter by " + threadNameFilter,
				interval);
	}

	private static double getMemoryUtilization(Long threadBytes, long totalBytes) {
		if (threadBytes == null || totalBytes == 0) {
			return 0;
		}

		return (threadBytes * 100d) / totalBytes;// 这里因为最后单位是百分比%，所以bytes除以totalBytes以后要乘以100，才可以再加上单位%
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
	}

	private void printJmxError() {
		System.out.print("\n " + Formats.RED_ANSI[0] + "ERROR: Could not fetch data via JMX");
		if (!vmInfo.currentGcCause.equals("No GC")) {
			System.out.println(" - Process is doing GC, cause is " + vmInfo.currentGcCause + Formats.RED_ANSI[1]);
		} else {
			System.out.println(" - Process terminated?" + Formats.RED_ANSI[1]);
		}
	}

	/**
	 * 打印单条线程的stack strace，不造成停顿
	 */
	public void printStack(long tid) throws IOException {
		System.out.println("\n Stack trace of thread " + tid + ":");

		ThreadInfo info = vmInfo.getThreadMXBean().getThreadInfo(tid, 20);
		if (info == null) {
			System.err.println(" TID not exist:" + tid);
			return;
		}
		StackTraceElement[] trace = info.getStackTrace();
		System.out.println(" " + info.getThreadId() + ": \"" + info.getThreadName() + "\"\n   java.lang.Thread.State: "
				+ info.getThreadState().toString());
		for (StackTraceElement traceElement : trace) {
			System.out.println("\tat " + traceElement);
		}
		System.out.flush();
	}

	/**
	 * 打印单条线程的stack strace，不造成停顿
	 */
	public void printTopStack() throws IOException {
		System.out.println("\n Stack trace of top " + threadLimit + " threads:");

		ThreadInfo[] infos = topThread.getTopThreadInfo();
		for (ThreadInfo info : infos) {
			if (info == null) {
				continue;
			}
			StackTraceElement[] trace = info.getStackTrace();
			System.out.println(" " + info.getThreadId() + ": \"" + info.getThreadName()
					+ "\"\n   java.lang.Thread.State: " + info.getThreadState().toString());
			for (StackTraceElement traceElement : trace) {
				System.out.println("\tat " + traceElement);
			}
		}
		System.out.flush();
	}

	/**
	 * 打印所有线程，只获取名称不获取stack，不造成停顿
	 */
	public void printAllThreads() throws IOException {
		System.out.println("\n Thread Id and name for all live threads:");

		long tids[] = vmInfo.getThreadMXBean().getAllThreadIds();
		ThreadInfo[] threadInfos = vmInfo.getThreadMXBean().getThreadInfo(tids);
		for (ThreadInfo info : threadInfos) {
			if (info == null) {
				continue;
			}

			String threadName = info.getThreadName();
			if (threadNameFilter != null && !threadName.toLowerCase().contains(threadNameFilter)) {
				continue;
			}
			System.out.println(
					" " + info.getThreadId() + "\t: \"" + threadName + "\" (" + info.getThreadState().toString() + ")");
		}

		if (threadNameFilter != null) {
			System.out.println(" Thread name filter is:" + threadNameFilter);
		}
		System.out.flush();
	}


	public void switchCpuAndMemory() {
		topThread.cleanupThreadsHistory();
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
