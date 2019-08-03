package com.vip.vjtools.vjmap;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import com.sun.tools.attach.VirtualMachine;
import com.vip.vjtools.vjmap.oops.GenAddressAccessor;
import com.vip.vjtools.vjmap.oops.HeapHistogramVisitor;
import com.vip.vjtools.vjmap.oops.HeapUtils;
import com.vip.vjtools.vjmap.oops.LoadedClassAccessor;
import com.vip.vjtools.vjmap.oops.OldgenAccessor;
import com.vip.vjtools.vjmap.oops.SurvivorAccessor;
import com.vip.vjtools.vjmap.utils.TimeController.TimeoutException;

import sun.jvm.hotspot.HotSpotAgent;
import sun.jvm.hotspot.oops.ObjectHeap;
import sun.jvm.hotspot.runtime.VM;
import sun.tools.attach.HotSpotVirtualMachine;;

public class VJMap {

	public static final String VERSION = "1.0.9";

	private static PrintStream tty = System.out;
	// 用于ctrl－C退出时仍然打印结果
	private static OldGenProcessor oldGenProcessor;
	private static HeapProcessor heapProcessor;

	public static void runHeapVisitor(int pid, boolean orderByName, long minSize) {
		ObjectHeap heap = VM.getVM().getObjectHeap();
		heapProcessor = new HeapProcessor(orderByName, minSize);

		tty.println("Iterating over heap. This may take a while...");
		tty.println("Geting live regions...");

		heap.iterate(heapProcessor.visitor);

		heapProcessor.printResult();
		heapProcessor = null;
	}

	public static class HeapProcessor {
		HeapHistogramVisitor visitor = new HeapHistogramVisitor();
		boolean orderByName;
		long minSize;

		public HeapProcessor(boolean orderByName, long minSize) {
			this.orderByName = orderByName;
			this.minSize = minSize;
		}

		public void printResult() {
			List<ClassStats> list = HeapUtils.getClassStatsList(visitor.getClassStatsMap());
			ResultPrinter resultPrinter = new ResultPrinter();
			resultPrinter.printAllGens(tty, list, orderByName, minSize);
		}
	}

	public static void runSurviorAccessor(int age, int minAge, boolean orderByName, long minSize) {
		SurvivorAccessor accessor = new SurvivorAccessor();

		tty.println("Iterating over survivor area. This may take a while...");
		List<ClassStats> list = accessor.caculateHistogram(age, minAge);

		ResultPrinter resultPrinter = new ResultPrinter();
		resultPrinter.printSurvivor(tty, list, orderByName, minSize, age, minAge);
	}

	public static void runOldGenAccessor(boolean orderByName, long minSize) {
		oldGenProcessor = new OldGenProcessor(orderByName, minSize);
		tty.println("Iterating over oldgen area. This may take a while...");
		oldGenProcessor.accessor.caculateHistogram();
		oldGenProcessor.printResult();
		oldGenProcessor = null;
	}

	public static class OldGenProcessor {
		OldgenAccessor accessor = new OldgenAccessor();
		boolean orderByName;
		long minSize;

		public OldGenProcessor(boolean orderByName, long minSize) {
			this.orderByName = orderByName;
			this.minSize = minSize;
		}

		public void printResult() {
			List<ClassStats> list = HeapUtils.getClassStatsList(accessor.getClassStatsMap());
			ResultPrinter resultPrinter = new ResultPrinter();
			resultPrinter.printOldGen(tty, list, orderByName, minSize);
		}
	}

	public static void printGenAddress() {
		GenAddressAccessor accessor = new GenAddressAccessor();
		accessor.printHeapAddress();
	}

	public static void printLoadedClass() {
		LoadedClassAccessor accessor = new LoadedClassAccessor();
		accessor.pringLoadedClass();
	}

	public static void main(String[] args) {
		// 分析参数
		boolean orderByName = false;
		long minSize = -1;
		int minAge = 2;
		int age = -1;
		boolean live = false;
		// boolean dead = false;
		if (!(args.length == 2 || args.length == 3)) {
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
						tty.println("parameter " + addtionalFlag + " is wrong");
						return;
					}
					minSize = Long.parseLong(values[1]);
				} else if (addtionalFlag.toLowerCase().startsWith("minage")) {
					String[] values = addtionalFlag.split("=");
					if (values.length == 1) {
						tty.println("parameter " + addtionalFlag + " is wrong");
						return;
					}
					minAge = Integer.parseInt(values[1]);
				} else if (addtionalFlag.toLowerCase().startsWith("age")) {
					String[] values = addtionalFlag.split("=");
					if (values.length == 1) {
						tty.println("parameter " + addtionalFlag + " is wrong");
						return;
					}
					age = Integer.parseInt(values[1]);
				} else if (addtionalFlag.toLowerCase().startsWith("live")) {
					live = true;
				}
			}
		}

		Integer pid = null;
		String executablePath = null;
		String coredumpPath = null;
		if (args.length == 2) {
			pid = Integer.valueOf(args[1]);
		} else {
			executablePath = args[1];
			coredumpPath = args[2];
		}

		// 如有需要，执行GC
		if (live) {
			if (pid == null) {
				tty.println("only a running vm can be attached when live option is on");
				return;
			}
			triggerGc(pid);
		}


		//// 正式执行
		HotSpotAgent agent = new HotSpotAgent();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {

				// 如果ctrl＋C退出，仍尽量打印结果
				if (oldGenProcessor != null) {
					tty.println("VJMap aborted. Below is the incomplete summary: ");
					oldGenProcessor.printResult();
				}
				if (heapProcessor != null) {
					tty.println("VJMap aborted. Below is the incomplete summary: ");
					heapProcessor.printResult();
				}
				tty.flush();
			}
		});

		try {
			if (args.length == 2) {
				agent.attach(pid);
			} else {
				agent.attach(executablePath, coredumpPath);
			}

			long startTime = System.currentTimeMillis();
			if (modeFlag.startsWith("-all")) {
				runHeapVisitor(pid, orderByName, minSize);
			} else if (modeFlag.startsWith("-sur")) {
				runSurviorAccessor(age, minAge, orderByName, minSize);
			} else if (modeFlag.startsWith("-old")) {
				runOldGenAccessor(orderByName, minSize);
			} else if (modeFlag.startsWith("-address")) {
				printGenAddress();
			} else if (modeFlag.startsWith("-class")) {
				printLoadedClass();
			} else if (modeFlag.startsWith("-version")) {
				tty.println("vjmap version:" + VERSION);
				return;
			} else {
				printHelp();
				return;
			}
			long endTime = System.currentTimeMillis();
			double secs = (endTime - startTime) / 1000.0d;
			tty.printf("%n Heap traversal took %.1f seconds.%n", secs);
			tty.flush();
		} catch (TimeoutException e) {
			tty.println("\n\nVJMap aborted by timeout.");
			tty.println("Try to use live option to reduce the fragments which make progress very slow.");
			tty.println("./vjmap.sh -old:live PID\n\n");
		} catch (Exception e) {
			tty.println("Error Happen:" + e.getMessage());
			if (e.getMessage() != null && e.getMessage().contains("Can't attach to the process")) {
				tty.println(
						"Please use the same user of the target JVM to run vjmap, or use root user to run it (sudo -E vjmap.sh ...)");
			}
		} finally {
			agent.detach();
		}
	}

	/**
	 * Trigger a remote gc using HotSpotVirtualMachine, inspired by jcmd's source code.
	 * 
	 * @param pid
	 */
	private static void triggerGc(Integer pid) {
		VirtualMachine vm = null;
		try {
			vm = VirtualMachine.attach(String.valueOf(pid));
			HotSpotVirtualMachine hvm = (HotSpotVirtualMachine) vm;
			try (InputStream in = hvm.executeJCmd("GC.run");) {
				byte b[] = new byte[256];
				int n;
				do {
					n = in.read(b);
					if (n > 0) {
						String s = new String(b, 0, n, "UTF-8");
						tty.print(s);
					}
				} while (n > 0);
				tty.println();
			}
		} catch (Exception e) {
			tty.println(e.getMessage());
		} finally {
			if (vm != null) {
				try {
					vm.detach();
				} catch (IOException e) {
					tty.println(e.getMessage());
				}
			}
		}
	}

	private static void printHelp() {
		int leftLength = "-all:minsize=1024,byname".length();
		String format = " %-" + leftLength + "s  %s%n";
		tty.println("vjmap " + VERSION
				+ " - prints per GC generation (Eden, Survivor, OldGen) object details of a given process.");
		tty.println("Usage: vjmap.sh <options> <PID>");
		tty.println("Usage: vjmap.sh <options> <executable java path> <coredump file path>");
		tty.println("");
		tty.printf(format, "-all", "print all gens histogram, order by total size");
		tty.printf(format, "-all:live", "print all gens histogram, live objects only");
		tty.printf(format, "-all:minsize=1024", "print all gens histogram, total size>=1024");
		tty.printf(format, "-all:minsize=1024,byname",
				"print all gens histogram, total size>=1024, order by class name");

		tty.printf(format, "-old", "print oldgen histogram, order by oldgen size");
		tty.printf(format, "-old:live", "print oldgen histogram, live objects only");
		tty.printf(format, "-old:minsize=1024", "print oldgen histogram, oldgen size>=1024");
		tty.printf(format, "-old:minsize=1024,byname",
				"print oldgen histogram, oldgen size>=1024, order by class name");

		tty.printf(format, "-sur", "print survivor histogram, age>=2");
		tty.printf(format, "-sur:age=4", "print survivor histogram, age==4");
		tty.printf(format, "-sur:minage=4", "print survivor histogram, age>=4, default is 2");
		tty.printf(format, "-sur:minsize=1024,byname",
				"print survivor histogram, age>=3, survivor size>=1024, order by class name");
		tty.printf(format, "-address", "print address for all gens");
		tty.printf(format, "-class", "print all loaded classes");
	}
}
