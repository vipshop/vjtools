package com.vip.vjtools.vjmap.oops;

import java.io.PrintStream;
import java.util.HashMap;

import com.vip.vjtools.vjmap.ClassStats;
import com.vip.vjtools.vjmap.utils.ProgressNotifier;

import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.gc_interface.CollectedHeap;
import sun.jvm.hotspot.memory.CMSCollector;
import sun.jvm.hotspot.memory.CompactibleFreeListSpace;
import sun.jvm.hotspot.memory.ConcurrentMarkSweepGeneration;
import sun.jvm.hotspot.memory.FreeChunk;
import sun.jvm.hotspot.oops.Klass;
import sun.jvm.hotspot.oops.ObjectHeap;
import sun.jvm.hotspot.oops.Oop;
import sun.jvm.hotspot.oops.UnknownOopException;
import sun.jvm.hotspot.runtime.VM;
import sun.jvm.hotspot.runtime.VMObjectFactory;

/**
 * 使用主动访问堆的方式统计OldGen的对象信息, only support CMS GC.
 * 
 * 迭代分区的代码，来自于 sun.jvm.hotspot.memory.CompactibleFreeListSpace.getLiveRegions()
 * sun.jvm.hotspot.oops.ObjectHeap.iterateLiveRegions()
 * 
 * 第一版全抄iterateLiveRegions()，后来发现getLiveRegions()本身已经遍历了一次堆，所以改为在其基础上修改。
 */
public class OldgenAccessor {

	private PrintStream tty = System.out;
	private Address cur;
	private Address regionStart;
	private int liveRegions = 0;
	private HashMap<Klass, ClassStats> classStatsMap = new HashMap<>(2048, 0.2f);

	public HashMap<Klass, ClassStats> getClassStatsMap() {
		return classStatsMap;
	}

	public void caculateHistogram() {
		ObjectHeap objectHeap = HeapUtils.getObjectHeap();
		CollectedHeap heap = checkHeapType();
		ConcurrentMarkSweepGeneration cmsGen = HeapUtils.getOldGenForCMS(heap);

		CompactibleFreeListSpace cmsSpace = cmsGen.cmsSpace();
		CMSCollector cmsCollector = cmsSpace.collector();
		cur = cmsSpace.bottom();
		regionStart = cur;
		Address limit = cmsSpace.end();

		printGenSummary(cmsGen);

		ProgressNotifier progressNotifier = new ProgressNotifier(cmsGen.used());
		progressNotifier.printHead();

		final long addressSize = VM.getVM().getAddressSize();

		for (; cur.lessThan(limit);) {
			Address k = cur.getAddressAt(addressSize);
			if (FreeChunk.indicatesFreeChunk(cur)) {
				skipFreeChunk(addressSize);
			} else if (k != null) {
				Oop obj = null;
				try {
					obj = objectHeap.newOop(cur.addOffsetToAsOopHandle(0));
				} catch (UnknownOopException ignored) {
					// ignored
				}

				if (obj == null) {
					continueNextAddress(cmsCollector);
					continue;
				}

				long objectSize = obj.getObjectSize();

				ClassStats stats = HeapUtils.getClassStats(obj.getKlass(), classStatsMap);
				stats.oldCount++;
				stats.oldSize += objectSize;

				progressNotifier.processingSize += objectSize;
				if (progressNotifier.processingSize > progressNotifier.nextNotificationSize) {
					progressNotifier.printProgress();
				}

				cur = cur.addOffsetTo(CompactibleFreeListSpace.adjustObjectSizeInBytes(objectSize));
			} else {
				continueNextAddress(cmsCollector);
			}
		}

		tty.println("\ntotal live regions:" + liveRegions);
	}

	private CollectedHeap checkHeapType() {
		CollectedHeap heap = HeapUtils.getHeap();

		if (!HeapUtils.isCMSGC(heap)) {
			throw new IllegalArgumentException("Only support CMS GC. Unsupport heap:" + heap.getClass().getName());
		}
		return heap;
	}

	private void printGenSummary(ConcurrentMarkSweepGeneration cmsGen) {
		cmsGen.printOn(tty);
		tty.println("");
	}

	private void skipFreeChunk(final long addressSize) {
		if (!cur.equals(regionStart)) {
			liveRegions++;
		}

		FreeChunk fc = (FreeChunk) VMObjectFactory.newObject(FreeChunk.class, cur);
		long chunkSize = fc.size();
		cur = cur.addOffsetTo(chunkSize * addressSize);
	}

	private void continueNextAddress(CMSCollector cmsCollector) {
		long size = cmsCollector.blockSizeUsingPrintezisBits(cur);
		if (size <= 0L) {
			throw new UnknownOopException();
		}
		cur = cur.addOffsetTo(CompactibleFreeListSpace.adjustObjectSizeInBytes(size));
	}
}
