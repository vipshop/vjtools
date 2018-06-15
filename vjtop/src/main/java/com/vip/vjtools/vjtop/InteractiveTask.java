package com.vip.vjtools.vjtop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import com.vip.vjtools.vjtop.VMDetailView.DetailMode;

/**
 * 与用户交互动态的控制器
 */
public class InteractiveTask implements Runnable {
	private VJTop app;
	private BufferedReader reader;
	private PrintStream tty;

	public InteractiveTask(VJTop app) {
		this.app = app;
		reader = new BufferedReader(new InputStreamReader(System.in));
		tty = System.err;
	}

	public void run() {
		while (true) {
			try {
				String command = reader.readLine();
				if (command == null) {
					return;
				}
				command = command.trim().toLowerCase();

				if (command.equals("t") || (command.startsWith("t "))) {
					printStacktrace(command);
				} else if (command.equals("a")) {
					displayAllThreads();
				} else if (command.equals("m")) {
					changeDisplayMode();
				} else if (command.equals("d")) {
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

				tty.print(" Input command (h for help):");
			} catch (Exception e) {
				e.printStackTrace(tty);
			}
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
			tty.print(" Input TID:");
			pidStr = reader.readLine().trim();
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

	private void displayAllThreads() throws Exception{
		try {
			app.preventFlush();
			app.view.printAllThreads();
			waitForEnter();
		} finally {
			app.continueFlush();
		}

	}

	private void changeDisplayMode() throws IOException {
		app.preventFlush();
		tty.print(
				" Input number of Display Mode(1.cpu, 2.syscpu 3.total cpu 4.total syscpu 5.memory 6.total memory): ");
		String mode = reader.readLine().trim().toLowerCase();
		switch (mode) {
			case "1":
				app.view.mode = DetailMode.cpu;
				break;
			case "2":
				app.view.mode = DetailMode.syscpu;
				break;
			case "3":
				app.view.mode = DetailMode.totalcpu;
				break;
			case "4":
				app.view.mode = DetailMode.totalsyscpu;
				break;
			case "5":
				app.view.mode = DetailMode.memory;
				break;
			case "6":
				app.view.mode = DetailMode.totalmemory;
				break;
			default:
				System.err.println(" Wrong option for display mode(1-6)");
				break;
		}
		tty.println(" Display mode changed to " + app.view.mode + " for next flush");
		app.continueFlush();
	}

	private void changeInterval() throws IOException {
		app.preventFlush();
		tty.print(" Input flush interval seconds:");
		String intervalStr = reader.readLine().trim();
		try {
			int interval = Integer.parseInt(intervalStr);
			app.interval = interval;
			tty.println(" Flush interval changed to " + interval + " seconds for next next flush");
		} catch (NumberFormatException e) {
			tty.println(" Wrong number format for interval");
		} finally {
			app.continueFlush();
		}
	}

	private void changeThreadLimit() throws IOException {
		app.preventFlush();
		tty.print(" Input number of threads to display :");
		String threadLimitStr = reader.readLine().trim();
		try {
			int threadLimit = Integer.parseInt(threadLimitStr);
			app.view.threadLimit = threadLimit;
			tty.println(" Number of threads to display changed to " + threadLimit + " for next flush");
		} catch (NumberFormatException e) {
			tty.println(" Wrong number format for number of threads");
		} finally {
			app.continueFlush();
		}
	}

	private void printHelp() {
		tty.println(" t [tid]: print stack trace for the thread you choose");
		tty.println(" a : list all thread's id and name");
		tty.println(" m : change threads display mode and ordering");
		tty.println(" d : change flush interval seconds");
		tty.println(" l : change number of display threads");
		tty.println(" q : quit");
		tty.println(" h : print help");
		app.preventFlush();
		waitForEnter();
		app.continueFlush();
	}

	private void waitForEnter() {
		tty.println(" Please hit <ENTER> to continue...");
		try {
			reader.readLine();
		} catch (IOException e) {
		}
	}
}