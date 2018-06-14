package com.vip.vjtools.vjtop;

import java.io.BufferedReader;
import java.io.InputStreamReader;

final class InteractiveTask implements Runnable {
	VMDetailView view;

	public InteractiveTask(VMDetailView view) {
		this.view = view;
	}

	public void run() {
		while (true) {
			try {
				String command = new BufferedReader(new InputStreamReader(System.in)).readLine();
				if (command.equals("t")) {
					System.err.print("Input TID for stack:");
					String pid = new BufferedReader(new InputStreamReader(System.in)).readLine();
					try {
						long pidLong = Long.parseLong(pid);
						view.printStack(pidLong);
					} catch (NumberFormatException e) {
						System.err.println("Wrong number format");
					}
				} else if (command.equals("h")) {
					System.err.println("t : print stack trace for the thread you choose");
					System.err.println("h : print help");
				} else if (command.trim().equals("")) {
				} else {
					System.err.println("Unkown command: " + command);
				}
				view.waitForCommand();
			} catch (Exception e) {

			}
		}
	}
}