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
package org.objectweb.proactive.core.security.test.jacobi;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.NodeException;


/**
 * @author The ProActive Team
 */
public class Jacobi {

    /**
     * Number of columns of SubMatrix
     */
    public static final int WIDTH = 4;

    /**
     * Number of lines of SubMatrix
     */
    public static final int HEIGHT = 4;

    /**
     * Max number of iterations
     */
    public static final int ITERATIONS = 100;

    /**
     * Min diff to stop
     */
    public static final double MINDIFF = 0.01;

    /**
     * Default external border value
     */
    public static final double DEFAULT_BORDER_VALUE = 0;

    public static void main(String[] args) {
        ProActiveDescriptor proActiveDescriptor = null;
        String[] nodes = null;
        try {
            proActiveDescriptor = PADeployment.getProactiveDescriptor("file:" + args[0]);
        } catch (ProActiveException e) {
            System.err.println("** ProActiveException **");
            e.printStackTrace();
        }
        proActiveDescriptor.activateMappings();
        VirtualNode vn = proActiveDescriptor.getVirtualNode("matrixNode");
        try {
            nodes = vn.getNodesURL();
        } catch (NodeException e) {
            System.err.println("** NodeException **");
        }

        //		String[] nodes = { "//crusoe:1243/node1", "//crusoe:1244/node2", "//crusoe:1246/node4",
        //									   "//crusoe:1246/node4", "//crusoe:1245/node3", "//crusoe:1246/node4",
        //									   "//crusoe:1246/node4", "//crusoe:1246/node4", "//crusoe:1246/node4" };
        //		String[] nodes = { "//tuba:6512/node1", "//scurra:6512/node2", "//lo:6512/node3", "//wapiti:6512/node4" };
        Object[][] params = new Object[Jacobi.WIDTH * Jacobi.HEIGHT][];
        for (int i = 0; i < params.length; i++) {
            params[i] = new Object[1];
            params[i][0] = "SubMatrix" + i;
        }

        SubMatrix matrix = null;
        try {
            matrix = (SubMatrix) PASPMD.newSPMDGroup(SubMatrix.class.getName(), params, nodes);
        } catch (ClassNotFoundException e) {
            System.err.println("** ClassNotFoundException **");
        } catch (ClassNotReifiableException e) {
            System.err.println("** ClassNotReifiableException **");
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
            System.err.println("** ActiveObjectCreationException **");
        } catch (NodeException e) {
            System.err.println("** NodeException **");
        }

        matrix.compute();
    }
}
