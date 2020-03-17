package com.example.liberty;

import java.util.logging.Logger;

public final class LibertyMonUtilities {
	public static final String APPNAME = "LibertyMon";
	public static final String VERSION = "0.1.20200317";

	private static final String SOURCE_CLASS = LibertyMonUtilities.class.getName();
	
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(SOURCE_CLASS);

	public static final void systemPrint(Object message) {
		System.out.println(APPNAME + ": " + message);
	}
	
	public static final void handleException(Throwable t, Logger log, String sourceClass, String sourceMethod, String message) {
		systemPrint("ERROR: " + sourceClass + "." + sourceMethod + ": " + message);
		t.printStackTrace();
	}
}
