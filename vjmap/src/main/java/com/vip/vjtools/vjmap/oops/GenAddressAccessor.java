package com.vip.vjtools.vjmap.oops;

import java.io.PrintStream;

import sun.jvm.hotspot.gc_implementation.parallelScavenge.PSOldGen;
import sun.jvm.hotspot.gc_implementation.parallelScavenge.PSYoungGen;
import sun.jvm.hotspot.gc_interface.CollectedHeap;
import sun.jvm.hotspot.memory.ConcurrentMarkSweepGeneration;
import sun.jvm.hotspot.memory.DefNewGeneration;

/**
 * 打印各区地址
 */
public class GenAddressAccessor {

	private PrintStream tty = System.out;

	public void printHeapAddress() {
		CollectedHeap heap = HeapUtils.getHeap();

		if (HeapUtils.isCMSGC(heap)) {
			DefNewGeneration youngGen = HeapUtils.getYoungGenForCMS(heap);
			youngGen.printOn(tty);
			tty.println("");
			ConcurrentMarkSweepGeneration cmsGen = HeapUtils.getOldGenForCMS(heap);
			cmsGen.printOn(tty);
		} else if (HeapUtils.isParallelGC(heap)) {
			// Parallel GC
			PSYoungGen psYoung = HeapUtils.getYongGenForPar(heap);
			psYoung.printOn(tty);
			tty.println("");
			PSOldGen oldgen = HeapUtils.getOldGenForPar(heap);
			oldgen.printOn(tty);
		} else {
			throw new IllegalArgumentException("Unsupport heap:" + heap.getClass().getName());
		}
	}
}
