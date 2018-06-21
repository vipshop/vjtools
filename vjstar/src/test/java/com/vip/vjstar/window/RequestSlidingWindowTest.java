package com.vip.vjstar.window;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author huangyunbin
 *         test for RequestSlidingWindow
 */
public class RequestSlidingWindowTest {
    
    
    @Test
    public void addTest1() {
        RequestSlidingWindow slidingWindow = new RequestSlidingWindow(1000);
        slidingWindow.success();
        slidingWindow.success();
        slidingWindow.fail();
        slidingWindow.success();
        slidingWindow.fail();
        assertThat(slidingWindow.getSucessNum()).isEqualTo(3);
        assertThat(slidingWindow.getFailNum()).isEqualTo(2);
    }
    
    @Test
    public void addTest2() {
        RequestSlidingWindow slidingWindow = new RequestSlidingWindow(3);
        slidingWindow.success();
        slidingWindow.fail();
        slidingWindow.success();
        slidingWindow.fail();
        slidingWindow.success();
        slidingWindow.fail();
        assertThat(slidingWindow.getSucessNum()).isEqualTo(1);
        assertThat(slidingWindow.getFailNum()).isEqualTo(2);
    }
    
    
    @Test
    public void addTest4() {
        RequestSlidingWindow slidingWindow = new RequestSlidingWindow(3);
        slidingWindow.fail();
        slidingWindow.fail();
        slidingWindow.fail();
        slidingWindow.success();
        slidingWindow.success();
        slidingWindow.success();
        assertThat(slidingWindow.getSucessNum()).isEqualTo(3);
        assertThat(slidingWindow.getFailNum()).isEqualTo(0);
    }
    
    
    @Test
    public void addTest5() {
        RequestSlidingWindow slidingWindow = new RequestSlidingWindow(3);
        slidingWindow.success();
        slidingWindow.success();
        slidingWindow.success();
        slidingWindow.fail();
        slidingWindow.fail();
        slidingWindow.fail();
        assertThat(slidingWindow.getSucessNum()).isEqualTo(0);
        assertThat(slidingWindow.getFailNum()).isEqualTo(3);
    }
    
    @Test
    public void addTest6() throws Exception {
        int threadNum = 100;
        final int num = 1000;
        final RequestSlidingWindow slidingWindow = new RequestSlidingWindow(threadNum * num * 2);
        final CyclicBarrier barrier = new CyclicBarrier(threadNum);
        final CountDownLatch countDownLatch = new CountDownLatch(threadNum);
        
        for (int i = 0; i < threadNum; i++) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        barrier.await();
                        
                        for (int j = 0; j < num; j++) {
                            slidingWindow.fail();
                            slidingWindow.success();
                        }
                        
                        countDownLatch.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            
        }
        countDownLatch.await();
        
        assertThat(slidingWindow.getSucessNum()).isEqualTo(threadNum * num);
        assertThat(slidingWindow.getFailNum()).isEqualTo(threadNum * num);
    }
    
    @Test
    public void addTest7() throws Exception {
        int threadNum = 100;
        final int num = 1000;
        final AtomicInteger index = new AtomicInteger();
        final RequestSlidingWindow slidingWindow = new RequestSlidingWindow(threadNum * num);
        final CyclicBarrier barrier = new CyclicBarrier(threadNum);
        final CountDownLatch countDownLatch = new CountDownLatch(threadNum);
        
        for (int i = 0; i < threadNum; i++) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        barrier.await();
                        for (int j = 0; j < num; j++) {
                            if (index.getAndIncrement() % 2 == 0) {
                                slidingWindow.success();
                            } else {
                                slidingWindow.fail();
                            }
                            
                        }
                        countDownLatch.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            
        }
        countDownLatch.await();
        
        assertThat(slidingWindow.getSucessNum()).isEqualTo(threadNum * num / 2);
        assertThat(slidingWindow.getFailNum()).isEqualTo(threadNum * num / 2);
    }
    
    
    @Test
    public void addTest8() throws Exception {
        int threadNum = 100;
        final int num = 1000;
        final int size = 1000;
        final AtomicInteger index = new AtomicInteger();
        final RequestSlidingWindow slidingWindow = new RequestSlidingWindow(size);
        final CyclicBarrier barrier = new CyclicBarrier(threadNum);
        final CountDownLatch countDownLatch = new CountDownLatch(threadNum);
        
        for (int i = 0; i < threadNum; i++) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        barrier.await();
                        for (int j = 0; j < num; j++) {
                            if (index.getAndIncrement() % 2 == 0) {
                                slidingWindow.success();
                            } else {
                                slidingWindow.fail();
                            }
                            
                        }
                        countDownLatch.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            
        }
        countDownLatch.await();
        
        assertThat(slidingWindow.getSucessNum()).isEqualTo(size / 2);
        assertThat(slidingWindow.getFailNum()).isEqualTo(size / 2);
    }
    
}