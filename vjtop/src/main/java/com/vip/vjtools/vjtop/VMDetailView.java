package com.vip.vjtools.vjtop;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.Date;

import com.sun.management.OperatingSystemMXBean;
import com.vip.vjtools.vjtop.TopThreadInfo.TopCpuResult;
import com.vip.vjtools.vjtop.TopThreadInfo.TopMemoryResult;
import com.vip.vjtools.vjtop.util.Formats;
import com.vip.vjtools.vjtop.util.Utils;

@SuppressWarnings("restriction")
public class VMDetailView {
	private static final int DEFAULT_WIDTH = 100;
	private static final int MIN_WIDTH = 80;

	public ThreadInfoMode threadInfoMode;
	private ContentMode contentMode;
	private OutputFormat format;

	public int threadLimit = 10;
	public int interval;
	public String threadNameFilter = null;

	private int width;

	public VMInfo vmInfo;
	public TopThreadInfo topThreadInfo;
	public ThreadPrinter threadPrinter;
	private WarningRule warning;

	// 纪录vjtop进程本身的消耗
	private boolean isDebugCost = false;
	private long lastCpu = 0;

	private boolean shouldExit = false;
	private boolean firstTime = true;
	public boolean displayCommandHints = false;

	public VMDetailView(VMInfo vmInfo, OutputFormat format, ContentMode contentMode, ThreadInfoMode threadInfoMode,
			Integer width, Integer interval) throws Exception {
		this.vmInfo = vmInfo;
		this.topThreadInfo = new TopThreadInfo(vmInfo);
		this.threadPrinter = new ThreadPrinter(this);
		this.warning = vmInfo.warningRule;

		this.contentMode = contentMode;
		this.threadInfoMode = threadInfoMode;
		this.format = format;

		this.interval = interval;
		setWidth(width);

		if (contentMode == ContentMode.all || contentMode == ContentMode.thread) {
			vmInfo.initThreadInfoAbility();
		}
	}

	public void printView() throws Exception {

		Formats.clearTerminal();

		// 计算vjtop自身消耗
		long iterationStartTime = 0;
		long iterationStartCpu = 0;
		if (isDebugCost) {
			iterationStartTime = System.currentTimeMillis();
			iterationStartCpu = ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean())
					.getProcessCpuTime();
		}

		vmInfo.update(contentMode == ContentMode.all || contentMode == ContentMode.jvm);

		if (!checkState()) {
			return;
		}

		// 打印进程级别内容
		if (contentMode == ContentMode.all || contentMode == ContentMode.jvm) {
			if (format == OutputFormat.text) {
				printJvmInfoAsText();
			} else {
				printJvmInfoAsConsole();
			}
		}

		// JMX更新失败，不打印后续一定需要JMX获取的数据
		if (!vmInfo.isJmxStateOk()) {
			printJmxError();
			return;
		}

		// 打印繁忙线程级别内容
		if (contentMode == ContentMode.all || contentMode == ContentMode.thread) {
			try {
				if (threadInfoMode.isCpuMode) {
					printTopCpuThreads(threadInfoMode, format != OutputFormat.text);
				} else {
					printTopMemoryThreads(threadInfoMode, format != OutputFormat.text);
				}
			} catch (Exception e) {
				System.out.println("");
				e.printStackTrace();
				System.out.println(Formats.red("ERROR: Exception happen when fetch thread information via JMX"));
			}
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
			System.out.println("\n" + Formats.red("ERROR: Could not attach to process, exit now."));
			shoulExit();
			return false;
		}
		return true;
	}

	private void printJvmInfoAsConsole() {
		System.out.printf(" %8tT - PID: %s JVM: %s USER: %s UPTIME: %s%n", new Date(), vmInfo.pid, vmInfo.jvmVersion,
				vmInfo.osUser, Formats.toTimeUnit(vmInfo.upTimeMills.current));

		String[] cpuLoadAnsi = Formats.colorAnsi(vmInfo.cpuLoad, warning.cpu);

		System.out.printf(" PROCESS: %.2f%% cpu(%s%.2f%%%s of %d core)", vmInfo.singleCoreCpuLoad, cpuLoadAnsi[0],
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

	private void printJvmInfoAsText() {
		System.out.printf("time:%8tT%npid:%s%njvm:%s%nuser:%s%nuptime:%s%n", new Date(), vmInfo.pid, vmInfo.jvmVersion,
				vmInfo.osUser, vmInfo.upTimeMills.current);

		System.out.printf("process.cpu.core:%.2f%nprocess.cpu.server:%.2f%nserver.core:%d%n", vmInfo.singleCoreCpuLoad,
				vmInfo.cpuLoad, vmInfo.processors);
		if (vmInfo.isLinux) {
			System.out.printf("process.thread:%d%nrss:%d%nrss.peak:%d%nswap:%d%n", vmInfo.osThreads, vmInfo.rss,
					vmInfo.peakRss, vmInfo.swap);

			if (vmInfo.ioDataSupport) {
				System.out.printf("disk.read:%d%ndisk.write:%d%n", vmInfo.readBytes.ratePerSecond,
						vmInfo.writeBytes.ratePerSecond, warning.io);
			}
		}

		System.out.printf("thread.live:%d%nthread.daemon:%d%nthread.peak:%d%nthread.new:%d%n", vmInfo.threadActive,
				vmInfo.threadDaemon, vmInfo.threadPeak, vmInfo.threadNew.delta);

		System.out.printf("class.loaded:%d%nclass.unloaded:%d%nclass.new:%d%n", vmInfo.classLoaded.current,
				vmInfo.classUnLoaded, vmInfo.classLoaded.delta);

		System.out.printf(
				"eden.use:%d%neden.commit:%d%neden.max:%d%nsur.use:%d%nsur.commit:%d%nsur.max:%d%nold.use:%d%nold.commit:%d%nold.max:%d%n",
				vmInfo.eden.used, vmInfo.eden.committed, vmInfo.eden.max, vmInfo.sur.used, vmInfo.sur.committed,
				vmInfo.sur.max, vmInfo.old.used, vmInfo.old.committed, vmInfo.old.max);

		System.out.printf(
				"%s.use:%d%n%s.commit:%d%n%s.max:%d%ncodeCache.use:%d%ncodeCache.commit:%d%ncodeCache.max:%d%n",
				vmInfo.permGenName, vmInfo.perm.used, vmInfo.permGenName, vmInfo.perm.committed, vmInfo.permGenName,
				vmInfo.perm.max, vmInfo.codeCache.used, vmInfo.codeCache.committed, vmInfo.codeCache.max);
		if (vmInfo.jvmMajorVersion >= 8) {
			System.out.printf("ccs.use:%d%nccs.commit:%d%nccs.max:%d%n", vmInfo.ccs.used, vmInfo.ccs.committed,
					vmInfo.ccs.max);
		}

		System.out.printf(
				"direct.use:%d%ndirect.commit:%d%ndirect.max:%d%nmap.use:%d%nmap.commit:%d%nmap.count:%d%nthreadStack:%d%n",
				vmInfo.direct.used, vmInfo.direct.committed, vmInfo.direct.max, vmInfo.map.used, vmInfo.map.committed,
				vmInfo.map.max, vmInfo.threadStackSize * vmInfo.threadActive);

		long ygcCount = vmInfo.ygcCount.delta;
		long ygcTime = vmInfo.ygcTimeMills.delta;
		long avgYgcTime = ygcCount == 0 ? 0 : ygcTime / ygcCount;
		long fgcCount = vmInfo.fullgcCount.delta;
		System.out.printf("ygc.count:%d%nygc.time:%d%nygc.avgtime::%d%nfgc.count:%d%nfgc.time:%d%n", ygcCount, ygcTime,
				avgYgcTime, fgcCount, vmInfo.fullgcTimeMills.delta);

		if (vmInfo.perfDataSupport) {
			System.out.printf("safePoint.count:%d%nsafePoint.time:%d%nsafePoint.syncTime:%d%n",
					vmInfo.safepointCount.delta, vmInfo.safepointTimeMills.delta, vmInfo.safepointSyncTimeMills.delta);
		}
	}

	private void printTopCpuThreads(ThreadInfoMode mode, boolean console) throws IOException {
		if (!vmInfo.threadCpuTimeSupported) {
			if (console) {
				System.out.printf("%n -Thread CPU telemetries are not available on the monitored jvm/platform-%n");
			}
			return;
		}

		TopCpuResult result = topThreadInfo.topCpuThreads(mode, threadLimit);

		// 第一次无数据时跳过
		if (!result.ready) {
			if (console) {
				printWelcome();
			}
			return;
		}

		// 打印线程view的页头
		String titleFormat = "%n %6s %-" + getThreadNameWidth() + "s %10s %6s %6s %6s %6s%n";
		String dataFormat = " %6d %-" + getThreadNameWidth() + "s %10s %5.2f%% %5.2f%% %5.2f%% %5.2f%%%n";
		String dataFormatAsText = "thread-%d:%s %s %.2f %.2f %.2f %.2f%n";
		if (console) {
			System.out.printf(titleFormat, "TID", "NAME  ", "STATE", "CPU", "SYSCPU", " TOTAL", "TOLSYS");

			if (result.activeThreads == 0) {
				System.out.printf("%n -Every thread use cpu lower than 0.05%%-%n");
			}
		}
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

			double syscpu = Utils.calcLoad(result.threadSysCpuDeltaTimes.get(tid), vmInfo.upTimeMills.delta,
					Utils.NANOS_TO_MILLS);

			// 在进程所有消耗的CPU里，本线程的比例
			double totalcpuPercent = Utils.calcLoad(result.threadCpuTotalTimes.get(tid), vmInfo.cpuTimeNanos.current,
					1);

			double totalsysPercent = Utils.calcLoad(result.threadSysCpuTotalTimes.get(tid), vmInfo.cpuTimeNanos.current,
					1);

			if (console) {

				System.out.printf(dataFormat, tid, threadName, Formats.leftStr(info.getThreadState().toString(), 10),
						cpu, syscpu, totalcpuPercent, totalsysPercent);
			} else {
				System.out.printf(dataFormatAsText, tid, threadName, info.getThreadState().toString(), cpu, syscpu,
						totalcpuPercent, totalsysPercent);
			}

		}

		// 打印线程汇总
		double deltaAllActiveThreadCpuLoad = Utils.calcLoad(result.deltaAllActiveThreadCpu / Utils.NANOS_TO_MILLS,
				vmInfo.upTimeMills.delta);
		double deltaAllActiveThreadSysCpuLoad = Utils.calcLoad(result.deltaAllActiveThreadSysCpu / Utils.NANOS_TO_MILLS,
				vmInfo.upTimeMills.delta);
		double deltaAllFreeThreadCpuLoad = Utils.calcLoad(result.deltaAllFreeThreadCpu / Utils.NANOS_TO_MILLS,
				vmInfo.upTimeMills.delta);
		// double deltaAllFreeThreadSysCpuLoad = Utils.calcLoad(result.deltaAllFreeThreadSysCpu / Utils.NANOS_TO_MILLS,
		// vmInfo.upTimeMills.delta);

		if (console) {
			System.out.printf(
					"%n Total  : %.2f%% cpu(user=%.2f%%, sys=%.2f%%) by %d active java threads, %.2f%% by others%n",
					deltaAllActiveThreadCpuLoad, deltaAllActiveThreadCpuLoad - deltaAllActiveThreadSysCpuLoad,
					deltaAllActiveThreadSysCpuLoad, result.activeThreads, deltaAllFreeThreadCpuLoad);

			System.out.printf(" Setting: top %d threads order by %s%s, flush every %ds%n", threadLimit,
					mode.toString().toUpperCase(), threadNameFilter == null ? "" : " filter by " + threadNameFilter,
					interval);
		} else {
			System.out.printf(
					"sum.active.threadCount:%d%nsum.active.cpu.total:%.2f%nsum.active.cpu.user:%.2f%nsum.active.cpu.sys:%.2f%nsum.free.cpu.total:%.2f%n",
					result.activeThreads, deltaAllActiveThreadCpuLoad,
					deltaAllActiveThreadCpuLoad - deltaAllActiveThreadSysCpuLoad, deltaAllActiveThreadSysCpuLoad,
					deltaAllFreeThreadCpuLoad);
		}
	}

	private void printTopMemoryThreads(ThreadInfoMode mode, boolean console) throws IOException {
		if (!vmInfo.threadMemoryAllocatedSupported) {
			if (console) {
				System.out.printf(
						"%n -Thread Memory Allocated telemetries are not available on the monitored jvm/platform-%n");
			}
			return;
		}

		TopMemoryResult result = topThreadInfo.topMemoryThreads(mode, threadLimit);

		// 第一次无数据跳过
		if (!result.ready) {
			if (console) {
				printWelcome();
			}
			return;
		}

		// 打印线程View的页头
		String titleFormat = "%n %6s %-" + getThreadNameWidth() + "s %10s %14s %18s%n";
		String dataFormat = " %6d %-" + getThreadNameWidth() + "s %10s %5s/s(%5.2f%%) %10s(%5.2f%%)%n";
		String dataFormatAsText = "thread-%d:%s %s %s %.2f %s %.2f%n";
		if (console) {
			System.out.printf(titleFormat, "TID", "NAME  ", "STATE", "MEMORY", "TOTAL-ALLOCATED");

			if (result.activeThreads == 0) {
				System.out.printf("%n -Every thread allocate memory slower than 1k/s-%n");
			}
		}

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
			if (console) {
				System.out.printf(dataFormat, tid, threadName, Formats.leftStr(info.getThreadState().toString(), 10),
						Formats.toFixLengthSizeUnit(allocationRate),
						Utils.calcMemoryUtilization(result.threadMemoryDeltaBytesMap.get(tid),
								result.deltaAllThreadBytes),
						Formats.toFixLengthSizeUnit(result.threadMemoryTotalBytesMap.get(tid)),
						Utils.calcMemoryUtilization(result.threadMemoryTotalBytesMap.get(tid),
								result.totalAllThreadBytes));
			} else {
				System.out.printf(dataFormatAsText, tid, threadName, info.getThreadState().toString(), allocationRate,
						Utils.calcMemoryUtilization(result.threadMemoryDeltaBytesMap.get(tid),
								result.deltaAllThreadBytes),
						result.threadMemoryTotalBytesMap.get(tid), Utils.calcMemoryUtilization(
								result.threadMemoryTotalBytesMap.get(tid), result.totalAllThreadBytes));
			}
		}

		if (console) {
			// 打印线程汇总信息，这里因为最后单位是精确到秒，所以bytes除以毫秒以后要乘以1000才是按秒统计
			System.out.printf("%n Total  : %5s/s memory allocated by %d active threads%n",
					Formats.toFixLengthSizeUnit((result.deltaAllThreadBytes * 1000) / vmInfo.upTimeMills.delta),
					result.activeThreads);

			System.out.printf(" Setting: top %d threads order by %s%s, flush every %ds%n", threadLimit,
					mode.toString().toUpperCase(), threadNameFilter == null ? "" : " filter by " + threadNameFilter,
					interval);
		} else {
			System.out.printf("sum.active.threadCount:%d%nsum.active.allocateRate:%d%n", result.activeThreads,
					(result.deltaAllThreadBytes * 1000) / vmInfo.upTimeMills.delta);
		}
	}

	private void printWelcome() {
		if (firstTime && contentMode != ContentMode.thread) {
			if (!vmInfo.isLinux) {
				System.out.printf(
						"%n" + Formats.yellow(" OS isn't linux, Process's MEMORY, THREAD, DISK data will be skipped.")
								+ "%n");
			}

			if (!vmInfo.ioDataSupport) {
				System.out.printf("%n"
						+ Formats.yellow(" /proc/%s/io is not readable, Process's DISK data will be skipped.") + "%n",
						vmInfo.pid);
			}

			if (!vmInfo.perfDataSupport) {
				System.out.printf(
						"%n" + Formats.yellow(" Perfdata doesn't support, SAFE-POINT data will be skipped.") + "%n");
			}

			System.out.printf("%n VMARGS: %s%n%n", vmInfo.vmArgs);

			firstTime = false;

		}
		System.out.printf("%n Collecting data, please wait ......%n%n");
	}

	private void printJmxError() {
		if (!vmInfo.currentGcCause.equals("No GC")) {
			System.out.println("\n" + Formats.red(
					"ERROR: Could not fetch data via JMX - Process is doing GC, cause is " + vmInfo.currentGcCause));
		} else {
			System.out.println(
					System.lineSeparator() + Formats.red("ERROR: Could not fetch data via JMX - Process terminated?"));
		}
	}

	private void printIterationCost(long iterationStartTime, long iterationStartCpu) {
		long currentCpu = ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getProcessCpuTime();
		long deltaIterationTime = System.currentTimeMillis() - iterationStartTime;

		long deltaIterationCpuTime = (currentCpu - iterationStartCpu) / Utils.NANOS_TO_MILLS;
		long deltaOtherCpuTime = (iterationStartCpu - lastCpu) / Utils.NANOS_TO_MILLS;
		long deltaTotalCpuTime = deltaIterationCpuTime + deltaOtherCpuTime;
		lastCpu = currentCpu;

		System.out.printf(" Cost %.2f%% cpu in %dms, other is %dms, total is %dms%n",
				deltaIterationCpuTime * 100d / deltaIterationTime, deltaIterationTime, deltaOtherCpuTime,
				deltaTotalCpuTime);
	}


	public void switchCpuAndMemory() {
		topThreadInfo.cleanupThreadsHistory();
	}


	public boolean shouldExit() {
		return shouldExit;
	}

	/**
	 * Requests the disposal of this view - it should be called again.
	 */
	public void shoulExit() {
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

	public enum ThreadInfoMode {
		cpu(true), totalcpu(true), syscpu(true), totalsyscpu(true), memory(false), totalmemory(false);

		public boolean isCpuMode;

		private ThreadInfoMode(boolean isCpuMode) {
			this.isCpuMode = isCpuMode;
		}

		public static ThreadInfoMode parse(String value) {
			try {
				return ThreadInfoMode.valueOf(value);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException(
						"wrong option of thread info mode(cpu,syscpu,totalcpu,totalsyscpu,memory,totalmemory)");
			}
		}

		public static ThreadInfoMode parseInt(String mode) {
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

	public enum OutputFormat {
		console(true), cleanConsole(false), text(false);
		OutputFormat(boolean ansi) {
			this.ansi = ansi;
		}

		public boolean ansi;
	}

	public enum ContentMode {
		all, jvm, thread
	}
}
