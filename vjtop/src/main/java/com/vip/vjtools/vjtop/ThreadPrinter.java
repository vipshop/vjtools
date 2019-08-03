package com.vip.vjtools.vjtop;

import java.io.IOException;
import java.lang.Thread.State;
import java.lang.management.LockInfo;
import java.lang.management.ThreadInfo;

/**
 * 打印线程名，线程栈信息
 */
public class ThreadPrinter {

	private VMDetailView view;

	public ThreadPrinter(VMDetailView view) {
		this.view = view;
	}

	/**
	 * 打印单条线程的stack strace，会造成停顿，但比获取全部线程的stack trace停顿少
	 */
	public void printStack(long tid) throws IOException {
		System.out.printf("%n Stack trace of thread %d:%n", tid);

		ThreadInfo info = view.vmInfo.getThreadInfo(tid, 20);
		if (info == null) {
			System.err.println(" TID not exist:" + tid);
			return;
		}
		printSingleThread(info);
		System.out.flush();
	}

	/**
	 * 打印所有活跃线程的stack strace，会造成停顿，但比获取全部线程的stack trace停顿少
	 */
	public void printTopStack() throws IOException {
		System.out.printf("%n Stack trace of top %d threads:%n", view.threadLimit);

		ThreadInfo[] infos = view.topThreadInfo.getTopThreadInfo();
		for (ThreadInfo info : infos) {
			if (info == null) {
				continue;
			}
			printSingleThread(info);
		}
		System.out.flush();
	}

	public StackTraceElement[] printSingleThread(ThreadInfo info) {
		StackTraceElement[] trace = info.getStackTrace();
		StringBuilder sb = new StringBuilder(512);

		sb.append(" ").append(info.getThreadId()).append(": \"").append(info.getThreadName()).append("\"");

		if (view.vmInfo.threadContentionMonitoringSupported) {
			sb.append(" (blocked:").append(info.getBlockedCount()).append("/").append(info.getBlockedTime())
					.append("ms, wait:").append(info.getWaitedCount()).append("/").append(info.getWaitedTime())
					.append("ms");
		} else {
			sb.append(" (blocked:").append(info.getBlockedCount()).append(" times, wait:").append(info.getWaitedCount())
					.append(" times");
		}

		if (info.isSuspended()) {
			sb.append(" ,suspended");
		}
		if (info.isInNative()) {
			sb.append(" ,in native");
		}
		sb.append(")\n");

		sb.append("   java.lang.Thread.State: " + info.getThreadState().toString());
		LockInfo lockInfo = info.getLockInfo();
		if (lockInfo != null) {
			sb.append("(on " + lockInfo + ")");
		}
		if (info.getLockOwnerName() != null) {
			sb.append(" owned by " + info.getLockOwnerId() + ":\"" + info.getLockOwnerName() + "\"");
		}
		sb.append("\n");
		for (StackTraceElement traceElement : trace) {
			sb.append("\tat ").append(traceElement).append("\n");
		}

		System.out.print(sb.toString());

		return trace;
	}

	/**
	 * 打印所有线程，只获取名称不获取stack，不造成停顿
	 */
	public void printAllThreads() throws IOException {
		int[] stateCounter = new int[6];

		System.out.println("\n Thread Id and name of all live threads:");

		long tids[] = view.vmInfo.getAllThreadIds();
		ThreadInfo[] threadInfos = view.vmInfo.getThreadInfo(tids);
		for (ThreadInfo info : threadInfos) {
			if (info == null) {
				continue;
			}

			String threadName = info.getThreadName();
			if (view.threadNameFilter != null && !threadName.toLowerCase().contains(view.threadNameFilter)) {
				continue;
			}
			System.out.println(
					" " + info.getThreadId() + "\t: \"" + threadName + "\" (" + info.getThreadState().toString() + ")");
			stateCounter[info.getThreadState().ordinal()]++;
		}

		StringBuilder statesSummary = new StringBuilder(" Summary: ");
		for (State state : State.values()) {
			statesSummary.append(state.toString()).append(':').append(stateCounter[state.ordinal()]).append("  ");
		}
		System.out.println(statesSummary.append("\n").toString());

		if (view.threadNameFilter != null) {
			System.out.println(" Thread name filter is:" + view.threadNameFilter);
		}
		System.out.flush();
	}

	public void printBlockedThreads() throws IOException {
		System.out.println("\n Stack trace of blocked threads:");
		int counter = 0;
		ThreadInfo[] threadInfos = view.vmInfo.getAllThreadInfo();
		for (ThreadInfo info : threadInfos) {
			if (info == null) {
				continue;
			}

			String threadName = info.getThreadName();

			if (Thread.State.BLOCKED.equals(info.getThreadState())
					&& (view.threadNameFilter == null || threadName.toLowerCase().contains(view.threadNameFilter))) {
				printSingleThread(info);
				counter++;
			}
		}

		System.out.println(" Total " + counter + " blocked threads");

		if (view.threadNameFilter != null) {
			System.out.println(" Thread name filter is:" + view.threadNameFilter);
		}
		System.out.flush();
	}
}
