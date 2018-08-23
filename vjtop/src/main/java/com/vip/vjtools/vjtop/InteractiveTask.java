package com.vip.vjtools.vjtop;

import java.io.Console;
import java.io.IOException;
import java.io.PrintStream;

import com.vip.vjtools.vjtop.VMDetailView.DetailMode;

/**
 * 与用户交互动态的控制器
 */
public class InteractiveTask implements Runnable {
	private VJTop app;
	private Console console;
	private PrintStream tty;

	public InteractiveTask(VJTop app) {
		this.app = app;
		tty = System.err;
		console = System.console();
	}

	public boolean inputEnabled() {
		return console != null;
	}

	@Override
	public void run() {
		// background执行时，console为Null
		if (console == null) {

			return;
		}

		while (true) {
			try {
				String command = readLine("");
				if (command == null) {
					break;
				}

				handleCommand(command.toLowerCase());
				if (!app.view.shouldExit()) {
					tty.print(" Input command (h for help):");
				}
			} catch (Exception e) {
				e.printStackTrace(tty);
			}
		}
	}

	public void handleCommand(String command) throws Exception {
		if (command.equals("t") || command.startsWith("t ")) {
			printStacktrace(command);
		} else if (command.equals("s")) {
			printTopStack();
		} else if (command.equals("a")) {
			displayAllThreads();
		} else if (command.equals("m")) {
			changeDisplayMode();
		} else if (command.equals("i") || command.startsWith("i ")) {
			changeInterval(command);
		} else if (command.equals("l") || command.startsWith("l ")) {
			changeThreadLimit(command);
		} else if (command.equals("f")) {
			changeThreadFilter();
		} else if (command.equals("q") || command.equals("quit") || command.equals("exit")) {
			app.exit();
			return;
		} else if (command.equals("h") || command.equals("help")) {
			printHelp();
		} else if (command.equals("")) {
		} else {
			tty.println(" Unkown command: " + command + ", available options:");
			printHelp();
		}
	}

	private void printStacktrace(String command) throws IOException {
		if (app.view.collectingData) {
			tty.println(" Please wait for top threads displayed");
			return;
		}

		app.preventFlush();
		String pidStr;
		if (command.length() == 1) {
			pidStr = readLine(" Input TID:");
		} else {
			pidStr = command.substring(2);
		}

		try {
			long pid = Long.parseLong(pidStr);
			app.view.printStack(pid);
			waitForEnter();
		} catch (NumberFormatException e) {
			tty.println(" Wrong number format for pid");
		} finally {
			app.continueFlush();
		}
	}

	private void printTopStack() throws IOException {
		try {
			app.preventFlush();
			app.view.printTopStack();
			waitForEnter();
		} finally {
			app.continueFlush();
		}
	}

	private void displayAllThreads() throws IOException {
		try {
			app.preventFlush();
			app.view.printAllThreads();
			waitForEnter();
		} finally {
			app.continueFlush();
		}
	}

	private void changeDisplayMode() {
		app.preventFlush();

		String mode = readLine(
				" Input number of Display Mode(1.cpu, 2.syscpu 3.total cpu 4.total syscpu 5.memory 6.total memory, current "
						+ app.view.mode + "): ");
		DetailMode detailMode = DetailMode.parse(mode);
		if (detailMode == null) {
			tty.println(" Wrong option for display mode(1-6)");
		} else if (detailMode == app.view.mode) {
			tty.println(" Nothing be changed");
		} else {
			if (app.view.mode.isCpuMode != detailMode.isCpuMode) {
				app.view.cleanupThreadsHistory();
				app.view.mode = detailMode;
				tty.println(" Display mode changed to " + app.view.mode + " for next flush");
				app.interruptSleep();
			} else {
				app.view.mode = detailMode;
				tty.println(" Display mode changed to " + app.view.mode + " for next flush(" + app.nextFlushTime()
						+ "s later)");
			}
		}
		app.continueFlush();
	}

	private void changeInterval(String command) {
		app.preventFlush();

		String intervalStr;
		if (command.length() == 1) {
			intervalStr = readLine(" Input flush interval seconds(current " + app.getInterval() + "):");
		} else {
			intervalStr = command.substring(2);
		}

		try {
			int interval = Integer.parseInt(intervalStr);
			if (interval != app.getInterval()) {
				app.updateInterval(interval);
				tty.println("Flush interval change to " + interval + " seconds");
				app.interruptSleep();
			} else {
				tty.println(" Nothing be changed");
			}
		} catch (NumberFormatException e) {
			tty.println(" Wrong number format for interval");
		} finally {
			app.continueFlush();
		}
	}

	private void changeThreadLimit(String command) {
		app.preventFlush();

		String threadLimitStr;
		if (command.length() == 1) {
			threadLimitStr = readLine(" Input number of threads to display(current " + app.view.threadLimit + "):");
		} else {
			threadLimitStr = command.substring(2);
		}

		try {
			int threadLimit = Integer.parseInt(threadLimitStr);
			if (threadLimit != app.view.threadLimit) {
				app.view.threadLimit = threadLimit;
				tty.println(" Number of threads to display change to " + threadLimit + " for next flush("
						+ app.nextFlushTime() + "s later)");
			} else {
				tty.println(" Nothing be changed");
			}
		} catch (NumberFormatException e) {
			tty.println(" Wrong number format for number of threads");
		} finally {
			app.continueFlush();
		}
	}

	private void changeThreadFilter() {
		app.preventFlush();

		String threadNameFilter = readLine(" Input thread name filter (current " + app.view.threadNameFilter + "):");

		if (threadNameFilter != null && threadNameFilter.trim().length() == 0) {
			threadNameFilter = null;
		}
		if (threadNameFilter != app.view.threadNameFilter) {
			app.view.threadNameFilter = threadNameFilter;
			tty.println("  thread name filter change to " + threadNameFilter + " for next flush (" + app.nextFlushTime()
					+ "s later)");

		} else {
			tty.println(" Nothing be changed");
		}

		app.continueFlush();
	}

	private void printHelp() throws Exception {
		tty.println(" t [tid]: print stack trace of the thread you choose");
		tty.println(" s : print stack trace of top " + app.view.threadLimit + " threads");
		tty.println(" a : list id and name of all threads");
		tty.println(" m : change threads display mode and ordering");
		tty.println(" i [num]: change flush interval seconds");
		tty.println(" l [num]: change number of display threads");
		tty.println(" f [name]: set thread name filter");
		tty.println(" q : quit");
		tty.println(" h : print help");
		app.preventFlush();
		waitForEnter();
		app.continueFlush();
	}

	private void waitForEnter() {
		readLine(" Please hit <ENTER> to continue...");
	}

	private String readLine(String hints) {
		String result = console.readLine(hints);

		if (result != null) {
			return result.trim();
		}

		return null;
	}
}