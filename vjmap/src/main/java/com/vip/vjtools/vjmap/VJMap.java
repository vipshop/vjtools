package com.vip.vjtools.vjmap;

import com.vip.vjtools.vjmap.oops.HistogramHeapAccessor;
import com.vip.vjtools.vjmap.oops.HistogramHeapVisitor;
import sun.jvm.hotspot.HotSpotAgent;
import sun.jvm.hotspot.oops.ObjectHeap;
import sun.jvm.hotspot.runtime.VM;

import java.util.ArrayList;
import java.util.List;

public class VJMap {

	public static void runHeapVisitor(int pid, boolean orderByName, long minSize) {
		ObjectHeap heap = VM.getVM().getObjectHeap();
		HistogramHeapVisitor visitor = new HistogramHeapVisitor();

		System.err.println("Start to dump all areas. This may take a while...");
		heap.iterate(visitor);

		List<ClassStats> list = new ArrayList<ClassStats>();
		list.addAll(visitor.getClassStatsMap().values());
		ResultPrinter resultPrinter = new ResultPrinter();
		resultPrinter.printAllGens(System.out, list, orderByName, minSize);
	}

	public static void runSurvior(int minAge, boolean orderByName, long minSize) {
		HistogramHeapAccessor accessor = new HistogramHeapAccessor();

		System.err.println("Start to dump survivor area. This may take a while...");
		List<ClassStats> list = accessor.dumpSurvivor(minAge);

		ResultPrinter resultPrinter = new ResultPrinter();
		resultPrinter.printSurvivor(System.out, list, orderByName, minSize, minAge);
	}

	public static void runCms(boolean orderByName, long minSize) {
		HistogramHeapAccessor accessor = new HistogramHeapAccessor();

		System.err.println("Start to dump oldgen area. This may take a while...");
		List<ClassStats> list = accessor.dumpCms();

		ResultPrinter resultPrinter = new ResultPrinter();
		resultPrinter.printOldGen(System.out, list, orderByName, minSize);
	}

	public static void main(String[] args) {
		boolean orderByName = false;
		long minSize = -1;
		int minAge = 3;

		if (args.length != 2) {
			printHelp();
			return;
		}

		String modeFlag = args[0];

		String[] modeFlags = modeFlag.split(":");
		if (modeFlags.length > 1) {
			String[] addtionalFlags = modeFlags[1].split(",");
			for (String addtionalFlag : addtionalFlags) {
				if ("byname".equalsIgnoreCase(addtionalFlag)) {
					orderByName = true;
				} else if (addtionalFlag.toLowerCase().startsWith("minsize")) {
					String[] values = addtionalFlag.split("=");
					if (values.length == 1) {
						System.out.println("parameter " + addtionalFlag + " is wrong");
						return;
					}
					minSize = Long.parseLong(values[1]);
				} else if (addtionalFlag.toLowerCase().startsWith("minage")) {
					String[] values = addtionalFlag.split("=");
					if (values.length == 1) {
						System.out.println("parameter " + addtionalFlag + " is wrong");
						return;
					}
					minAge = Integer.parseInt(values[1]);
				}
			}
		}

		Integer pid = Integer.valueOf(args[1]);
		System.out.println("PID:" + pid);

		HotSpotAgent agent = new HotSpotAgent();

		try {
			agent.attach(pid);
			long startTime = System.currentTimeMillis();
			if (modeFlag.startsWith("-all")) {
				runHeapVisitor(pid, orderByName, minSize);
			} else if (modeFlag.startsWith("-sur")) {
				runSurvior(minAge, orderByName, minSize);
			} else if (modeFlag.startsWith("-old")) {
				runCms(orderByName, minSize);
			} else {
				printHelp();
				return;
			}
			long endTime = System.currentTimeMillis();
			double secs = (endTime - startTime) / 1000.0d;
			System.out.printf("%n Heap traversal took %.1f seconds.%n", secs);
			System.out.flush();
		} finally {
			agent.detach();
		}
	}

	private static void printHelp() {
		int leftLength = "-all:minsize=1024,byname".length();
		String format = " %-" + leftLength + "s  %s%n";
		System.out.println("vjmap.sh <options> <PID>");
		System.out.printf(format, "-all", "print all gens histogram, order by total size");
		System.out.printf(format, "-all:minsize=1024", "print all gens histogram, total size>=1024");
		System.out.printf(format, "-all:minsize=1024,byname",
				"print all gens histogram, total size>=1024, order by class name");

		System.out.printf(format, "-old", "print oldgen histogram, order by oldgen size");
		System.out.printf(format, "-old:minsize=1024", "print oldgen histogram, oldgen size>=1024");
		System.out.printf(format, "-old:minsize=1024,byname",
				"print oldgen histogram, oldgen size>=1024, order by class name");

		System.out.printf(format, "-sur", "print survivor histogram, age>=3");
		System.out.printf(format, "-sur:minage=4", "print survivor histogram, age>=4");
		System.out.printf(format, "-sur:minsize=1024,byname",
				"print survivor histogram, age>=3, survivor size>=1024, order by class name");
	}
}
