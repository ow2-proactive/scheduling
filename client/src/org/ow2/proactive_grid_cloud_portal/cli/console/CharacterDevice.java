package org.ow2.proactive_grid_cloud_portal.cli.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;

class CharacterDevice extends AbstractDevice {
	private BufferedReader in;
	private PrintStream out;

	public CharacterDevice(InputStream in, PrintStream out) {
		this.in = new BufferedReader(new InputStreamReader(in));
		this.out = out;
	}

	@Override
	public String readLine(String fmt, Object... args) throws IOException {
		out.printf(fmt, args);
		return in.readLine();
	}

	@Override
	public char[] readPassword(String fmt, Object... args) throws IOException {
		return readLine(fmt, args).toCharArray();
	}

	@Override
	public Writer getWriter() {
		return new PrintWriter(out);
	}

	@Override
	public void writeLine(String format, Object... args) {
		out.println(String.format(format, args));
	}
}