package com.vip.vjtools.vjtop.data.jmx;

import java.io.IOException;
import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

import javax.management.MBeanServerConnection;

public class JmxBufferPoolManager {
	private static final String DIRECT = "direct";
	private static final String MAPPED = "mapped";

	private BufferPoolMXBean directBufferPool = null;
	private BufferPoolMXBean mappedBufferPool = null;

	public JmxBufferPoolManager(MBeanServerConnection connection) throws IOException {

		List<BufferPoolMXBean> bufferPoolMXBeans = ManagementFactory.getPlatformMXBeans(connection,
				BufferPoolMXBean.class);
		if (bufferPoolMXBeans == null) {
			return;
		}

		for (BufferPoolMXBean bufferPool : bufferPoolMXBeans) {
			String name = bufferPool.getName().toLowerCase().trim();
			if (name.contains(DIRECT)) {
				directBufferPool = bufferPool;
			} else if (name.contains(MAPPED)) {
				mappedBufferPool = bufferPool;
			}
		}
	}

	public long getDirectBufferPoolUsed() {
		return directBufferPool != null ? directBufferPool.getMemoryUsed() : 0;
	}

	public long getDirectBufferPoolCapacity() {
		return directBufferPool != null ? directBufferPool.getTotalCapacity() : 0;
	}

	public long getMappedBufferPoolUsed() {
		return mappedBufferPool != null ? mappedBufferPool.getMemoryUsed() : 0;
	}

	public long getMappedBufferPoolCapacity() {
		return mappedBufferPool != null ? mappedBufferPool.getTotalCapacity() : 0;
	}

	public long getMappedBufferPoolCount() {
		return mappedBufferPool != null ? mappedBufferPool.getCount() : 0;
	}
}
