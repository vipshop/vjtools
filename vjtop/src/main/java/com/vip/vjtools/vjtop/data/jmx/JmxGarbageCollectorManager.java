package com.vip.vjtools.vjtop.data.jmx;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;

import javax.management.MBeanServerConnection;

import com.sun.management.GarbageCollectorMXBean;

public class JmxGarbageCollectorManager {

	private GarbageCollectorMXBean ygcMXBean = null;
	private GarbageCollectorMXBean fgcMXBean = null;
	private String ygcStrategy = null;
	private String fgcStrategy = null;

	public static String getByGcName(String gcName, String defaultName) {

		return defaultName;
	}

	public JmxGarbageCollectorManager(MBeanServerConnection connection) throws IOException {
		if (ygcMXBean != null || fgcMXBean != null) {
			return;
		}

		List<GarbageCollectorMXBean> gcMXBeans = ManagementFactory.getPlatformMXBeans(connection,
				GarbageCollectorMXBean.class);

		for (GarbageCollectorMXBean gcMXBean : gcMXBeans) {
			String gcName = gcMXBean.getName();
			if ("Copy".equals(gcName) || "PS Scavenge".equals(gcName) || "ParNew".equals(gcName)
					|| "G1 Young Generation".equals(gcName) || "ZGC".equals(gcName)) {
				ygcMXBean = gcMXBean;
				ygcStrategy = gcName;
			} else if ("MarkSweepCompact".equals(gcName) || "PS MarkSweep".equals(gcName)
					|| "ConcurrentMarkSweep".equals(gcName) || "G1 Old Generation".equals(gcName)) {
				fgcMXBean = gcMXBean;
				fgcStrategy = gcName;
			} else {
				// default
				ygcMXBean = gcMXBean;
				ygcStrategy = gcName;
			}
		}
	}

	public synchronized GarbageCollectorMXBean getYoungCollector() {
		return ygcMXBean;
	}

	public synchronized GarbageCollectorMXBean getFullCollector() {
		return fgcMXBean;
	}

	public String getYgcStrategy() {
		return ygcStrategy;
	}

	public String getFgcStrategy() {
		return fgcStrategy;
	}

}
