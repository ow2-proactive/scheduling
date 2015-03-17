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
package org.ow2.proactive.scripting;

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

    private static final long serialVersionUID = 61L;

    Map<String, Object> additionalBindings = null;

    /**
     * ProActive Constructor
     */
    public ScriptHandler() {
    }

    /**
     * Execute a script
     * @param script a script to execute
     * @return a ScriptResult object containing the result.
     */
    public <T> ScriptResult<T> handle(Script<T> script) {
        try {
            return script.execute(additionalBindings);
        } catch (Throwable t) {
            ScriptException se = new ScriptException("An exception occurred while executing the script " +
                script.getClass().getSimpleName() +
                ((script.parameters != null) ? " with parameters=" + Arrays.asList(script.parameters) : "") +
                ", and content:\n" + script.getScript(), t);

            return new ScriptResult<T>(se);
        }
    }

    /**
     * Add a binding to the script that will be handle by this handler.
     *
     * @param name the name of the variable
     * @param value the value of the variable
     */
    public void addBinding(String name, Object value) {
        if (additionalBindings == null) {
            additionalBindings = new HashMap<String, Object>();
        }
        additionalBindings.put(name, value);
    }

    /**
     * no implemented
     */
    public void destroy() {

        //TODO gsigety probably something to do, like stop execution of the script ?
    }
}
