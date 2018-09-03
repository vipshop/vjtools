package com.vip.vjtools.vjtop.data.jmx;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;

import javax.management.MBeanServerConnection;

import com.sun.management.GarbageCollectorMXBean;

public class JmxGarbageCollectorManager {

	private GarbageCollectorMXBean fullGarbageCollectorMXBean = null;
	private GarbageCollectorMXBean youngGarbageCollectorMXBean = null;

	public JmxGarbageCollectorManager(MBeanServerConnection connection) throws IOException {
		if (fullGarbageCollectorMXBean != null || youngGarbageCollectorMXBean != null) {
			return;
		}

		List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getPlatformMXBeans(connection,
				GarbageCollectorMXBean.class);
		A: for (GarbageCollectorMXBean gcollector : garbageCollectorMXBeans) {
			String[] memoryPoolNames = gcollector.getMemoryPoolNames();
			for (String poolName : memoryPoolNames) {
				if (poolName.toLowerCase().contains(JmxMemoryPoolManager.OLD)
						|| poolName.toLowerCase().contains(JmxMemoryPoolManager.TENURED)) {
					fullGarbageCollectorMXBean = gcollector;
					continue A;
				}
			}
			youngGarbageCollectorMXBean = gcollector;
		}
	}

	public synchronized GarbageCollectorMXBean getFullCollector() {
		return fullGarbageCollectorMXBean;
	}

	public synchronized GarbageCollectorMXBean getYoungCollector() {
		return youngGarbageCollectorMXBean;
	}
}
