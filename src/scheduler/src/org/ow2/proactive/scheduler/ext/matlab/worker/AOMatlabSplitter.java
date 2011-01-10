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
package org.ow2.proactive.scheduler.ext.matlab.worker;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.ext.matlab.common.PASolveMatlabGlobalConfig;
import org.ow2.proactive.scheduler.ext.matlab.common.PASolveMatlabTaskConfig;
import org.ow2.proactive.scheduler.ext.matsci.common.exception.IllegalReturnTypeException;
import org.ow2.proactive.scheduler.ext.matsci.common.exception.InvalidNumberOfParametersException;
import org.ow2.proactive.scheduler.ext.matsci.common.exception.InvalidParameterException;
import org.ow2.proactive.scheduler.ext.matlab.worker.util.MatlabEngineConfig;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;

import java.io.Serializable;
import java.util.ArrayList;


public class AOMatlabSplitter extends AOMatlabWorker {

    /**
     * 
     */
    private static final long serialVersionUID = 30L;
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
    public AOMatlabSplitter(MatlabEngineConfig matlabConfig) {
        super(matlabConfig);

    }

    /**
     *
     * @param inputScript a pre-matlab script that will be launched before the main one (e.g. to set input params)
     * @param scriptLines a list of lines which represent the main script
     * @param debug debug mode on
     * @param numberOfChildren the number of children to which the input will be divided
     */
    public void init(String inputScript, ArrayList<String> scriptLines, PASolveMatlabGlobalConfig paconfig,
            PASolveMatlabTaskConfig taskconfig, MatlabEngineConfig matlabConfig, Integer numberOfChildren) {
        super.init(inputScript, scriptLines, paconfig, taskconfig, matlabConfig);
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
