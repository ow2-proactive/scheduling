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
package org.objectweb.proactive.examples.pi;

import java.io.File;
import java.util.Set;

import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


public class MyPiSolved {
    public static void main(String[] args) throws Exception {
        Integer numberOfDecimals = new Integer(args[0]);
        String descriptorPath = args[1];

        GCMApplication descriptor = PAGCMDeployment.loadApplicationDescriptor(new File(descriptorPath));
        descriptor.startDeployment();
        GCMVirtualNode virtualNode = descriptor.getVirtualNode("computers-vn");
        virtualNode.waitReady();

        Set<Node> nodes = virtualNode.getCurrentNodes();

        Node[] nodeArray = nodes.toArray(new Node[0]);

        PiComputer piComputer = (PiComputer) PAGroup.newGroupInParallel(PiComputer.class.getName(),
                new Object[] { numberOfDecimals }, nodeArray);

        int numberOfWorkers = PAGroup.getGroup(piComputer).size();

        Interval intervals = PiUtil.dividePI(numberOfWorkers, numberOfDecimals.intValue());
        PAGroup.setScatterGroup(intervals);

        Result results = piComputer.compute(intervals);
        Result result = PiUtil.conquerPI(results);
        System.out.println("Pi:" + result);

        descriptor.kill();
        System.exit(0);
    }
}
