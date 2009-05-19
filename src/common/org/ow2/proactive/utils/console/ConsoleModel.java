/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.util.userconsole.UserController;


/**
 * UserSchedulerModel is the class to extend to drive consoles.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public abstract class ConsoleModel {

    protected static final int cmdHelpMaxCharLength = 24;
    protected String JS_INIT_FILE = null;

    protected boolean initialized = false;
    protected boolean terminated = false;
    protected boolean displayOnStdStream = false;

    protected boolean displayStack = true;
    protected boolean displayOnDemand = true;

    protected ScriptEngine engine;
    protected Console console;

    /**
     * Start this model
     *
     * @throws IOException
     * @throws SchedulerException
     */
    public abstract void start() throws IOException, SchedulerException;

    //***************** DISPLAY HANDLING *******************

    /**
     * Display the given message with the given exception according to the options set.
     * This will display the exception on demand or not at all.
     *
     * @param msg the message to display
     * @param t the exception to manage
     */
    public void handleExceptionDisplay(String msg, Throwable t) {
        if (!displayOnStdStream) {
            if (!displayStack) {
                console.error(msg + " : " + (t.getMessage() == null ? t : t.getMessage()));
            } else {
                if (displayOnDemand) {
                    console.handleExceptionDisplay(msg, t);
                } else {
                    console.printStackTrace(t);
                }
            }
        } else {
            System.err.printf(msg + "\n");
            t.printStackTrace();
        }
    }

    /**
     * print the message to the selected output
     *
     * @param msg the message to print
     */
    public void print(String msg) {
        if (!displayOnStdStream) {
            console.print(msg);
        } else {
            System.out.println(msg);
        }
    }

    /**
     * print the message to the selected error output
     *
     * @param msg the message to print
     */
    public void error(String msg) {
        if (!displayOnStdStream) {
            console.error(msg);
        } else {
            System.err.println(msg);
        }
    }

    /**
     * Set the exception mode
     *
     * @param displayStack true if the stack must be displayed, false otherwise. If false, second parameter is ignored.
     * @param displayOnDemand true if the console ask if user want to display the stack or not.
     */
    protected void setExceptionMode_(boolean displayStack, boolean displayOnDemand) {
        this.displayStack = displayStack;
        this.displayOnDemand = displayOnDemand;
        String msg = "Exception display mode changed : ";
        if (!displayStack) {
            msg += "stack trace not displayed";
        } else {
            if (displayOnDemand) {
                msg += "stack trace displayed on demand";
            } else {
                msg += "stack trace displayed everytime";
            }
        }
        print(msg);
    }

    //***************** OTHER *******************

    /**
     * Check if the model is ready. First check if the console is set and the display is not set on standard output.
     */
    protected void checkIsReady() {
        if (console == null && displayOnStdStream == false) {
            throw new RuntimeException("Console is not set, it must be set before starting the model");
        }
    }

    /**
     * Initialize the console model with the given script file if set.
     * Set it with the {@link #setJS_INIT_FILE(String)} if needed.
     *
     * @throws IOException if something wrong occurs
     */
    protected void initialize() throws IOException {
        if (!initialized) {
            ScriptEngineManager manager = new ScriptEngineManager();
            // Engine selection
            engine = manager.getEngineByExtension("js");
            engine.getContext().setWriter(console.writer());
            initialized = true;
            //read and launch Action.js
            if (JS_INIT_FILE != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(UserController.class
                        .getResourceAsStream(JS_INIT_FILE)));
                eval(readFileContent(br));
            }
        }
    }

    /**
     * Method use to evaluate a new script command.
     *
     * @param cmd the command to evaluate
     */
    protected void eval(String cmd) {
        try {
            if (!initialized) {
                initialize();
            }
            //Evaluate the command
            if (cmd == null) {
                console.error("*ERROR* - Standard input stream has been terminated !");
                terminated = true;
            } else {
                engine.eval(cmd);
            }
        } catch (ScriptException e) {
            console.error("*SYNTAX ERROR* - " + format(e.getMessage()));
        } catch (Exception e) {
            handleExceptionDisplay("Error while evaluating command", e);
        }
    }

    /**
     * Read the given file and return its content as a string.
     *
     * @param reader the reader on an opened file.
     * @return the content of the file as a string.
     * @throws IOException
     */
    protected static String readFileContent(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        String tmp;
        while ((tmp = reader.readLine()) != null) {
            sb.append(tmp);
        }
        return sb.toString();
    }

    /**
     * Format the given string and return the result.
     * Use to remove some useless characters in the exception message returned by the script engine.
     *
     * @param msg the message to format.
     * @return the formatted message.
     */
    private static String format(String msg) {
        msg = msg.replaceFirst("[^:]+:", "");
        return msg.replaceFirst("[(]<.*", "").trim();
    }

    //***************** GETTER SETTER *******************

    /**
     * Get the console
     *
     * @return the console
     */
    public Console getConsole() {
        return console;
    }

    /**
     * Connect the console value to the given console value
     *
     * @param console the console to connect
     */
    public void connectConsole(Console console) {
        if (console == null) {
            throw new NullPointerException("Given console is null");
        }
        this.console = console;
    }

    /**
     * Get the displayOnStdStream
     *
     * @return the displayOnStdStream
     */
    public boolean isDisplayOnStdStream() {
        return displayOnStdStream;
    }

    /**
     * Set the displayOnStdStream value to the given displayOnStdStream value
     *
     * @param displayOnStdStream the displayOnStdStream to set
     */
    public void setDisplayOnStdStream(boolean displayOnStdStream) {
        this.displayOnStdStream = displayOnStdStream;
    }

    /**
     * Get the JS_INIT_FILE
     *
     * @return the JS_INIT_FILE
     */
    protected String getJS_INIT_FILE() {
        return JS_INIT_FILE;
    }

    /**
     * Set the JS_INIT_FILE value to the given js_init_file value
     *
     * @param js_init_file the JS_INIT_FILE to set
     */
    protected void setJS_INIT_FILE(String js_init_file) {
        JS_INIT_FILE = js_init_file;
    }

}
