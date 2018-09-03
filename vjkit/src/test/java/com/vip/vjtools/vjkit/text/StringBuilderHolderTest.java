package com.vip.vjtools.vjkit.text;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import org.junit.Test;

public class StringBuilderHolderTest {

	@Test
	public void test() throws InterruptedException {

		final CountDownLatch countdown = new CountDownLatch(10);
		final CyclicBarrier barrier = new CyclicBarrier(10);

		Runnable runnable = new Runnable() {

			StringBuilderHolder holder = new StringBuilderHolder(512);

			@Override
			public void run() {
				try {
					barrier.await();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				StringBuilder builder = StringBuilderHolder.getGlobal();
				builder.append(Thread.currentThread().getName() + "-1");
				System.out.println(builder.toString());

				builder = StringBuilderHolder.getGlobal();
				builder.append(Thread.currentThread().getName() + "-2");
				System.out.println(builder.toString());

				StringBuilder builder2 = holder.get();
				builder2.append(Thread.currentThread().getName() + "-11");
				System.out.println(builder2.toString());

				builder2 = holder.get();
				builder2.append(Thread.currentThread().getName() + "-22");
				System.out.println(builder2.toString());

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
