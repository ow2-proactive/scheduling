/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $PROACTIVE_INITIAL_DEV$
 */
package org.ow2.proactive.utils.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;


/**
 * SimpleConsole is a simple implementation of the {@link Console} interface.<br>
 * If this console is not started, it ensure that it won't write anything on the display.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class SimpleConsole implements Console {

    private boolean started = false;
    private String prompt;
    private BufferedReader reader;
    private PrintWriter writer;

    /**
     * Create a new instance of SimpleConsole.
     */
    public SimpleConsole() {
    }

    /**
     * Create a new instance of SimpleConsole using the given prompt.<br>
     * This constructor does not need a call to the {@link #start(String)} method. It
     * automatically starts the console with the given prompt.
     *
     * @param prompt the prompt to be displayed on the console.
     */
    public SimpleConsole(String prompt) {
        start(prompt);
    }

    /**
     * @see org.ow2.proactive.utils.console.Console#start(java.lang.String)
     */
    public Console start(String prompt) {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.writer = new PrintWriter(System.out);
        this.prompt = prompt;
        this.started = true;
        return this;
    }

    /**
     * @see org.ow2.proactive.utils.console.Console#stop()
     */
    public void stop() {
        if (this.started) {
            try {
                this.reader.close();
                this.writer.close();
            } catch (IOException e) {
            }
            this.reader = null;
            this.writer = null;
            this.started = false;
        }
    }

    /**
     * @see org.ow2.proactive.utils.console.Console#flush()
     */
    public void flush() {
        if (this.started && writer != null) {
            writer.flush();
        }
    }

    /**
     * Escape special character used by java in regexp in the given string and retrieve the escaped String.
     * 
     * @param formattedString a string that contains special char to escape such as %.
     * @return the same string with special char escaped
     */
    private String escape(String formattedString) {
        String toRet = formattedString.replaceAll("%", "%%");
        //add other replacement if needed
        return toRet;
    }

    /**
     * @see org.ow2.proactive.utils.console.Console#printf(java.lang.String, java.lang.Object[])
     */
    public Console printf(String format, Object... args) {
        if (this.started) {
            format = escape(format);
            writer.format(format, args);
            writer.println();
            writer.flush();
        } else {
            throw new RuntimeException("Console is not started !");
        }
        return this;
    }

    /**
     * @see org.ow2.proactive.utils.console.Console#error(java.lang.String, java.lang.Object[])
     */
    public Console error(String format, Object... args) {
        if (this.started) {
            format = escape(format);
            System.err.format(format, args);
            System.err.println();
        } else {
            throw new RuntimeException("Console is not started !");
        }
        return this;
    }

    /**
     * @see org.ow2.proactive.utils.console.Console#readStatement()
     */
    public String readStatement() throws IOException {
        return readStatement(prompt);
    }

    /**
     * @see org.ow2.proactive.utils.console.Console#readStatement(java.lang.String)
     */
    public String readStatement(String prompt) throws IOException {
        if (this.started) {
            System.out.print(prompt);
            return reader.readLine();
        } else {
            throw new RuntimeException("Console is not started !");
        }
    }

    /**
     * @see org.ow2.proactive.utils.console.Console#reader()
     */
    public Reader reader() {
        return reader;
    }

    /**
     * @see org.ow2.proactive.utils.console.Console#writer()
     */
    public PrintWriter writer() {
        return writer;
    }

    /**
     * @see org.ow2.proactive.utils.console.Console#handleError(java.lang.String, java.lang.Throwable)
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
     * @see org.ow2.proactive.utils.console.Console#printStackTrace(java.lang.Throwable)
     */
    public void printStackTrace(Throwable t) {
        t.printStackTrace();
    }

}
