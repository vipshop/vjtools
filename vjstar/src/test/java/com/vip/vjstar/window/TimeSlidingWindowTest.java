package com.vip.vjstar.window;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author huangyunbin
 *         test for TimeSlidingWindow
 */
public class TimeSlidingWindowTest {
    
    @Test
    public void notOverWindowTest() {
        TimeSlidingWindow slidingWindow = new TimeSlidingWindow(1);
        slidingWindow.add();
        slidingWindow.add();
        long count = slidingWindow.count();
        assertThat(count).isEqualTo(2);
    }
    
    @Test
    public void overWindowTest() throws Exception {
        TimeSlidingWindow slidingWindow = new TimeSlidingWindow(1);
        slidingWindow.add();
        slidingWindow.add();
        Thread.sleep(1000);
        slidingWindow.add();
        long count = slidingWindow.count();
        assertThat(count).isEqualTo(1);
    }
    
    
    @Test
    public void mutiThreadNotOverWindowTest() {
        int threadNum = 100;
        final int num = 100;
        final int circle = 300;
        final TimeSlidingWindow slidingWindow = new TimeSlidingWindow(10);
        final CyclicBarrier barrier = new CyclicBarrier(threadNum);
        final CountDownLatch countDownLatch = new CountDownLatch(threadNum);
        
        for (int i = 0; i < threadNum; i++) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        barrier.await();
                        
                        for (int i = 0; i < circle; i++) {
                            for (int j = 0; j < num; j++) {
                                slidingWindow.add();
                            }
                            
                            TimeUnit.MILLISECONDS.sleep(10);
                        }
                        
                        countDownLatch.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            
        }
        try {
            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long count = slidingWindow.count();
        assertThat(count).isEqualTo(circle * threadNum * num);
    }
    
    
    @Test
    public void mutiThreadOverWindowTest() {
        int threadNum = 100;
        final int num = 1000;
        final int circle = 4;
        final int size = 3;
        final TimeSlidingWindow slidingWindow = new TimeSlidingWindow(size);
        final CyclicBarrier barrier = new CyclicBarrier(threadNum);
        final CountDownLatch countDownLatch = new CountDownLatch(threadNum);
        
        for (int i = 0; i < threadNum; i++) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        barrier.await();
                        
                        for (int i = 0; i < circle; i++) {
                            for (int j = 0; j < num; j++) {
                                slidingWindow.add();
                            }
                            
                            TimeUnit.MILLISECONDS.sleep(1100);
                        }
                        
                        countDownLatch.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            
        }
        try {
            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long count = slidingWindow.count();
        assertThat(count).isEqualTo(size * threadNum * num);
    }
    
    
}