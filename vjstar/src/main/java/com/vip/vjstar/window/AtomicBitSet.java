package com.vip.vjstar.window;

import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * @author huangyunbin
 *         thread safe bitset
 */
public class AtomicBitSet {
    private final AtomicIntegerArray atomicIntegerArray;
    private final int size;
    
    public AtomicBitSet(int size) {
        this.size = size;
        int intLength = (size + 31) / 32;
        atomicIntegerArray = new AtomicIntegerArray(intLength);
    }
    
    public void set(long n, boolean flag) {
        int bit = 1 << n;
        int idx = (int) (n >>> 5);
        while (true) {
            int num = atomicIntegerArray.get(idx);
            int num2;
            if (flag) {
                num2 = num | bit;
            } else {
                num2 = num & ~bit;
            }
            if (num == num2 || atomicIntegerArray.compareAndSet(idx, num, num2)) {
                return;
            }
        }
    }
    
    public boolean get(long n) {
        int bit = 1 << n;
        int idx = (int) (n >>> 5);
        int num = atomicIntegerArray.get(idx);
        return (num & bit) != 0;
    }
    
    public int cardinality() {
        int result = 0;
        for (int i = 0; i < size; i++) {
            if (get(i)) {
                result++;
            }
        }
        return result;
    }
}