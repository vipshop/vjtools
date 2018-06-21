package com.vip.vjstar.window;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author huangyunbin
 *         SlidingWindow for request
 */
public class RequestSlidingWindow {
    
    /**
     * thread safe bitset，a request is a bit
     * success use 1
     * fail use 0
     */
    private final AtomicBitSet bitSet;
    /**
     * window size
     */
    private final int size;
    /**
     * current position
     */
    private volatile AtomicInteger index = new AtomicInteger();
    /**
     * current  request num
     */
    private volatile AtomicInteger capacity = new AtomicInteger();
    
    public RequestSlidingWindow(int size) {
        this.size = size;
        bitSet = new AtomicBitSet(size);
    }
    
    
    public void success() {
        processCapacity();
        setNext(true);
    }
    
    public void fail() {
        processCapacity();
        setNext(false);
    }
    
    private void setNext(boolean flag) {
        int target = index.getAndIncrement() % size;
        bitSet.set(target, flag);
    }
    
    private void processCapacity() {
        if (capacity.get() < size) {
            capacity.getAndIncrement();
        }
    }
    
    
    public long getSucessNum() {
        return bitSet.cardinality();
    }
    
    public long getFailNum() {
        if (capacity.get() >= size) {
            return size - bitSet.cardinality();
        } else {
            return capacity.get() - bitSet.cardinality();
        }
        
    }
}