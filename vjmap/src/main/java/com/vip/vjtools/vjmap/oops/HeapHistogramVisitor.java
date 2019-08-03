package com.vip.vjtools.vjmap.oops;

import java.util.HashMap;

import com.vip.vjtools.vjmap.ClassStats;
import com.vip.vjtools.vjmap.utils.ProgressNotifier;

import sun.jvm.hotspot.debugger.OopHandle;
import sun.jvm.hotspot.gc_implementation.parallelScavenge.PSOldGen;
import sun.jvm.hotspot.gc_implementation.parallelScavenge.PSYoungGen;
import sun.jvm.hotspot.gc_implementation.shared.MutableSpace;
import sun.jvm.hotspot.gc_interface.CollectedHeap;
import sun.jvm.hotspot.memory.ConcurrentMarkSweepGeneration;
import sun.jvm.hotspot.memory.ContiguousSpace;
import sun.jvm.hotspot.memory.DefNewGeneration;
import sun.jvm.hotspot.memory.EdenSpace;
import sun.jvm.hotspot.oops.HeapVisitor;
import sun.jvm.hotspot.oops.Klass;
import sun.jvm.hotspot.oops.Oop;

/**
 * 实现HeapVisitor接口，实现遍历堆的回调方法
 */
public class HeapHistogramVisitor implements HeapVisitor {


	private CollectedHeap heap;

	private EdenSpace cmsEden;
	private ContiguousSpace cmsSur;
	private ConcurrentMarkSweepGeneration cmsOld;

	private MutableSpace parEden;
	private MutableSpace parSur;
	private PSOldGen parOld;

	private boolean isCms;

	private HashMap<Klass, ClassStats> classStatsMap;
	private ProgressNotifier progressNodifier;

	public HeapHistogramVisitor() {
		classStatsMap = new HashMap<>(2048, 0.2f);

		heap = HeapUtils.getHeap();
		if (HeapUtils.isCMSGC(heap)) {
			DefNewGeneration youngGen = HeapUtils.getYoungGenForCMS(heap);
			cmsEden = youngGen.eden();
			cmsSur = youngGen.from();
			cmsOld = HeapUtils.getOldGenForCMS(heap);
			isCms = true;
		} else if (HeapUtils.isParallelGC(heap)) {
			PSYoungGen youngGen = HeapUtils.getYongGenForPar(heap);
			parEden = youngGen.edenSpace();
			parSur = youngGen.fromSpace();
			parOld = HeapUtils.getOldGenForPar(heap);
			isCms = false;
		} else {
			throw new RuntimeException("Only support CMS and Parallel GC. Unsupport Heap:" + heap.getClass().getName());
		}
	}

	@Override
	public boolean doObj(Oop obj) {
		Klass klass = obj.getKlass();

		ClassStats classStats = HeapUtils.getClassStats(klass, classStatsMap);
		Place place = isCms ? getCmsLocation(obj) : getParLocation(obj);
		long objSize = obj.getObjectSize();

		updateWith(classStats, objSize, place);

		// 每完成1％ 打印一个.，每完成10% 打印百分比提示
		progressNodifier.processingSize += objSize;
		if (progressNodifier.processingSize > progressNodifier.nextNotificationSize) {
			progressNodifier.printProgress();
		}

		return false;
	}


	@Override
	public void prologue(long size) {
		progressNodifier = new ProgressNotifier(size);
		progressNodifier.printHead();
	}

	@Override
	public void epilogue() {
	}

	private void updateWith(ClassStats classStats, long objSize, Place place) {
		classStats.count++;
		classStats.size += objSize;

		switch (place) {
			case InEden:
				classStats.edenCount++;
				classStats.edenSize += objSize;
				break;
			case InSurvivor:
				classStats.survivorCount++;
				classStats.survivorSize += objSize;
				break;
			case InOld:
				classStats.oldCount++;
				classStats.oldSize += objSize;
				break;
			case Unknown:
				break;
		}
	}

	private Place getCmsLocation(Oop obj) {
		OopHandle handle = obj.getHandle();

		if (cmsEden.contains(handle)) {
			return Place.InEden;
		}
		if (cmsOld.contains(handle)) {
			return Place.InOld;
		}
		if (cmsSur.contains(handle)) {
			return Place.InSurvivor;
		}

		return Place.Unknown;
	}

	public Place getParLocation(Oop obj) {
		OopHandle handle = obj.getHandle();

		if (parEden.contains(handle)) {
			return Place.InEden;
		}
		if (parOld.isIn(handle)) {
			return Place.InOld;
		}
		if (parSur.contains(handle)) {
			return Place.InSurvivor;
		}

		return Place.Unknown;
	}

	public HashMap<Klass, ClassStats> getClassStatsMap() {
		return classStatsMap;
	}

	public enum Place {
		Unknown, InEden, InSurvivor, InOld;
	}
}