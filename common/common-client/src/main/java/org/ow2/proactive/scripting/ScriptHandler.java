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

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.annotation.ActiveObject;


/**
 * A script handler is an object able to perform execution of a script.
 *
 * @author ProActive team
 *
 */
@ActiveObject
@PublicAPI
public class ScriptHandler implements Serializable {

    Map<String, Object> additionalBindings = null;

    /**
     * ProActive Constructor
     */
    public ScriptHandler() {
    }

    /**
     * Execute a script
     * @param script a script to execute
     * @param outputSink the output stream for the script
     * @param errorSink the error stream for the script
     * @return a ScriptResult object containing the result.
     */
    public <T> ScriptResult<T> handle(Script<T> script, PrintStream outputSink, PrintStream errorSink) {
        try {
            return script.execute(additionalBindings, outputSink, errorSink);
        } catch (Throwable t) {
            ScriptException se = new ScriptException("An exception occurred while executing the script " +
                                                     script.getClass().getSimpleName() +
                                                     ((script.parameters != null) ? " with parameters=" +
                                                                                    Arrays.asList(script.parameters)
                                                                                  : "") +
                                                     ", and content:\n" + script.getScript(), t);

            return new ScriptResult<>(se);
        }
    }

    /**
     * Execute a script using standard error and output as the output sink.
     * @param script a script to execute
     * @return a ScriptResult object containing the result.
     */
    public <T> ScriptResult<T> handle(Script<T> script) {
        return script.execute(additionalBindings, System.out, System.err);
    }

    /**
     * Add a binding to the script that will be handle by this handler.
     *
     * @param name the name of the variable
     * @param value the value of the variable
     */
    public void addBinding(String name, Object value) {
        if (additionalBindings == null) {
            additionalBindings = new HashMap<>();
        }
        additionalBindings.put(name, value);
    }

    /**
     * Add multiple bindings to the script that will be handle by this handler.
     * @param bindings as a map
     */
    public void addBindings(Map<String, Object> bindings) {
        if (additionalBindings == null) {
            additionalBindings = new HashMap<>();
        }
        additionalBindings.putAll(bindings);
    }
}
