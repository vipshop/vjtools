package com.vip.vjtools.vjmap.utils;

import java.io.PrintStream;

public class ProgressNotifier {
	public long nextNotificationSize;
	public long processingSize;

	private int processingPercent;
	private long onePercentSize;
	private long totalSize;

	private PrintStream tty = System.out;

	private TimeController timeController = new TimeController();

	public ProgressNotifier(long totalSize) {
		this.totalSize = totalSize;
		onePercentSize = totalSize / 100;
		nextNotificationSize = onePercentSize;
		processingPercent = 0;
		processingSize = 0;
	}

	public void printHead() {
		tty.println("Total live size to process: " + FormatUtils.toFloatUnit(totalSize));
		tty.print(" 0%:");
	}

	public void printProgress() {
		timeController.checkTimedOut();
		tty.print(".");
		processingPercent++;
		nextNotificationSize += onePercentSize;
		if (processingPercent % 10 == 0) {
			tty.print("\n" + processingPercent + "%:");
		}
	}
}
