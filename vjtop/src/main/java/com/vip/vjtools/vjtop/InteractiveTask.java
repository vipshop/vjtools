package com.vip.vjtools.vjtop;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.vip.vjtools.vjtop.VMDetailView.DetailMode;

final class InteractiveTask implements Runnable {
	private VMDetailView view;
	private Thread mainThread;

	public InteractiveTask(VMDetailView view, Thread mainThread) {
		this.view = view;
		this.mainThread = mainThread;
	}

	public void run() {
		while (true) {
			try {
				String command = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
				if (command.equals("t")) {
					System.err.print(" Input TID for stack:");
					String pidStr = new BufferedReader(new InputStreamReader(System.in)).readLine();
					try {
						long pid = Long.parseLong(pidStr);
						view.printStack(pid);
					} catch (NumberFormatException e) {
						System.err.println(" Wrong number format");
					}
				} else if (command.equals("d")) {
					System.err.print(
							" Input number of Display Mode(1.cpu, 2.syscpu 3.total cpu 4.total syscpu 5.memory 6.total memory): ");
					String mode = new BufferedReader(new InputStreamReader(System.in)).readLine();
					switch (mode) {
						case "1":
							view.setMode(DetailMode.cpu);
							break;
						case "2":
							view.setMode(DetailMode.syscpu);
							break;
						case "3":
							view.setMode(DetailMode.totalcpu);
							break;
						case "4":
							view.setMode(DetailMode.totalsyscpu);
							break;
						case "5":
							view.setMode(DetailMode.memory);
							break;
						case "6":
							view.setMode(DetailMode.totalmemory);
							break;
						default:
							System.err.println(" Wrong option for display mode");
							break;
					}
					System.err.println(" Display mode changed to " + view.getMode() + " for next flush");
				} else if (command.equals("q")) {
					view.exit();
					mainThread.interrupt();
					System.err.println(" Quit.");
					return;
				} else if (command.equals("h")) {
					System.err.println(" t : print stack trace for the thread you choose");
					System.err.println(" d : change threads display mode and ordering");
					System.err.println(" q : quit");
					System.err.println(" h : print help");
				} else if (command.equals("")) {
				} else {
					System.err.println("Unkown command: " + command);
				}

				view.waitForCommand();
			} catch (Exception e) {

			}
		}
	}
}