package com.vip.vjtools.vjtop;

import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * VJTop entry point class.
 *
 * - parses program arguments - selects console view - prints header - main "iteration loop"
 *
 * @author paru
 *
 */
public class VJTop {

	public static final String VERSION = "1.0.3";

	public static final int DEFAULT_INTERVAL = 10;

	private static final String CLEAR_TERMINAL_ANSI_CMD = new String(
			new byte[] { (byte) 0x1b, (byte) 0x5b, (byte) 0x32, (byte) 0x4a, (byte) 0x1b, (byte) 0x5b, (byte) 0x48 });

	public VMDetailView view;

	public volatile Integer interval = DEFAULT_INTERVAL;

	private volatile boolean needMoreInput = false;

	private Thread mainThread;

	private int maxIterations = -1;

	private static OptionParser createOptionParser() {
		OptionParser parser = new OptionParser();
		// commmon
		parser.acceptsAll(Arrays.asList(new String[] { "help", "?", "h" }), "shows this help").forHelp();
		parser.acceptsAll(Arrays.asList(new String[] { "n", "iteration" }),
				"vjtop will exit after n output iterations  (defaults to unlimit)").withRequiredArg()
				.ofType(Integer.class);
		parser.acceptsAll(Arrays.asList(new String[] { "d", "interval" }),
				"interval between each output iteration (defaults to 10s)").withRequiredArg().ofType(Integer.class);
		parser.acceptsAll(Arrays.asList(new String[] { "w", "width" }),
				"Number of columns for the console display (defaults to 100)").withRequiredArg().ofType(Integer.class);
		parser.acceptsAll(Arrays.asList(new String[] { "l", "limit" }),
				"Number of threads to display ( default to 10 threads)").withRequiredArg().ofType(Integer.class);

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

			// 2. create vminfo & view
			String pid = parsePid(parser, optionSet);

			VMDetailView.DetailMode displayMode = parseDisplayMode(optionSet);

			Integer width = null;
			if (optionSet.hasArgument("width")) {
				width = (Integer) optionSet.valueOf("width");
			}

			VMInfo vminfo = VMInfo.processNewVM(pid);
			VMDetailView view = new VMDetailView(vminfo, displayMode, width);

			if (optionSet.hasArgument("limit")) {
				Integer limit = (Integer) optionSet.valueOf("limit");
				view.threadLimit = limit;
			}

			// 3. create main application
			VJTop app = new VJTop();
			app.mainThread = Thread.currentThread();
			app.view = view;

			Integer interval = DEFAULT_INTERVAL;
			if (optionSet.hasArgument("interval")) {
				interval = (Integer) (optionSet.valueOf("interval"));
				if (interval < 1) {
					throw new IllegalArgumentException("Interval cannot be set below 1.0");
				}
			}
			app.interval = interval;

			if (optionSet.hasArgument("n")) {
				Integer iterations = (Integer) optionSet.valueOf("n");
				app.maxIterations = iterations;
			}

			// 4. start thread to get user input
			Thread interactiveThread = new Thread(new InteractiveTask(app), "InteractiveThread");
			interactiveThread.setDaemon(true);
			interactiveThread.start();

			// 5. run views
			app.run(view);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	private void run(VMDetailView view) throws Exception {
		try {
			System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(FileDescriptor.out)), false));
			int iterations = 0;
			while (!view.shouldExit()) {

				if (maxIterations > 1 || maxIterations == -1) {
					waitForInput();
					clearTerminal();
				}

				view.printView();

				System.out.flush();

				if (maxIterations > 0 && iterations >= maxIterations) {
					break;
				}

				// 第一次只等待2秒
				int sleepTime = iterations == 0 ? 2 : interval;

				++iterations;

				Utils.sleep(sleepTime * 1000);
			}
		} catch (NoClassDefFoundError e) {
			e.printStackTrace(System.err);

			System.err.println("");
			System.err.println("ERROR: Some JDK classes cannot be found.");
			System.err.println("       Please check if the JAVA_HOME environment variable has been set to a JDK path.");
			System.err.println("");
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

	private static void clearTerminal() {
		if (System.getProperty("os.name").contains("Windows")) {
			// hack
			System.out.printf("%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n");
		} else if (System.getProperty("vjtop.altClear") != null) {
			System.out.print('\f');
		} else {
			System.out.print(CLEAR_TERMINAL_ANSI_CMD);
		}
	}

	public void exit() {
		view.exit();
		mainThread.interrupt();
		System.err.println(" Quit.");
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
}
