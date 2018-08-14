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

				handleCommand(command);
				if (!app.view.shouldExit()) {
					tty.print(" Input command (h for help):");
				}
			} catch (Exception e) {
				e.printStackTrace(tty);
			}
		}
	}

	public void handleCommand(String command) throws Exception {
		if (command.equals("t") || (command.startsWith("t "))) {
			printStacktrace(command);
		} else if (command.equals("a")) {
			displayAllThreads();
		} else if (command.equals("m")) {
			changeDisplayMode();
		} else if (command.equals("i")) {
			changeInterval();
		} else if (command.equals("l")) {
			changeThreadLimit();
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

	private void displayAllThreads() throws Exception {
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
			app.view.mode = detailMode;
			if (app.nextFlushTime() > 1) {
				tty.println(" Display mode changed to " + app.view.mode + " for next flush (" + app.nextFlushTime()
						+ "s later)");
			}
		}
		app.continueFlush();
	}

	private void changeInterval() {
		app.preventFlush();
		String intervalStr = readLine(" Input flush interval seconds(current " + app.interval + "):");
		try {
			int interval = Integer.parseInt(intervalStr);
			if (interval != app.interval) {
				if (app.nextFlushTime() > 1) {
					tty.println(" Flush interval changed to " + interval + " seconds for next flush ("
							+ app.nextFlushTime() + "s later)");
				}
				app.interval = interval;
			} else {
				tty.println(" Nothing be changed");
			}
		} catch (NumberFormatException e) {
			tty.println(" Wrong number format for interval");
		} finally {
			app.continueFlush();
		}
	}

	private void changeThreadLimit() {
		app.preventFlush();
		String threadLimitStr = readLine(" Input number of threads to display(current " + app.view.threadLimit + "):");
		try {
			int threadLimit = Integer.parseInt(threadLimitStr);
			if (threadLimit != app.view.threadLimit) {
				app.view.threadLimit = threadLimit;
				if (app.nextFlushTime() > 1) {
					tty.println(" Number of threads to display changed to " + threadLimit + " for next flush ("
							+ app.nextFlushTime() + "s later)");
				}
			} else {
				tty.println(" Nothing be changed");
			}
		} catch (NumberFormatException e) {
			tty.println(" Wrong number format for number of threads");
		} finally {
			app.continueFlush();
		}
	}

	private void printHelp() throws Exception {
		tty.println(" t [tid]: print stack trace for the thread you choose");
		tty.println(" a : list all thread's id and name");
		tty.println(" m : change threads display mode and ordering");
		tty.println(" i : change flush interval seconds");
		tty.println(" l : change number of display threads");
		tty.println(" q : quit");
		tty.println(" h : print help");
		app.preventFlush();
		String command = waitForEnter();
		app.continueFlush();
		if (command.length() > 0) {
			handleCommand(command);
		}
	}

	private String waitForEnter() {
		return readLine(" Please hit <ENTER> to continue...");
	}

	private String readLine(String hints) {
		String result = console.readLine(hints);

		if (result != null) {
			return result.trim().toLowerCase();
		}

		return null;
	}
}