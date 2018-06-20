package com.vip.vjtools.vjmap;

import java.util.List;

import com.vip.vjtools.vjmap.oops.GenAddressAccessor;
import com.vip.vjtools.vjmap.oops.HeapHistogramVisitor;
import com.vip.vjtools.vjmap.oops.OldgenAccessor;
import com.vip.vjtools.vjmap.oops.SurvivorAccessor;

import sun.jvm.hotspot.HotSpotAgent;
import sun.jvm.hotspot.oops.ObjectHeap;
import sun.jvm.hotspot.runtime.VM;

public class VJMap {

	public static final String VERSION = "1.0.1";

	public static void runHeapVisitor(int pid, boolean orderByName, long minSize) {
		ObjectHeap heap = VM.getVM().getObjectHeap();
		HeapHistogramVisitor visitor = new HeapHistogramVisitor();

		System.out.println("Start to dump all areas. This may take a while...");
		heap.iterate(visitor);

		List<ClassStats> list = visitor.getClassStatsList();
		ResultPrinter resultPrinter = new ResultPrinter();
		resultPrinter.printAllGens(System.out, list, orderByName, minSize);
	}

	public static void runSurviorAccessor(int minAge, boolean orderByName, long minSize) {
		SurvivorAccessor accessor = new SurvivorAccessor();

		System.out.println("Start to dump survivor area. This may take a while...");
		List<ClassStats> list = accessor.dump(minAge);

		ResultPrinter resultPrinter = new ResultPrinter();
		resultPrinter.printSurvivor(System.out, list, orderByName, minSize, minAge);
	}

	public static void runOldGenAccessor(boolean orderByName, long minSize) {
		OldgenAccessor accessor = new OldgenAccessor();

		System.out.println("Start to dump oldgen area. This may take a while...");
		List<ClassStats> list = accessor.dump();

		ResultPrinter resultPrinter = new ResultPrinter();
		resultPrinter.printOldGen(System.out, list, orderByName, minSize);
	}

	public static void printGenAddress() {
		GenAddressAccessor accessor = new GenAddressAccessor();
		accessor.printHeapAddress();
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

		HotSpotAgent agent = new HotSpotAgent();

		try {
			agent.attach(pid);
			long startTime = System.currentTimeMillis();
			if (modeFlag.startsWith("-all")) {
				runHeapVisitor(pid, orderByName, minSize);
			} else if (modeFlag.startsWith("-sur")) {
				runSurviorAccessor(minAge, orderByName, minSize);
			} else if (modeFlag.startsWith("-old")) {
				runOldGenAccessor(orderByName, minSize);
			} else if (modeFlag.startsWith("-address")) {
				printGenAddress();
			} else if (modeFlag.startsWith("-version")) {
				System.out.println("vjmap version:" + VERSION);
				return;
			} else {
				printHelp();
				return;
			}
			long endTime = System.currentTimeMillis();
			double secs = (endTime - startTime) / 1000.0d;
			System.out.printf("%n Heap traversal took %.1f seconds.%n", secs);
			System.out.flush();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			if (e.getMessage().contains("Can't attach to the process")) {
				System.out.println(
						"Please use the same user of the target JVM to run vjmap or use root to run it (sudo -E vjmap.sh ...)");
			}
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
		System.out.printf(format, "-address", "print address for all gens");
	}
}
