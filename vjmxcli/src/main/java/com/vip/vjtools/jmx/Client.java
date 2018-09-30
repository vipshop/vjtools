package com.vip.vjtools.jmx;

import java.io.File;
/*
 * Client
 *
 * $Id$
 *
 * Created on Nov 12, 2004
 *
 * Copyright (C) 2004 Internet Archive.
 *
 * This file is part of the Heritrix web crawler (crawler.archive.org).
 *
 * Heritrix is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser Public License
 * as published by the Free Software Foundation; either version 2.1 of the License, or any later version.
 *
 * Heritrix is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along with Heritrix; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

/**
 * A Simple Command-Line JMX Client. Tested against the JDK 1.5.0 JMX Agent. See
 * <a href="http://java.sun.com/j2se/1.5.0/docs/guide/management/agent.html">Monitoring and Management Using JMX</a>.
 * <p>
 * Can supply credentials and do primitive string representation of tabular and composite openmbeans.
 * @author stack
 */
public class Client {
	private static final Logger logger = Logger.getLogger(Client.class.getName());

	public static final String V_GCUTIL_BEAN_NAME = "gcutil";

	private static final String LOCAL_CONNECTOR_ADDRESS_PROP = "com.sun.management.jmxremote.localConnectorAddress";

	/**
	 * Usage string.
	 */
	private static final String USAGE = "See README.md";
	/**
	 * Pattern that matches a command name followed by an optional equals and optional comma-delimited list of
	 * arguments.
	 */
	protected static final Pattern CMD_LINE_ARGS_PATTERN = Pattern.compile("^([^=]+)(?:(?:\\=)(.+))?$");

	private static final String CREATE_CMD_PREFIX = "create=";

	public static void main(String[] args) {
		Client client = new Client();
		// Set the logger to use our all-on-one-line formatter.
		Logger l = Logger.getLogger("");
		Handler[] hs = l.getHandlers();
		for (int i = 0; i < hs.length; i++) {
			Handler h = hs[0];
			if (h instanceof ConsoleHandler) {
				h.setFormatter(client.new OneLineSimpleLogger());
			}
		}
		try {
			client.execute(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected static void usage() {
		usage(0, null);
	}

	protected static void usage(int exitCode, String message) {
		if (message != null && message.length() > 0) {
			System.out.println(message);
		}
		System.out.println(USAGE);
		System.exit(exitCode);
	}

	/**
	 * Constructor.
	 */
	public Client() {
		super();
	}

	/**
	 * Parse a 'login:password' string. Assumption is that no colon in the login name.
	 * @param userpass
	 * @return Array of strings with login in first position.
	 */
	protected String[] parseUserpass(final String userpass) {
		if (userpass == null || userpass.equals("-")) {
			return null;
		}
		int index = userpass.indexOf(':');
		if (index <= 0) {
			throw new RuntimeException("Unable to parse: " + userpass);
		}
		return new String[] { userpass.substring(0, index), userpass.substring(index + 1) };
	}

	/**
	 * @param login
	 * @param password
	 * @return Credentials as map for RMI.
	 */
	protected static Map formatCredentials(final String login, final String password) {
		Map env = null;
		String[] creds = new String[] { login, password };
		env = new HashMap(1);
		env.put(JMXConnector.CREDENTIALS, creds);
		return env;
	}

	/**
	 * 扩展支持以pid or host-port两种方式接入
	 */
	public static JMXConnector connect(final String hostportOrPid, final String login, final String password)
			throws IOException {
		// ./vjmxcli.sh - 127.0.0.1:8060 gcutil
		if (hostportOrPid.contains(":")) {
			JMXServiceURL rmiurl = new JMXServiceURL(
					"service:jmx:rmi://" + hostportOrPid + "/jndi/rmi://" + hostportOrPid + "/jmxrmi");
			return JMXConnectorFactory.connect(rmiurl, formatCredentials(login, password));
		} else {
			// ./vjmxcli.sh - 112222 gcutil
			String localAddress = getLocalConnectorAddress(hostportOrPid);
			JMXServiceURL localRmiurl = new JMXServiceURL(localAddress);
			return JMXConnectorFactory.connect(localRmiurl);
		}
	}

	/**
	 * VirtualMachine保证JMX Agent已启动, 并向JMXClient提供连接地址
	 * 
	 * 地址样例：service:jmx:rmi://127.0.0.1/stub/rO0ABXN9AAAAAQAl...
	 */
	public static String getLocalConnectorAddress(String pid) throws IOException {// NOSONAR
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
			// JDK8后有更直接的vm.startLocalManagementAgent()方法
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

	/**
	 * Version of execute called from the cmdline. Prints out result of execution on stdout. Parses cmdline args. Then
	 * calls {@link #execute(String, String, String, String, String[], boolean)}.
	 * @param args Cmdline args.
	 * @throws Exception
	 */
	protected void execute(final String[] args) throws Exception {
		// Process command-line.
		if (args.length == 0 || args.length == 1) {
			usage();
		}
		String userpass = args[0];
		String hostportOrPid = args[1];
		String beanname = null;// vGCutil
		String[] command = null;
		if (args.length > 2) {
			beanname = args[2];
		}
		if (args.length > 3) {
			command = new String[args.length - 3];
			for (int i = 3; i < args.length; i++) {
				command[i - 3] = args[i];
			}
		}
		String[] loginPassword = parseUserpass(userpass);

		// 模拟GC Util命令的扩展
		if (V_GCUTIL_BEAN_NAME.equalsIgnoreCase(beanname)) {
			// 支持配置interval 固定事件间隔连续输出
			int interval = 0;
			if (command != null && command.length > 0) {
				try {
					interval = Math.abs(Integer.parseInt(command[0]));// 拿绝对值, 避免负数的情况
				} catch (NumberFormatException e) {// NOSONAR
				}
			}

			ExtraCommand extraCommand = new ExtraCommand();
			extraCommand.execute(hostportOrPid, ((loginPassword == null) ? null : loginPassword[0]),
					((loginPassword == null) ? null : loginPassword[1]), beanname, interval);
			return;
		}

		Object[] result = execute(hostportOrPid, ((loginPassword == null) ? null : loginPassword[0]),
				((loginPassword == null) ? null : loginPassword[1]), beanname, command);
		if (result != null) {
			for (int i = 0; i < result.length; i++) {
				if (result[i] != null && result[i].toString().length() > 0) {
					if (command != null) {
						logger.info(command[i] + ": " + result[i]);
					} else {
						logger.info("\n" + result[i].toString());
					}
				}
			}
		}
	}

	protected Object[] execute(final String hostport, final String login, final String password, final String beanname,
			final String[] command) throws Exception {
		return execute(hostport, login, password, beanname, command, false);
	}

	public Object[] executeOneCmd(final String hostport, final String login, final String password,
			final String beanname, final String command) throws Exception {
		return execute(hostport, login, password, beanname, new String[] { command }, true);
	}

	/**
	 * Execute command against remote JMX agent.
	 * @param hostportOrPid 'host:port' combination.
	 * @param login RMI login to use.
	 * @param password RMI password to use.
	 * @param beanname Name of remote bean to run command against.
	 * @param command Array of commands to run.
	 * @param oneBeanOnly Set true if passed <code>beanname</code> is an exact name and the query for a bean is only
	 * supposed to return one bean instance. If not, we raise an exception (Otherwise, if false, then we deal with
	 * possibility of multiple bean instances coming back from query). Set to true when want to get an attribute or run
	 * an operation.
	 * @return Array of results -- one per command.
	 * @throws Exception
	 */
	protected Object[] execute(final String hostportOrPid, final String login, final String password,
			final String beanname, String[] command, final boolean oneBeanOnly) throws Exception {
		JMXConnector jmxc = connect(hostportOrPid, login, password);
		Object[] result = null;

		try {
			MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
			result = doBeans(mbsc, getObjectName(beanname), command, oneBeanOnly);
		} finally {
			jmxc.close();
		}
		return result;
	}

	public static ObjectName getObjectName(final String beanname)
			throws MalformedObjectNameException, NullPointerException {
		return notEmpty(beanname) ? new ObjectName(beanname) : null;
	}

	public static boolean notEmpty(String s) {
		return s != null && s.length() > 0;
	}

	protected static Object[] doBeans(final MBeanServerConnection mbsc, final ObjectName objName,
			final String[] command, final boolean oneBeanOnly) throws Exception {
		Object[] result = null;
		Set beans = mbsc.queryMBeans(objName, null);
		if (beans.isEmpty()) {
			// No bean found. Check if we are to create a bean?
			if (command.length == 1 && notEmpty(command[0]) && command[0].startsWith(CREATE_CMD_PREFIX)) {
				String className = command[0].substring(CREATE_CMD_PREFIX.length());
				mbsc.createMBean(className, objName);
			} else {
				// TODO: Is there a better JMX exception that RE for this
				// scenario?
				throw new RuntimeException(objName.getCanonicalName() + " not registered.");
			}
		} else if (beans.size() == 1) {
			result = doBean(mbsc, (ObjectInstance) beans.iterator().next(), command);
		} else {
			if (oneBeanOnly) {
				throw new RuntimeException("Only supposed to be one bean " + "query result");
			}
			// This is case of multiple beans in query results.
			// Print name of each into a StringBuffer. Return as one
			// result.
			StringBuffer buffer = new StringBuffer();
			for (Iterator i = beans.iterator(); i.hasNext();) {
				Object obj = i.next();
				if (obj instanceof ObjectName) {
					buffer.append((((ObjectName) obj).getCanonicalName()));
				} else if (obj instanceof ObjectInstance) {
					buffer.append((((ObjectInstance) obj).getObjectName().getCanonicalName()));
				} else {
					throw new RuntimeException("Unexpected object type: " + obj);
				}
				buffer.append("\n");
			}
			result = new String[] { buffer.toString() };
		}
		return result;
	}

	/**
	 * Get attribute or run operation against passed bean <code>instance</code>.
	 * 
	 * @param mbsc Server connection.
	 * @param instance Bean instance we're to get attributes from or run operation against.
	 * @param command Command to run (May be null).
	 * @return Result. If multiple commands, multiple results.
	 * @throws Exception
	 */
	protected static Object[] doBean(MBeanServerConnection mbsc, ObjectInstance instance, String[] command)
			throws Exception {
		// If no command, then print out list of attributes and operations.
		if (command == null || command.length <= 0) {
			return new String[] { listOptions(mbsc, instance) };
		}

		// Maybe multiple attributes/operations listed on one command line.
		Object[] result = new Object[command.length];
		for (int i = 0; i < command.length; i++) {
			result[i] = doSubCommand(mbsc, instance, command[i]);
		}
		return result;
	}

	public static Object doSubCommand(MBeanServerConnection mbsc, ObjectInstance instance, String subCommand)
			throws Exception {
		// First, handle special case of our being asked to destroy a bean.
		if (subCommand.equals("destroy")) {
			mbsc.unregisterMBean(instance.getObjectName());
			return null;
		} else if (subCommand.startsWith(CREATE_CMD_PREFIX)) {
			throw new IllegalArgumentException("You cannot call create " + "on an already existing bean.");
		}

		// Get attribute and operation info.
		MBeanAttributeInfo[] attributeInfo = mbsc.getMBeanInfo(instance.getObjectName()).getAttributes();
		MBeanOperationInfo[] operationInfo = mbsc.getMBeanInfo(instance.getObjectName()).getOperations();
		// Now, bdbje JMX bean doesn't follow the convention of attributes
		// having uppercase first letter and operations having lowercase
		// first letter. But most beans do. Be prepared to handle the bdbje
		// case.
		Object result = null;
		if (Character.isUpperCase(subCommand.charAt(0))) {
			// Probably an attribute.
			if (!isFeatureInfo(attributeInfo, subCommand) && isFeatureInfo(operationInfo, subCommand)) {
				// Its not an attribute name. Looks like its name of an
				// operation. Try it.
				result = doBeanOperation(mbsc, instance, subCommand, operationInfo);
			} else {
				// Then it is an attribute OR its not an attribute name nor
				// operation name and the below invocation will throw a
				// AttributeNotFoundException.
				result = doAttributeOperation(mbsc, instance, subCommand, attributeInfo);
			}
		} else {
			// Must be an operation.
			if (!isFeatureInfo(operationInfo, subCommand) && isFeatureInfo(attributeInfo, subCommand)) {
				// Its not an operation name but looks like it could be an
				// attribute name. Try it.
				result = doAttributeOperation(mbsc, instance, subCommand, attributeInfo);
			} else {
				// Its an operation name OR its neither operation nor attribute
				// name and the below will throw a NoSuchMethodException.
				result = doBeanOperation(mbsc, instance, subCommand, operationInfo);
			}
		}

		// Look at the result. Is it of composite or tabular type?
		// If so, convert to a String representation.
		if (result instanceof CompositeData) {
			result = recurseCompositeData(new StringBuffer("\n"), "", "", (CompositeData) result);
		} else if (result instanceof TabularData) {
			result = recurseTabularData(new StringBuffer("\n"), "", "", (TabularData) result);
		} else if (result instanceof String[]) {
			String[] strs = (String[]) result;
			StringBuffer buffer = new StringBuffer("\n");
			for (int i = 0; i < strs.length; i++) {
				buffer.append(strs[i]);
				buffer.append("\n");
			}
			result = buffer;
		} else if (result instanceof AttributeList) {
			AttributeList list = (AttributeList) result;
			if (list.isEmpty()) {
				result = null;
			} else {
				StringBuffer buffer = new StringBuffer("\n");
				for (Iterator ii = list.iterator(); ii.hasNext();) {
					Attribute a = (Attribute) ii.next();
					buffer.append(a.getName());
					buffer.append(": ");
					buffer.append(a.getValue());
					buffer.append("\n");
				}
				result = buffer;
			}
		}
		return result;
	}

	protected static boolean isFeatureInfo(MBeanFeatureInfo[] infos, String cmd) {
		return getFeatureInfo(infos, cmd) != null;
	}

	protected static MBeanFeatureInfo getFeatureInfo(MBeanFeatureInfo[] infos, String cmd) {
		// Cmd may be carrying arguments. Don't count them in the compare.
		int index = cmd.indexOf('=');
		String name = (index > 0) ? cmd.substring(0, index) : cmd;
		for (int i = 0; i < infos.length; i++) {
			if (infos[i].getName().equals(name)) {
				return infos[i];
			}
		}
		return null;
	}

	protected static StringBuffer recurseTabularData(StringBuffer buffer, String indent, String name,
			TabularData data) {
		addNameToBuffer(buffer, indent, name);
		java.util.Collection c = data.values();
		for (Iterator i = c.iterator(); i.hasNext();) {
			Object obj = i.next();
			if (obj instanceof CompositeData) {
				recurseCompositeData(buffer, indent + " ", "", (CompositeData) obj);
			} else if (obj instanceof TabularData) {
				recurseTabularData(buffer, indent, "", (TabularData) obj);
			} else {
				buffer.append(obj);
			}
		}
		return buffer;
	}

	protected static StringBuffer recurseCompositeData(StringBuffer buffer, String indent, String name,
			CompositeData data) {
		indent = addNameToBuffer(buffer, indent, name);
		for (Iterator i = data.getCompositeType().keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			Object o = data.get(key);
			if (o instanceof CompositeData) {
				recurseCompositeData(buffer, indent + " ", key, (CompositeData) o);
			} else if (o instanceof TabularData) {
				recurseTabularData(buffer, indent, key, (TabularData) o);
			} else {
				buffer.append(indent);
				buffer.append(key);
				buffer.append(": ");
				buffer.append(o);
				buffer.append("\n");
			}
		}
		return buffer;
	}

	protected static String addNameToBuffer(StringBuffer buffer, String indent, String name) {
		if (name == null || name.length() == 0) {
			return indent;
		}
		buffer.append(indent);
		buffer.append(name);
		buffer.append(":\n");
		// Move all that comes under this 'name' over by one space.
		return indent + " ";
	}

	/**
	 * Class that parses commandline arguments. Expected format is 'operationName=arg0,arg1,arg2...'. We are assuming no
	 * spaces nor comma's in argument values.
	 */
	protected static class CommandParse {
		private String cmd;
		private String[] args;

		protected CommandParse(String command) throws ParseException {
			parse(command);
		}

		private void parse(String command) throws ParseException {
			Matcher m = CMD_LINE_ARGS_PATTERN.matcher(command);
			if (m == null || !m.matches()) {
				throw new ParseException("Failed parse of " + command, 0);
			}

			this.cmd = m.group(1);
			if (m.group(2) != null && m.group(2).length() > 0) {
				this.args = m.group(2).split(",");
			} else {
				this.args = null;
			}
		}

		protected String getCmd() {
			return this.cmd;
		}

		protected String[] getArgs() {
			return this.args;
		}
	}

	protected static Object doAttributeOperation(MBeanServerConnection mbsc, ObjectInstance instance, String command,
			MBeanAttributeInfo[] infos) throws Exception {
		// Usually we get attributes. If an argument, then we're being asked
		// to set attribute.
		CommandParse parse = new CommandParse(command);
		if (parse.getArgs() == null || parse.getArgs().length == 0) {
			// Special-casing. If the subCommand is 'Attributes', then return
			// list of all attributes.
			if (command.equals("Attributes")) {
				String[] names = new String[infos.length];
				for (int i = 0; i < infos.length; i++) {
					names[i] = infos[i].getName();
				}
				return mbsc.getAttributes(instance.getObjectName(), names);
			}
			return mbsc.getAttribute(instance.getObjectName(), parse.getCmd());
		}
		if (parse.getArgs().length != 1) {
			throw new IllegalArgumentException("One only argument setting " + "attribute values: " + parse.getArgs());
		}
		// Get first attribute of name 'cmd'. Assumption is no method
		// overrides. Then, look at the attribute and use its type.
		MBeanAttributeInfo info = (MBeanAttributeInfo) getFeatureInfo(infos, parse.getCmd());
		java.lang.reflect.Constructor c = Class.forName(info.getType()).getConstructor(new Class[] { String.class });
		Attribute a = new Attribute(parse.getCmd(), c.newInstance(new Object[] { parse.getArgs()[0] }));
		mbsc.setAttribute(instance.getObjectName(), a);
		return null;
	}

	protected static Object doBeanOperation(MBeanServerConnection mbsc, ObjectInstance instance, String command,
			MBeanOperationInfo[] infos) throws Exception {
		// Parse command line.
		CommandParse parse = new CommandParse(command);

		// Get first method of name 'cmd'. Assumption is no method
		// overrides. Then, look at the method and use its signature
		// to make sure client sends over parameters of the correct type.
		MBeanOperationInfo op = (MBeanOperationInfo) getFeatureInfo(infos, parse.getCmd());
		Object result = null;
		if (op == null) {
			result = "Operation " + parse.getCmd() + " not found.";
		} else {
			MBeanParameterInfo[] paraminfos = op.getSignature();
			int paraminfosLength = (paraminfos == null) ? 0 : paraminfos.length;
			int objsLength = (parse.getArgs() == null) ? 0 : parse.getArgs().length;
			if (paraminfosLength != objsLength) {
				result = "Passed param count does not match signature count";
			} else {
				String[] signature = new String[paraminfosLength];
				Object[] params = (paraminfosLength == 0) ? null : new Object[paraminfosLength];
				for (int i = 0; i < paraminfosLength; i++) {
					MBeanParameterInfo paraminfo = paraminfos[i];
					java.lang.reflect.Constructor c = Class.forName(paraminfo.getType())
							.getConstructor(new Class[] { String.class });
					params[i] = c.newInstance(new Object[] { parse.getArgs()[i] });
					signature[i] = paraminfo.getType();
				}
				result = mbsc.invoke(instance.getObjectName(), parse.getCmd(), params, signature);
			}
		}
		return result;
	}

	protected static String listOptions(MBeanServerConnection mbsc, ObjectInstance instance)
			throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException {
		StringBuffer result = new StringBuffer();
		MBeanInfo info = mbsc.getMBeanInfo(instance.getObjectName());
		MBeanAttributeInfo[] attributes = info.getAttributes();
		if (attributes.length > 0) {
			result.append("Attributes:");
			result.append("\n");
			for (int i = 0; i < attributes.length; i++) {
				result.append(' ' + attributes[i].getName() + ": " + attributes[i].getDescription() + " (type="
						+ attributes[i].getType() + ")");
				result.append("\n");
			}
		}
		MBeanOperationInfo[] operations = info.getOperations();
		if (operations.length > 0) {
			result.append("Operations:");
			result.append("\n");
			for (int i = 0; i < operations.length; i++) {
				MBeanParameterInfo[] params = operations[i].getSignature();
				StringBuffer paramsStrBuffer = new StringBuffer();
				if (params != null) {
					for (int j = 0; j < params.length; j++) {
						paramsStrBuffer.append("\n   name=");
						paramsStrBuffer.append(params[j].getName());
						paramsStrBuffer.append(" type=");
						paramsStrBuffer.append(params[j].getType());
						paramsStrBuffer.append(" ");
						paramsStrBuffer.append(params[j].getDescription());
					}
				}

				result.append(' ' + operations[i].getName() + ": " + operations[i].getDescription() + "\n  Parameters "
						+ params != null ? params.length
								: 0 + ", return type=" + operations[i].getReturnType() + paramsStrBuffer.toString());
				result.append("\n");
			}
		}
		return result.toString();
	}

	/**
	 * Logger that writes entry on one line with less verbose date. Modelled on the OneLineSimpleLogger from Heritrix.
	 * 
	 * @author stack
	 * @version $Revision$, $Date$
	 */
	private class OneLineSimpleLogger extends SimpleFormatter {
		/**
		 * Date instance.
		 * 
		 * Keep around instance of date.
		 */
		private Date date = new Date();

		/**
		 * Field position instance.
		 * 
		 * Keep around this instance.
		 */
		private FieldPosition position = new FieldPosition(0);

		/**
		 * MessageFormatter for date.
		 */
		private SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss Z");

		/**
		 * Persistent buffer in which we conjure the log.
		 */
		private StringBuffer buffer = new StringBuffer();

		public OneLineSimpleLogger() {
			super();
		}

		@Override
		public synchronized String format(LogRecord record) {
			this.buffer.setLength(0);
			this.date.setTime(record.getMillis());
			this.position.setBeginIndex(0);
			this.formatter.format(this.date, this.buffer, this.position);
			this.buffer.append(' ');
			if (record.getSourceClassName() != null) {
				this.buffer.append(record.getSourceClassName());
			} else {
				this.buffer.append(record.getLoggerName());
			}
			this.buffer.append(' ');
			this.buffer.append(formatMessage(record));
			this.buffer.append(System.getProperty("line.separator"));
			if (record.getThrown() != null) {
				try {
					StringWriter writer = new StringWriter();
					PrintWriter printer = new PrintWriter(writer);
					record.getThrown().printStackTrace(printer);
					writer.close();
					this.buffer.append(writer.toString());
				} catch (Exception e) {
					this.buffer.append("Failed to get stack trace: " + e.getMessage());
				}
			}
			return this.buffer.toString();
		}
	}

}
