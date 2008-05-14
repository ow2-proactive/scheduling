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
package org.objectweb.proactive.extensions.scheduler.ext.matlab;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extensions.scheduler.ext.matlab.exception.InvalidNumberOfParametersException;
import org.objectweb.proactive.extensions.scheduler.ext.matlab.exception.InvalidParameterException;
import ptolemy.data.Token;

import java.io.Serializable;
import java.util.ArrayList;


public class AOSimpleMatlab implements Serializable {

    /**
     * log4j logger 
     */
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.SCHEDULER_MATLAB_EXT);

    static String nl = System.getProperty("line.separator");

    /**
     * input script
     */
    private String inputScript = null;

    /**
     * lines of the main script
     */
    private ArrayList<String> scriptLines = new ArrayList<String>();

    public AOSimpleMatlab() {
    }

    /**
     * Constructor for the Simple task
     * @param matlabCommandName the name of the Matlab command
     */
    public AOSimpleMatlab(String matlabCommandName) {
        MatlabEngine.setCommandName(matlabCommandName);
    }

    public void init(String inputScript, ArrayList<String> scriptLines) {
        this.inputScript = inputScript;
        this.scriptLines = scriptLines;
    }

    public Object execute(int index, TaskResult... results) throws Throwable {
        Token out = null;
        MatlabEngine.Connection conn = MatlabEngine.acquire();
        try {
            conn.clear();
            if (results.length > 1) {
                throw new InvalidNumberOfParametersException(results.length);
            }

            if (results.length == 1) {
                TaskResult res = results[0];

                if (index != -1) {
                    if (!(res.value() instanceof SplittedResult)) {
                        throw new InvalidParameterException(res.value().getClass());
                    }

                    SplittedResult sr = (SplittedResult) res.value();
                    Token tok = sr.getResult(index);
                    conn.put("in", tok);
                } else {
                    if (!(res.value() instanceof Token)) {
                        throw new InvalidParameterException(res.value().getClass());
                    }

                    Token in = (Token) res.value();
                    conn.put("in", in);
                }
            }

            executeScript(conn);

            out = conn.get("out");
        } finally {
            conn.release();
        }
        return out;
    }

    /**
     * Terminates the Matlab engine
     * @return true for synchronous call
     */
    public boolean terminate() {
        MatlabEngine.close();

        return true;
    }

    /**
     * Executes both input and main scripts on the engine
     * @throws Throwable
     */
    protected final void executeScript(MatlabEngine.Connection conn) throws Throwable {
        if (inputScript != null) {
            if (logger.isDebugEnabled()) {
                System.out.println("Feeding input");
            }
            conn.evalString(inputScript);
        }

        String execScript = prepareScript();
        if (logger.isDebugEnabled()) {
            System.out.println("Executing Matlab command");
        }
        conn.evalString(execScript);
        if (logger.isDebugEnabled()) {
            System.out.println("Matlab command completed successfully");
        }
    }

    /**
     * Appends all the script's lines as a single string
     * @return
     */
    private String prepareScript() {
        String script = "";

        for (String line : scriptLines) {
            script += line;
            script += nl;
        }

        return script;
    }
}
