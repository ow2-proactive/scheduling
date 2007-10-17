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
package org.objectweb.proactive.extra.infrastructuremanager.test.util;

import java.io.File;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.extra.infrastructuremanager.IMFactory;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMAdmin;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMMonitoring;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMUser;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.dynamic.DynamicNodeSource;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.dynamic.P2PNodeSource;


public class IMLauncher {

    /**
     *
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        System.out.println(
            "STARTING INFRASTRUCTURE MANAGER: Press <ENTER> to Shutdown.");
        IMFactory.startLocal();
        IMAdmin admin = IMFactory.getAdmin();

        //		DynamicNodeSource d = (DynamicNodeSource) ProActiveObject.newActive(P2PNodeSource.class
        //				.getCanonicalName(), new Object[] { "Nodes on P2P", 3, 3000, 30000 });
        //		admin.addDynamicNodeSources(d);
        IMUser user = IMFactory.getUser();
        IMMonitoring monitor = IMFactory.getMonitoring();
        admin.deployAllVirtualNodes(new File(
                "/user/jfradj/home/WorkSpace/ProActive/descriptors/scheduler/deployment/Demo_descriptor.xml"),
            null);
        // admin.deployAllVirtualNodes(new File(
        // "/user/jmartin/home/test.xml"),
        // null);
        // PADNSInterface padInterface = admin.getPADNodeSource();
        // padInterface.
        Thread.sleep(Integer.MAX_VALUE);
        // System.out.println("Number of nodes : "+
        // monitor.getNumberOfAllResources().intValue());
        //        
        // System.out.println("Asking for 2 nodes :");
        // NodeSet ns = user.getAtMostNodes(new IntWrapper(3), null);
        // System.out.println("Nodes obtained : "+ ns.size());
        // System.out.println("Free nodes : "+
        // monitor.getNumberOfFreeResource().intValue());
        // System.out.println("Free nodes : "+
        // monitor.getNumberOfFreeResource().intValue());
        // System.in.read();
        try {
            IMFactory.getAdmin().shutdown();
        } catch (Exception e) {
            e.printStackTrace();
            ProActive.exitFailure();
        }
    }
}
