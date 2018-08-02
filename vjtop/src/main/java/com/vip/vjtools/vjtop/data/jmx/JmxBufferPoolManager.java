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
		for (BufferPoolMXBean bufferPool : bufferPoolMXBeans) {
			String name = bufferPool.getName().trim();
			String lowerCaseName = name.toLowerCase();
			if (lowerCaseName.contains(DIRECT)) {
				directBufferPool = bufferPool;
			} else if (lowerCaseName.contains(MAPPED)) {
				mappedBufferPool = bufferPool;
			}
		}
	}

	public BufferPoolMXBean getDirectBufferPool() {
		return directBufferPool;
	}

	public BufferPoolMXBean getMappedBufferPool() {
		return mappedBufferPool;
	}
}
