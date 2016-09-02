/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
package org.ow2.proactive.scripting;

import org.objectweb.proactive.annotation.PublicAPI;

import javax.script.Bindings;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;


/**
 * A task Script : return true by default, 'result' binding can be used to change the returned value.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.4
 */
@PublicAPI
public class TaskScript extends Script<Serializable> {
    /**
     * The variable name which must be set after the evaluation
     * of a script task.
     */
    public static final String RESULT_VARIABLE = "result";
    /**
     * The variable name to access results from dependent tasks (an array).
     */
    public static final String RESULTS_VARIABLE = "results";
    /**
     * The variable name to access the user's third party credentials.
     */
    public static final String CREDENTIALS_VARIABLE = "credentials";

    /**
     * The variable containing a proxy to the scheduler server
     */
    public static final String SCHEDULER_CLIENT_VARIABLE = "scheduler";

    public TaskScript(Script script) throws InvalidScriptException {
        super(script);
    }

    @Override
    protected ScriptResult<Serializable> getResult(Object evalResult, Bindings bindings) {
        if (bindings.containsKey(RESULT_VARIABLE)) {
            Object result = bindings.get(RESULT_VARIABLE);
            if (result == null) {
                return new ScriptResult<>(null);
            } else {
                if (result instanceof Serializable) {
                    return new ScriptResult<>((Serializable) result);
                } else {
                    return new ScriptResult<>(new Exception(
                      "Bad result format : awaited Serializable, found " + result.getClass().getName()));
                }
            }
        } else {
            if(evalResult != null && evalResult instanceof Serializable) {
                return new ScriptResult<>((Serializable) evalResult);
            } else {
                // assuming script ran fine
                return new ScriptResult<Serializable>(true);
            }
        }
    }

    @Override
    protected String getDefaultScriptName() {
        return "TaskScript";
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    protected void prepareSpecialBindings(Bindings bindings) {

    }

    @Override
    protected Reader getReader() {
        return new StringReader(script);
    }

}
