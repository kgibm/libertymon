package com.example.liberty;

import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.ibm.websphere.kernel.server.ServerInfoMBean;
import com.ibm.websphere.monitor.jmx.JvmMXBean;
import com.ibm.websphere.monitor.jmx.ThreadPoolMXBean;

public class LibertyMonitors {
	private static final String SOURCE_CLASS = LibertyMonitors.class.getName();
	private static final Logger LOG = Logger.getLogger(SOURCE_CLASS);

	public ServerInfoMBean server;
	public JvmMXBean jvm;
	public ThreadPoolMXBean threadPool;

	public static LibertyMonitors build() throws MalformedObjectNameException {
		return new LibertyMonitors().create();
	}

	public LibertyMonitors create() throws MalformedObjectNameException {
		// https://www.ibm.com/support/knowledgecenter/SSEQTP_liberty/com.ibm.websphere.wlp.doc/ae/rwlp_mbeans_list.html

		// https://www.ibm.com/support/knowledgecenter/SSEQTP_liberty/com.ibm.websphere.javadoc.liberty.doc/com.ibm.websphere.appserver.api.kernel.service_1.0-javadoc/com/ibm/websphere/kernel/server/ServerInfoMBean.html
		server = lookupMXBean(ServerInfoMBean.OBJECT_NAME, ServerInfoMBean.class);
		if (server == null) {
			LibertyMonUtilities.error(LOG, SOURCE_CLASS, "create",
					"ServerInfo MXBean can't be looked up; probably something wrong with the MBeanServer, Liberty, or this is not a Liberty process.");
		}

		// https://www.ibm.com/support/knowledgecenter/SSEQTP_liberty/com.ibm.websphere.wlp.doc/ae/rwlp_mon_jvm.html
		// https://www.ibm.com/support/knowledgecenter/SSEQTP_liberty/com.ibm.websphere.javadoc.liberty.doc/com.ibm.websphere.appserver.api.monitor_1.1-javadoc/com/ibm/websphere/monitor/jmx/JvmMXBean.html
		jvm = lookupMXBean("WebSphere:type=JvmStats", JvmMXBean.class);
		if (jvm == null) {
			LibertyMonUtilities.warning(LOG, SOURCE_CLASS, "create",
					"monitor feature not enabled. Some statistics will be skipped.");
		}

		// https://www.ibm.com/support/knowledgecenter/SSEQTP_liberty/com.ibm.websphere.wlp.doc/ae/rwlp_mon_threadpool.html
		// https://www.ibm.com/support/knowledgecenter/SSEQTP_liberty/com.ibm.websphere.javadoc.liberty.doc/com.ibm.websphere.appserver.api.monitor_1.1-javadoc/com/ibm/websphere/monitor/jmx/ThreadPoolMXBean.html
		threadPool = lookupMXBean("WebSphere:type=ThreadPoolStats,name=Default Executor", ThreadPoolMXBean.class);

		return this;
	}

	public static <T> T lookupMXBean(String objectName, Class<T> mxbeanClass) throws MalformedObjectNameException {
		if (LOG.isLoggable(Level.FINER))
			LOG.entering(SOURCE_CLASS, "lookupMXBean", new Object[] { objectName, mxbeanClass });

		MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
		ObjectName objName = new ObjectName(objectName);
		T result = null;
		if (mbeanServer.isRegistered(objName)) {
			result = JMX.newMBeanProxy(mbeanServer, objName, mxbeanClass);
		}

		if (LOG.isLoggable(Level.FINER))
			LOG.exiting(SOURCE_CLASS, "lookupMXBean", result);

		return result;
	}
}
