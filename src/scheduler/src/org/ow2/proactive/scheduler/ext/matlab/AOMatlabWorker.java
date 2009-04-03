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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.matlab;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.ext.matlab.exception.InvalidNumberOfParametersException;
import org.ow2.proactive.scheduler.ext.matlab.exception.InvalidParameterException;
import org.ow2.proactive.scheduler.ext.matlab.util.MatlabConfiguration;
import ptolemy.data.Token;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * An active object which handles the interaction between the MatlabTask and a local Matlab engine
 * @author The ProActive Team
 */
public class AOMatlabWorker implements Serializable {

    protected boolean debug;

    static String nl = System.getProperty("line.separator");

    /**
     * input script
     */
    private String inputScript = null;

    /**
     * lines of the main script
     */
    private ArrayList<String> scriptLines = new ArrayList<String>();

    public AOMatlabWorker() {
    }

    public AOMatlabWorker(MatlabConfiguration matlabConfig) {
        MatlabEngine.setConfiguration(matlabConfig);
    }

    public void init(String inputScript, ArrayList<String> scriptLines, boolean debug) {
        this.inputScript = inputScript;
        this.scriptLines = scriptLines;
        this.debug = debug;
    }

    public Serializable execute(int index, TaskResult... results) throws Throwable {
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
            if (debug) {
                System.out.println("Receiving output:");
            }
            out = conn.get("out");
            if (debug) {
                System.out.println(out);
                System.out.flush();
            }
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
            if (debug) {
                System.out.println("Feeding input:");
                System.out.println(inputScript);
            }
            conn.evalString(inputScript);
        }

        String execScript = prepareScript();
        if (debug) {
            System.out.println("Executing Matlab command:");
            System.out.println(execScript);
            System.out.flush();
        }
        conn.evalString(execScript);
        if (debug) {
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
