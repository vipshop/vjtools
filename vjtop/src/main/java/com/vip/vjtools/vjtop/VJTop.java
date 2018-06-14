package com.vip.vjtools.vjtop;

import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

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


	public static final String VERSION = "1.0.0";

	public static final double DEFAULT_DELAY = 10.0;

	private final static String CLEAR_TERMINAL_ANSI_CMD = new String(
			new byte[]{(byte) 0x1b, (byte) 0x5b, (byte) 0x32, (byte) 0x4a, (byte) 0x1b, (byte) 0x5b, (byte) 0x48});

	private static Logger logger;

	private Double delay_ = -1d;

	private int maxIterations_ = -1;

	private static OptionParser createOptionParser() {
		OptionParser parser = new OptionParser();
		// commmon
		parser.acceptsAll(Arrays.asList(new String[]{"help", "?", "h"}), "shows this help").forHelp();
		parser.acceptsAll(Arrays.asList(new String[]{"n", "iteration"}),
				"vjtop will exit after n output iterations  (defaults to unlimit)").withRequiredArg()
				.ofType(Integer.class);
		parser.acceptsAll(Arrays.asList(new String[]{"d", "delay"}),
				"delay between each output iteration (defaults to 10s)").withRequiredArg().ofType(Double.class);
		parser.acceptsAll(Arrays.asList(new String[]{"v", "verbose"}), "verbose mode");
		parser.acceptsAll(Arrays.asList(new String[]{"w", "width"}),
				"Number of columns for the console display (defaults to 100)").withRequiredArg().ofType(Integer.class);
		parser.acceptsAll(Arrays.asList(new String[]{"l", "limit"}),
				"Number of rows for the console display ( default to 10 threads)").withRequiredArg()
				.ofType(Integer.class);

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
			// 1. create logger
			Locale.setDefault(Locale.US);
			logger = Logger.getLogger("vjtop");

			// 2. create option parser
			OptionParser parser = createOptionParser();
			OptionSet optionSet = parser.parse(args);

			if (optionSet.has("help")) {
				printHelper(parser);
				System.exit(0);
			}

			// 3. create view
			String pid = parsePid(parser, optionSet);

			VMDetailView.DetailMode displayMode = parseDisplayMode(optionSet);

			Integer width = null;
			if (optionSet.hasArgument("width")) {
				width = (Integer) optionSet.valueOf("width");
			}

			VMDetailView view = new VMDetailView(pid, displayMode, width);

			if (optionSet.hasArgument("limit")) {
				Integer limit = (Integer) optionSet.valueOf("limit");
				view.setThreadLimit(limit);
			}

			// 4. create main application
			VJTop vjtop = new VJTop();
			double delay = DEFAULT_DELAY;
			if (optionSet.hasArgument("delay")) {
				delay = (Double) (optionSet.valueOf("delay"));
				if (delay < 1d) {
					throw new IllegalArgumentException("Delay cannot be set below 1.0");
				}
			}
			view.setDelay(delay);
			vjtop.setDelay(delay);

			if (optionSet.hasArgument("n")) {
				Integer iterations = (Integer) optionSet.valueOf("n");
				vjtop.setMaxIterations(iterations);
			}

			if (optionSet.has("verbose")) {
				fineLogging();
				logger.setLevel(Level.ALL);
				logger.fine("Verbosity mode.");
			}

			// 5. start thread to get user input
			Thread interactiveThread = new Thread(new InteractiveTask(view, Thread.currentThread()));
			interactiveThread.setDaemon(true);
			interactiveThread.start();

			// 6. run views
			vjtop.run(view);

		} catch (Exception e) {
			e.printStackTrace();
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
			System.out.println("vjtop - java monitoring for the command-line");
			System.out.println("Usage: vjtop.sh [options...] <PID>");
			System.out.println("");
			parser.printHelpOn(System.out);
		} catch (IOException ignored) {

		}
	}

	private static void fineLogging() {
		// get the top Logger:
		Logger topLogger = java.util.logging.Logger.getLogger("");

		// Handler for console (reuse it if it already exists)
		Handler consoleHandler = null;
		// see if there is already a console handler
		for (Handler handler : topLogger.getHandlers()) {
			if (handler instanceof ConsoleHandler) {
				// found the console handler
				consoleHandler = handler;
				break;
			}
		}

		if (consoleHandler == null) {
			// there was no console handler found, create a new one
			consoleHandler = new ConsoleHandler();
			topLogger.addHandler(consoleHandler);
		}
		// set the console handler to fine:
		consoleHandler.setLevel(java.util.logging.Level.FINEST);
	}

	private void run(VMDetailView view) throws Exception {
		try {
			System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(FileDescriptor.out)), false));
			int iterations = 0;
			while (!view.shouldExit()) {

				if (maxIterations_ > 1 || maxIterations_ == -1) {
					clearTerminal();
				}

				view.printView();

				System.out.flush();

				if (maxIterations_ > 0 && iterations >= maxIterations_) {
					break;
				}

				++iterations;
				view.sleep((long) (delay_ * 1000));
			}
		} catch (NoClassDefFoundError e) {
			e.printStackTrace(System.err);

			System.err.println("");
			System.err.println("ERROR: Some JDK classes cannot be found.");
			System.err.println("       Please check if the JAVA_HOME environment variable has been set to a JDK path.");
			System.err.println("");
		}
	}

	private void clearTerminal() {
		if (System.getProperty("os.name").contains("Windows")) {
			// hack
			System.out.printf("%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n%n");
		} else if (System.getProperty("vjtop.altClear") != null) {
			System.out.print('\f');
		} else {
			System.out.print(CLEAR_TERMINAL_ANSI_CMD);
		}
	}

	public void setDelay(Double delay) {
		delay_ = delay;
	}

	public void setMaxIterations(int iterations) {
		maxIterations_ = iterations;
	}
}
