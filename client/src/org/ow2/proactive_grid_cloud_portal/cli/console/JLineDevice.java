package org.ow2.proactive_grid_cloud_portal.cli.console;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive_grid_cloud_portal.cli.RestCommand;

import jline.ArgumentCompletor;
import jline.ClassNameCompletor;
import jline.Completor;
import jline.ConsoleReader;
import jline.FileNameCompletor;
import jline.History;
import jline.MultiCompletor;
import jline.SimpleCompletor;

public class JLineDevice extends AbstractDevice {
	private static final int HLENGTH = 20;
	private static final String HFILE = System.getProperty("user.home")
			+ File.separator + ".proactive" + File.separator + "restcli.hist";

	private ConsoleReader reader;
	private PrintWriter writer;

	public JLineDevice(InputStream in, PrintStream out) throws IOException {
		File hfile = new File(HFILE);
		if (!hfile.exists()) {
			hfile.createNewFile();
		}
		writer = new PrintWriter(out, true);
		reader = new ConsoleReader(in, writer);
		reader.setHistory(new History(hfile));
		Completor[] completors = new Completor[] {
				new SimpleCompletor(getCommandsAsArray()),
				new ClassNameCompletor(), new FileNameCompletor() };
		reader.addCompletor(new ArgumentCompletor(
				new MultiCompletor(completors)));

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				writeHistory();
			}
		}));
	}

	public Writer getWriter() {
		return writer;
	}

	private void writeHistory() {
		try {
			File hfile = new File(HFILE);
			if (hfile.exists()) {
				hfile.delete();
			}
			hfile.createNewFile();
			PrintWriter pw = new PrintWriter(hfile);
			@SuppressWarnings("rawtypes")
			List historyList = reader.getHistory().getHistoryList();
			if (historyList.size() > HLENGTH) {
				historyList = historyList.subList(historyList.size() - HLENGTH,
						historyList.size());
			}
			for (int index = 0; index < historyList.size(); index++) {
				pw.println(historyList.get(index));
			}
			pw.flush();
			pw.close();
		} catch (IOException fnfe) {
		}
	}

	private String[] getCommandsAsArray() {
		ArrayList<String> cmds = new ArrayList<String>();
		for (RestCommand command : RestCommand.values()) {
			if (command.getJsOpt() != null) {
				cmds.add(command.getJsOpt());
			}
		}
		return cmds.toArray(new String[cmds.size()]);
	}

	@Override
	public String readLine(String fmt, Object... args) throws IOException {
		return reader.readLine(String.format(fmt, args));
	}

	@Override
	public char[] readPassword(String fmt, Object... args) throws IOException {
		// String.format(fmt, args),
		return reader.readLine(String.format(fmt, args), new Character('*'))
				.toCharArray();

	}

	@Override
	public void writeLine(String format, Object... args) throws IOException {
		reader.printString(String.format(format, args));
		reader.printNewline();
	}

}
