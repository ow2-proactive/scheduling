package org.ow2.proactive.scheduler.ext.mapreduce.logging;

public interface Logger {

	public void info(String msg);

	public void profile(String msg);

	public void debug(String msg);

	public void debug(String msg, Exception e);

	public void warning(String msg);

	public void warning(String message, Exception e);

	public void error(String message);

	public void error(String message, Exception e);

	public void setDebugLogLevel(boolean debugLogLevel);

	public void setProfileLogLevel(boolean profileLogLevel);
}
