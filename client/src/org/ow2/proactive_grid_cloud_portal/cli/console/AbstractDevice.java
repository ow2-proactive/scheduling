package org.ow2.proactive_grid_cloud_portal.cli.console;

import java.io.IOException;
import java.io.Writer;

public abstract class AbstractDevice {

	public static final short STARDARD = 1;
	public static final short JLINE = 2;

	public abstract String readLine(String fmt, Object... args)
			throws IOException;

	public abstract char[] readPassword(String fmt, Object... args)
			throws IOException;

	public abstract void writeLine(String fmtm, Object... args)
			throws IOException;

	public abstract Writer getWriter();

	public static AbstractDevice getConsole(int type) throws IOException {
		switch (type) {
		case STARDARD:
			return (System.console() != null) ? new ConsoleDevice(
					System.console()) : new CharacterDevice(System.in,
					System.out);
		case JLINE:
			return new JLineDevice(System.in, System.out);
		default:
			throw new IllegalArgumentException("Unknown console type [" + type
					+ "]");
		}
	}
}
