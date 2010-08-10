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

    private boolean started = false;
    private String prompt = "";
    private ConsoleReader console;
    private SimpleCompletor completor;

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
                console.printString(msg);
                console.printNewline();
                //flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("Console is not started !");
        }
        return this;
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

}
