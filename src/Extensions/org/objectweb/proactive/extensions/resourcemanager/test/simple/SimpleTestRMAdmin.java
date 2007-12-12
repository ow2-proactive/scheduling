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
package org.objectweb.proactive.extensions.resourcemanager.test.simple;

import java.util.ArrayList;

import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.extensions.resourcemanager.common.RMConstants;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMAdmin;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMConnection;


public class SimpleTestRMAdmin {
    public static String URL_PAD_LOCAL = "../../../descriptors/Workers2.xml";
    public static String[] vnodesName = new String[] { "Idefix", "Asterix" };

    public static void main(String[] args) {
        System.out.println("# --oOo-- Simple Test  Admin --oOo-- ");
        try {
            String url;
            if (args.length > 0) {
                url = args[0];
            } else {
                url = "rmi://localhost:1099/" +
                    RMConstants.NAME_ACTIVE_OBJECT_RMADMIN;
            }

            RMAdmin admin = RMConnection.connectAsAdmin(url);
            System.out.println("#[SimpleTestRMAdmin] deployAllVirtualNodes : " +
                URL_PAD_LOCAL);

            ProActiveDescriptor pad = PADeployment.getProactiveDescriptor(URL_PAD_LOCAL);
            ArrayList<ProActiveDescriptor> padList = new ArrayList<ProActiveDescriptor>();
            padList.add(pad);
            admin.createStaticNodesource("static source", padList);
            System.out.println("Sleep 12s");
            Thread.sleep(12000);
            admin.removeSource("static source", true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("##[TestRMAdmin] END TEST");
    }
}
