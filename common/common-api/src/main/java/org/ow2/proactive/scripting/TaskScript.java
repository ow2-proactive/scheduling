/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scripting;

import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;

import javax.script.Bindings;

import org.objectweb.proactive.annotation.PublicAPI;


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
                    return new ScriptResult<>(new Exception("Bad result format : awaited Serializable, found " +
                                                            result.getClass().getName()));
                }
            }
        } else {
            if (evalResult != null && evalResult instanceof Serializable) {
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
