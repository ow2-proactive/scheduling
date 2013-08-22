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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
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
