package com.vip.vjtools.vjtop;

import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import com.vip.vjtools.vjtop.VMDetailView.DetailMode;
import com.vip.vjtools.vjtop.VMInfo.VMInfoState;
import com.vip.vjtools.vjtop.util.Formats;
import com.vip.vjtools.vjtop.util.Utils;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class VJTop {

	public static final String VERSION = "1.0.6";

	public static final int DEFAULT_INTERVAL = 10;

	public VMDetailView view;

	private volatile Integer interval = DEFAULT_INTERVAL;

	private volatile boolean needMoreInput = false;

	private Thread mainThread;
	private long sleepStartTime;
	private int maxIterations = -1;

	private static OptionParser createOptionParser() {
		OptionParser parser = new OptionParser();
		// commmon
		parser.acceptsAll(Arrays.asList(new String[] { "help", "?", "h" }), "shows this help").forHelp();
		parser.acceptsAll(Arrays.asList(new String[] { "n", "iteration" }),
				"vjtop will exit after n output iterations  (defaults to unlimit)").withRequiredArg()
				.ofType(Integer.class);
		parser.acceptsAll(Arrays.asList(new String[] { "i", "interval", "d" }),
				"interval between each output iteration (defaults to 10s)").withRequiredArg().ofType(Integer.class);
		parser.acceptsAll(Arrays.asList(new String[] { "w", "width" }),
				"Number of columns for the console display (defaults to 100)").withRequiredArg().ofType(Integer.class);
		parser.acceptsAll(Arrays.asList(new String[] { "l", "limit" }),
				"Number of threads to display ( default to 10 threads)").withRequiredArg().ofType(Integer.class);
		parser.acceptsAll(Arrays.asList(new String[] { "f", "filter" }), "Thread name filter ( no default)")
				.withRequiredArg().ofType(String.class);

		parser.acceptsAll(Arrays.asList(new String[] { "j", "jmxurl" }),
				"JMX url like 127.0.0.1:7001 when VM attach is not work").withRequiredArg().ofType(String.class);

		// detail mode
		parser.acceptsAll(Arrays.asList(new String[] { "m", "mode" }),
				"number of thread display mode: \n"
						+ " 1.cpu(default): display thread cpu usage and sort by its delta cpu time\n"
						+ " 2.syscpu: display thread cpu usage and sort by delta syscpu time\n"
						+ " 3.total cpu: display thread cpu usage and sort by total cpu time\n"
						+ " 4.total syscpu: display thread cpu usage and sort by total syscpu time\n"
						+ " 5.memory: display thread memory allocated and sort by delta\n"
						+ " 6.total memory: display thread memory allocated and sort by total")
				.withRequiredArg().ofType(Integer.class);

		parser.acceptsAll(Arrays.asList(new String[] { "o", "output" }),
				"output format: \n" + " console(default): console with warning and flush ascii code\n"
						+ " cleanconsole: console without warning and flush ascii code\n"
						+ " text: plain text like /proc/status for 3rd tools\n")
				.withRequiredArg().ofType(String.class);

		parser.acceptsAll(Arrays.asList(new String[] { "c", "content" }),
				"output format: \n"
						+ " all(default): jvm info and theads info\n jvm: only jvm info\n thread: only thread info\n")
				.withRequiredArg().ofType(String.class);

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
				System.out.println("\n" + Formats.RED_ANSI[0]
						+ "ERROR: Could not attach to process, see the solution in README" + Formats.RED_ANSI[1]);
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
				view.printView();
				if (view.shouldExit()) {
					break;
				}

				System.out.flush();

				if (maxIterations > 0 && iterations >= maxIterations) {
					break;
				}

				// 第一次最多只等待2秒
				int sleepSeconds = (iterations == 0) ? Math.min(2, interval) : interval;

				iterations++;
				sleepStartTime = System.currentTimeMillis();
				Utils.sleep(sleepSeconds * 1000L);
			}
			System.out.println("");
			System.out.flush();
		} catch (NoClassDefFoundError e) {
			e.printStackTrace(System.out);
			System.out.println(Formats.RED_ANSI[0] + "ERROR: Some JDK classes cannot be found." + Formats.RED_ANSI[1]);
			System.out.println("       Please check if the JAVA_HOME environment variable has been set to a JDK path.");
			System.out.println("");
			System.out.flush();
		}
	}

	private static VMDetailView.DetailMode parseDisplayMode(OptionSet optionSet) {
		VMDetailView.DetailMode displayMode = VMDetailView.DetailMode.cpu;
		if (optionSet.hasArgument("mode")) {
			Integer mode = (Integer) optionSet.valueOf("mode");
			displayMode = DetailMode.parse(mode.toString());
		}
		return displayMode;
	}

	private static String parsePid(OptionParser parser, OptionSet optionSet) {
		Integer pid = null;

		// to support PID as non option argument
		if (optionSet.nonOptionArguments().size() > 0) {
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
		view.interval = interval;
	}

	public int getInterval() {
		return interval;
	}
}
