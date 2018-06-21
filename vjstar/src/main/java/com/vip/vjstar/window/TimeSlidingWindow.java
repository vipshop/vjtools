package com.vip.vjstar.window;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author huangyunbin
 *         SlidingWindow for time
 */
public class TimeSlidingWindow {
    private final int size;
    private volatile AtomicIntegerArray counts;
    private volatile AtomicLong lastTime = new AtomicLong();
    
    public TimeSlidingWindow(int size) {
        this.size = size;
        counts = new AtomicIntegerArray(size);
    }
    
    /**
     * add request
     */
    public void add() {
        long currentSecond = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime());
        add(currentSecond);
    }
    
    
    void add(long time) {
        clear(time);
        
        int index = (int) (time % size);
        long current = lastTime.longValue();
        
        if (time > current) {
            int oldValue = counts.get(index);
            if (lastTime.compareAndSet(current, time)) {
                counts.getAndAdd(index, 1 - oldValue);
            } else {
                counts.getAndIncrement(index);
            }
            
        } else {
            counts.getAndIncrement(index);
        }
    }
    
    /**
     * clear prev data
     */
    private void clear(long time) {
        if (time < lastTime.get() + size) {
            return;
        }
        int index = (int) (time % size);
        for (int i = 0; i < size; i++) {
            if (i != index) {
                counts.set(i, 0);
            }
        }
    }
    
    /**
     * count request num of time window
     */
    public long count() {
        long currentSecond = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime());
        return count(currentSecond);
    }
    
    long count(long time) {
        if (time >= lastTime.get() + size) {
            return 0;
        }
        long result = 0;
        for (int i = 0; i < size; i++) {
            result += counts.get(i);
        }
        return result;
    }
    
    
}