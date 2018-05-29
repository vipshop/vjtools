package com.vip.vjtools.vjkit.concurrent.threadpool;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.vip.vjtools.vjkit.concurrent.threadpool.QueuableCachedThreadPool.ControllableQueue;

public class ThreadPoolBuilderTest {

	@Test
	public void fixPool() {
		ThreadPoolExecutor singlePool = ThreadPoolBuilder.fixedPool().build();
		assertThat(singlePool.getCorePoolSize()).isEqualTo(1);
		assertThat(singlePool.getMaximumPoolSize()).isEqualTo(1);
		assertThat(singlePool.getQueue()).isInstanceOf(LinkedBlockingQueue.class);
		singlePool.shutdown();

		ThreadPoolExecutor fixPoolWithUnlimitQueue = ThreadPoolBuilder.fixedPool().setPoolSize(10).build();
		assertThat(fixPoolWithUnlimitQueue.getCorePoolSize()).isEqualTo(10);
		assertThat(fixPoolWithUnlimitQueue.getMaximumPoolSize()).isEqualTo(10);
		fixPoolWithUnlimitQueue.shutdown();

		ThreadPoolExecutor fixPoolWithlimitQueue = ThreadPoolBuilder.fixedPool().setPoolSize(10).setQueueSize(100)
				.setThreadFactory(ThreadPoolUtil.buildThreadFactory("kaka")).build();

		assertThat(fixPoolWithlimitQueue.getQueue()).isInstanceOf(ArrayBlockingQueue.class);
		Thread thread = fixPoolWithlimitQueue.getThreadFactory().newThread(new Runnable() {
			@Override
			public void run() {
			}
		});
		assertThat(thread.getName()).startsWith("kaka");

		fixPoolWithlimitQueue.shutdown();

		ThreadPoolExecutor fixPoolWithNamePrefix = ThreadPoolBuilder.fixedPool().setPoolSize(10)
				.setThreadNamePrefix("fixPool").build();
		Thread thread2 = fixPoolWithNamePrefix.getThreadFactory().newThread(new Runnable() {
			@Override
			public void run() {
			}
		});
		assertThat(thread2.getName()).startsWith("fixPool");
		assertThat(thread2.isDaemon()).isFalse();
		fixPoolWithNamePrefix.shutdown();

		ThreadPoolExecutor fixPoolWithNamePrefixAndDaemon = ThreadPoolBuilder.fixedPool().setPoolSize(10)
				.setThreadNamePrefix("fixPoolDaemon").setDaemon(true).build();
		Thread thread3 = fixPoolWithNamePrefixAndDaemon.getThreadFactory().newThread(new Runnable() {
			@Override
			public void run() {
			}
		});
		assertThat(thread3.getName()).startsWith("fixPoolDaemon");
		assertThat(thread3.isDaemon()).isTrue();
		fixPoolWithNamePrefixAndDaemon.shutdown();
	}

	@Test
	public void cachedPool() {
		ThreadPoolExecutor singlePool = ThreadPoolBuilder.cachedPool().build();
		assertThat(singlePool.getCorePoolSize()).isEqualTo(0);
		assertThat(singlePool.getMaximumPoolSize()).isEqualTo(Integer.MAX_VALUE);
		assertThat(singlePool.getKeepAliveTime(TimeUnit.SECONDS)).isEqualTo(10);
		assertThat(singlePool.getQueue()).isInstanceOf(SynchronousQueue.class);
		singlePool.shutdown();

		ThreadPoolExecutor sizeablePool = ThreadPoolBuilder.cachedPool().setMinSize(10).setMaxSize(100)
				.setKeepAliveSecs(20).build();
		assertThat(sizeablePool.getCorePoolSize()).isEqualTo(10);
		assertThat(sizeablePool.getMaximumPoolSize()).isEqualTo(100);
		assertThat(sizeablePool.getKeepAliveTime(TimeUnit.SECONDS)).isEqualTo(20);
		sizeablePool.shutdown();

		ThreadPoolExecutor fixPoolWithNamePrefix = ThreadPoolBuilder.cachedPool().setThreadNamePrefix("cachedPool")
				.build();
		Thread thread = fixPoolWithNamePrefix.getThreadFactory().newThread(new Runnable() {

			@Override
			public void run() {
			}
		});
		assertThat(thread.getName()).startsWith("cachedPool");
		fixPoolWithNamePrefix.shutdown();
	}

	@Test
	public void scheduledPool() {
		ScheduledThreadPoolExecutor singlePool = ThreadPoolBuilder.scheduledPool().build();
		assertThat(singlePool.getCorePoolSize()).isEqualTo(1);
		assertThat(singlePool.getMaximumPoolSize()).isEqualTo(Integer.MAX_VALUE);
		singlePool.shutdown();

		ScheduledThreadPoolExecutor sizeablePool = ThreadPoolBuilder.scheduledPool().setPoolSize(2).build();
		assertThat(sizeablePool.getCorePoolSize()).isEqualTo(2);
		assertThat(sizeablePool.getMaximumPoolSize()).isEqualTo(Integer.MAX_VALUE);
		sizeablePool.shutdown();

		ThreadPoolExecutor fixPoolWithNamePrefix = ThreadPoolBuilder.scheduledPool()
				.setThreadNamePrefix("scheduledPool").build();
		Thread thread = fixPoolWithNamePrefix.getThreadFactory().newThread(new Runnable() {
			@Override
			public void run() {
			}
		});
		assertThat(thread.getName()).startsWith("scheduledPool");
		fixPoolWithNamePrefix.shutdown();
	}

	@Test
	public void quequablePool() {
		ThreadPoolExecutor singlePool = ThreadPoolBuilder.queuableCachedPool().build();
		assertThat(singlePool.getCorePoolSize()).isEqualTo(0);
		assertThat(singlePool.getMaximumPoolSize()).isEqualTo(Integer.MAX_VALUE);
		assertThat(singlePool.getKeepAliveTime(TimeUnit.SECONDS)).isEqualTo(10);
		assertThat(singlePool.getQueue()).isInstanceOf(ControllableQueue.class);
		singlePool.shutdown();

		ThreadPoolExecutor sizeablePool = ThreadPoolBuilder.queuableCachedPool().setMinSize(10).setMaxSize(100)
				.setKeepAliveSecs(20).build();
		assertThat(sizeablePool.getCorePoolSize()).isEqualTo(10);
		assertThat(sizeablePool.getMaximumPoolSize()).isEqualTo(100);
		assertThat(sizeablePool.getKeepAliveTime(TimeUnit.SECONDS)).isEqualTo(20);
		sizeablePool.shutdown();

		ThreadPoolExecutor fixPoolWithNamePrefix = ThreadPoolBuilder.queuableCachedPool()
				.setThreadNamePrefix("queuableCachedPool").build();
		Thread thread = fixPoolWithNamePrefix.getThreadFactory().newThread(new Runnable() {

			@Override
			public void run() {
			}
		});
		assertThat(thread.getName()).startsWith("queuableCachedPool");
		fixPoolWithNamePrefix.shutdown();
	}
}
