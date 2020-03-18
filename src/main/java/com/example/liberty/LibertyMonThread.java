package com.example.liberty;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class LibertyMonThread extends Thread {
	private static final String SOURCE_CLASS = LibertyMonThread.class.getName();
	private static final Logger LOG = Logger.getLogger(SOURCE_CLASS);

	private LibertyMonitors monitors;
	private boolean running = true;
	private int sleepTime = Integer.getInteger("LIBERTYMON_SLEEP_MILLISECONDS", 30000);
	private final File directory;
	private final File file;
	private int maxFailures = Integer.getInteger("LIBERTYMON_MAX_IO_FAILURES", 5);
	private int failures = 0;
	private List<String> columns = new ArrayList<>();
	private List<Object> data = new ArrayList<>();
	private boolean backupOldLogWithDate = Boolean.parseBoolean(System.getProperty("LIBERTYMON_BOLWDATE", "false"));
	private boolean backupOldLogOnce = Boolean.parseBoolean(System.getProperty("LIBERTYMON_BOLONCE", "true"));
	private static final SimpleDateFormat filesdf = new SimpleDateFormat("yy.MM.dd'_'HH.mm.ss");

	public LibertyMonThread(LibertyMonitors monitors) {
		super(LibertyMonUtilities.APPNAME + "Thread");
		setDaemon(true);
		setMonitors(monitors);

		String outputDir = System.getProperty("LIBERTYMON_DIR");
		if (outputDir == null || outputDir.length() == 0) {
			outputDir = System.getenv("LOG_DIR");
			if (outputDir == null || outputDir.length() == 0) {
				outputDir = System.getenv("WLP_OUTPUT_DIR");
				if (outputDir == null || outputDir.length() == 0) {
					outputDir = ".";
				}
			}
		}

		directory = new File(outputDir);
		String filename = System.getProperty("LIBERTYMON_FILE", "libertymon.csv");
		file = new File(directory, filename);

		if (file.exists()) {
			if (backupOldLogWithDate) {
				File renamedFile = new File(outputDir,
						filename.replaceAll(".csv", "") + "_" + filesdf.format(new Date()) + ".csv");

				LibertyMonUtilities.debug(LOG, "Rotating old file to " + renamedFile.getAbsolutePath());

				file.renameTo(renamedFile);
			} else if (backupOldLogOnce) {
				File renamedFile = new File(outputDir, filename + ".prev");
				
				if (renamedFile.exists()) {
					renamedFile.delete();
				}

				LibertyMonUtilities.debug(LOG, "Rotating old file to " + renamedFile.getAbsolutePath());

				file.renameTo(renamedFile);
			}
		}

		LibertyMonUtilities.info(LOG, SOURCE_CLASS, "<init>", "Writing to " + file.getAbsolutePath());

	}

	public LibertyMonitors getMonitors() {
		return monitors;
	}

	public void setMonitors(LibertyMonitors monitors) {
		if (monitors == null)
			throw new IllegalArgumentException("monitors");
		this.monitors = monitors;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	@Override
	public void run() {
		if (LOG.isLoggable(Level.FINER))
			LOG.entering(SOURCE_CLASS, "run");

		process();

		while (running) {
			if (LOG.isLoggable(Level.FINE))
				LOG.fine("run iteration started");

			try {
				if (LOG.isLoggable(Level.FINE))
					LOG.fine("entering sleep");

				Thread.sleep(sleepTime);

				if (LOG.isLoggable(Level.FINE))
					LOG.fine("exited sleep");
			} catch (InterruptedException e) {
				if (running) { // Allow a "graceful" interrupt (no error) if running is set to false first
					LibertyMonUtilities.handleException(e, LOG, SOURCE_CLASS, "run", "sleep interrupted");
				}
			}

			process();
		}

		if (LOG.isLoggable(Level.FINER))
			LOG.exiting(SOURCE_CLASS, "run");
	}

	private void process() {
		if (LOG.isLoggable(Level.FINER))
			LOG.entering(SOURCE_CLASS, "process");

		int previousColumnsSize = columns.size();
		columns.clear();
		data.clear();

		columns.add("Time");
		data.add(Instant.now().toString());

		if (columns.size() != previousColumnsSize) {
			if (!append(columns.toArray())) {
				return;
			}
		}

		if (!append(data.toArray())) {
			return;
		}

		if (LOG.isLoggable(Level.FINER))
			LOG.exiting(SOURCE_CLASS, "process");
	}

	public boolean append(Object... values) {
		// https://commons.apache.org/proper/commons-csv/apidocs/org/apache/commons/csv/CSVPrinter.html
		try (FileWriter fw = new FileWriter(file, true)) {
			try (CSVPrinter printer = new CSVPrinter(fw, CSVFormat.DEFAULT)) {
				printer.printRecord(values);
				printer.flush();
				return true;
			}
		} catch (IOException e) {
			LibertyMonUtilities.handleException(e, LOG, SOURCE_CLASS, "process", "file I/O error");

			if (++failures >= maxFailures) {
				LibertyMonUtilities.error(LOG, SOURCE_CLASS, "append",
						"Reached maximum I/O failures, stopping thread.");
				setRunning(false);
			}

			return false;
		}
	}
}
