package com.example.liberty;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public final class LibertyMonInitializer implements ServletContextListener {

	private static final String SOURCE_CLASS = LibertyMonInitializer.class.getName();

	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(SOURCE_CLASS);

	static {
		LibertyMon.systemPrint("Version: " + LibertyMon.VERSION);
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		if (LOG.isLoggable(Level.FINER))
			LOG.entering(SOURCE_CLASS, "contextInitialized");
		

		if (LOG.isLoggable(Level.FINER))
			LOG.exiting(SOURCE_CLASS, "contextInitialized");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if (LOG.isLoggable(Level.FINER))
			LOG.entering(SOURCE_CLASS, "contextDestroyed");

		LibertyMon.systemPrint("Application unloaded.");

		if (LOG.isLoggable(Level.FINER))
			LOG.exiting(SOURCE_CLASS, "contextDestroyed");
	}
}
