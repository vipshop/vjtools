package com.vip.vjtools.vjkit.concurrent.type;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import org.junit.Test;

import com.vip.vjtools.vjkit.concurrent.Concurrents;
import com.vip.vjtools.vjkit.concurrent.ThreadUtil;
import com.vip.vjtools.vjkit.concurrent.type.ThreadLocalContext;
import com.vip.vjtools.vjkit.number.RandomUtil;

public class ThreadLocalContextTest {

	@Test
	public void test() throws InterruptedException {

		final CountDownLatch countdown = Concurrents.countDownLatch(10);
		final CyclicBarrier barrier = Concurrents.cyclicBarrier(10);

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					barrier.await();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ThreadLocalContext.put("myname", Thread.currentThread().getName());
				ThreadUtil.sleep(RandomUtil.nextLong(100, 300));
				System.out.println((String) ThreadLocalContext.get("myname"));
				ThreadLocalContext.reset();
				System.out.println(
						"shoud null for " + Thread.currentThread().getName() + ":" + ThreadLocalContext.get("myname"));
				countdown.countDown();
			}
		};

		for (int i = 0; i < 10; i++) {
			Thread thread = new Thread(runnable);
			thread.start();
		}

		countdown.await();
	}
}
