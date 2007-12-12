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
package org.objectweb.proactive.core.component.controller;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * The priority controller interface.
 *
 * @author Cedric Dalmasso
 */
@PublicAPI
public interface PriorityController {

    /**
     * All the possible kind of priority for a request on a component.
     *
     */
    public enum RequestPriority {F,
        NF1,
        NF2,
        NF3;
    }

    /**
     * Set priority of all methods named 'methodName' in the interface
     * 'interfaceName' to 'priority'.
     *
     * @param interfaceName Name of the component interface providing the
     * method
     * @param methodName Name of the method on which set the priority
     * @param priority The priority
     * @return true if success, else false
     */
    public void setPriority(String interfaceName, String methodName,
        RequestPriority priority);

    /**
     * Set priority of the method named 'methodName' with the signature
     * defined by 'parametersTypes' in the interface 'interfaceName' to
     * 'priority'.
     *
     * @param interfaceName Name of the component interface providing the
     * method
     * @param methodName Name of the method on which set the priority
     * @param parametersTypes The type of the method's parameters signature
     * @param priority The priority
     * @return true if success, else false
     */
    public void setPriority(String interfaceName, String methodName,
        Class<?>[] parametersTypes, RequestPriority priority);

    /**
     * Get the priority for a given method.
     *
     * @param interfaceName Name of the component interface
     * @param methodName Name of the method
     * @param parametersTypes The type of the method's parameters signature
     * @return
     */
    public RequestPriority getPriority(String interfaceName, String methodName,
        Class<?>[] parametersTypes);
}
