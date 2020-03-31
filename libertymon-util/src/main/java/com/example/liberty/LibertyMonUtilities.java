package com.example.liberty;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class LibertyMonUtilities {
	public static final String APPNAME = "LibertyMon";
	public static final String VERSION = "v0.2.20200331";

	private static final String SOURCE_CLASS = LibertyMonUtilities.class.getName();

	// Useful to send info message to the Liberty console
	public static boolean infoAsWarning = Boolean.parseBoolean(System.getProperty("LIBERTYMON_INFOASWARNING", "false"));
	public static boolean debugAsWarning = Boolean
			.parseBoolean(System.getProperty("LIBERTYMON_DEBUGASWARNING", "false"));

	public static final void systemPrint(Object message) {
		System.out.println(APPNAME + ": " + message);
	}

	public static final void handleException(Throwable t, Logger log, String sourceClass, String sourceMethod,
			String message) {
		error(log, sourceClass, sourceMethod, message + " - " + t.getLocalizedMessage());
		t.printStackTrace();
	}

	public static final void info(Logger log, String sourceClass, String sourceMethod, String message) {
		if (infoAsWarning) {
			warning(log, sourceClass, sourceMethod, message, false);
		} else {
			if (log.isLoggable(Level.INFO))
				log.info(APPNAME + ": " + message);
		}
	}

	public static final void warning(Logger log, String sourceClass, String sourceMethod, String message) {
		warning(log, sourceClass, sourceMethod, message, true);
	}

	public static final void warning(Logger log, String sourceClass, String sourceMethod, String message,
			boolean showPrefix) {
		if (log.isLoggable(Level.WARNING))
			log.warning(APPNAME + ": " + (showPrefix ? "WARNING: " : "") + message + " (" + sourceClass + "."
					+ sourceMethod + ")");
	}

	public static final void error(Logger log, String sourceClass, String sourceMethod, String message) {
		if (log.isLoggable(Level.SEVERE))
			log.severe(APPNAME + ": ERROR: " + message + " (" + sourceClass + "." + sourceMethod + ")");
	}

	public static final void debug(Logger log, String message) {
		if (debugAsWarning) {
			warning(log, SOURCE_CLASS, "debug", message, false);
		} else {
			if (log.isLoggable(Level.FINE))
				log.fine(message);
		}
	}
}
