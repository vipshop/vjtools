package com.vip.vjtools.vjtop;

import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import com.vip.vjtools.vjtop.VMInfo.VMInfoState;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class VJTop {

	public static final String VERSION = "1.0.5";

	public static final int DEFAULT_INTERVAL = 10;

	private static final String CLEAR_TERMINAL_ANSI_CMD = new String(
			new byte[] { (byte) 0x1b, (byte) 0x5b, (byte) 0x32, (byte) 0x4a, (byte) 0x1b, (byte) 0x5b, (byte) 0x48 });

	public VMDetailView view;

	private volatile Integer interval = DEFAULT_INTERVAL;

	private volatile boolean needMoreInput = false;

	private Thread mainThread;
	private long sleepStartTime;
	private int maxIterations = -1;

	private static OptionParser createOptionParser() {
		OptionParser parser = new OptionParser();
		// commmon
		parser.acceptsAll(Arrays.asList("help", "?", "h" ), "shows this help").forHelp();
		parser.acceptsAll(Arrays.asList("n", "iteration" ),
				"vjtop will exit after n output iterations  (defaults to unlimit)").withRequiredArg()
				.ofType(Integer.class);
		parser.acceptsAll(Arrays.asList("i", "interval", "d" ),
				"interval between each output iteration (defaults to 10s)").withRequiredArg().ofType(Integer.class);
		parser.acceptsAll(Arrays.asList("w", "width" ),
				"Number of columns for the console display (defaults to 100)").withRequiredArg().ofType(Integer.class);
		parser.acceptsAll(Arrays.asList("l", "limit" ),
				"Number of threads to display ( default to 10 threads)").withRequiredArg().ofType(Integer.class);
		parser.acceptsAll(Arrays.asList("f", "filter"), "Thread name filter ( no default)")
				.withRequiredArg().ofType(String.class);

		parser.acceptsAll(Arrays.asList("j", "jmxurl"),
				"JMX url like 127.0.0.1:7001 when VM attach is not work").withRequiredArg().ofType(String.class);

		// detail mode
		parser.accepts("cpu",
				"default mode in detail view, display thread cpu usage and sort by thread delta cpu time ");
		parser.accepts("totalcpu", "display thread cpu usage and sort by total cpu time");
		parser.accepts("syscpu", "display thread cpu usage and sort by delta syscpu time");
		parser.accepts("totalsyscpu", "display thread cpu usage and sort by total syscpu time");
		parser.accepts("memory", "display thread memory allocated and sort by delta");
		parser.accepts("totalmemory", "display thread memory allocated and sort by total");

		return parser;
	}

	public static void main(String[] args) {
		try {
			// 1. create option parser
			OptionParser parser = createOptionParser();
			OptionSet optionSet = parser.parse(args);

			if (optionSet.has("help")) {
				printHelper(parser);
				System.exit(0);
			}

			// 2. create vminfo
			String pid = parsePid(parser, optionSet);

			String jmxHostAndPort = null;
			if (optionSet.hasArgument("jmxurl")) {
				jmxHostAndPort = (String) optionSet.valueOf("jmxurl");
			}

			VMInfo vminfo = VMInfo.processNewVM(pid, jmxHostAndPort);
			if (vminfo.state != VMInfoState.ATTACHED) {
				System.out.println("\nERROR: Could not attach to process, see the solution in README\n");
				return;
			}

			// 3. create view
			VMDetailView.DetailMode displayMode = parseDisplayMode(optionSet);
			Integer width = null;
			if (optionSet.hasArgument("width")) {
				width = (Integer) optionSet.valueOf("width");
			}

			Integer interval = DEFAULT_INTERVAL;
			if (optionSet.hasArgument("interval")) {
				interval = (Integer) (optionSet.valueOf("interval"));
				if (interval < 1) {
					throw new IllegalArgumentException("Interval cannot be set below 1.0");
				}
			}

			VMDetailView view = new VMDetailView(vminfo, displayMode, width, interval);

			if (optionSet.hasArgument("limit")) {
				Integer limit = (Integer) optionSet.valueOf("limit");
				view.threadLimit = limit;
			}

			if (optionSet.hasArgument("filter")) {
				String filter = (String) optionSet.valueOf("filter");
				view.threadNameFilter = filter;
			}

			// 4. create main application
			VJTop app = new VJTop();
			app.mainThread = Thread.currentThread();
			app.view = view;
			app.updateInterval(interval);

			if (optionSet.hasArgument("n")) {
				Integer iterations = (Integer) optionSet.valueOf("n");
				app.maxIterations = iterations;
			}

			// 5. start thread to get user input
			if (app.maxIterations == -1) {
				InteractiveTask task = new InteractiveTask(app);
				if (task.inputEnabled()) {
					view.displayCommandHints = true;

					Thread interactiveThread = new Thread(task, "InteractiveThread");
					interactiveThread.setDaemon(true);
					interactiveThread.start();
				}
			}

			// 6. run app
			app.run(view);
		} catch (Exception e) {
			e.printStackTrace(System.out);
			System.out.flush();
		}
	}

	private void run(VMDetailView view) throws Exception {
		try {
			// System.out 设为Buffered，需要使用System.out.flush刷新
			System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(FileDescriptor.out)), false));

			int iterations = 0;
			while (!view.shouldExit()) {
				waitForInput();
				clearTerminal();

				view.printView();

				System.out.flush();

				if (maxIterations > 0 && iterations >= maxIterations) {
					break;
				}

				// 第一次最多只等待2秒
				int sleepSeconds = (iterations == 0) ? Math.min(2, interval) : interval;

				iterations++;
				sleepStartTime = System.currentTimeMillis();
				Utils.sleep(sleepSeconds * 1000);
			}
			System.out.println("");
			System.out.flush();
		} catch (NoClassDefFoundError e) {
			e.printStackTrace(System.out);
			System.out.println("ERROR: Some JDK classes cannot be found.");
			System.out.println("       Please check if the JAVA_HOME environment variable has been set to a JDK path.");
			System.out.println("");
			System.out.flush();
		}
	}

	private static VMDetailView.DetailMode parseDisplayMode(OptionSet optionSet) {
		VMDetailView.DetailMode displayMode = VMDetailView.DetailMode.cpu;
		if (optionSet.has("memory")) {
			displayMode = VMDetailView.DetailMode.memory;
		} else if (optionSet.has("totalmemory")) {
			displayMode = VMDetailView.DetailMode.totalmemory;
		} else if (optionSet.has("totalcpu")) {
			displayMode = VMDetailView.DetailMode.totalcpu;
		} else if (optionSet.has("syscpu")) {
			displayMode = VMDetailView.DetailMode.syscpu;
		} else if (optionSet.has("totalsyscpu")) {
			displayMode = VMDetailView.DetailMode.totalsyscpu;
		}
		return displayMode;
	}

	private static String parsePid(OptionParser parser, OptionSet optionSet) {
		Integer pid = null;

		// to support PID as non option argument
        if (!optionSet.nonOptionArguments().isEmpty()) {
			pid = Integer.valueOf((String) optionSet.nonOptionArguments().get(0));
		}

		if (pid == null) {
			System.out.println("PID can't be empty !!!");
			printHelper(parser);
			System.exit(0);
		}

		return String.valueOf(pid);
	}

	private static void printHelper(OptionParser parser) {
		try {
			System.out.println("vjtop " + VERSION + " - java monitoring for the command-line");
			System.out.println("Usage: vjtop.sh [options...] <PID>");
			System.out.println("");
			parser.printHelpOn(System.out);
		} catch (IOException ignored) {

		}
	}

	private static void clearTerminal() {
		if (Utils.isWindows) {
			System.out.printf("%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n");
		} else {
			System.out.print(CLEAR_TERMINAL_ANSI_CMD);
		}
	}

	public void exit() {
		view.exit();
		mainThread.interrupt();
	}

	public void interruptSleep() {
		mainThread.interrupt();
	}

	public void preventFlush() {
		needMoreInput = true;
	}

	public void continueFlush() {
		needMoreInput = false;
	}

	private void waitForInput() {
		while (needMoreInput) {
			Utils.sleep(1000);
		}
	}

	public int nextFlushTime() {
		return Math.max(0, interval - (int) ((System.currentTimeMillis() - sleepStartTime) / 1000));
	}

	public void updateInterval(int interval) {
		this.interval = interval;
		view.updateInterval(interval);
		view.vmInfo.warning.updateInterval(interval);
	}

	public int getInterval() {
		return interval;
	}
}
