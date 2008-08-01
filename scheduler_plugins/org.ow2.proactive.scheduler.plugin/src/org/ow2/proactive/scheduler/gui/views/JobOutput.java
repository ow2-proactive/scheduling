/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.ow2.proactive.scheduler.gui.views;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
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
                MessageConsoleStream stream = newMessageStream();
                stream.setColor(col);
                stream.print(mess);
            }
        });
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
}
