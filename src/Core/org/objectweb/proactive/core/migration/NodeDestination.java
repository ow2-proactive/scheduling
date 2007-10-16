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
package org.objectweb.proactive.core.migration;

import java.io.Serializable;


public class NodeDestination implements Destination, Serializable {
    private String nodeURL;
    private String methodName;

    /**
     * Construct a Roadmap with location l and methodName name.
     * We don't use the parameters yet.
     */
    public NodeDestination(String nodeURL, String methodName) {
        this.nodeURL = nodeURL;
        this.methodName = methodName;
    }

    /**
     * Returns a string representing the node
     */
    public String getDestination() {
        return nodeURL;
    }

    /**
     * Return the name of the Method
     */
    public String getMethodName() {
        return methodName;
    }
}
