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
package org.objectweb.proactive.extensions.resourcemanager.utils;

import java.io.File;
import java.util.ArrayList;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.extensions.resourcemanager.RMFactory;
import org.objectweb.proactive.extensions.resourcemanager.common.FileToBytesConverter;
import org.objectweb.proactive.extensions.resourcemanager.common.RMConstants;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMAdmin;


/**
 * Class with main which instantiates a Resource Manager.
 *
 * @author The ProActive Team
 * @version 3.9
 * @since ProActive 3.9
 */
public class RMLauncher {

    public static void main(String[] args) throws Exception {
        System.out.println("STARTING RESOURCE MANAGER: Press 'e' to shutdown.");
        RMFactory.startLocal();
        RMAdmin admin = RMFactory.getAdmin();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int i = 1;
        if (args.length > 0) {

            ArrayList<ProActiveDescriptor> padList = new ArrayList<ProActiveDescriptor>();
            for (String desc : args) {
                padList.add(PADeployment.getProactiveDescriptor(desc));
            }
            admin.createStaticNodesource("PAD" + RMConstants.DEFAULT_STATIC_SOURCE_NAME, padList);

        } else {
            File GCMDeployFile = new File("../../../descriptors/scheduler/deployment/Local4JVMDeployment.xml");
            admin.addNodes((FileToBytesConverter.convertFileToByteArray(GCMDeployFile)));
        }

        //                        Vector<String> v = new Vector<String>();
        //                        v.add("//localhost:6444");
        //                        try {
        //                        	admin.createDynamicNodeSource("P2P", 3, 10000, 50000, v);
        //                        } catch (RMException e) {
        //                        	e.printStackTrace();
        //                        }

        @SuppressWarnings("unused")
        char typed = 'x';
        while ((typed = (char) System.in.read()) != 'e') {
        }
        try {
            RMFactory.getAdmin().shutdown(false);
        } catch (Exception e) {
            e.printStackTrace();
            PALifeCycle.exitFailure();
        }
    }
}
