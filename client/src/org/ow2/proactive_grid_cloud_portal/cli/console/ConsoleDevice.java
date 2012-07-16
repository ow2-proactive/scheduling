package org.ow2.proactive_grid_cloud_portal.cli.console;

import java.io.Console;
import java.io.IOException;
import java.io.Writer;

class ConsoleDevice extends AbstractDevice {
	private Console console;

	public ConsoleDevice(Console console) {
		this.console = console;
	}

	@Override
	public String readLine(String fmt, Object... args) throws IOException {
		return console.readLine(fmt, args);
	}

	@Override
	public char[] readPassword(String fmt, Object... args) throws IOException {
		return console.readPassword(fmt, args);
	}

	@Override
	public Writer getWriter() {
		return console.writer();
	}

	@Override
	public void writeLine(String format, Object... args) {
		console.printf(format, args);
	}

}