package com.example.liberty;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Schedule;
import javax.ejb.Singleton;

@Singleton
public class LibertyMonInitializer {
	private static final String SOURCE_CLASS = LibertyMonInitializer.class.getName();
	private static final Logger LOG = Logger.getLogger(SOURCE_CLASS);

	private boolean firstExecution = true;
	private LibertyMonWriter libertyMon;

	@Schedule(second = "0", minute = "*", hour = "*", persistent = false)
	public void run() {
		if (LOG.isLoggable(Level.FINER))
			LOG.entering(SOURCE_CLASS, "run");

		synchronized (this) {
			if (firstExecution) {
				firstExecution = false;

				if (LOG.isLoggable(Level.INFO))
					LOG.info(LibertyMonUtilities.APPNAME + " " + LibertyMonUtilities.VERSION + " loaded.");

				try {
					libertyMon = new LibertyMonWriter(LibertyMonitors.build());
				} catch (Throwable t) {
					LibertyMonUtilities.handleException(t, LOG, SOURCE_CLASS, "run", "Failed to lookup MBeans");
				}
			}
		}

		if (libertyMon != null) {
			libertyMon.process();
		}

		if (LOG.isLoggable(Level.FINER))
			LOG.exiting(SOURCE_CLASS, "run");
	}
}
