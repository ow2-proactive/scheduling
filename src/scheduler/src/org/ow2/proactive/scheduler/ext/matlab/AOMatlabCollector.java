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

import java.io.Serializable;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.ext.matlab.exception.InvalidNumberOfParametersException;
import org.ow2.proactive.scheduler.ext.matlab.exception.InvalidParameterException;
import ptolemy.data.ArrayToken;
import ptolemy.data.Token;


public class AOMatlabCollector extends AOSimpleMatlab {

    /**
     *
     */
    public AOMatlabCollector() {
    }

    /**
     * Constructor for the Collector task
     * @param matlabCommandName the name of the Matlab command
     * @param inputScript  a pre-matlab script that will be launched before the main one (e.g. to set input params)
     * @param scriptLines a list of lines which represent the main script
     */
    public AOMatlabCollector(String matlabCommandName) {
        super(matlabCommandName);
    }

    public Serializable execute(TaskResult... results) throws Throwable {
        MatlabEngine.Connection conn = MatlabEngine.acquire();
        conn.clear();
        if (results.length <= 0) {
            throw new InvalidNumberOfParametersException(results.length);
        }

        Token[] tokens = new Token[results.length];

        for (int i = 0; i < results.length; i++) {
            TaskResult res = results[i];

            if (res.hadException()) {
                throw res.getException();
            }

            if (!(res.value() instanceof Token)) {
                throw new InvalidParameterException(res.getClass());
            }

            Token token = (Token) res.value();

            if (i > 0) {
                if (!tokens[i - 1].getType().equals(token.getType())) {
                    throw new InvalidParameterException(token.getType(), tokens[i - 1].getType());
                }
            }

            tokens[i] = token;
        }

        ArrayToken collectArray = new ArrayToken(tokens);
        conn.put("in", collectArray);
        executeScript(conn);

        Token out = conn.get("out");
        conn.release();
        return out;
    }
}
