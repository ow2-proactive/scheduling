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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive_grid_cloud_portal;

import java.awt.Color;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.core.util.CircularArrayList;


/**
 * This class allow to write message in the default Message console
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class JobOutput {

    public static class RemoteHint {
        public String appType;
        public String url;

        /** if the other fields are null, this explains why; 
         * put it in the error message */
        public String errorMsg;

        public RemoteHint(String appType, String url) {
            this.appType = appType;
            this.url = url;
        }

        public RemoteHint(String errorMsg) {
            this.errorMsg = errorMsg;
        }
    }

    private String name;

    private CircularArrayList<String> cl;

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    /**
     * The default constructor.
     *
     * @param name the name.
     */
    public JobOutput(String name) {
        this.name = name;
    }

    public JobOutput(String name, CircularArrayList<String> cl) {
        this(name);
        this.cl = cl;
    }

    // -------------------------------------------------------------------- //
    // ----------------------------- private ------------------------------ //
    // -------------------------------------------------------------------- //
    /**
     * Logs a message to the console
     *
     * @param message the message to log
     * @param color the color
     */
    private synchronized void log_(String message) {
        getCl().add(message);
    }

    // -------------------------------------------------------------------- //
    // ------------------------------ public ------------------------------ //
    // -------------------------------------------------------------------- //
    /**
     * Directly call the method {@link #log(String, Color)} with the given
     * message and the FATAL_COLOR color
     *
     * @param message the message
     * @see #FATAL_COLOR
     */
    public synchronized void fatal(String message) {
        log(message);
    }

    /**
     * Directly call the method {@link #log(String, Color)} with the given
     * message and the ERROR_COLOR color
     *
     * @param message the message
     * @see #ERROR_COLOR
     */
    public synchronized void error(String message) {
        log(message);
    }

    /**
     * Directly call the method {@link #log(String, Color)} with the given
     * message and the WARN_COLOR color
     *
     * @param message the message
     * @see #WARN_COLOR
     */
    public synchronized void warn(String message) {
        log(message);
    }

    /**
     * Directly call the method {@link #log(String, Color)} with the given
     * message and the DEFAULT_COLOR color
     *
     * @param message the message
     * @see #DEFAULT_COLOR
     */
    public synchronized void log(String message) {
        log_(message);
    }

    /**
     * Directly call the method {@link #log(String, Color)} with the given
     * message and the DEBUG_COLOR color
     *
     * @param message the message
     * @see #DEBUG_COLOR
     */
    public synchronized void debug(String message) {
        log(message);
    }

    /**
     * Directly call the method {@link #log(String, Color)} with the given
     * message and the INFO_COLOR color
     *
     * @param message the message
     * @see #INFO_COLOR
     */
    public synchronized void info(String message) {
        log(message);
    }

    /**
     * Directly call the method {@link #log(String, Color)} with the given
     * message and the TRACE_COLOR color
     *
     * @param message the message
     * @see #TRACE_COLOR
     */
    public synchronized void trace(String message) {
        log(message);
    }

    /**
     * Do nothing.
     */
    public synchronized void off() {
    }

    public CircularArrayList<String> getCl() {
        return cl;
    }

}
