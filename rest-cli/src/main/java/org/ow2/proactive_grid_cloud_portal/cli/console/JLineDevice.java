/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import jline.ArgumentCompletor;
import jline.ClassNameCompletor;
import jline.Completor;
import jline.ConsoleReader;
import jline.FileNameCompletor;
import jline.History;
import jline.MultiCompletor;
import jline.SimpleCompletor;

import org.ow2.proactive_grid_cloud_portal.cli.CommandSet;

public class JLineDevice extends AbstractDevice {
    private static final int HLENGTH = 20;
    private static final String HFILE = System.getProperty("user.home")
            + File.separator + ".proactive" + File.separator + "restcli.hist";

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
        reader = new ConsoleReader(in, writer);
        reader.setHistory(new History(hfile));
        
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
        FileOutputStream outStream = null;
        try {
            File hfile = new File(HFILE);
            if (hfile.exists()) {
                hfile.delete();
            }
            hfile.createNewFile();
            outStream = new FileOutputStream(hfile);
            PrintWriter decorated = new PrintWriter(outStream);
            @SuppressWarnings("rawtypes")
            List historyList = reader.getHistory().getHistoryList();
            if (historyList.size() > HLENGTH) {
                historyList = historyList.subList(historyList.size() - HLENGTH,
                        historyList.size());
            }
            for (int index = 0; index < historyList.size(); index++) {
                decorated.println(historyList.get(index));
            }
            decorated.flush();
        } catch (IOException fnfe) {
            // can't do much 
            
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private String[] getCommandsAsArray(CommandSet.Entry[] entries) {
        List<String> cmds = new ArrayList<String>();
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
        Completor[] completors = new Completor[] {
                new SimpleCompletor(getCommandsAsArray(entries)),
                new ClassNameCompletor(), new FileNameCompletor() };
        reader.addCompletor(new ArgumentCompletor(
                new MultiCompletor(completors)));
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
