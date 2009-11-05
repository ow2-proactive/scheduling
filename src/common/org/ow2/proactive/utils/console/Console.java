/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.utils.console;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;


/**
 * Methods to access a character-based console device.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public interface Console {

    /**
     * Start the console.
     * Cause this class to read and write line to and from the standard output.
     *
     * @param prompt the prompt to be displayed on the console.
     * @return this console
     */
    public Console start(String prompt);

    /**
     * Stop using this console
     */
    public void stop();

    /**
     * Flushes the console and forces any buffered output to be written immediately .
     */
    public void flush();

    /**
     * A convenience method to write a string to this console's output stream.
     *
     * @param msg A string representing the message to be printed out.
     * @return This console
     */
    public Console print(String msg);

    /**
     * A convenience method to write a string to this console's error stream.
     *
     * @param msg A string representing the message to be printed out.
     * @return This console
     */
    public Console error(String format);

    /**
     * Retrieves the unique Reader object associated with this console.
     *
     * @return The reader associated with this console, or null if the console has not been started.
     */
    public Reader reader();

    /**
     * Retrieves the unique PrintWriter object associated with this console.
     *
     * @return The PrintWriter object associated with this console, or null if the console has not been started.
     */
    public PrintWriter writer();

    /**
     * Reads a statement of text from the console.<br>
     * This method blocks until new statement comes<br><br>
     *
     * @return A string containing the statement read from the console, or null if an end of stream has been reached.
     * @throws IOException
     */
    public String readStatement() throws IOException;

    /**
     * Reads a statement of text from the console and use the given prompt.<br>
     * This method blocks until new statement comes<br><br>
     *
     * @return A string containing the statement read from the console, or null if an end of stream has been reached.
     * @throws IOException
     */
    public String readStatement(String prompt) throws IOException;

    /**
     * This method can be used to display exception.<br>
     * It will first print the given message following by the exception message if exist, otherwise the exception itself.<br>
     * Next it will prompt to ask if stack trace must be printed or not.<br>
     * If user answer 'yes', the stack trace will be displayed on the console.
     *
     * @param msg the message to display first
     * @param t the exception (stack trace) to eventually add to the message
     */
    public void handleExceptionDisplay(String msg, Throwable t);

    /**
     * Convenience method to print a stackTrace on the console
     *
     * @param t the throwable to display
     */
    public void printStackTrace(Throwable t);

}
