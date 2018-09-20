package com.vip.vjtools.vjkit.concurrent.threadpool;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.vjtools.vjkit.concurrent.ThreadDumpper;

/**
 * Abort Policy.
 * 如果线程池已满，退出申请并打印Thread Dump(会有一定的最少间隔，默认为10分钟）
 */
public class AbortPolicyWithReport extends ThreadPoolExecutor.AbortPolicy {

	protected static final Logger logger = LoggerFactory.getLogger(AbortPolicyWithReport.class);

	private final String threadName;

	private ThreadDumpper dummper = new ThreadDumpper();

	public AbortPolicyWithReport(String threadName) {
		this.threadName = threadName;
	}

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
		String msg = String.format(
				"Thread pool is EXHAUSTED!"
						+ " Thread Name: %s, Pool Size: %d (active: %d, core: %d, max: %d, largest: %d), Task: %d (completed: %d),"
						+ " Executor status:(isShutdown:%s, isTerminated:%s, isTerminating:%s)!",
				threadName, e.getPoolSize(), e.getActiveCount(), e.getCorePoolSize(), e.getMaximumPoolSize(),
				e.getLargestPoolSize(), e.getTaskCount(), e.getCompletedTaskCount(), e.isShutdown(), e.isTerminated(),
				e.isTerminating());
		logger.warn(msg);
		dummper.tryThreadDump(null);
		throw new RejectedExecutionException(msg);
	}


}
