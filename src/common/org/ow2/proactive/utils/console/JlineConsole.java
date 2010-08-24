/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.utils.console;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Stack;

import jline.ArgumentCompletor;
import jline.ClassNameCompletor;
import jline.Completor;
import jline.ConsoleReader;
import jline.FileNameCompletor;
import jline.MultiCompletor;
import jline.SimpleCompletor;


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
            console = new ConsoleReader(System.in, new PrintWriter(System.out, true));
            completor = new SimpleCompletor(new String[] {});
            ArgumentCompletor comp = new ArgumentCompletor(new MultiCompletor(new Completor[] {
                    new ClassNameCompletor(), completor, new FileNameCompletor() }),
                new ArgumentCompletor.WhitespaceArgumentDelimiter() {
                    @Override
                    public boolean isDelimiterChar(String buffer, int pos) {
                        return super.isDelimiterChar(buffer, pos) || buffer.charAt(pos) == '\'' ||
                            buffer.charAt(pos) == '"' || buffer.charAt(pos) == '{' ||
                            buffer.charAt(pos) == '}' || buffer.charAt(pos) == ',' ||
                            buffer.charAt(pos) == ';';
                    }
                });
            comp.setStrict(false);
            console.addCompletor(comp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.prompt = prompt;
        this.started = true;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void stop() {
        if (this.started) {
            this.console = null;
            this.started = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void flush() {
        if (this.started && console != null) {
            try {
                console.flushConsole();
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
                    console.printNewline();
                    return this;
                }
                if (!paginationActivated) {
                    console.printString(sb.toString());
                    console.printNewline();
                } else {
                    while (sb.length() > 0) {
                        console.printString(nFirstLine(sb, console.getTermheight() - 3) + newLineChar);
                        if (sb.length() > 0) {
                            console.printString("- more - (" + ABORT + " : abort | " + ALL +
                                " : display all | any : next page)" + newLineChar);
                            console.flushConsole();
                            int c = console.readVirtualKey();
                            if (c == ABORT) {
                                break;
                            } else if (c == ALL) {
                                console.printString(sb.toString());
                                console.printNewline();
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
                if (filters.size() > 0) {
                    console.printString("- Output filtered - (" + fr.nbFiltered + "/" + fr.nbLines +
                        " lines by " + filters.size() + " filter" + (filters.size() == 1 ? "" : "s") + ") " +
                        filters + newLineChar);
                }
                console.flushConsole();
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
                console.printString("ERROR : " + t.getMessage() + newLineChar);
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
            console.addCompletor(completor);
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

}
