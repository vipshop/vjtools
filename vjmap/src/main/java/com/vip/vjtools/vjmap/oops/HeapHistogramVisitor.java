package com.vip.vjtools.vjmap.oops;

import java.util.HashMap;
import java.util.List;

import com.vip.vjtools.vjmap.ClassStats;

import sun.jvm.hotspot.debugger.OopHandle;
import sun.jvm.hotspot.gc_implementation.parallelScavenge.PSOldGen;
import sun.jvm.hotspot.gc_implementation.parallelScavenge.PSYoungGen;
import sun.jvm.hotspot.gc_implementation.shared.MutableSpace;
import sun.jvm.hotspot.gc_interface.CollectedHeap;
import sun.jvm.hotspot.memory.ContiguousSpace;
import sun.jvm.hotspot.memory.DefNewGeneration;
import sun.jvm.hotspot.memory.EdenSpace;
import sun.jvm.hotspot.memory.Generation;
import sun.jvm.hotspot.oops.HeapVisitor;
import sun.jvm.hotspot.oops.Klass;
import sun.jvm.hotspot.oops.Oop;

/**
 * 实现HeapVisitor接口，实现遍历堆的回调方法
 */
public class HeapHistogramVisitor implements HeapVisitor {

	private static final int PROCERSSING_DOT_SIZE = 50000;
	private int processingObject;

	private CollectedHeap heap;

	private EdenSpace cmsEden;
	private ContiguousSpace cmsSur;
	private Generation cmsOld;

	private MutableSpace parEden;
	private MutableSpace parSur;
	private PSOldGen parOld;

	private boolean isCms;

	private HashMap<Klass, ClassStats> classStatsMap;

	public HeapHistogramVisitor() {
		classStatsMap = new HashMap<Klass, ClassStats>(2048, 0.2f);

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


	public boolean doObj(Oop obj) {
		Klass klass = obj.getKlass();

		ClassStats classStats = HeapUtils.getClassStats(klass, classStatsMap);

		Place place = isCms ? getCmsLocation(obj) : getParLocation(obj);

		updateWith(classStats, obj, place);

		if ((processingObject++) == PROCERSSING_DOT_SIZE) {
			System.err.print(".");
			processingObject = 0;
		}

		return false;
	}

	public void prologue(long size) {
	}

	public void epilogue() {
	}

	private void updateWith(ClassStats classStats, Oop obj, Place place) {
		long objSize = obj.getObjectSize();
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
		if (cmsOld.isIn(handle)) {
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

	public List<ClassStats> getClassStatsList() {
		return HeapUtils.getClassStatsList(classStatsMap);
	}

	public enum Place {
		Unknown, InEden, InSurvivor, InOld;
	}
}