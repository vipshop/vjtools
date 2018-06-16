package com.vip.vjtools.vjmap.oops;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;

import com.vip.vjtools.vjmap.ClassStats;

import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.debugger.AddressException;
import sun.jvm.hotspot.debugger.OopHandle;
import sun.jvm.hotspot.gc_interface.CollectedHeap;
import sun.jvm.hotspot.memory.CMSCollector;
import sun.jvm.hotspot.memory.CompactibleFreeListSpace;
import sun.jvm.hotspot.memory.ConcurrentMarkSweepGeneration;
import sun.jvm.hotspot.memory.MemRegion;
import sun.jvm.hotspot.oops.Klass;
import sun.jvm.hotspot.oops.ObjectHeap;
import sun.jvm.hotspot.oops.Oop;
import sun.jvm.hotspot.oops.UnknownOopException;

/**
 * 使用主动访问堆的方式统计OldGen的对象信息, only support CMS GC.
 * 
 * 迭代分区的代码，copy from sun.jvm.hotspot.oops.ObjectHeap.iterateLiveRegions()
 */
public class OldgenAccessor {

	public List<ClassStats> dump() {

		HashMap<Klass, ClassStats> classStatsMap = new HashMap<Klass, ClassStats>(2048, 0.2f);
		CollectedHeap heap = HeapUtils.getHeap();
		ObjectHeap objectHeap = HeapUtils.getObjectHeap();
		PrintStream tty = System.err;

		if (!HeapUtils.isCMSGC(heap)) {
			throw new IllegalArgumentException("Only support CMS GC. Unsupport heap:" + heap.getClass().getName());
		}

		ConcurrentMarkSweepGeneration cmsGen = HeapUtils.getOldGenForCMS(heap);
		CompactibleFreeListSpace cmsSpace = cmsGen.cmsSpace();
		CMSCollector cmsCollector = cmsSpace.collector();

		cmsGen.printOn(tty);
		tty.println("");

		tty.print("Getting live regions...");
		List liveRegions = cmsSpace.getLiveRegions();
		int liveRegionsSize = liveRegions.size();
		tty.printf("%d live regions. %n", liveRegionsSize);

		for (int i = 0; i < liveRegionsSize; i++) {
			MemRegion region = (MemRegion) liveRegions.get(i);
			Address bottom = region.start();
			Address top = region.end();

			try {
				OopHandle handle = bottom.addOffsetToAsOopHandle(0L);
				while (handle.lessThan(top)) {
					HeapUtils.printDot();

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

					ClassStats stats = HeapUtils.getClassStats(klass, classStatsMap);

					stats.oldCount++;
					stats.oldSize += objectSize;

					handle = handle
							.addOffsetToAsOopHandle(CompactibleFreeListSpace.adjustObjectSizeInBytes(objectSize));
				}
			} catch (AddressException e) {
			} catch (UnknownOopException e) {
			}
		}

		return HeapUtils.getClassStatsList(classStatsMap);
	}
}
