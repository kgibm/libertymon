package com.example.liberty;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public final class LibertyMonInitializer implements ServletContextListener {

	private static final String SOURCE_CLASS = LibertyMonInitializer.class.getName();
	private static final Logger LOG = Logger.getLogger(SOURCE_CLASS);

	private LibertyMonWriter thread;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		if (LOG.isLoggable(Level.FINER))
			LOG.entering(SOURCE_CLASS, "contextInitialized");

		if (LOG.isLoggable(Level.INFO))
			LOG.info(LibertyMonUtilities.APPNAME + " " + LibertyMonUtilities.VERSION + " loaded.");

		try {
			thread = new LibertyMonWriter(LibertyMonitors.build());
			thread.start();
		} catch (Throwable t) {
			LibertyMonUtilities.handleException(t, LOG, SOURCE_CLASS, "contextInitialized", "Failed to lookup MBeans");
		}

		if (LOG.isLoggable(Level.FINER))
			LOG.exiting(SOURCE_CLASS, "contextInitialized");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if (LOG.isLoggable(Level.FINER))
			LOG.entering(SOURCE_CLASS, "contextDestroyed", thread);

		if (thread != null) {
			thread.setRunning(false);
			thread.interrupt();
			thread = null;
		}

		if (LOG.isLoggable(Level.INFO))
			LOG.info(LibertyMonUtilities.APPNAME + " unloaded.");

		if (LOG.isLoggable(Level.FINER))
			LOG.exiting(SOURCE_CLASS, "contextDestroyed");
	}
}
