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
import java.util.Map;


/**
 * StdOutConsole is a minimal console to display on standard output stream.
 * No input stream is managed is this console.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
public class StdOutConsole implements Console {

    /**
     * Do nothing in this implementation
     */
    public void addCompletion(String... candidates) {
    }

    /**
     * Do nothing in this implementation
     */
    public void configure(Map<String, String> params) {
    }

    /**
     * {@inheritDoc}
     */
    public Console error(String format) {
        System.err.println(format);
        return this;
    }

    /**
     * Do nothing in this implementation
     */
    public void filtersClear() {
    }

    /**
     * Do nothing in this implementation
     * 
     * @return null
     */
    public String filtersPop() {
        return null;
    }

    /**
     * Do nothing in this implementation
     */
    public void filtersPush(String regexp) {
    }

    /**
     * {@inheritDoc}
     */
    public void flush() {
        System.out.flush();
    }

    /**
     * Display error message and exception
     */
    public void handleExceptionDisplay(String msg, Throwable t) {
        error(msg + " : " + (t.getMessage() == null ? t : t.getMessage()));
        //printStackTrace(t);
    }

    /**
     * Do nothing in this implementation
     * 
     * @return false;
     */
    public boolean isPaginationActivated() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Console print(String msg) {
        System.out.println(msg);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void printStackTrace(Throwable t) {
        t.printStackTrace();
    }

    /**
     * Do nothing in this implementation
     * 
     * @return null;
     */
    public String readStatement() throws IOException {
        return null;
    }

    /**
     * Do nothing in this implementation
     * 
     * @return null;
     */
    public String readStatement(String prompt) throws IOException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Reader reader() {
        return new InputStreamReader(System.in);
    }

    /**
     * Do nothing in this implementation
     */
    public void setPaginationActivated(boolean paginationActivated) {
    }

    /**
     * Do nothing in this implementation
     * Console is always started and ready
     * 
     * @return this console
     */
    public Console start(String prompt) {
        return this;
    }

    /**
     * Stop has no effect
     */
    public void stop() {
    }

    /**
     * {@inheritDoc}
     */
    public PrintWriter writer() {
        return new PrintWriter(System.out);
    }

}
