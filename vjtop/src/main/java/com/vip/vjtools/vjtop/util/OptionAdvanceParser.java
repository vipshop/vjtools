package com.vip.vjtools.vjtop.util;

import java.util.Arrays;

import com.vip.vjtools.vjtop.VJTop;
import com.vip.vjtools.vjtop.VMDetailView.ContentMode;
import com.vip.vjtools.vjtop.VMDetailView.OutputFormat;
import com.vip.vjtools.vjtop.VMDetailView.ThreadInfoMode;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class OptionAdvanceParser {

	private static final int DEFAULT_INTERVAL = 10;

	public static String parsePid(OptionParser parser, OptionSet optionSet) {
		Integer pid = null;

		// to support PID as non option argument
		if (optionSet.nonOptionArguments().size() > 0) {
			pid = Integer.valueOf((String) optionSet.nonOptionArguments().get(0));
		}

		if (pid == null) {
			System.out.println("PID can't be empty !!!");
			VJTop.printHelper(parser);
			System.exit(0);
		}

		return String.valueOf(pid);
	}

	public static OutputFormat parseOutputFormat(OptionSet optionSet) {
		OutputFormat outputFormat = OutputFormat.console;
		if (optionSet.hasArgument("output")) {
			String format = (String) optionSet.valueOf("output");
			if (format.equals("clean")) {
				outputFormat = OutputFormat.cleanConsole;
			} else if (format.equals("text")) {
				outputFormat = OutputFormat.text;
			}
		}

		return outputFormat;
	}


	public static ContentMode parseContentMode(OptionSet optionSet) {
		ContentMode contentMode = ContentMode.all;
		if (optionSet.hasArgument("content")) {
			String format = (String) optionSet.valueOf("content");
			if (format.equals("jvm")) {
				contentMode = ContentMode.jvm;
			} else if (format.equals("thread")) {
				contentMode = ContentMode.thread;
			}
		}

		return contentMode;
	}

	public static ThreadInfoMode parseThreadInfoMode(OptionSet optionSet) {
		ThreadInfoMode threadInfoMode = ThreadInfoMode.cpu;
		if (optionSet.hasArgument("mode")) {
			Integer mode = (Integer) optionSet.valueOf("mode");
			threadInfoMode = ThreadInfoMode.parse(mode.toString());
		}
		return threadInfoMode;
	}

	public static OptionParser createOptionParser() {
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
				"give JMX url like 127.0.0.1:7001 when VM attach doesn't work").withRequiredArg().ofType(String.class);

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
				"output format: \n" + " console(default): console with warning and flush ansi code\n"
						+ " clean: console without warning and flush ansi code\n"
						+ " text: plain text like /proc/status for 3rd tools\n")
				.withRequiredArg().ofType(String.class);

		parser.acceptsAll(Arrays.asList(new String[] { "c", "content" }),
				"output format: \n"
						+ " all(default): jvm info and theads info\n jvm: only jvm info\n thread: only thread info\n")
				.withRequiredArg().ofType(String.class);

		return parser;
	}

	public static Integer parseInterval(OptionSet optionSet) {
		Integer interval = OptionAdvanceParser.DEFAULT_INTERVAL;
		if (optionSet.hasArgument("interval")) {
			interval = (Integer) (optionSet.valueOf("interval"));
			if (interval < 1) {
				throw new IllegalArgumentException("Interval cannot be set below 1.0");
			}
		}
		return interval;
	}
}
