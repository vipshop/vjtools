package com.vip.vjtools.vjkit.concurrent.threadpool;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * From Tomcat 8.5.6, 传统的FixedThreadPool有Queue但线程数量不变，而CachedThreadPool线程数可变但没有Queue
 * 
 * Tomcat的线程池，通过控制TaskQueue，线程数，但线程数到达最大时会进入Queue中.
 * 
 * 代码从Tomcat复制，主要修改包括：
 * 
 * 1. 删除定期重启线程避免内存泄漏的功能，
 * 
 * 2. TaskQueue中可能3次有锁的读取线程数量，改为只读取1次，这把锁也是这个实现里的唯一遗憾了。
 * 
 * https://github.com/apache/tomcat/blob/trunk/java/org/apache/tomcat/util/threads/ThreadPoolExecutor.java
 */
public final class QueuableCachedThreadPool extends java.util.concurrent.ThreadPoolExecutor {

	/**
	 * The number of tasks submitted but not yet finished. This includes tasks in the queue and tasks that have been
	 * handed to a worker thread but the latter did not start executing the task yet. This number is always greater or
	 * equal to {@link #getActiveCount()}.
	 */
	private final AtomicInteger submittedCount = new AtomicInteger(0);

	public QueuableCachedThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			ControllableQueue workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
		workQueue.setParent(this);
		prestartAllCoreThreads(); // NOSOANR
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		submittedCount.decrementAndGet();
	}

	public int getSubmittedCount() {
		return submittedCount.get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute(Runnable command) {
		execute(command, 0, TimeUnit.MILLISECONDS);
	}

	/**
	 * Executes the given command at some time in the future. The command may execute in a new thread, in a pooled
	 * thread, or in the calling thread, at the discretion of the <tt>Executor</tt> implementation. If no threads are
	 * available, it will be added to the work queue. If the work queue is full, the system will wait for the specified
	 * time and it throw a RejectedExecutionException if the queue is still full after that.
	 *
	 * @param command the runnable task
	 * @param timeout A timeout for the completion of the task
	 * @param unit The timeout time unit
	 * @throws RejectedExecutionException if this task cannot be accepted for execution - the queue is full
	 * @throws NullPointerException if command or unit is null
	 */
	public void execute(Runnable command, long timeout, TimeUnit unit) {
		submittedCount.incrementAndGet();
		try {
			super.execute(command);
		} catch (RejectedExecutionException rx) { // NOSONAR
			// not to re-throw this exception because this is only used to find out whether the pool is full, not for a
			// exception purpose
			final ControllableQueue queue = (ControllableQueue) super.getQueue();
			try {
				if (!queue.force(command, timeout, unit)) {
					submittedCount.decrementAndGet();
					throw new RejectedExecutionException("Queue capacity is full.");
				}
			} catch (InterruptedException ignore) {
				submittedCount.decrementAndGet();
				throw new RejectedExecutionException(ignore);
			}
		}
	}

	/**
	 * https://github.com/apache/tomcat/blob/trunk/java/org/apache/tomcat/util/threads/TaskQueue.java
	 */
	protected static class ControllableQueue extends LinkedBlockingQueue<Runnable> {

		private static final long serialVersionUID = 5044057462066661171L;
		private transient volatile QueuableCachedThreadPool parent = null;

		public ControllableQueue(int capacity) {
			super(capacity);
		}

		public void setParent(QueuableCachedThreadPool tp) {
			parent = tp;
		}

		public boolean force(Runnable o) {
			if (parent.isShutdown()) {
				throw new RejectedExecutionException("Executor not running, can't force a command into the queue");
			}
			return super.offer(o); // forces the item onto the queue, to be used if the task is rejected
		}

		public boolean force(Runnable o, long timeout, TimeUnit unit) throws InterruptedException {
			if (parent.isShutdown()) {
				throw new RejectedExecutionException("Executor not running, can't force a command into the queue");
			}
			return super.offer(o, timeout, unit); // forces the item onto the queue, to be used if the task is rejected
		}

		@Override
		public boolean offer(Runnable o) {
			// springside: threadPool.getPoolSize() 是个有锁的操作，所以尽量减少

			int currentPoolSize = parent.getPoolSize();

			// we are maxed out on threads, simply queue the object
			if (currentPoolSize >= parent.getMaximumPoolSize()) {
				return super.offer(o);
			}
			// we have idle threads, just add it to the queue
			if (parent.getSubmittedCount() < currentPoolSize) {
				return super.offer(o);
			}
			// if we have less threads than maximum force creation of a new thread
			if (currentPoolSize < parent.getMaximumPoolSize()) {
				return false;
			}
			// if we reached here, we need to add it to the queue
			return super.offer(o);
		}
	}
}
