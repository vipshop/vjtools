package com.vip.vjtools.vjkit.concurrent.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.vip.vjtools.vjkit.base.annotation.NotNull;
import com.vip.vjtools.vjkit.base.annotation.Nullable;

/**
 * 线程池工具集
 * 
 * 1. 优雅关闭线程池的(via guava)
 * 
 * 2. 创建可自定义线程名的ThreadFactory(via guava)
 * 
 * 3. 防止第三方Runnable未捕捉异常导致线程跑飞
 * 
 * @author calvin
 *
 */
public class ThreadPoolUtil {

	/**
	 * 按照ExecutorService JavaDoc示例代码编写的Graceful Shutdown方法.
	 * 
	 * 先使用shutdown, 停止接收新任务并尝试完成所有已存在任务.
	 * 
	 * 如果1/2超时时间后, 则调用shutdownNow,取消在workQueue中Pending的任务,并中断所有阻塞函数.
	 * 
	 * 如果1/2超时仍然超時，則強制退出.
	 * 
	 * 另对在shutdown时线程本身被调用中断做了处理.
	 * 
	 * 返回线程最后是否被中断.
	 * 
	 * 使用了Guava的工具类
	 * @see MoreExecutors#shutdownAndAwaitTermination(ExecutorService, long, TimeUnit)
	 */
	public static boolean gracefulShutdown(@Nullable ExecutorService threadPool, int shutdownTimeoutMills) {
		return threadPool == null
				|| MoreExecutors.shutdownAndAwaitTermination(threadPool, shutdownTimeoutMills, TimeUnit.MILLISECONDS);
	}

	/**
	 * @see gracefulShutdown
	 */
	public static boolean gracefulShutdown(@Nullable ExecutorService threadPool, int shutdownTimeout,
			TimeUnit timeUnit) {
		return threadPool == null || MoreExecutors.shutdownAndAwaitTermination(threadPool, shutdownTimeout, timeUnit);
	}

	/**
	 * 创建ThreadFactory，使得创建的线程有自己的名字而不是默认的"pool-x-thread-y"
	 * 
	 * 使用了Guava的工具类
	 * 
	 * @see ThreadFactoryBuilder#build()
	 */
	public static ThreadFactory buildThreadFactory(@NotNull String threadNamePrefix) {
		return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build();
	}

	/**
	 * 可设定是否daemon, daemon线程在主线程已执行完毕时, 不会阻塞应用不退出, 而非daemon线程则会阻塞.
	 * 
	 * @see buildThreadFactory
	 */
	public static ThreadFactory buildThreadFactory(@NotNull String threadNamePrefix, @NotNull boolean daemon) {
		return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").setDaemon(daemon).build();
	}

	/**
	 * 防止用户没有捕捉异常导致中断了线程池中的线程, 使得SchedulerService无法继续执行.
	 * 
	 * 在无法控制第三方包的Runnable实现时，调用本函数进行包裹.
	 */
	public static Runnable safeRunnable(@NotNull Runnable runnable) {
		return new SafeRunnable(runnable);
	}

	/**
	 * 保证不会有Exception抛出到线程池的Runnable包裹类，防止用户没有捕捉异常导致中断了线程池中的线程, 使得SchedulerService无法执行. 在无法控制第三方包的Runnalbe实现时，使用本类进行包裹.
	 */
	private static class SafeRunnable implements Runnable {

		private static Logger logger = LoggerFactory.getLogger(SafeRunnable.class);

		private Runnable runnable;

		public SafeRunnable(Runnable runnable) {
			Validate.notNull(runnable);
			this.runnable = runnable;
		}

		@Override
		public void run() {
			try {
				runnable.run();
			} catch (Throwable e) {
				// catch any exception, because the scheduled thread will break if the exception thrown to outside.
				logger.error("Unexpected error occurred in task", e);
			}
		}
	}
}
