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
package org.objectweb.proactive.ic2d.console;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;


/**
 * Used to log informations in a console view.
 */
public class Console extends MessageConsole {

    /**
     * Contains all consoles.
     */
    private static Map<String, Console> consoles = new HashMap<String, Console>();

    /**
     * To know the date's format.
     */
    private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    /**
     * Some useful colors.
     */
    private static final Color RED;
    private static final Color BLUE;

    //private static final Color GREEN;
    private static final Color GRAY;
    private static final Color BLACK;

    static {
        Display device = Display.getCurrent();
        RED = new Color(device, 255, 0, 0);
        BLUE = new Color(device, 0, 0, 128);
        //GREEN = new Color(device, 180, 255, 180);
        GRAY = new Color(device, 120, 120, 120);
        BLACK = new Color(device, 0, 0, 0);
    }

    public static boolean debug = false;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates a new Console
     * @param title Title of the frame console
     */
    private Console(String title) {
        super(title, null);
        activate();

        // Add the standard output and standard error output stream to the Console.
        //		MessageConsole console = new MessageConsole("System output", null);
        //
        //		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(
        //		console);
        //
        //		MessageConsoleStream stream = console.newMessageStream();
        //		System.setOut(new PrintStream(stream));
        //		System.setErr(new PrintStream(stream));

        //----- Log4j Console ------
        // log4j output in the console
        MessageConsole log4jConsole = new MessageConsole("log4j", null);

        MessageConsoleStream log4jStream = log4jConsole.newMessageStream();

        //		Logger logger = ProActiveLogger.getLogger(Loggers.CORE);
        Logger logger = LogManager.getRootLogger();
        WriterAppender app = new WriterAppender(new SimpleLayout(), log4jStream);
        //		logger.addAppender(app);
        ConsolePlugin.getDefault().getConsoleManager().addConsoles(
                new IConsole[] { /*console,*/log4jConsole });
        //-------------------------
        ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { this });
    }

    //
    // -- PUBLICS METHODS -----------------------------------------------
    //

    /**
     * Returns the console having for title 'title'
     * @param title The console's title
     */
    public static synchronized Console getInstance(String title) {
        Console console = consoles.get(title);
        if (console == null) {
            console = new Console(title);
            consoles.put(title, console);
        }
        return console;
    }

    /**
     * Logs a message to the console
     * @param message
     */
    public synchronized void log(String message) {
        final String text = message;

        printTime();

        // Print the message in the UI Thread in async mode
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                MessageConsoleStream stream = newMessageStream();
                stream.setColor(Console.BLUE);
                stream.println(text);
            }
        });
    }

    /**
     * Logs an warning message to the console.
     * @param message
     */
    public synchronized void warn(String message) {
        final String text = message;

        printTime();

        // Print the message in the UI Thread in async mode
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                MessageConsoleStream stream = newMessageStream();
                stream.setColor(Console.RED);
                stream.println(text);
            }
        });
    }

    /**
     * Logs an error message to the console.
     * @param message
     */
    public synchronized void err(String message) {
        final String text = message;

        printTime();

        // Print the message in the UI Thread in async mode
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                MessageConsoleStream stream = newMessageStream();
                stream.setColor(Console.GRAY);
                stream.println(text);
            }
        });
    }

    /**
     * Logs an exception in the console
     * @param e the exception to log
     */
    public synchronized void logException(Throwable e) {
        /*                Display.getDefault().asyncExec(new Runnable() {
                                public void run() {
                                        MessageConsoleStream stream = newMessageStream();
                                        stream.print("\n");
                                }});*/
        printTime();
        logExceptionWhithoutTime(e, false);
        e.printStackTrace();
    }

    /**
     * Logs an exception in the console
     * @param message the message to display.
     * @param e the exception to log
     */
    public synchronized void logException(String message, Throwable e) {
        printTime();
        err(message);
        logExceptionWhithoutTime(e, false);
        e.printStackTrace();
    }

    public synchronized void debug(String message) {
        if (debug) {
            log(message);
        }
    }

    public synchronized void debug(Throwable e) {
        if (debug) {
            logException(e);
        }
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //

    /**
     * Prints the current time in the console.
     */
    public void printTime() {
        // Print the message in the UI Thread in async mode
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                MessageConsoleStream stream = newMessageStream();
                stream.setColor(Console.BLACK);
                stream.print(dateFormat.format(new java.util.Date()) + " => ");
            }
        });
    }

    private synchronized void logExceptionWhithoutTime(Throwable e, boolean cause) {
        StringBuilder builder = new StringBuilder();
        if (cause) {
            builder.append("Caused by: ");
        }

        builder.append(e.getClass().getName() + ": " + e.getMessage() + "\n");
        StackTraceElement[] traces = e.getStackTrace();
        for (int i = 0; i < traces.length; i++)
            builder.append("\t" + traces[i] + "\n");

        final String log = builder.toString();

        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                MessageConsoleStream stream = newMessageStream();
                stream.setColor(Console.GRAY);
                stream.print(log);
            }
        });

        if (e.getCause() != null) {
            logExceptionWhithoutTime(e.getCause(), true);
        }
    }
}
