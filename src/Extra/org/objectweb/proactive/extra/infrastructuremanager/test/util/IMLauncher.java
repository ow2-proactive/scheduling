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

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.extra.infrastructuremanager.IMFactory;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMAdmin;


public class IMLauncher {

    /**
     *
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        System.out.println(
            "STARTING INFRASTRUCTURE MANAGER: Press 'e' to shutdown.");
        IMFactory.startLocal();
        IMAdmin admin = IMFactory.getAdmin();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (args.length > 0) {
            for (String desc : args) {
                ProActiveDescriptor pad = PADeployment.getProactiveDescriptor(desc);
                admin.addNodes(pad);
            }
        } else {
            ProActiveDescriptor pad = PADeployment.getProactiveDescriptor(
                    "../../../descriptors/scheduler/deployment/Local4JVM.xml");
            admin.addNodes(pad);
        }

        //        Vector<String> v = new Vector<String>();
        //        v.add("//macyavel:6444");
        //        admin.createP2PNodeSource("P2P", 2, 10000, 50000, v);

        //DynamicNodeSource d = (DynamicNodeSource) PAActiveObject.newActive(P2PNodeSource.class.getCanonicalName(),
        //new Object[] { "Nodes on P2P", 4, 6, 60000000 });
        //admin.addDynamicNodeSources(d);
        //IMUser user = IMFactory.getUser();
        //IMMonitoring monitor = IMFactory.getMonitoring();

        // Thread.sleep(Integer.MAX_VALUE);
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
        System.out.println("[RESSOUCE MANAGER] Press e+enter to exit...");
        char typed = 'x';
        while ((typed = (char) System.in.read()) != 'e') {
        }
        try {
            IMFactory.getAdmin().shutdown(false);
        } catch (Exception e) {
            e.printStackTrace();
            ProActive.exitFailure();
        }
    }
}
