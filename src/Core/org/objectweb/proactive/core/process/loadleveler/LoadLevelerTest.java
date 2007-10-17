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
package org.objectweb.proactive.core.process.loadleveler;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.objectweb.proactive.api.ProDeployment;
import org.objectweb.proactive.api.ProGroup;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public class LoadLevelerTest {
    public LoadLevelerTest() {
    }

    public StringWrapper ping() {
        try {
            return new StringWrapper("Hi from " +
                URIBuilder.getLocalAddress().getHostName());
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new StringWrapper("RESOLUTION FAILED");
        }
    }

    public static void main(String[] args) {
        try {
            //		String path = "/users/icmcb/cave/proactive.loadleveler/descriptors/examples";
            ProActiveDescriptor pad =  //			ProActive.getProactiveDescriptor(path+"/LoadLeveler_SimpleExample.xml");
                                       //			ProActive.getProactiveDescriptor(path+"/LoadLeveler_AdvancedTasksPerNodeExample.xml");
                                       //			ProActive.getProactiveDescriptor(path+"/LoadLeveler_AdvancedTotalTasksExample.xml");
                                       //			ProActive.getProactiveDescriptor(path+"/LoadLeveler_AdvancedTaskGeometryExample.xml");
                ProDeployment.getProactiveDescriptor(args[0]);

            //			ProActive.getProactiveDescriptor(path+"/SSH_LSF_Example.xml");
            pad.activateMappings();
            VirtualNode vn = pad.getVirtualNode("levelerVn");

            LoadLevelerTest group = (LoadLevelerTest) ProGroup.newActiveAsGroup(LoadLevelerTest.class.getName(),
                    null, vn);

            StringWrapper p = group.ping();

            ProGroup.waitAll(p);

            StringWrapper sw1 = (StringWrapper) ProGroup.get(p, 0);
            StringWrapper sw2 = (StringWrapper) ProGroup.get(p, 1);

            System.out.println(sw1);
            System.out.println(sw2);
        } catch (ProActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
