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

public class LibertyMonWriter extends Thread {
	private static final String SOURCE_CLASS = LibertyMonWriter.class.getName();
	private static final Logger LOG = Logger.getLogger(SOURCE_CLASS);

	private LibertyMonitors monitors;
	private boolean running = true;
	private int sleepTime = Integer.getInteger("LIBERTYMON_SLEEP_MILLISECONDS", 60000);
	private final File directory;
	private final File file;
	private int maxFailures = Integer.getInteger("LIBERTYMON_MAX_IO_FAILURES", 5);
	private int failures = 0;
	private List<String> columns = new ArrayList<>();
	private List<Object> data = new ArrayList<>();
	private boolean backupOldLogWithDate = Boolean.parseBoolean(System.getProperty("LIBERTYMON_BOL_WDATE", "false"));
	private boolean backupOldLogOnce = Boolean.parseBoolean(System.getProperty("LIBERTYMON_BOL_ONCE", "false"));
	private boolean backupOldLogNone = Boolean.parseBoolean(System.getProperty("LIBERTYMON_BOL_NONE", "false"));
	private static final SimpleDateFormat filesdf = new SimpleDateFormat("yy.MM.dd'_'HH.mm.ss");

	private double prevProcessCPU = 0;
	private long prevGcTime = 0;
	private long prevGcs = 0;

	public LibertyMonWriter(LibertyMonitors monitors) {
		super(LibertyMonUtilities.APPNAME + "Thread");
		setDaemon(true);
		setMonitors(monitors);

		String outputDir = System.getProperty("LIBERTYMON_DIR");

		if (LOG.isLoggable(Level.FINE))
			LOG.fine("LIBERTYMON_DIR: " + outputDir);

		if (outputDir == null || outputDir.length() == 0) {
			outputDir = System.getenv("LOG_DIR");

			if (LOG.isLoggable(Level.FINE))
				LOG.fine("LOG_DIR: " + outputDir);

			if (outputDir == null || outputDir.length() == 0) {
				outputDir = System.getenv("WLP_OUTPUT_DIR");

				if (LOG.isLoggable(Level.FINE))
					LOG.fine("WLP_OUTPUT_DIR: " + outputDir);

				if (outputDir == null || outputDir.length() == 0) {
					outputDir = ".";
				} else {
					outputDir += File.separator + monitors.server.getName() + File.separator + "logs";
					if (!new File(outputDir).exists()) {
						outputDir = System.getenv("WLP_OUTPUT_DIR");
					}
				}
			}
		}

		if (LOG.isLoggable(Level.FINE))
			LOG.fine("outputDir: " + outputDir);

		directory = new File(outputDir);
		String filename = System.getProperty("LIBERTYMON_FILE", "libertymon.csv");
		file = new File(directory, filename);

		try {
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
				} else if (backupOldLogNone) {
					file.delete();
				}
			}
		} catch (Throwable t) {
			LibertyMonUtilities.handleException(t, LOG, SOURCE_CLASS, "<init>",
					"failed to rename old file " + file.getAbsolutePath());
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

			if (running) {
				process();
			}
		}

		if (LOG.isLoggable(Level.FINER))
			LOG.exiting(SOURCE_CLASS, "run");
	}

	public synchronized void process() {
		if (LOG.isLoggable(Level.FINER))
			LOG.entering(SOURCE_CLASS, "process");

		int previousColumnsSize = columns.size();
		columns.clear();
		data.clear();

		populateData();

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

	public void populateData() {
		columns.add("Time");
		data.add(Instant.now().toString());

		columns.add("Name");
		data.add(monitors.server.getName());

		columns.add("PID");
		data.add(monitors.pid);

		columns.add("Classes");
		data.add(monitors.classloading.getLoadedClassCount());

		columns.add("JavaHeap");
		data.add(monitors.memory.getHeapMemoryUsage().getUsed());

		columns.add("JVMHeap");
		data.add(monitors.memory.getNonHeapMemoryUsage().getUsed());

		columns.add("TotalThreads");
		data.add(monitors.threads.getThreadCount());

		columns.add("CPUThreads");
		int cpus = monitors.os.getAvailableProcessors();
		data.add(cpus);

		columns.add("SystemLoadAverage1Min");
		data.add(monitors.os.getSystemLoadAverage());

		columns.add("ProcessCPUCumulative");
		double processCpu = monitors.jvm.getProcessCPU();
		data.add(processCpu);

		double processCpuDiff = processCpu - prevProcessCPU;
		columns.add("ProcessCPUDiff");
		if (processCpu != -1 && prevProcessCPU > 0 && processCpuDiff >= 0) {
			data.add(processCpuDiff);
		} else {
			data.add(0);
		}
		prevProcessCPU = processCpu;

		columns.add("ProcessCPU%");
		if (processCpu != -1 && prevProcessCPU > 0 && processCpuDiff >= 0) {
			data.add((processCpu - prevProcessCPU) / (double) ((double) cpus * (double) sleepTime / 1000D));
		} else {
			data.add(0);
		}

		columns.add("GCsCumulative");
		long gcCount = monitors.jvm.getGcCount();
		data.add(gcCount);

		columns.add("GCsDiff");
		if (prevGcs > 0) {
			data.add(gcCount - prevGcs);
		} else {
			data.add(0);
		}
		prevGcs = gcCount;

		columns.add("GCTimeCumulative");
		long gctime = monitors.jvm.getGcTime();
		data.add(gctime);

		columns.add("GCTimeDiff");
		if (prevGcTime > 0) {
			data.add(gctime - prevGcTime);
		} else {
			data.add(0);
		}
		prevGcTime = gctime;

		columns.add("LibertyThreadsActive");
		data.add(monitors.libertyThreadPool.getActiveThreads());
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
