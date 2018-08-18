/*
 * Copyright (c) 2004, 2008, Oracle and/or its affiliates. All rights reserved. DO NOT ALTER OR REMOVE COPYRIGHT NOTICES
 * OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License version 2 only, as published by the Free Software Foundation. Oracle designates this particular file as
 * subject to the "Classpath" exception as provided by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License version 2 for
 * more details (a copy is included in the LICENSE file that accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version 2 along with this work; if not, write to
 * the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA or visit www.oracle.com if you need
 * additional information or have any questions.
 *
 */

package com.vip.vjtools.vjtop.data.jmx;

import java.io.File;
import java.io.IOException;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.JMX;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.sun.management.GarbageCollectorMXBean;
import com.sun.management.OperatingSystemMXBean;
import com.sun.management.ThreadMXBean;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

@SuppressWarnings("restriction")
public class JmxClient {
	private static final String LOCAL_CONNECTOR_ADDRESS_PROP = "com.sun.management.jmxremote.localConnectorAddress";

	private boolean connected = false;
	private boolean hasPlatformMXBeans = false;

	private String pid;

	private JMXServiceURL jmxUrl = null;
	private MBeanServerConnection mbsc = null;
	private SnapshotMBeanServerConnection server = null;
	private JMXConnector jmxc = null;

	private ClassLoadingMXBean classLoadingMBean = null;
	private OperatingSystemMXBean operatingSystemMBean = null;
	private RuntimeMXBean runtimeMBean = null;
	private ThreadMXBean threadMBean = null;
	private GarbageCollectorMXBean fullGarbageCollectorMXBean = null;
	private GarbageCollectorMXBean youngGarbageCollectorMXBean = null;
	private JmxMemoryPoolManager memoryPoolManager = null;
	private JmxBufferPoolManager bufferPoolManager = null;

	public JmxClient(String pid) throws IOException {
		this.pid = pid;
	}

	public boolean isConnected() {
		return this.connected;
	}

	public void flush() {
		if (server != null) {
			server.flush();
		}
	}

	public void connect() throws Exception {
		try {
			tryConnect();
			connected = true;
		} catch (Exception e) {
			connected = false;
			throw e;
		}
	}

	private void tryConnect() throws IOException {
		// 如果jmx agent未启动，主动attach进JVM后加载
		String address = getConnectorAddress();

		this.jmxUrl = new JMXServiceURL(address);
		this.jmxc = JMXConnectorFactory.connect(jmxUrl);// NOSONAR
		this.mbsc = jmxc.getMBeanServerConnection();
		this.server = Snapshot.newSnapshot(mbsc);

		try {
			ObjectName on = createBeanName(ManagementFactory.THREAD_MXBEAN_NAME);
			this.hasPlatformMXBeans = server.isRegistered(on);
		} catch (Exception e) {
			// should not reach here
			throw new InternalError(e.getMessage());
		}

		if (hasPlatformMXBeans) {
			// WORKAROUND for bug 5056632
			// Check if the access role is correct by getting a RuntimeMXBean
			getRuntimeMXBean();
		}
	}

	/**
	 * 因为已在退出JVM，因此仅关闭jmx连接即可
	 */
	public void disconnect() {
		// Close MBeanServer connection
		if (jmxc != null) {
			try {
				jmxc.close();
			} catch (IOException e) {
				// Ignore ???
			}
		}
	}

	public synchronized ClassLoadingMXBean getClassLoadingMXBean() throws IOException {
		if (hasPlatformMXBeans && classLoadingMBean == null) {
			classLoadingMBean = ManagementFactory.newPlatformMXBeanProxy(server,
					ManagementFactory.CLASS_LOADING_MXBEAN_NAME, ClassLoadingMXBean.class);
		}
		return classLoadingMBean;
	}

	public synchronized JmxMemoryPoolManager getMemoryPoolManager() throws IOException {
		if (hasPlatformMXBeans && memoryPoolManager == null) {
			memoryPoolManager = new JmxMemoryPoolManager(server);
		}
		return memoryPoolManager;
	}

	public synchronized RuntimeMXBean getRuntimeMXBean() throws IOException {
		if (hasPlatformMXBeans && runtimeMBean == null) {
			runtimeMBean = ManagementFactory.newPlatformMXBeanProxy(server, ManagementFactory.RUNTIME_MXBEAN_NAME,
					RuntimeMXBean.class);
		}
		return runtimeMBean;
	}

	public synchronized OperatingSystemMXBean getOperatingSystemMXBean() throws IOException {
		if (hasPlatformMXBeans && operatingSystemMBean == null) {
			operatingSystemMBean = ManagementFactory.newPlatformMXBeanProxy(server,
					ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);
		}
		return operatingSystemMBean;
	}

	public synchronized GarbageCollectorMXBean getFullCollector() throws IOException {
		if (fullGarbageCollectorMXBean == null) {
			initGarbageCollector();
		}
		return fullGarbageCollectorMXBean;
	}

	public synchronized JmxBufferPoolManager getBufferPoolManager() throws IOException {
		if (hasPlatformMXBeans && bufferPoolManager == null) {
			bufferPoolManager = new JmxBufferPoolManager(server);
		}

		return bufferPoolManager;
	}

	public synchronized GarbageCollectorMXBean getYoungCollector() throws IOException {
		if (hasPlatformMXBeans && youngGarbageCollectorMXBean == null) {
			initGarbageCollector();
		}
		return youngGarbageCollectorMXBean;
	}

	private synchronized void initGarbageCollector() throws IOException {
		if (fullGarbageCollectorMXBean != null || youngGarbageCollectorMXBean != null) {
			return;
		}

		List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getPlatformMXBeans(server,
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

	public synchronized ThreadMXBean getThreadMXBean() throws IOException {
		if (hasPlatformMXBeans && threadMBean == null) {
			threadMBean = JMX.newMXBeanProxy(server, createBeanName(ManagementFactory.THREAD_MXBEAN_NAME),
					ThreadMXBean.class);
		}
		return threadMBean;
	}

	private ObjectName createBeanName(String beanName) {
		try {
			return new ObjectName(beanName);
		} catch (MalformedObjectNameException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return pid;
	}

	/**
	 * VirtualMachine保证JMX Agent已启动,
	 * 并向JMXClient提供连接地址地址样例：service:jmx:rmi://127.0
	 * .0.1/stub/rO0ABXN9AAAAAQAl...
	 */
	public String getConnectorAddress() throws IOException {
		VirtualMachine vm = null;
		// 1. attach vm

		try {
			vm = VirtualMachine.attach(pid);
		} catch (AttachNotSupportedException x) {
			IOException ioe = new IOException(x.getMessage());
			ioe.initCause(x);
			throw ioe;
		}

		try {

			// 2. 检查smartAgent是否已启动
			Properties agentProps = vm.getAgentProperties();
			String address = (String) agentProps.get(LOCAL_CONNECTOR_ADDRESS_PROP);

			if (address != null) {
				return address;
			}

			// 3. 未启动，尝试启动
			String home = vm.getSystemProperties().getProperty("java.home");

			// Normally in ${java.home}/jre/lib/management-agent.jar but might
			// be in ${java.home}/lib in build environments.

			String agentPath = home + File.separator + "jre" + File.separator + "lib" + File.separator
					+ "management-agent.jar";
			File f = new File(agentPath);
			if (!f.exists()) {
				agentPath = home + File.separator + "lib" + File.separator + "management-agent.jar";
				f = new File(agentPath);
				if (!f.exists()) {
					throw new IOException("Management agent not found");
				}
			}

			agentPath = f.getCanonicalPath();
			try {
				vm.loadAgent(agentPath, "com.sun.management.jmxremote");
			} catch (AgentLoadException x) {
				IOException ioe = new IOException(x.getMessage());
				ioe.initCause(x);
				throw ioe;
			} catch (AgentInitializationException x) {
				IOException ioe = new IOException(x.getMessage());
				ioe.initCause(x);
				throw ioe;
			}

			// 4. 再次获取connector address
			agentProps = vm.getAgentProperties();
			address = (String) agentProps.get(LOCAL_CONNECTOR_ADDRESS_PROP);

			if (address == null) {
				throw new IOException("Fails to find connector address");
			}

			return address;
		} finally {
			vm.detach();
		}
	}

	// JDK自带的Snapshot实现，在flush前缓存值

	// Snapshot MBeanServerConnection:
	//
	// This is an object that wraps an existing MBeanServerConnection and adds
	// caching to it, as follows:
	//
	// - The first time an attribute is called in a given MBean, the result is
	// cached. Every subsequent time getAttribute is called for that attribute
	// the cached result is returned.
	//
	// - Before every call to VMPanel.update() or when the Refresh button in the
	// Attributes table is pressed down the attributes cache is flushed. Then
	// any subsequent call to getAttribute will retrieve all the values for
	// the attributes that are known to the cache.
	//
	// - The attributes cache uses a learning approach and only the attributes
	// that are in the cache will be retrieved between two subsequent updates.
	//

	public interface SnapshotMBeanServerConnection extends MBeanServerConnection {
		/**
		 * Flush all cached values of attributes.
		 */
		void flush();
	}

	public static class Snapshot {
		private Snapshot() {
		}

		public static SnapshotMBeanServerConnection newSnapshot(MBeanServerConnection mbsc) {
			final InvocationHandler ih = new SnapshotInvocationHandler(mbsc);
			return (SnapshotMBeanServerConnection) Proxy.newProxyInstance(Snapshot.class.getClassLoader(),
					new Class[] { SnapshotMBeanServerConnection.class }, ih);
		}
	}

	static class SnapshotInvocationHandler implements InvocationHandler {

		private final MBeanServerConnection conn;
		private Map<ObjectName, NameValueMap> cachedValues = newMap();
		private Map<ObjectName, Set<String>> cachedNames = newMap();

		@SuppressWarnings("serial")
		private static final class NameValueMap extends HashMap<String, Object> {
		}

		SnapshotInvocationHandler(MBeanServerConnection conn) {
			this.conn = conn;
		}

		synchronized void flush() {
			cachedValues = newMap();
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			final String methodName = method.getName();
			if (methodName.equals("getAttribute")) {
				return getAttribute((ObjectName) args[0], (String) args[1]);
			} else if (methodName.equals("getAttributes")) {
				return getAttributes((ObjectName) args[0], (String[]) args[1]);
			} else if (methodName.equals("flush")) {
				flush();
				return null;
			} else {
				try {
					return method.invoke(conn, args);
				} catch (InvocationTargetException e) {
					throw e.getCause();
				}
			}
		}

		private Object getAttribute(ObjectName objName, String attrName) throws MBeanException,
				InstanceNotFoundException, AttributeNotFoundException, ReflectionException, IOException {
			final NameValueMap values = getCachedAttributes(objName, Collections.singleton(attrName));
			Object value = values.get(attrName);
			if (value != null || values.containsKey(attrName)) {
				return value;
			}
			// Not in cache, presumably because it was omitted from the
			// getAttributes result because of an exception. Following
			// call will probably provoke the same exception.
			return conn.getAttribute(objName, attrName);
		}

		private AttributeList getAttributes(ObjectName objName, String[] attrNames) throws InstanceNotFoundException,
				ReflectionException, IOException {
			final NameValueMap values = getCachedAttributes(objName, new TreeSet<String>(Arrays.asList(attrNames)));
			final AttributeList list = new AttributeList();
			for (String attrName : attrNames) {
				final Object value = values.get(attrName);
				if (value != null || values.containsKey(attrName)) {
					list.add(new Attribute(attrName, value));
				}
			}
			return list;
		}

		private synchronized NameValueMap getCachedAttributes(ObjectName objName, Set<String> attrNames)
				throws InstanceNotFoundException, ReflectionException, IOException {
			NameValueMap values = cachedValues.get(objName);
			if (values != null && values.keySet().containsAll(attrNames)) {
				return values;
			}
			attrNames = new TreeSet<>(attrNames);
			Set<String> oldNames = cachedNames.get(objName);
			if (oldNames != null) {
				attrNames.addAll(oldNames);
			}
			values = new NameValueMap();
			final AttributeList attrs = conn.getAttributes(objName, attrNames.toArray(new String[attrNames.size()]));
			for (Attribute attr : attrs.asList()) {
				values.put(attr.getName(), attr.getValue());
			}
			cachedValues.put(objName, values);
			cachedNames.put(objName, attrNames);
			return values;
		}

		// See http://www.artima.com/weblogs/viewpost.jsp?thread=79394
		private static <K, V> Map<K, V> newMap() {
			return new HashMap<>();
		}
	}

}
