package com.vip.vjtools.vjmap.utils;

import java.io.PrintStream;

public class ProgressNodifier {
	public long notificationSize;
	public long processingSize;

	private int processingPercent;
	private long chunkSize;
	private long totalSize;

	private PrintStream tty = System.out;

	public ProgressNodifier(long totalSize) {
		this.totalSize = totalSize;
		chunkSize = totalSize / 100;
		notificationSize = chunkSize;
		processingPercent = 0;
		processingSize = 0;
	}

	public void printHead() {
		tty.println("Total live size to process: " + FormatUtils.toFloatUnit(totalSize));
		tty.print(" 0%:");
	}

	public void printProgress() {
		tty.print(".");
		processingPercent++;
		notificationSize += chunkSize;
		if (processingPercent % 20 == 0) {
			tty.print("\n" + processingPercent + "%:");
		}
	}
}
