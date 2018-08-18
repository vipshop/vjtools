package com.vip.vjtools.vjmap.oops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.vip.vjtools.vjmap.ClassStats;

import sun.jvm.hotspot.gc_implementation.parallelScavenge.PSOldGen;
import sun.jvm.hotspot.gc_implementation.parallelScavenge.PSYoungGen;
import sun.jvm.hotspot.gc_implementation.parallelScavenge.ParallelScavengeHeap;
import sun.jvm.hotspot.gc_interface.CollectedHeap;
import sun.jvm.hotspot.memory.ConcurrentMarkSweepGeneration;
import sun.jvm.hotspot.memory.DefNewGeneration;
import sun.jvm.hotspot.memory.GenCollectedHeap;
import sun.jvm.hotspot.oops.Klass;
import sun.jvm.hotspot.oops.ObjectHeap;
import sun.jvm.hotspot.runtime.VM;

public class HeapUtils {

	public static CollectedHeap getHeap() {
		return VM.getVM().getUniverse().heap();
	}

	public static ObjectHeap getObjectHeap() {
		return VM.getVM().getObjectHeap();
	}

	public static boolean isCMSGC(CollectedHeap heap) {
		return heap instanceof GenCollectedHeap;
	}

	public static boolean isParallelGC(CollectedHeap heap) {
		return heap instanceof ParallelScavengeHeap;
	}

	public static DefNewGeneration getYoungGenForCMS(CollectedHeap heap) {
		return (DefNewGeneration) ((GenCollectedHeap) heap).getGen(0);
	}

	public static ConcurrentMarkSweepGeneration getOldGenForCMS(CollectedHeap heap) {
		return (ConcurrentMarkSweepGeneration) ((GenCollectedHeap) heap).getGen(1);
	}

	public static PSYoungGen getYongGenForPar(CollectedHeap heap) {
		return ((ParallelScavengeHeap) heap).youngGen();
	}

	public static PSOldGen getOldGenForPar(CollectedHeap heap) {
		return ((ParallelScavengeHeap) heap).oldGen();
	}

	public static List<ClassStats> getClassStatsList(HashMap<Klass, ClassStats> classStatsMap) {
		List<ClassStats> list = new ArrayList<>(classStatsMap.size());
		list.addAll(classStatsMap.values());
		return list;
	}

	public static ClassStats getClassStats(Klass klass, HashMap<Klass, ClassStats> classStatsMap) {
		ClassStats stats = classStatsMap.get(klass);
		if (stats == null) {
			stats = new ClassStats(klass);
			classStatsMap.put(klass, stats);
		}
		return stats;
	}
}
