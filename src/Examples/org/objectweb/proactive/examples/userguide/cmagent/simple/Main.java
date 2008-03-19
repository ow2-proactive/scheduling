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
//@snippet-start CMA_Main
package org.objectweb.proactive.examples.userguide.cmagent.simple;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.ActiveObjectCreationException;


public class Main {
    public static void main(String args[]) {
        try {
            String currentState = new String();
            //@snippet-start CMA_instatiation
            //create the active oject
            CMAgent ao = (CMAgent) PAActiveObject.newActive(CMAgent.class.getName(), null); // 
            //@snippet-end CMA_instatiation
            //@snippet-start CMA_call 
            currentState = ao.getCurrentState().toString();
            System.out.println(currentState);
            //@snippet-end CMA_call
            PAActiveObject.terminateActiveObject(ao, true);
        } catch (NodeException nodeExcep) {
            System.err.println(nodeExcep.getMessage());
        } catch (ActiveObjectCreationException aoExcep) {
            System.err.println(aoExcep.getMessage());
        }
    }
}
//@snippet-end CMA_Main