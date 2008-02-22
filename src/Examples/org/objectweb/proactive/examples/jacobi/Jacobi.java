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
package org.objectweb.proactive.examples.jacobi;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.GCMApplicationDescriptor;
import org.objectweb.proactive.extra.gcmdeployment.core.GCMVirtualNode;


public class Jacobi implements Serializable {

    /**
     * Number of columns of SubMatrix
     */
    public static final int WIDTH = 3;

    /**
     * Number of lines of SubMatrix
     */
    public static final int HEIGHT = 3;

    /**
     * Max number of iterations
     */
    public static final int ITERATIONS = 40;

    /**
     * Min diff to stop
     */
    public static final double MINDIFF = 0.000000001;

    /**
     * Default external border value
     */
    public static final double DEFAULT_BORDER_VALUE = 1;

    /**
     * the filename which will store the results
     */
    public static final String resultsFileName = "resultsJacobi.txt";
    private GCMApplicationDescriptor descriptor = null;
    public static Jacobi singleton = null;
    private static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    private Node[] nodes;

    public Jacobi() {
    }

    public Jacobi(File descriptorFile) {

        nodes = null;
        try {
            descriptor = PAGCMDeployment.loadApplicationDescriptor(descriptorFile);
        } catch (ProActiveException e) {
            System.err.println("** ProActiveException **");
        }
        descriptor.startDeployment();
        GCMVirtualNode vnode = descriptor.getVirtualNode("matrixNode");

        vnode.waitReady();

        Set<Node> currentNodes = vnode.getCurrentNodes();
        nodes = currentNodes.toArray(new Node[0]);

    }

    public Node[] getNodes() {
        return nodes;
    }

    public static Jacobi getSingleton(File descriptor) {
        if (singleton == null) {
            Object[] params = new Object[1];
            params[0] = descriptor;
            try {
                singleton = (Jacobi) PAActiveObject.newActive(Jacobi.class.getName(), params);
            } catch (ActiveObjectCreationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NodeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return singleton;
    }

    public void terminateAll() {
        // Terminating
        descriptor.kill();
        PALifeCycle.exitSuccess();
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            logger.error("Usage: java " + Jacobi.class.getName() + " <deployment file>");
            System.exit(1);
        }

        File resultFile = new File(System.getProperty("user.dir") + File.separator + resultsFileName);

        if (resultFile.exists()) {
            resultFile.delete();
        }

        try {
            if (!resultFile.createNewFile()) {
                logger.error("Error creating : " + resultFile.getAbsolutePath());
                System.exit(1);
            }
        } catch (IOException e2) {
            e2.printStackTrace();
            System.exit(1);
        }

        Jacobi jacobi = getSingleton(new File(args[0]));

        Object[][] params = new Object[Jacobi.WIDTH * Jacobi.HEIGHT][];
        for (int i = 0; i < params.length; i++) {
            params[i] = new Object[3];
            params[i][0] = "SubMatrix" + i;
            params[i][1] = resultFile;
            params[i][2] = jacobi;
        }

        SubMatrix matrix = null;
        Node[] jacobiNodes = jacobi.getNodes();

        try {
            matrix = (SubMatrix) PASPMD.newSPMDGroup(SubMatrix.class.getName(), params, jacobiNodes);
            matrix.compute();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
