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

package org.objectweb.proactive.examples.userguide.cmagent.simple;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.ActiveObjectCreationException;


public class Main {
    public static void main(String args[]) {
        try {
            String currentState = new String();

            //TODO 1. Create the active object
            CMAgent ao = (CMAgent) PAActiveObject.newActive(CMAgent.class.getName(), null);

            //TODO 2. Get the current state
            currentState = ao.getCurrentState().toString();

            //TODO 3. Print the state
            System.out.println(currentState);

            //TODO 4. Stop the active object
            PAActiveObject.terminateActiveObject(ao, true);

        } catch (NodeException nodeExcep) {
            System.err.println(nodeExcep.getMessage());
        } catch (ActiveObjectCreationException aoExcep) {
            System.err.println(aoExcep.getMessage());
        }
    }
}
