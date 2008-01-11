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
package org.objectweb.proactive.extensions.scheduler.examples;

import java.net.URI;
import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.resourcemanager.RMFactory;
import org.objectweb.proactive.extensions.resourcemanager.frontend.RMAdmin;
import org.objectweb.proactive.extensions.scheduler.core.AdminScheduler;
import org.objectweb.proactive.extensions.scheduler.resourcemanager.ResourceManagerProxy;


/**
 * LocalSchedulerExample start a new scheduler.
 *
 * @author jlscheef - ProActiveTeam
 * @version 3.9, Jul 17, 2007
 * @since ProActive 3.9
 *
 */
public class LocalSchedulerExample {
    //shows how to run the scheduler
    private static Logger logger = ProActiveLogger.getLogger(Loggers.SCHEDULER);
    private static RMAdmin admin;

    public static void main(String[] args) {
        //get the path of the file
        ResourceManagerProxy imp = null;

        String rm = null;
        String authPath = ".";

        if (args.length > 0 && args[0].startsWith("-a")) {
            authPath = args[1];
            if (args.length > 2) {
                rm = args[2];
            }
        } else if (args.length > 0) {
            rm = args[0];
        }

        try {
            if (rm != null) {
                try {
                    imp = ResourceManagerProxy.getProxy(new URI(rm));

                    logger.info("[SCHEDULER] Connect to ResourceManager on " + rm);
                } catch (Exception e) {
                    throw new Exception("ResourceManager doesn't exist on " + rm);
                }
            } else {
                RMFactory.startLocal();
                admin = RMFactory.getAdmin();
                logger.info("[SCHEDULER] Start local Resource Manager with 4 local nodes.");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                ProActiveDescriptor pad = PADeployment
                        .getProactiveDescriptor("../../../descriptors/scheduler/deployment/Local4JVM.xml");
                admin.addNodes(pad);

                //                Runtime.getRuntime().addShutdownHook(new Thread() {
                //                        public void run() {
                //                            try {
                //                                admin.killAll();
                //                            } catch (ProActiveException e) {
                //                                // TODO Auto-generated catch block
                //                                e.printStackTrace();
                //                            }
                //                        }
                //                    });
                imp = ResourceManagerProxy.getProxy(new URI("rmi://localhost:" +
                    System.getProperty("proactive.rmi.port") + "/"));

                logger.info("ResourceManager created on " + PAActiveObject.getActiveObjectNodeUrl(imp));
            }

            AdminScheduler.createScheduler(authPath, imp,
                    "org.objectweb.proactive.extensions.scheduler.policy.PriorityPolicy");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
