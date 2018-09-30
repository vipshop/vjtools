package com.vip.vjtools.jmx;

import java.text.DecimalFormat;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;

public class GCutilExpression {

	// MBean Name
	private static final String GARBAGE_COLLECTORS = "java.lang:type=GarbageCollector,name=*";
	private static final String MEM_POOL_PREFIX = "java.lang:type=MemoryPool,name=";

	// Collector的Attribute Name
	private static final String COLLECTION_TIME_ATTRIBUTE = "CollectionTime";
	private static final String COLLECTION_COUNT_ATTRIBUTE = "CollectionCount";

	private static final DecimalFormat DF = new DecimalFormat("0.00");

	private MBeanServerConnection mbsc;

	private ObjectName ygcCollector;
	private ObjectName fgcCollector;
	private ObjectName eden;
	private ObjectName old;
	private ObjectName sur;
	private ObjectName perm;
	private ObjectName ccs;

	public GCutilExpression(MBeanServerConnection mbsc) throws Exception {
		this.mbsc = mbsc;
		mappingCollctors();
		mappingPools();
	}

	private void mappingCollctors() throws Exception {
		Set<ObjectInstance> beans = mbsc.queryMBeans(Client.getObjectName(GARBAGE_COLLECTORS), null);

		for (ObjectInstance collector : beans) {
			ObjectName collectorObjName = collector.getObjectName();
			String collectorName = getAttribute(collectorObjName, "Name");
			if ("Copy".equals(collectorName) || "PS Scavenge".equals(collectorName) || "ParNew".equals(collectorName)
					|| "G1 Young Generation".equals(collectorName)) {
				ygcCollector = collectorObjName;
			} else if ("MarkSweepCompact".equals(collectorName) || "PS MarkSweep".equals(collectorName)
					|| "ConcurrentMarkSweep".equals(collectorName) || "G1 Old Generation".equals(collectorName)) {
				fgcCollector = collectorObjName;
			} else {
				ygcCollector = collectorObjName;
			}
		}
	}

	private void mappingPools() throws Exception {
		Set<ObjectInstance> beans = mbsc.queryMBeans(Client.getObjectName(MEM_POOL_PREFIX + "*"), null);
		for (ObjectInstance pool : beans) {
			ObjectName poolObjName = pool.getObjectName();
			String poolName = getAttribute(poolObjName, "Name");
			poolName = poolName.trim().toLowerCase();
			if (poolName.contains("eden")) {
				eden = poolObjName;
			} else if (poolName.contains("survivor")) {
				sur = poolObjName;
			} else if (poolName.contains("old") || poolName.contains("tenured")) {
				old = poolObjName;
			} else if (poolName.contains("perm") || poolName.contains("metaspace")) {
				perm = poolObjName;
			} else if (poolName.contains("compressed class space")) {
				ccs = poolObjName;
			}
		}
	}

	public String getE() throws Exception {
		return usedPercentage(eden);
	}

	public String getS() throws Exception {
		return usedPercentage(sur);
	}

	public String getO() throws Exception {
		return usedPercentage(old);
	}

	public String getP() throws Exception {
		return usedPercentage(perm);
	}

	public String getCCS() throws Exception {
		return usedPercentage(ccs);
	}

	public Object getYGC() throws Exception {
		return getAttribute(ygcCollector, COLLECTION_COUNT_ATTRIBUTE);
	}

	public Double getYGCT() throws Exception {
		return Double.parseDouble(getAttribute(ygcCollector, COLLECTION_TIME_ATTRIBUTE).toString()) / 1000;
	}

	public Object getFGC() throws Exception {
		return getAttribute(fgcCollector, COLLECTION_COUNT_ATTRIBUTE);
	}

	public Double getFGCT() throws Exception {
		return Double.parseDouble(getAttribute(fgcCollector, COLLECTION_TIME_ATTRIBUTE).toString()) / 1000;
	}

	public Object getGCT() throws Exception {
		return getFGCT() + getYGCT();
	}

	private <T> T getAttribute(ObjectName beanName, String attributeName) throws Exception {
		if (beanName != null) {
			return (T) mbsc.getAttribute(beanName, attributeName);
		} else {
			return null;
		}
	}

	private String usedPercentage(ObjectName poolObjectName) throws Exception {
		if (poolObjectName == null) {
			return DF.format(0.0d);
		}

		CompositeDataSupport usage = getAttribute(poolObjectName, "Usage");
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