package com.vip.vjtools.vjmap.oops;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.vip.vjtools.vjmap.ClassStats;

import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.debugger.AddressException;
import sun.jvm.hotspot.debugger.OopHandle;
import sun.jvm.hotspot.gc_implementation.parallelScavenge.PSOldGen;
import sun.jvm.hotspot.gc_implementation.parallelScavenge.PSYoungGen;
import sun.jvm.hotspot.gc_implementation.parallelScavenge.ParallelScavengeHeap;
import sun.jvm.hotspot.gc_implementation.shared.MutableSpace;
import sun.jvm.hotspot.gc_interface.CollectedHeap;
import sun.jvm.hotspot.memory.CMSCollector;
import sun.jvm.hotspot.memory.CompactibleFreeListSpace;
import sun.jvm.hotspot.memory.ConcurrentMarkSweepGeneration;
import sun.jvm.hotspot.memory.ContiguousSpace;
import sun.jvm.hotspot.memory.DefNewGeneration;
import sun.jvm.hotspot.memory.GenCollectedHeap;
import sun.jvm.hotspot.memory.MemRegion;
import sun.jvm.hotspot.oops.Klass;
import sun.jvm.hotspot.oops.ObjectHeap;
import sun.jvm.hotspot.oops.Oop;
import sun.jvm.hotspot.oops.UnknownOopException;
import sun.jvm.hotspot.runtime.VM;

/**
 * 使用主动访问堆的方式，暂时只支持CMS
 * 
 * 迭代分区的代码，copy from sun.jvm.hotspot.oops.ObjectHeap.iterateLiveRegions()
 */
public class HistogramHeapAccessor {

	private static final int PROCERSSING_DOT_SIZE = 10000;
	private HashMap<Klass, ClassStats> classStatsMap;
	private CollectedHeap heap;
	private ObjectHeap objectHeap;
	private PrintStream tty;

	public HistogramHeapAccessor() {
		this.classStatsMap = new HashMap<Klass, ClassStats>(2048, 0.2f);
		heap = VM.getVM().getUniverse().heap();
		objectHeap = VM.getVM().getObjectHeap();
		tty = System.err;
	}

	/**
	 * 统计Survivor区对象统计信息
	 */
	public List<ClassStats> dumpSurvivor(int minAge) {
		// 获取Survivor区边界
		Address fromBottom = null;
		Address fromTop = null;
		if (heap instanceof GenCollectedHeap) {
			//CMS GC
			DefNewGeneration youngGen = (DefNewGeneration) ((GenCollectedHeap) heap).getGen(0);
			ContiguousSpace from = youngGen.from();
			fromBottom = from.bottom();
			fromTop = from.top();
			
			from.printOn(tty);
			tty.println("");
		} else if (heap instanceof ParallelScavengeHeap) {
			//Parallel GC
			PSYoungGen psYoung = ((ParallelScavengeHeap) heap).youngGen();
			MutableSpace from = psYoung.fromSpace();
			fromBottom = from.bottom();
			fromTop = from.top();
			
			from.printOn(tty);
			tty.println("");
		} else {
			throw new IllegalArgumentException("Only Support CMS and Parallel GC. Unsupport heap:" + heap.getClass().getName());
		}

		OopHandle handle = fromBottom.addOffsetToAsOopHandle(0);
		int processingObject = 0;

		while (handle.lessThan(fromTop)) {
			Oop obj = null;
			try {
				obj = objectHeap.newOop(handle);
			} catch (UnknownOopException ex) {
				// ok
			}

			if (obj == null) {
				throw new UnknownOopException();
			}
			
			long objectSize = obj.getObjectSize();
			handle = handle.addOffsetToAsOopHandle(objectSize);
			
			Klass klass = obj.getKlass();
			if (klass == null || obj.getMark().age() < minAge) {
				continue;
			}

			ClassStats stats = getClassStats(klass);
			stats.survivorCount++;
			stats.survivorSize += objectSize;

			if ((processingObject++) == PROCERSSING_DOT_SIZE) {
				tty.print('.');
				processingObject = 0;
			}
		}

		return getClassStatsList();
	}

	/**
	 * 统计CMS OldGen的对象信息
	 */
	public List<ClassStats> dumpCms() {
		if (!(heap instanceof GenCollectedHeap)) {
			throw new IllegalArgumentException("Only Support CMS GC. Unsupport heap:" + heap.getClass().getName());
		}

		ConcurrentMarkSweepGeneration cmsGen = (ConcurrentMarkSweepGeneration) ((GenCollectedHeap) heap).getGen(1);
		CompactibleFreeListSpace cmsSpace = cmsGen.cmsSpace();
		CMSCollector cmsCollector = cmsSpace.collector();
		
		cmsGen.printOn(tty);
		tty.println("");
		
		tty.println("Getting live regions.");
		long start = System.currentTimeMillis();
		List liveRegions = cmsSpace.getLiveRegions();
		int liveRegionsSize = liveRegions.size();
		tty.printf("OldGen has %d live regions, took %.1fs. %n", liveRegionsSize,
				(System.currentTimeMillis() - start) / 1000d);

		int processingObject = 0;

		for (int i = 0; i < liveRegionsSize; i++) {
			MemRegion region = (MemRegion) liveRegions.get(i);
			Address bottom = region.start();
			Address top = region.end();

			try {
				OopHandle handle = bottom.addOffsetToAsOopHandle(0L);
				while (handle.lessThan(top)) {
					Oop obj = null;

					try {
						obj = objectHeap.newOop(handle);
					} catch (UnknownOopException ignored) {
						// ignored
					}

					if (obj == null) {
						long size = cmsCollector.blockSizeUsingPrintezisBits(handle);
						if (size <= 0L) {
							throw new UnknownOopException();
						}

						handle = handle.addOffsetToAsOopHandle(CompactibleFreeListSpace.adjustObjectSizeInBytes(size));
						continue;
					}

					long objectSize = obj.getObjectSize();

					Klass klass = obj.getKlass();
					if (klass == null) {
						handle = handle
								.addOffsetToAsOopHandle(CompactibleFreeListSpace.adjustObjectSizeInBytes(objectSize));
						continue;
					}

					ClassStats stats = getClassStats(klass);

					stats.oldCount++;
					stats.oldSize += objectSize;

					if ((processingObject++) == PROCERSSING_DOT_SIZE) {
						tty.print(".");
						processingObject = 0;
					}

					handle = handle
							.addOffsetToAsOopHandle(CompactibleFreeListSpace.adjustObjectSizeInBytes(objectSize));
				}
			} catch (AddressException e) {
			} catch (UnknownOopException e) {
			}
		}

		return getClassStatsList();
	}

	public void printHeapAddress() {
		if (heap instanceof GenCollectedHeap) {
			// CMS GC
			DefNewGeneration youngGen = (DefNewGeneration) ((GenCollectedHeap) heap).getGen(0);
			youngGen.printOn(tty);
			tty.println("");
			ConcurrentMarkSweepGeneration cmsGen = (ConcurrentMarkSweepGeneration) ((GenCollectedHeap) heap).getGen(1);
			cmsGen.printOn(tty);
		} else if (heap instanceof ParallelScavengeHeap) {
			// Parallel GC
			PSYoungGen psYoung = ((ParallelScavengeHeap) heap).youngGen();
			psYoung.printOn(tty);
			tty.println("");
			PSOldGen oldgen = ((ParallelScavengeHeap) heap).oldGen();
			oldgen.printOn(tty);
		} else {
			throw new IllegalArgumentException("Unsupport heap:" + heap.getClass().getName());
		}
	}

	private List<ClassStats> getClassStatsList() {
		List<ClassStats> list = new ArrayList<ClassStats>(classStatsMap.size());
		list.addAll(classStatsMap.values());
		return list;
	}

	private ClassStats getClassStats(Klass klass) {
		ClassStats stats = this.classStatsMap.get(klass);
		if (stats == null) {
			stats = new ClassStats(klass);
			this.classStatsMap.put(klass, stats);
		}
		return stats;
	}
}
