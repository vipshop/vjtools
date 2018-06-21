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
    public void addTest1() {
        TimeSlidingWindow slidingWindow = new TimeSlidingWindow(3);
        slidingWindow.add(1L);
        long count = slidingWindow.count(3L);
        assertThat(count).isEqualTo(1);
    }
    
    
    @Test
    public void addTest2() {
        TimeSlidingWindow slidingWindow = new TimeSlidingWindow(3);
        slidingWindow.add(1L);
        slidingWindow.add(1L);
        long count = slidingWindow.count(3L);
        assertThat(count).isEqualTo(2);
    }
    
    @Test
    public void addTest3() {
        TimeSlidingWindow slidingWindow = new TimeSlidingWindow(3);
        slidingWindow.add(1L);
        slidingWindow.add(1L);
        long count = slidingWindow.count(4L);
        assertThat(count).isEqualTo(0);
    }
    
    
    @Test
    public void addTest4() {
        TimeSlidingWindow slidingWindow = new TimeSlidingWindow(3);
        slidingWindow.add(1L);
        slidingWindow.add(2L);
        slidingWindow.add(3L);
        long count = slidingWindow.count(3L);
        assertThat(count).isEqualTo(3);
    }
    
    
    @Test
    public void addTest5() {
        TimeSlidingWindow slidingWindow = new TimeSlidingWindow(3);
        slidingWindow.add(1L);
        slidingWindow.add(2L);
        slidingWindow.add(3L);
        slidingWindow.add(4L);
        long count = slidingWindow.count(4L);
        assertThat(count).isEqualTo(3);
    }
    
    
    @Test
    public void addTest6() {
        int threadNum = 1000;
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
    
    /**
     * 跨秒的测试
     */
    @Test
    public void addTest7() {
        int threadNum = 100;
        final int num = 10000;
        final int circle = 3;
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
        assertThat(count).isEqualTo(circle * threadNum * num);
    }
    
    
    /**
     * 跨周期的测试
     */
    @Test
    public void addTest8() {
        int threadNum = 100;
        final int num = 1000;
        final int circle = 3;
        final int size = 2;
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
                            
                            TimeUnit.MILLISECONDS.sleep(1000);
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
    
    
    /**
     * 跨周期的测试2
     */
    @Test
    public void addTest9() {
        int threadNum = 100;
        final int num = 1000;
        final int size = 2;
        final TimeSlidingWindow slidingWindow = new TimeSlidingWindow(size);
        final CyclicBarrier barrier = new CyclicBarrier(threadNum);
        final CountDownLatch countDownLatch = new CountDownLatch(threadNum);
        
        for (int i = 0; i < threadNum; i++) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        barrier.await();
                        
                        for (int j = 0; j < num; j++) {
                            slidingWindow.add();
                        }
                        TimeUnit.MILLISECONDS.sleep(900);
                        
                        for (int j = 0; j < num; j++) {
                            slidingWindow.add();
                        }
                        
                        TimeUnit.MILLISECONDS.sleep(2100);
                        
                        for (int j = 0; j < num; j++) {
                            slidingWindow.add();
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
        assertThat(count).isEqualTo(threadNum * num);
    }
    
    
}