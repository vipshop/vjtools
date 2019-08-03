package com.vip.vjtools.vjtop;

import java.io.IOException;
import java.lang.management.ThreadInfo;

import com.vip.vjtools.vjtop.VMDetailView.ThreadInfoMode;
import com.vip.vjtools.vjtop.util.LongObjectHashMap;
import com.vip.vjtools.vjtop.util.LongObjectMap;
import com.vip.vjtools.vjtop.util.Utils;

public class TopThreadInfo {
	private VMInfo vmInfo;
	private long[] topTidArray;

	private LongObjectMap<Long> lastThreadCpuTotalTimes = new LongObjectHashMap<>();
	private LongObjectMap<Long> lastThreadSysCpuTotalTimes = new LongObjectHashMap<>();
	private LongObjectMap<Long> lastThreadMemoryTotalBytes = new LongObjectHashMap<>();

	public TopThreadInfo(VMInfo vmInfo) throws Exception {
		this.vmInfo = vmInfo;
	}

	public TopCpuResult topCpuThreads(ThreadInfoMode mode, int threadLimit) throws IOException {

		TopCpuResult result = new TopCpuResult();

		try {
			long tids[] = vmInfo.getAllThreadIds();

			int mapSize = tids.length * 2;
			result.threadCpuTotalTimes = new LongObjectHashMap<>(mapSize);
			result.threadCpuDeltaTimes = new LongObjectHashMap<>(mapSize);
			result.threadSysCpuTotalTimes = new LongObjectHashMap<>(mapSize);
			result.threadSysCpuDeltaTimes = new LongObjectHashMap<>(mapSize);

			// 批量获取CPU times，性能大幅提高。
			// 两次获取之间有间隔，在低流量下可能造成负数
			long[] threadCpuTotalTimeArray = vmInfo.getThreadCpuTime(tids);
			long[] threadUserCpuTotalTimeArray = vmInfo.getThreadUserTime(tids);

			// 过滤CPU占用太少的线程，每秒0.01%CPU (0.1ms cpu time)
			long minDeltaCpuTime = (vmInfo.upTimeMills.delta * Utils.NANOS_TO_MILLS / 10000);

			// 计算本次CPU Time
			// 此算法第一次不会显示任何数据，保证每次显示都只显示区间内数据
			for (int i = 0; i < tids.length; i++) {
				long tid = tids[i];
				Long threadCpuTotalTime = threadCpuTotalTimeArray[i];
				result.threadCpuTotalTimes.put(tid, threadCpuTotalTime);

				Long lastTime = lastThreadCpuTotalTimes.get(tid);
				if (lastTime != null) {
					Long deltaThreadCpuTime = threadCpuTotalTime - lastTime;
					if (deltaThreadCpuTime >= minDeltaCpuTime) {
						result.threadCpuDeltaTimes.put(tid, deltaThreadCpuTime);
						result.deltaAllActiveThreadCpu += deltaThreadCpuTime;
					} else {
						result.deltaAllFreeThreadCpu += deltaThreadCpuTime;
					}
				}
			}

			// 计算本次SYSCPU Time
			for (int i = 0; i < tids.length; i++) {
				long tid = tids[i];
				// 因为totalTime 与 userTime 的获取时间有先后，实际sys接近0时，后取的userTime可能比前一时刻的totalTime高，计算出来的sysTime可为负数
				Long threadSysCpuTotalTime = Math.max(0, threadCpuTotalTimeArray[i] - threadUserCpuTotalTimeArray[i]);
				result.threadSysCpuTotalTimes.put(tid, threadSysCpuTotalTime);

				Long lastTime = lastThreadSysCpuTotalTimes.get(tid);
				if (lastTime != null) {
					Long deltaThreadSysCpuTime = Math.max(0, threadSysCpuTotalTime - lastTime);
					if (deltaThreadSysCpuTime >= minDeltaCpuTime) {
						result.threadSysCpuDeltaTimes.put(tid, deltaThreadSysCpuTime);
						result.deltaAllActiveThreadSysCpu += deltaThreadSysCpuTime;
					}
				}
			}

			if (lastThreadCpuTotalTimes.isEmpty()) {
				lastThreadCpuTotalTimes = result.threadCpuTotalTimes;
				lastThreadSysCpuTotalTimes = result.threadSysCpuTotalTimes;
				result.ready = false;
				return result;
			}

			// 按不同类型排序,过滤
			if (mode == ThreadInfoMode.cpu) {
				topTidArray = Utils.sortAndFilterThreadIdsByValue(result.threadCpuDeltaTimes, threadLimit);
			} else if (mode == ThreadInfoMode.syscpu) {
				topTidArray = Utils.sortAndFilterThreadIdsByValue(result.threadSysCpuDeltaTimes, threadLimit);
			} else if (mode == ThreadInfoMode.totalcpu) {
				topTidArray = Utils.sortAndFilterThreadIdsByValue(result.threadCpuTotalTimes, threadLimit);
			} else if (mode == ThreadInfoMode.totalsyscpu) {
				topTidArray = Utils.sortAndFilterThreadIdsByValue(result.threadSysCpuTotalTimes, threadLimit);
			} else {
				throw new RuntimeException("unkown mode:" + mode);
			}

			result.activeThreads = result.threadCpuDeltaTimes.size();

			// 获得线程名等信息threadInfo
			result.threadInfos = vmInfo.getThreadInfo(topTidArray);

			lastThreadCpuTotalTimes = result.threadCpuTotalTimes;
			lastThreadSysCpuTotalTimes = result.threadSysCpuTotalTimes;
		} catch (Exception e) {
			vmInfo.handleJmxFetchDataError(e);
		}

		return result;
	}

	public TopMemoryResult topMemoryThreads(ThreadInfoMode mode, int threadLimit) throws IOException {
		TopMemoryResult result = new TopMemoryResult();
		try {
			long tids[] = vmInfo.getAllThreadIds();

			int mapSize = tids.length * 2;
			result.threadMemoryTotalBytesMap = new LongObjectHashMap<>(mapSize);
			result.threadMemoryDeltaBytesMap = new LongObjectHashMap<>(mapSize);

			// 批量获取内存分配
			long[] threadMemoryTotalBytesArray = vmInfo.getThreadAllocatedBytes(tids);

			// 此算法第一次不会显示任何数据，保证每次显示都只显示区间内数据
			for (int i = 0; i < tids.length; i++) {
				long tid = tids[i];
				Long threadMemoryTotalBytes = threadMemoryTotalBytesArray[i];
				result.threadMemoryTotalBytesMap.put(tid, threadMemoryTotalBytes);
				result.totalAllThreadBytes += threadMemoryTotalBytes;

				Long threadMemoryDeltaBytes = 0L;
				Long lastBytes = lastThreadMemoryTotalBytes.get(tid);

				if (lastBytes != null) {
					threadMemoryDeltaBytes = threadMemoryTotalBytes - lastBytes;
					if (threadMemoryDeltaBytes > 0) {
						result.threadMemoryDeltaBytesMap.put(tid, threadMemoryDeltaBytes);
						result.deltaAllThreadBytes += threadMemoryDeltaBytes;
					}
				}
			}

			if (lastThreadMemoryTotalBytes.isEmpty()) {
				lastThreadMemoryTotalBytes = result.threadMemoryTotalBytesMap;
				result.ready = false;
				return result;
			}

			// 线程排序
			long[] topTidArray;
			if (mode == ThreadInfoMode.memory) {
				topTidArray = Utils.sortAndFilterThreadIdsByValue(result.threadMemoryDeltaBytesMap, threadLimit);
			} else {
				topTidArray = Utils.sortAndFilterThreadIdsByValue(result.threadMemoryTotalBytesMap, threadLimit);
			}

			result.activeThreads = result.threadMemoryDeltaBytesMap.size();

			result.threadInfos = vmInfo.getThreadInfo(topTidArray);

			lastThreadMemoryTotalBytes = result.threadMemoryTotalBytesMap;
		} catch (Exception e) {
			vmInfo.handleJmxFetchDataError(e);
		}
		return result;
	}

	public ThreadInfo[] getTopThreadInfo() throws IOException {
		return vmInfo.getThreadInfo(topTidArray, 20);
	}

	public void cleanupThreadsHistory() {
		this.lastThreadCpuTotalTimes.clear();
		this.lastThreadSysCpuTotalTimes.clear();
		this.lastThreadMemoryTotalBytes.clear();
	}

	public static class TopCpuResult {
		public ThreadInfo[] threadInfos;
		public long activeThreads = 0;

		public long deltaAllActiveThreadCpu = 0;
		public long deltaAllActiveThreadSysCpu = 0;

		public long deltaAllFreeThreadCpu = 0;

		public LongObjectMap<Long> threadCpuTotalTimes;
		public LongObjectMap<Long> threadCpuDeltaTimes;
		public LongObjectMap<Long> threadSysCpuTotalTimes;
		public LongObjectMap<Long> threadSysCpuDeltaTimes;

		public boolean ready = true;
	}

	public static class TopMemoryResult {
		public ThreadInfo[] threadInfos;
		public long activeThreads = 0;

		public long deltaAllThreadBytes = 0;
		public long totalAllThreadBytes = 0;

		public LongObjectMap<Long> threadMemoryTotalBytesMap;
		public LongObjectMap<Long> threadMemoryDeltaBytesMap;

		public boolean ready = true;
	}
}
