/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.utils;

import java.io.File;

import org.objectweb.proactive.api.PALifeCycle;
import org.ow2.proactive.resourcemanager.RMFactory;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;
import org.ow2.proactive.resourcemanager.frontend.RMAdmin;


/**
 * Class with main which instantiates a Resource Manager.
 *
 * @author The ProActive Team
 * @since ProActive 3.9
 */
public class RMLauncher {

    /**
     * main function
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        System.out.println("STARTING RESOURCE MANAGER: Press 'e' to shutdown.");
        RMFactory.startLocal();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        RMAdmin admin = RMFactory.getAdmin();

        if (args.length > 0) {
            for (String desc : args) {
                File GCMDeployFile = new File(desc);
                admin.addNodes(FileToBytesConverter.convertFileToByteArray(GCMDeployFile));
            }
        } else {
            //select the appropriate deployment descriptor regarding to the OS
            if (System.getProperty("os.name").contains("Windows")) {
                File GCMDeployFile = new File(PAResourceManagerProperties.RM_HOME.getValueAsString() +
                    File.separator + "config/deployment/Local4JVMDeploymentWindows.xml");
                admin.addNodes(FileToBytesConverter.convertFileToByteArray(GCMDeployFile));
            } else {
                File GCMDeployFile = new File(PAResourceManagerProperties.RM_HOME.getValueAsString() +
                    File.separator + "config/deployment/Local4JVMDeploymentUnix.xml");
                admin.addNodes(FileToBytesConverter.convertFileToByteArray(GCMDeployFile));
            }
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
