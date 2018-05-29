package com.vip.vjtools.vjtop.data.jmx;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.List;

import javax.management.MBeanServerConnection;

public class JmxMemoryPoolManager {
	public static final String EDEN = "eden";
	public static final String SURVIVOR = "survivor";
	public static final String OLD = "old";
	public static final String TENURED = "tenured"; // 并行GC算法老生代的名称
	public static final String PERM = "perm";
	public static final String METASPACE = "metaspace";// JDK8永久代名称
	public static final String CODE_CACHE = "code cache";
	public static final String COMPRESSED_CLASS_SPACE = "compressed class space";

	private MemoryPoolMXBean survivorMemoryPool = null;
	private MemoryPoolMXBean edenMemoryPool = null;
	private MemoryPoolMXBean oldMemoryPool = null;
	private MemoryPoolMXBean permMemoryPool = null;
	private MemoryPoolMXBean codeCacheMemoryPool = null;
	private MemoryPoolMXBean compressedClassSpaceMemoryPool = null;

	public JmxMemoryPoolManager(MBeanServerConnection connection) throws IOException {
		List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getPlatformMXBeans(connection,
				MemoryPoolMXBean.class);
		for (MemoryPoolMXBean memoryPool : memoryPoolMXBeans) {
			String name = memoryPool.getName().trim();
			String lowerCaseName = name.toLowerCase();
			if (lowerCaseName.contains(SURVIVOR)) {
				survivorMemoryPool = memoryPool;
			} else if (lowerCaseName.contains(EDEN)) {
				edenMemoryPool = memoryPool;
			} else if (lowerCaseName.contains(OLD) || lowerCaseName.contains(TENURED)) {
				oldMemoryPool = memoryPool;
			} else if (lowerCaseName.contains(PERM) || lowerCaseName.contains(METASPACE)) {
				permMemoryPool = memoryPool;
			} else if (lowerCaseName.contains(CODE_CACHE)) {
				codeCacheMemoryPool = memoryPool;
			} else if (lowerCaseName.contains(COMPRESSED_CLASS_SPACE)) {
				compressedClassSpaceMemoryPool = memoryPool;
			}
		}
	}

	public MemoryPoolMXBean getSurvivorMemoryPool() {
		return survivorMemoryPool;
	}

	public MemoryPoolMXBean getEdenMemoryPool() {
		return edenMemoryPool;
	}

	public MemoryPoolMXBean getOldMemoryPool() {
		return oldMemoryPool;
	}

	public MemoryPoolMXBean getPermMemoryPool() {
		return permMemoryPool;
	}

	public MemoryPoolMXBean getCodeCacheMemoryPool() {
		return codeCacheMemoryPool;
	}

	public MemoryPoolMXBean getCompressedClassSpaceMemoryPool() {
		return compressedClassSpaceMemoryPool;
	}
}
