/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.cli.console;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import jline.console.ConsoleReader;
import jline.console.completer.AggregateCompleter;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.FileNameCompleter;
import jline.console.history.FileHistory;

import org.ow2.proactive_grid_cloud_portal.cli.CommandSet;


public class JLineDevice extends AbstractDevice {
    private static final String HFILE = System.getProperty("user.home") + File.separator + ".proactive" +
        File.separator + "restcli.hist";
    private final FileHistory history;

    private ConsoleReader reader;
    private PrintWriter writer;

    public JLineDevice(InputStream in, PrintStream out) throws IOException {
        File hfile = new File(HFILE);
        if (!hfile.exists()) {
            File parentFile = hfile.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            hfile.createNewFile();
        }
        writer = new PrintWriter(out, true);
        reader = new ConsoleReader(in, out);
        history = new FileHistory(hfile);
        reader.setHistory(history);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                writeHistory();
            }
        }));
    }

    @Override
    public Writer getWriter() {
        return writer;
    }

    private void writeHistory() {
        try {
            history.flush();
        } catch (IOException ignored) {
        }
    }

    private String[] getCommandsAsArray(CommandSet.Entry[] entries) {
        List<String> cmds = new ArrayList<>();
        for (CommandSet.Entry entry : entries) {
            if (entry.jsCommand() != null) {
                String jsCommand = entry.jsCommand();
                int index = jsCommand.indexOf('(');
                if ((jsCommand.indexOf(')') - index) == 1) {
                    cmds.add(jsCommand.substring(0, index + 2));
                } else {
                    cmds.add(jsCommand.substring(0, index + 1));
                }
            }
        }

        return cmds.toArray(new String[cmds.size()]);
    }

    public void setCommands(CommandSet.Entry[] entries) throws IOException {
        AggregateCompleter aggregateCompleter = new AggregateCompleter(new SimpleCompletor(
            getCommandsAsArray(entries)), new ClassNameCompletor(), new FileNameCompleter());

        ArgumentCompleter argumentCompleter = new ArgumentCompleter(createArgumentDelimiter(),
            aggregateCompleter);
        argumentCompleter.setStrict(false);
        reader.addCompleter(argumentCompleter);
    }

    private ArgumentCompleter.WhitespaceArgumentDelimiter createArgumentDelimiter() {
        return new ArgumentCompleter.WhitespaceArgumentDelimiter() {
            @Override
            public boolean isDelimiterChar(CharSequence buffer, int pos) {
                return super.isDelimiterChar(buffer, pos) || buffer.charAt(pos) == '\'' ||
                    buffer.charAt(pos) == '"' || buffer.charAt(pos) == '{' || buffer.charAt(pos) == '}' ||
                    buffer.charAt(pos) == ',' || buffer.charAt(pos) == ';';
            }
        };
    }

    @Override
    public String readLine(String fmt, Object... args) throws IOException {
        return reader.readLine(String.format(fmt, args));
    }

    @Override
    public char[] readPassword(String fmt, Object... args) throws IOException {
        // String.format(fmt, args),
        return reader.readLine(String.format(fmt, args), new Character('*')).toCharArray();

    }

    @Override
    public void writeLine(String format, Object... args) throws IOException {
        writer.println(String.format(format, args));
    }

    @Override
    public int read() throws IOException {
        return reader.getInput().read();
    }

    @Override
    public boolean canRead() throws IOException {
        return reader.getInput().available() > 0;
    }

    @Override
    public int getHeight() {
        return reader.getTerminal().getHeight();
    }

    @Override
    public int getWidth() {
        return reader.getTerminal().getWidth();
    }
}
