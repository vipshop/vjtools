package com.vip.vjtools.jmx;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;

public class ExtraCommand {

	public void execute(final String hostportOrPid, final String login, final String password, final String beanname,
			int interval) throws Exception {
		JMXConnector jmxc = Client.connect(hostportOrPid, login, password);

		try {
			MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

			gcUtilCommand(mbsc, interval);
		} finally {
			jmxc.close();
		}
	}

	private void gcUtilCommand(MBeanServerConnection mbsc, int interval) throws Exception {
		GCutilExpression gcE = new GCutilExpression(mbsc);

		String[] commands;

		if (getJavaVersion(mbsc) > 7) {
			commands = new String[] { "S", "S", "E", "O", "M", "CCS", "YGC", "YGCT", "FGC", "FGCT", "GCT" };
		} else {
			commands = new String[] { "S", "S", "E", "O", "P", "YGC", "YGCT", "FGC", "FGCT", "GCT" };
		}

		for (String commmand : commands) {
			System.out.print(commmand + "\t");
		}
		System.out.print("\n");

		while (true) {
			Object[] results = executGCutil(commands, gcE);
			for (Object result : results) {
				System.out.print(result.toString() + "\t");
			}
			System.out.print("\n");
			if (interval == 0) {
				break;
			}
			Thread.sleep(interval * 1000);
		}
	}

	private Object[] executGCutil(final String[] commands, GCutilExpression gcE) throws Exception {
		Object[] result = new Object[commands.length];

		for (int i = 0; i < commands.length; i++) {
			String command = commands[i];
			if ("S".equals(command)) {
				result[i] = gcE.getS();
			} else if ("E".equals(command)) {
				result[i] = gcE.getE();
			} else if ("O".equals(command)) {
				result[i] = gcE.getO();
			} else if ("M".equals(command)) {
				result[i] = gcE.getP();
			} else if ("P".equals(command)) {
				result[i] = gcE.getP();
			} else if ("CCS".equals(command)) {
				result[i] = gcE.getCCS();
			} else if ("YGC".equals(command)) {
				result[i] = gcE.getYGC();
			} else if ("YGCT".equals(command)) {
				result[i] = gcE.getYGCT();
			} else if ("FGC".equals(command)) {
				result[i] = gcE.getFGC();
			} else if ("FGCT".equals(command)) {
				result[i] = gcE.getFGCT();
			} else if ("GCT".equals(command)) {
				result[i] = gcE.getGCT();
			} else {
				throw new RuntimeException("Unknown Command:" + command);
			}
		}
		return result;
	}

	public static int getJavaVersion(final MBeanServerConnection mbsc) throws Exception {
		Object version = mbsc.getAttribute(Client.getObjectName("java.lang:type=Runtime"), "SpecVersion");
		String javaVersion = version.toString();
		if (javaVersion.startsWith("1.8") || Double.parseDouble(javaVersion.substring(0, 3)) > 1.7) {
			return 8;
		} else if (javaVersion.startsWith("1.7")) {
			return 7;
		} else if (javaVersion.startsWith("1.6")) {
			return 6;
		} else {
			return 0;
		}
	}
}
