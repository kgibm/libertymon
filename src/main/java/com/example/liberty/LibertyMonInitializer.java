package com.example.liberty;

import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.ibm.websphere.monitor.jmx.ThreadPoolMXBean;

@WebListener
public final class LibertyMonInitializer implements ServletContextListener {

	private static final String SOURCE_CLASS = LibertyMonInitializer.class.getName();

	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(SOURCE_CLASS);

	static {
		LibertyMonUtilities.systemPrint("Version: " + LibertyMonUtilities.VERSION);
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		if (LOG.isLoggable(Level.FINER))
			LOG.entering(SOURCE_CLASS, "contextInitialized");

		try {
			MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

			// https://www.ibm.com/support/knowledgecenter/SSEQTP_liberty/com.ibm.websphere.wlp.doc/ae/rwlp_mbeans_list.html
			ObjectName objectName = new ObjectName("WebSphere:type=ThreadPoolStats,name=Default Executor");

			if (mbeanServer.isRegistered(objectName)) {
				
				if (LOG.isLoggable(Level.INFO))
					LOG.info("LibertyMon started");

				ThreadPoolMXBean threadPool = JMX.newMBeanProxy(mbeanServer, objectName, ThreadPoolMXBean.class);

				LibertyMonUtilities.systemPrint(threadPool.getActiveThreads());
			} else {
				LibertyMonUtilities.systemPrint("ThreadPoolStats is not registered");
			}
		} catch (Throwable t) {
			LibertyMonUtilities.handleException(t, LOG, SOURCE_CLASS, "contextInitialized", "Failed to lookup MBeans");
		}

		if (LOG.isLoggable(Level.FINER))
			LOG.exiting(SOURCE_CLASS, "contextInitialized");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if (LOG.isLoggable(Level.FINER))
			LOG.entering(SOURCE_CLASS, "contextDestroyed");

		if (LOG.isLoggable(Level.INFO))
			LOG.info("Application unloaded.");

		if (LOG.isLoggable(Level.FINER))
			LOG.exiting(SOURCE_CLASS, "contextDestroyed");
	}
}
