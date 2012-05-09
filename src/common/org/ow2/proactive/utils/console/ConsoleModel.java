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
package org.ow2.proactive.utils.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
//import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;


/**
 * UserSchedulerModel is the class to extend to drive consoles.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public abstract class ConsoleModel {

    protected static String newline = System.getProperty("line.separator");
    protected static int cmdHelpMaxCharLength = 28;

    protected boolean initialized = false;
    protected boolean terminated = false;

    protected boolean displayStack = true;
    protected boolean displayOnDemand = true;

    protected ScriptEngine engine;
    protected Console console;

    protected static ConsoleModel model;
    protected boolean allowExitCommand;

    protected String initEnvFileName = null;

    protected ArrayList<Command> commands;

    //protected Logger logger = ProActiveLogger.getLogger(SchedulerLoggers.CONSOLE);

    protected ConsoleModel() {
        commands = new ArrayList<Command>();
        commands.add(new Command("filterspush(regexp)", "Add a new regexp to the list of filters"));
        commands.add(new Command("filterspop()", "Remove the last inserted regexp from the list of filters"));
        commands.add(new Command("filtersclear()", "Clear the list of filters"));
        commands.add(new Command("setpagination(state)", "Enable or disable the pagination of the console "
            + "(state is a boolean, true to enable pagination, false to disable it)"));
        commands.add(new Command("addcandidate(str)",
            "Add a completion candidate to the current completion list "
                + "(str is a string representing the candidate to add)"));
        commands
                .add(new Command(
                    "exmode(display,onDemand)",
                    "Change the way exceptions are displayed (if display is true, stacks are displayed - if onDemand is true, prompt before displaying stacks)"));
    }

    /**
     * Retrieve a completion list from the list of commands
     *
     * @return a completion list as a string array
     */
    protected String[] getCompletionList() {
        String[] ret = new String[commands.size()];
        for (int i = 0; i < commands.size(); i++) {
            String name = commands.get(i).getName();
            int lb = name.indexOf('(');
            if (lb > 0) {
                ret[i] = name.substring(0, lb + 1);
                if (name.indexOf(')') - lb == 1) {
                    ret[i] += ");";
                }
            } else {
                ret[i] = name;
            }
        }
        return ret;
    }

    public void setInitEnv(String fileName) {
        this.initEnvFileName = fileName;
    }

    /**
     * Start this model
     *
     * @throws Exception
     */
    public abstract void startModel() throws Exception;

    //***************** DISPLAY HANDLING *******************

    /**
     * @param msg the message to display
     * @param the exception to manage
     */
    protected void logUserException(String msg, Throwable t) {
        //log the exception independently on the configuration
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        t.printStackTrace(printWriter);
        //logger.info("User exception occured. Msg:  " + msg + " stacktrace: " + result);
    }

    /**
     * Display the given message with the given exception according to the options set.
     * This will display the exception on demand or not at all.
     * 
     * @param msg the message to display
     * @param t the exception to manage
     */
    public void handleExceptionDisplay(String msg, Throwable t) {
        if (!displayStack) {
            console.error(msg + " : " + (t.getMessage() == null ? t : t.getMessage()));
        } else {
            if (displayOnDemand) {
                console.handleExceptionDisplay(msg, t);
            } else {
                console.printStackTrace(t);
            }
        }
    }

    /**
     * print the message to the selected output
     *
     * @param msg the message to print
     */
    public void print(String msg) {
        console.print(msg);
    }

    /**
     * print the message to the selected error output
     *
     * @param msg the message to print
     */
    public void error(String msg) {
        console.error(msg);
    }

    //***************** COMMAND LISTENER *******************
    //note : method marked with a "_" are called from JS evaluation

    public void filtersPush_(String regexp) {
        console.filtersPush(regexp);
    }

    public String filtersPop_() {
        return console.filtersPop();
    }

    public void filtersClear_() {
        console.filtersClear();
    }

    public void setPagination_(boolean state) {
        console.setPaginationActivated(state);
    }

    /**
     * Add a candidate for completion
     *
     * @param candidate
     */
    public void addCandidate_(String candidate) {
        if (candidate == null) {
            error("Candidate string cannot be null or empty");
        } else {
            console.addCompletion(candidate);
        }
    }

    /**
     * Set the exception mode
     *
     * @param displayStack true if the stack must be displayed, false otherwise. If false, second parameter is ignored.
     * @param displayOnDemand true if the console ask if user want to display the stack or not.
     */
    public void setExceptionMode_(boolean displayStack, boolean displayOnDemand) {
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

    public void help_() {
        print(newline + helpScreen());
    }

    public void cnslhelp_() {
        print(newline + helpScreenCnsl());
    }

    //***************** OTHER *******************

    /**
     * Check if the model is ready. First check if the console is set and the display is not set on standard output.
     */
    protected void checkIsReady() {
        if (console == null) {
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
        }
    }

    /**
     * Convenience method use to evaluate a new script command.
     *
     * @param cmd the command to evaluate
     */
    protected void eval(String cmd) {
        eval(cmd, null);
    }

    /**
     * Method use to evaluate a new script command.
     *
     * @param cmd the command to evaluate
     * @param bindings will be added to the JS context if not null
     */
    protected void eval(String cmd, Map<String, String> bindings) {
        try {
            if (!initialized) {
                initialize();
            }
            //Evaluate the command
            if (cmd == null) {
                error("*ERROR* - Standard input stream has been terminated !");
                terminated = true;
            } else {
                checkIsReady();
                if (bindings != null && bindings.size() > 0) {
                    Bindings bdgs = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                    if (bdgs != null) {
                        bdgs.putAll(bindings);
                    }
                }
                engine.eval(cmd);
            }
        } catch (ScriptException e) {
            error("*SYNTAX ERROR* - " + format(e.getMessage()));
            e.printStackTrace();
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

    //*****************  HELP SCREEN  *******************

    protected abstract String helpScreen();

    protected String helpScreenCnsl() {
        StringBuilder out = new StringBuilder("Console options commands are :" + newline + newline);

        for (int i = 0; i < 6; i++) {
            out.append(String.format(" %1$-" + cmdHelpMaxCharLength + "s %2$s" + newline, commands.get(i)
                    .getName(), commands.get(i).getDescription()));
        }

        return out.toString();
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

}
