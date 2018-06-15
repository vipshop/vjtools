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
				String command = reader.readLine().trim().toLowerCase();
				if (command.equals("t")) {
					printStacktrace();
				} else if (command.equals("d")) {
					changeDisplayMode();
				} else if (command.equals("q")) {
					app.exit();
					return;
				} else if (command.equalsIgnoreCase("h") || command.equalsIgnoreCase("help")) {
					printHelp();
				} else if (command.equals("")) {
				} else {
					tty.println("Unkown command: " + command);
					printHelp();
				}

				tty.print(" Input command (h for help):");
			} catch (Exception e) {
				e.printStackTrace(tty);
			}
		}
	}

	private void printStacktrace() throws IOException {
		app.needMoreInput = true;
		tty.print(" Input TID for stack:");
		String pidStr = reader.readLine();
		try {
			long pid = Long.parseLong(pidStr);
			app.view.printStack(pid);
		} catch (NumberFormatException e) {
			tty.println(" Wrong number format for pid");
		} finally {
			app.needMoreInput = false;
		}
	}

	private void changeDisplayMode() throws IOException {
		app.needMoreInput = true;
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
		app.needMoreInput = false;
	}

	private void printHelp() {
		tty.println(" t : print stack trace for the thread you choose");
		tty.println(" d : change threads display mode and ordering");
		tty.println(" q : quit");
		tty.println(" h : print help");
	}
}