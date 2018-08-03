package com.vip.vjtools.vjmap.oops;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;

import com.vip.vjtools.vjmap.ClassStats;

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

	private ProgressNodifier progressNodifier;

	public List<ClassStats> caculateHistogram() {

		HashMap<Klass, ClassStats> classStatsMap = new HashMap<Klass, ClassStats>(2048, 0.2f);

		ObjectHeap objectHeap = HeapUtils.getObjectHeap();
		CollectedHeap heap = checkHeapType();
		ConcurrentMarkSweepGeneration cmsGen = HeapUtils.getOldGenForCMS(heap);

		CompactibleFreeListSpace cmsSpace = cmsGen.cmsSpace();
		CMSCollector cmsCollector = cmsSpace.collector();
		Address cur = cmsSpace.bottom();
		Address limit = cmsSpace.end();

		printGenSummary(cmsGen);

		progressNodifier = new ProgressNodifier(cmsGen.used());
		progressNodifier.printHead();

		final long addressSize = VM.getVM().getAddressSize();

		for (; cur.lessThan(limit);) {
			Address k = cur.getAddressAt(addressSize);
			if (FreeChunk.indicatesFreeChunk(cur)) {
				cur = skipFreeChunk(cur, addressSize);
			} else if (k != null) {
				Oop obj = null;
				try {
					obj = objectHeap.newOop(cur.addOffsetToAsOopHandle(0));
				} catch (UnknownOopException ignored) {
					// ignored
				}

				if (obj == null) {
					cur = continueNextAddress(cmsCollector, cur);
					continue;
				}

				long objectSize = obj.getObjectSize();

				ClassStats stats = HeapUtils.getClassStats(obj.getKlass(), classStatsMap);
				stats.oldCount++;
				stats.oldSize += objectSize;

				progressNodifier.processingSize += objectSize;
				if (progressNodifier.processingSize > progressNodifier.notificationSize) {
					progressNodifier.printProgress();
				}

				cur = cur.addOffsetTo(CompactibleFreeListSpace.adjustObjectSizeInBytes(objectSize));
			} else {
				cur = continueNextAddress(cmsCollector, cur);
			}
		}


		return HeapUtils.getClassStatsList(classStatsMap);
	}

	private CollectedHeap checkHeapType() {
		CollectedHeap heap = HeapUtils.getHeap();

		if (!HeapUtils.isCMSGC(heap)) {
			throw new IllegalArgumentException("Only support CMS GC. Unsupport heap:" + heap.getClass().getName());
		}
		return heap;
	}

	private void printGenSummary(ConcurrentMarkSweepGeneration cmsGen) {
		PrintStream tty = System.out;
		cmsGen.printOn(tty);
		tty.println("");
	}

	private Address skipFreeChunk(Address cur, final long addressSize) {
		FreeChunk fc = (FreeChunk) VMObjectFactory.newObject(FreeChunk.class, cur);
		long chunkSize = fc.size();
		cur = cur.addOffsetTo(chunkSize * addressSize);
		return cur;
	}

	private Address continueNextAddress(CMSCollector cmsCollector, Address cur) {
		long size = cmsCollector.blockSizeUsingPrintezisBits(cur);
		if (size <= 0L) {
			throw new UnknownOopException();
		}
		return cur.addOffsetTo(CompactibleFreeListSpace.adjustObjectSizeInBytes(size));
	}
}
