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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.gui.views;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.ow2.proactive.scheduler.Activator;
import org.ow2.proactive.scheduler.common.SchedulerConstants;
import org.ow2.proactive.scheduler.gui.Colors;


/**
 * This class allow to write message in the default Message console
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class JobOutput extends MessageConsole {

    /** The color for the fatal log level */
    public static final Color FATAL_COLOR = Colors.RED;

    /** The color for the error log level */
    public static final Color ERROR_COLOR = Colors.RED;

    /** The color for the warn log level */
    public static final Color WARN_COLOR = Colors.BLUE;

    /** The color for the default log level */
    public static final Color DEFAULT_COLOR = Colors.BLACK;

    /** The color for the debug log level */
    public static final Color DEBUG_COLOR = Colors.GREEN;

    /** The color for the info log level */
    public static final Color INFO_COLOR = Colors.BLACK;

    /** The color for the trace log level */
    public static final Color TRACE_COLOR = Colors.BLACK;

    // the default stream to write in
    private MessageConsoleStream defaultStream;

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

    /** maps a Task ID to a remote connection hint extracted from the logs */
    private Map<String, RemoteHint> remoteConnHints = new HashMap<String, RemoteHint>();

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    /**
     * The default constructor.
     *
     * @param name the name.
     */
    public JobOutput(String name) {
        super(name, null);
        ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { this });
        this.defaultStream = newMessageStream();
    }

    /**
     * The default constructor.
     *
     * @param name the name.
     * @param initialMessage the initial message to be displayed in the console
     */
    public JobOutput(String name, String initialMessage) {
        this(name);
        this.info(initialMessage);
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
    private synchronized void log(String message, Color color) {
        final String mess = message;
        final Color col = color;
        // Print the message in the UI Thread in async mode
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                defaultStream.setColor(col);
                defaultStream.print(mess);
                findRemoteHint(mess);
            }
        });
    }

    /**
     * Search for remote connection hints in this log message
     * 
     * @param message log message ; may contain multiple lines
     */
    private void findRemoteHint(String message) {
        if (message.indexOf(SchedulerConstants.REMOTE_CONNECTION_MARKER) != -1) {
            BufferedReader br = new BufferedReader(new StringReader(message));
            String line = null;
            try {
                while ((line = br.readLine()) != null) {
                    String[] hint = line.split(SchedulerConstants.REMOTE_CONNECTION_MARKER);
                    if (hint.length > 1) {
                        String[] expl = hint[1].split("" + SchedulerConstants.REMOTE_CONNECTION_SEPARATOR);

                        // expl = { '', 'taskid', 'application type', 'url', 'maybe more url' }
                        String url = expl[3];

                        // some URLs may contain the separator : 'http://foo.com:99/A&amp;B'
                        // accumulate everything until EOF as the URL
                        for (int j = 4; j < expl.length; j++)
                            url += SchedulerConstants.REMOTE_CONNECTION_SEPARATOR + expl[j];

                        String appType = expl[2];
                        String taskId = expl[1];

                        if (!appType.matches("[a-zA-Z]+")) {
                            this.remoteConnHints.put(taskId, new RemoteHint(
                                "Application type needs to contain alphabetical characters only (was '" +
                                    appType + "')"));
                            break;
                        }

                        RemoteHint h = new RemoteHint(appType, url);
                        this.remoteConnHints.put(taskId, h);
                        break;
                    }
                }
            } catch (IOException e) {
                Activator.log(IStatus.ERROR, "Failed to parse Job Output", e);
            }
        }

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
        log(message, FATAL_COLOR);
    }

    /**
     * Directly call the method {@link #log(String, Color)} with the given
     * message and the ERROR_COLOR color
     *
     * @param message the message
     * @see #ERROR_COLOR
     */
    public synchronized void error(String message) {
        log(message, ERROR_COLOR);
    }

    /**
     * Directly call the method {@link #log(String, Color)} with the given
     * message and the WARN_COLOR color
     *
     * @param message the message
     * @see #WARN_COLOR
     */
    public synchronized void warn(String message) {
        log(message, WARN_COLOR);
    }

    /**
     * Directly call the method {@link #log(String, Color)} with the given
     * message and the DEFAULT_COLOR color
     *
     * @param message the message
     * @see #DEFAULT_COLOR
     */
    public synchronized void log(String message) {
        log(message, DEFAULT_COLOR);
    }

    /**
     * Directly call the method {@link #log(String, Color)} with the given
     * message and the DEBUG_COLOR color
     *
     * @param message the message
     * @see #DEBUG_COLOR
     */
    public synchronized void debug(String message) {
        log(message, DEBUG_COLOR);
    }

    /**
     * Directly call the method {@link #log(String, Color)} with the given
     * message and the INFO_COLOR color
     *
     * @param message the message
     * @see #INFO_COLOR
     */
    public synchronized void info(String message) {
        log(message, INFO_COLOR);
    }

    /**
     * Directly call the method {@link #log(String, Color)} with the given
     * message and the TRACE_COLOR color
     *
     * @param message the message
     * @see #TRACE_COLOR
     */
    public synchronized void trace(String message) {
        log(message, TRACE_COLOR);
    }

    /**
     * Do nothing.
     */
    public synchronized void off() {
    }

    /**
     * @return maps a TaskID value (ie "10001") to a remote connection hint : protocol and url
     *      all tasks may not have a visualization hint
     */
    public Map<String, RemoteHint> getRemoteConnHints() {
        return this.remoteConnHints;
    }
}
