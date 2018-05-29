package com.vip.vjtools.vjmap.oops;

import java.util.HashMap;
import java.util.Map;

import com.vip.vjtools.vjmap.ClassStats;

import sun.jvm.hotspot.debugger.OopHandle;
import sun.jvm.hotspot.gc_implementation.parallelScavenge.PSOldGen;
import sun.jvm.hotspot.gc_implementation.parallelScavenge.PSYoungGen;
import sun.jvm.hotspot.gc_implementation.parallelScavenge.ParallelScavengeHeap;
import sun.jvm.hotspot.gc_interface.CollectedHeap;
import sun.jvm.hotspot.memory.ContiguousSpace;
import sun.jvm.hotspot.memory.DefNewGeneration;
import sun.jvm.hotspot.memory.EdenSpace;
import sun.jvm.hotspot.memory.GenCollectedHeap;
import sun.jvm.hotspot.memory.Generation;
import sun.jvm.hotspot.oops.HeapVisitor;
import sun.jvm.hotspot.oops.Klass;
import sun.jvm.hotspot.oops.Oop;
import sun.jvm.hotspot.runtime.VM;

/**
 * 实现HeapVisitor接口，实现遍历堆的回调方法
 */
public class HistogramHeapVisitor implements HeapVisitor {

	private static final int PROCERSSING_DOT_SIZE = 10000;

	private CollectedHeap heap;

	private EdenSpace eden;
	private ContiguousSpace from;
	private Generation old;

	private PSYoungGen psYoung;
	private PSOldGen psOld;

	private boolean isCms;

	private Map<Klass, ClassStats> classStatsMap;

	private int processingObject = 0;

	public HistogramHeapVisitor() {
		this.classStatsMap = new HashMap<Klass, ClassStats>(2048, 0.2f);

		heap = VM.getVM().getUniverse().heap();
		if ((heap instanceof GenCollectedHeap)) {
			DefNewGeneration gen0 = (DefNewGeneration) ((GenCollectedHeap) heap).getGen(0);
			eden = gen0.eden();
			from = gen0.from();
			old = ((GenCollectedHeap) heap).getGen(1);
			isCms = true;
		} else if ((heap instanceof ParallelScavengeHeap)) {
			psYoung = ((ParallelScavengeHeap) heap).youngGen();
			psOld = ((ParallelScavengeHeap) heap).oldGen();
			isCms = false;
		} else {
			throw new RuntimeException("Unsupport Heap:" + heap.getClass().getName());
		}
	}

	public void prologue(long size) {
	}

	public void epilogue() {
	}

	public boolean doObj(Oop obj) {
		Klass klass = obj.getKlass();
		ClassStats classStats = this.classStatsMap.get(klass);

		if (classStats == null) {
			classStats = new ClassStats(klass);
			this.classStatsMap.put(klass, classStats);
		}

		Place place = isCms ? getGenLocation(obj) : getParLocation(obj);

		updateWith(classStats, obj, place);
		if ((processingObject++) == PROCERSSING_DOT_SIZE) {
			System.err.print(".");
			processingObject = 0;
		}
		return false;
	}

	public void updateWith(ClassStats classStats, Oop obj, Place place) {
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

	private Place getGenLocation(Oop obj) {
		OopHandle handle = obj.getHandle();

		if (eden.contains(handle)) {
			return Place.InEden;
		}
		if (from.contains(handle)) {
			return Place.InSurvivor;
		}
		if (old.isIn(handle)) {
			return Place.InOld;
		}

		return Place.Unknown;
	}

	public Place getParLocation(Oop obj) {
		OopHandle handle = obj.getHandle();

		if (psYoung.edenSpace().contains(handle)) {
			return Place.InEden;
		}

		if (psYoung.fromSpace().contains(handle)) {
			return Place.InSurvivor;
		}

		if (psOld.isIn(handle)) {
			return Place.InOld;
		}

		return Place.Unknown;
	}

	public Map<Klass, ClassStats> getClassStatsMap() {
		return classStatsMap;
	}

	public enum Place {
		Unknown, InEden, InSurvivor, InOld;
	}
}