package com.vip.vjtools.jmx;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;

public class GCutilExpression {

	private static final String EDEN = "eden";
	private static final String SURVIVOR = "survivor";
	private static final String OLD = "old";
	private static final String TENURED = "tenured"; // 并行GC算法老生代的名称
	private static final String PERM = "perm";
	private static final String METASPACE = "metaspace";// JDK8永久代名称
	public static final String COMPRESSED_CLASS_SPACE = "compressed class space";

	private static final DecimalFormat DF = new DecimalFormat("0.00");

	private static final String GARBAGE_COLLECTORS = "java.lang:type=GarbageCollector,name=*";
	private static final String MEM_POOL_PREFIX = "java.lang:type=MemoryPool,name=";

	// Collectors的三个属性
	private static final String MEMORY_POOL_ATTRIBUTE = "MemoryPoolNames";
	private static final String COLLECTION_TIME_ATTRIBUTE = "CollectionTime";
	private static final String COLLECTION_COUNT_ATTRIBUTE = "CollectionCount";

	private ObjectName YGC_COLLECTOR;
	private ObjectName FGC_COLLECTOR;
	private Map<String, String> pollNameMapping;

	private MBeanServerConnection mbsc;

	public GCutilExpression(MBeanServerConnection mbsc) throws Exception {
		this.mbsc = mbsc;
		mappingCollctors();
		mappingPools();
	}

	private void mappingCollctors() throws Exception {
		Set<ObjectInstance> beans = mbsc.queryMBeans(Client.getObjectName(GARBAGE_COLLECTORS), null);

		A: for (ObjectInstance collector : beans) {
			ObjectName collectorName = collector.getObjectName();
			// 获得这个Collector负责的Memory Pool Name
			String[] memoryPoolNames = (String[]) mbsc.getAttribute(collectorName, MEMORY_POOL_ATTRIBUTE);

			for (String poolName : memoryPoolNames) {
				if (poolName.toLowerCase().contains(OLD) || poolName.toLowerCase().contains(TENURED)) {
					FGC_COLLECTOR = collectorName;
					continue A;
				}
			}
			// 如果此收集器负责的分区没有Old，则是YGC_COLLECTOR
			YGC_COLLECTOR = collectorName;
		}
	}

	private void mappingPools() throws Exception {
		// full gc的collector，下属的memoryPool包括所有需要GC的Pool(Code Reserve等则不在此列)
		Set<ObjectInstance> beans = mbsc.queryMBeans(Client.getObjectName(MEM_POOL_PREFIX + "*"), null);
		pollNameMapping = new HashMap<>();
		for (ObjectInstance collector : beans) {
			ObjectName collectorName = collector.getObjectName();
			String name = (String) getAttribute(collectorName, "Name");
			name = name.trim();
			String lowerCaseName = name.toLowerCase();
			if (lowerCaseName.contains(SURVIVOR)) {
				pollNameMapping.put(SURVIVOR, name);
			} else if (lowerCaseName.contains(EDEN)) {
				pollNameMapping.put(EDEN, name);
			} else if (lowerCaseName.contains(OLD) || lowerCaseName.contains(TENURED)) {
				pollNameMapping.put(OLD, name);
			} else if (lowerCaseName.contains(PERM) || lowerCaseName.contains(METASPACE)) {
				pollNameMapping.put(PERM, name);
			} else if (lowerCaseName.contains(COMPRESSED_CLASS_SPACE)) {
				pollNameMapping.put(COMPRESSED_CLASS_SPACE, name);
			}
		}
	}

	public String getE() throws Exception {
		String poolName = MEM_POOL_PREFIX + pollNameMapping.get(EDEN);
		return usedPercentage(poolName);
	}

	public String getS() throws Exception {
		String poolName = MEM_POOL_PREFIX + pollNameMapping.get(SURVIVOR);
		return usedPercentage(poolName);
	}

	public String getO() throws Exception {
		String poolName = MEM_POOL_PREFIX + pollNameMapping.get(OLD);
		return usedPercentage(poolName);
	}

	public String getP() throws Exception {
		String poolName = MEM_POOL_PREFIX + pollNameMapping.get(PERM);
		return usedPercentage(poolName);
	}

	public String getCCS() throws Exception {
		String poolName = MEM_POOL_PREFIX + pollNameMapping.get(COMPRESSED_CLASS_SPACE);
		return usedPercentage(poolName);
	}

	public Object getYGC() throws Exception {
		return getAttribute(YGC_COLLECTOR, COLLECTION_COUNT_ATTRIBUTE);
	}

	public Double getYGCT() throws Exception {
		return Double.parseDouble(getAttribute(YGC_COLLECTOR, COLLECTION_TIME_ATTRIBUTE).toString()) / 1000;
	}

	public Object getFGC() throws Exception {
		return getAttribute(FGC_COLLECTOR, COLLECTION_COUNT_ATTRIBUTE);
	}

	public Double getFGCT() throws Exception {
		return Double.parseDouble(getAttribute(FGC_COLLECTOR, COLLECTION_TIME_ATTRIBUTE).toString()) / 1000;
	}

	public Object getGCT() throws Exception {
		return getFGCT() + getYGCT();
	}

	private Object getAttribute(ObjectName beanName, String attributeName) throws Exception {
		return mbsc.getAttribute(beanName, attributeName);
	}

	private String usedPercentage(String poolName) throws Exception {
		CompositeDataSupport usage = (CompositeDataSupport) mbsc.getAttribute(Client.getObjectName(poolName), "Usage");
		double max = Double.parseDouble(usage.get("max").toString());

		// 如果Max没有设置或GC算法原因没有max，则以committed为准
		if (max < 0) {
			max = Double.parseDouble(usage.get("committed").toString());
		}

		if (max > 0.0d) {
			double used = Double.parseDouble(usage.get("used").toString()) / max * 100;
			return DF.format(used);
		} else {
			return DF.format(0.0d);
		}
	}
}