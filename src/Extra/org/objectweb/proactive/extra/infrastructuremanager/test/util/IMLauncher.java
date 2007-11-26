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

import java.util.Vector;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.api.ProDeployment;
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
            "STARTING INFRASTRUCTURE MANAGER: Press <ENTER> to Shutdown.");
        System.out.println("IMLauncher.main()");
        IMFactory.startLocal();
        IMAdmin admin = IMFactory.getAdmin();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String urlPad;
        if (args.length > 0) {
            urlPad = args[0];
        } else {
            urlPad = "../../../descriptors/Workers.xml";
        }

        ProActiveDescriptor pad = ProDeployment.getProactiveDescriptor(urlPad);
        admin.addNodes(pad);

        Vector<String> v = new Vector<String>();
        v.add("//macyavel:6444");
        admin.createP2PNodeSource("P2P", 2, 10000, 50000, v);

        Thread.sleep(Integer.MAX_VALUE);

        try {
            IMFactory.getAdmin().shutdown();
        } catch (Exception e) {
            e.printStackTrace();
            ProActive.exitFailure();
        }
    }
}
