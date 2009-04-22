/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.ext.matlab.exception.IllegalReturnTypeException;
import org.ow2.proactive.scheduler.ext.matlab.exception.InvalidNumberOfParametersException;
import org.ow2.proactive.scheduler.ext.matlab.exception.InvalidParameterException;
import org.ow2.proactive.scheduler.ext.matlab.util.MatlabConfiguration;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;

import java.io.Serializable;
import java.util.ArrayList;


public class AOMatlabSplitter extends AOMatlabWorker {

    /**
     *
     */
    private int numberOfChildren;

    public AOMatlabSplitter() {
    }

    /**
     * Constructor for a Splitter task
     * @param matlabConfig the configuration of the MatlabEngine
     */
    public AOMatlabSplitter(MatlabConfiguration matlabConfig) {
        super(matlabConfig);

    }

    /**
     *
     * @param inputScript a pre-matlab script that will be launched before the main one (e.g. to set input params)
     * @param scriptLines a list of lines which represent the main script
     * @param debug debug mode on
     * @param numberOfChildren the number of children to which the input will be divided
     */
    public void init(String inputScript, ArrayList<String> scriptLines, boolean debug,
            Integer numberOfChildren) {
        super.init(inputScript, scriptLines, debug);
        this.numberOfChildren = numberOfChildren;
    }

    public Serializable execute(TaskResult... results) throws Throwable {
        MatlabEngine.Connection conn = MatlabEngine.acquire();
        conn.clear();
        if (results.length > 1) {
            throw new InvalidNumberOfParametersException(results.length);
        }

        if (results.length == 1) {
            TaskResult res = results[0];

            if (res.hadException()) {
                throw res.getException();
            }

            if (!(res.value() instanceof Token)) {
                throw new InvalidParameterException(res.value().getClass());
            }

            Token in = (Token) res.value();
            conn.put("in", in);
        }

        conn.put("nout", new IntToken(numberOfChildren));
        executeScript(conn);

        Token result = conn.get("out");

        if (!(result instanceof ArrayToken)) {
            throw new IllegalReturnTypeException(result.getClass());
        }

        SplittedResult splitted = new SplittedResult((ArrayToken) result);
        conn.release();
        return splitted;
    }
}
