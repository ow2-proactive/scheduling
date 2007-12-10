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

import java.util.ArrayList;

import org.objectweb.proactive.extensions.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extensions.scheduler.ext.matlab.exception.IllegalReturnTypeException;
import org.objectweb.proactive.extensions.scheduler.ext.matlab.exception.InvalidNumberOfParametersException;
import org.objectweb.proactive.extensions.scheduler.ext.matlab.exception.InvalidParameterException;

import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;


public class AOMatlabSplitter extends AOSimpleMatlab {

    /**
         *
         */
    private static final long serialVersionUID = -1673684977325219341L;
    private int numberOfChildren;

    public AOMatlabSplitter() {
    }

    /**
     * Constructor for a Splitter task
     * @param matlabCommandName the name of the Matlab command
     * @param inputScript a pre-matlab script that will be launched before the main one (e.g. to set input params)
     * @param scriptLines a list of lines which represent the main script
     * @param numberOfChildren the number of children to which the input will be divided
     */
    public AOMatlabSplitter(String matlabCommandName, String inputScript,
        ArrayList<String> scriptLines, Integer numberOfChildren) {
        super(matlabCommandName, inputScript, scriptLines);
        this.numberOfChildren = numberOfChildren;
    }

    public Object execute(TaskResult... results) throws Throwable {
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
            MatlabEngine.put("in", in);
        }

        MatlabEngine.put("nout", new IntToken(numberOfChildren));
        executeScript();

        Token result = MatlabEngine.get("out");

        if (!(result instanceof ArrayToken)) {
            throw new IllegalReturnTypeException(result.getClass());
        }

        return new SplittedResult((ArrayToken) result);
    }
}
