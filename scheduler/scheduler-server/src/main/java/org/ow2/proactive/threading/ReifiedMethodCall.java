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
package org.ow2.proactive.threading;

import java.lang.reflect.Method;


/**
 * ReifiedMethodCall is a simple class used to store method call in order to make the call later.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public class ReifiedMethodCall {

    private Method method;

    private Object[] arguments;

    /**
     * Create a new instance of MethodCall
     *
     * @param method
     * @param arguments
     */
    public ReifiedMethodCall(Method method, Object[] arguments) {
        this.method = method;
        this.arguments = arguments;
    }

    /**
     * Get the method
     *
     * @return the method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Set the method value to the given method value
     *
     * @param method the method to set
     */
    public void setMethod(Method method) {
        this.method = method;
    }

    /**
     * Get the arguments
     *
     * @return the arguments
     */
    public Object[] getArguments() {
        return arguments;
    }

    /**
     * Set the arguments value to the given arguments value
     *
     * @param arguments the arguments to set
     */
    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

}
