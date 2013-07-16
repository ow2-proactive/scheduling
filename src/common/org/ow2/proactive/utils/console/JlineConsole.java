/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.utils.console;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Map;
import java.util.Stack;

import jline.console.ConsoleReader;
import jline.console.completer.AggregateCompleter;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.ArgumentCompleter.WhitespaceArgumentDelimiter;
import jline.console.completer.Completer;
import jline.console.history.FileHistory;
import org.ow2.proactive.utils.console.jline1.ClassNameCompletor;
import org.ow2.proactive.utils.console.jline1.FileNameCompletor;
import org.ow2.proactive.utils.console.jline1.SimpleCompletor;


/**
 * JlineConsole is an implementation of the {@link Console} interface.<br>
 * It uses the ConsoleReader from Jline package (located in jruby.jar) which provides completion and history.
 * 
 * If this console is not started, it ensure that it won't write anything on the display.
 *
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.1
 */
public class JlineConsole implements Console {

    private static String newLineChar = System.getProperty("line.separator");
    private static char ABORT = 'q';
    private static char ALL = 'a';

    private boolean started = false;
    private String prompt = "";
    private ConsoleReader console;
    private SimpleCompletor completor;
    private boolean paginationActivated = true;
    private Stack<String> filters = new Stack<String>();

    private File historyFile = new File(System.getProperty("user.home") + File.separator + ".proactive" +
        File.separator + "console.hist");
    private int historySize = 20;
    private FileHistory history;

    /**
     * Create a new instance of SimpleConsole.
     */
    public JlineConsole() {
    }

    /**
     * Create a new instance of SimpleConsole using the given prompt.<br>
     * This constructor does not need a call to the {@link #start(String)} method. It
     * automatically starts the console with the given prompt.
     *
     * @param prompt the prompt to be displayed on the console.
     */
    public JlineConsole(String prompt) {
        start(prompt);
    }

    /**
     * {@inheritDoc}
     */
    public Console start(String prompt) {
        try {
            console = new ConsoleReader(System.in, System.out);
            console.addCompleter(createCompleter());
            history = new FileHistory(historyFile);
            console.setHistory(history);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    dumpHistory();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.prompt = prompt;
        this.started = true;
        return this;
    }

    private ArgumentCompleter createCompleter() throws IOException {
        completor = new SimpleCompletor(new String[] {});
        Completer aggregateCompleter = new AggregateCompleter(new ClassNameCompletor(), completor,
            new FileNameCompletor());
        WhitespaceArgumentDelimiter delimiter = new WhitespaceArgumentDelimiter() {
            @Override
            public boolean isDelimiterChar(CharSequence buffer, int pos) {
                return super.isDelimiterChar(buffer, pos) || buffer.charAt(pos) == '\'' ||
                    buffer.charAt(pos) == '"' || buffer.charAt(pos) == '{' || buffer.charAt(pos) == '}' ||
                    buffer.charAt(pos) == ',' || buffer.charAt(pos) == ';';
            }
        };
        ArgumentCompleter argumentCompleter = new ArgumentCompleter(delimiter, aggregateCompleter);
        argumentCompleter.setStrict(false);
        return argumentCompleter;
    }

    private synchronized void dumpHistory() {
        try {
            history.flush();
        } catch (IOException e) {
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stop() {
        if (this.started) {
            this.started = false;
            dumpHistory();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void flush() {
        if (this.started && console != null) {
            try {
                console.flush();
            } catch (IOException e) {
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Console print(String msg) {
        if (this.started) {
            try {
                FilterResult fr = filter(new StringBuffer(msg));
                StringBuffer sb = fr.content;
                if (msg.length() == 0) {
                    console.println();
                    return this;
                }
                if (!paginationActivated) {
                    console.print(sb.toString());
                    console.println();
                } else {
                    while (sb.length() > 0) {
                        console.print(nFirstLine(sb, console.getTerminal().getHeight() - 3) + newLineChar);
                        if (sb.length() > 0) {
                            console.print("- more - (" + ABORT + " : abort | " + ALL +
                                " : display all | any : next page)" + newLineChar);
                            console.flush();
                            int c = console.readCharacter();
                            if (c == ABORT) {
                                break;
                            } else if (c == ALL) {
                                console.print(sb.toString());
                                console.println();
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
                if (filters.size() > 0) {
                    console.print("- Output filtered - (" + fr.nbFiltered + "/" + fr.nbLines + " lines by " +
                        filters.size() + " filter" + (filters.size() == 1 ? "" : "s") + ") " + filters +
                        newLineChar);
                }
                console.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("Console is not started !");
        }
        return this;
    }

    /**
     * Filter the given message according to the list of filters of this class.<br />
     * Line by line, return a StringBuffer which contains the same lines except the one that
     * don't match the filters.
     * Lines are delimited by independent platform newLine char.
     *
     * @param msg the original message
     * @return a FilterResult containing the total number of lines, the number of filtered lines,
     * 			and every lines that matche the filters
     */
    private FilterResult filter(StringBuffer msg) {
        int nbFiltered = 0;
        int nbLine = 0;
        StringBuffer lines = new StringBuffer();
        try {
            while (msg.length() > 0) {
                int io = msg.indexOf(newLineChar);
                if (io == -1) {
                    io = msg.length();
                }
                String toAppend = msg.substring(0, io);
                msg.delete(0, io + newLineChar.length());
                nbLine++;
                if (isfiltered(toAppend)) {
                    nbFiltered++;
                    lines.append(toAppend);
                    if (msg.length() > 0) {
                        lines.append(newLineChar);
                    }
                }
            }
        } catch (Throwable t) {
            try {
                console.print("ERROR : " + t.getMessage() + newLineChar);
            } catch (IOException e) {
            }
        }
        return new FilterResult(nbLine, nbFiltered, lines);
    }

    /**
     * Return n first lines of the given string buffer and remove those lines from the buffer.
     * If there are less than n lines, everything is returned
     *
     * @param msg the message to be evaluate
     * @param n the number of line to extract
     * @return the n first lines of the string buffer
     */
    private String nFirstLine(StringBuffer msg, int n) {
        StringBuffer nLines = new StringBuffer();
        while (n > 0 && msg.length() > 0) {
            int io = msg.indexOf(newLineChar);
            if (io == -1) {
                io = msg.length();
            }
            String toAppend = msg.substring(0, io);
            nLines.append(toAppend);
            msg.delete(0, io + newLineChar.length());
            n--;
            if (n > 0 && msg.length() > 0) {
                nLines.append(newLineChar);
            }
        }
        return nLines.toString();
    }

    /**
     * Filter the given string according to the list of filters defined in this class
     *
     * @param line the line to be filtered
     * @return true if the line matches the current filters array, false otherwise.
     */
    private boolean isfiltered(String line) {
        if (filters == null) {
            return true;
        }
        for (String f : filters) {
            if (!line.matches(f)) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public Console error(String msg) {
        System.err.println(msg);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public String readStatement() throws IOException {
        return readStatement(prompt);
    }

    /**
     * {@inheritDoc}
     */
    public String readStatement(String prompt) throws IOException {
        if (this.started) {
            return console.readLine(prompt);
        } else {
            throw new RuntimeException("Console is not started !");
        }
    }

    /**
     * {@inheritDoc}
     */
    public Reader reader() {
        return new InputStreamReader(System.in);
    }

    /**
     * {@inheritDoc}
     */
    public PrintWriter writer() {
        return new PrintWriter(System.out);
    }

    /**
     * {@inheritDoc}
     */
    public void handleExceptionDisplay(String msg, Throwable t) {
        error(msg + " : " + (t.getMessage() == null ? t : t.getMessage()));
        try {
            if ("yes".equalsIgnoreCase(readStatement("Display stack trace ? (yes/no)" + prompt))) {
                printStackTrace(t);
            }
        } catch (IOException e) {
            error("Could not display the stack trace");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void printStackTrace(Throwable t) {
        t.printStackTrace();
    }

    /**
     * {@inheritDoc}
     */
    public void addCompletion(String... candidates) {
        if (completor == null) {
            completor = new SimpleCompletor(candidates);
            console.addCompleter(completor);
        }
        for (String s : candidates) {
            if (s != null && !"".equals(s)) {
                completor.addCandidateString(s);
            } else {
                throw new IllegalArgumentException("Candidates argument cannot contains null or empty values");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void filtersPush(String regexp) {
        filters.push(regexp);
    }

    /**
     * {@inheritDoc}
     */
    public String filtersPop() {
        return filters.pop();
    }

    /**
     * {@inheritDoc}
     */
    public void filtersClear() {
        filters.clear();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPaginationActivated() {
        return paginationActivated;
    }

    /**
     * {@inheritDoc}
     */
    public void setPaginationActivated(boolean paginationActivated) {
        this.paginationActivated = paginationActivated;
    }

    class FilterResult {
        int nbLines;
        int nbFiltered;
        StringBuffer content;

        FilterResult(int nbLines, int nbFiltered, StringBuffer content) {
            this.nbLines = nbLines;
            this.nbFiltered = nbFiltered;
            this.content = content;
        }
    }

    /**
     * Configure this console.<br>
     * Configuration key:value is :<br>
     * <ul>
     * 	<li>'history_filepath' : value is the path to the file that will contains the history</li>
     * 	<li>'history_size' : value is the number of lines of the history</li>
     * <ul>
     * 
     */
    public void configure(Map<String, String> params) {
        if (this.started) {
            throw new IllegalStateException("Configure can only be called before starting the console");
        }
        if (params == null) {
            throw new IllegalArgumentException("Cannot configure with a null map of parameters");
        }
        if (params.get("history_filepath") != null) {
            historyFile = new File(params.get("history_filepath"));
            if (!historyFile.exists()) {
                try {
                    historyFile.createNewFile();
                } catch (IOException e) {
                    throw new IllegalStateException("Cannot create the history file");
                }
            }
        }
        if (params.get("history_size") != null) {
            historySize = Integer.parseInt(params.get("history_size"));
        }
    }

}
