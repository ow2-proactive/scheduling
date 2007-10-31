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
package org.objectweb.proactive.extra.scheduler.examples;

import java.io.File;
import java.net.URI;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.infrastructuremanager.IMFactory;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMAdmin;
import org.objectweb.proactive.extra.scheduler.common.scheduler.AdminSchedulerInterface;
import org.objectweb.proactive.extra.scheduler.core.AdminScheduler;
import org.objectweb.proactive.extra.scheduler.resourcemanager.InfrastructureManagerProxy;


/**
 * LocalSchedulerExample start a new scheduler.
 *
 * @author jlscheef - ProActiveTeam
 * @date 18 oct. 07
 * @version 3.2
 *
 */
public class LocalSchedulerExample {
    //shows how to run the scheduler
    private static Logger logger = ProActiveLogger.getLogger(Loggers.SCHEDULER);
    protected static IMAdmin admin;

    public static void main(String[] args) {
        //get the path of the file
        InfrastructureManagerProxy imp = null;

        try {
            if (args.length > 0) {
                try {
                    imp = InfrastructureManagerProxy.getProxy(new URI(args[0]));

                    logger.info("Connect to ResourceManager on " + args[0]);
                } catch (Exception e) {
                    throw new Exception("ResourceManager doesn't exist on " +
                        args[0]);
                }
            } else {
                IMFactory.startLocal();
                admin = IMFactory.getAdmin();

                admin.deployAllVirtualNodes(new File(
                        "../../../descriptors/scheduler/deployment/test.xml"),
                    null);

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
                imp = InfrastructureManagerProxy.getProxy(new URI(
                            "rmi://localhost:" +
                            System.getProperty("proactive.rmi.port") + "/"));

                logger.info("ResourceManager created on " +
                    ProActiveObject.getActiveObjectNodeUrl(imp));
            }

            AdminScheduler.createScheduler(LocalSchedulerExample.class.getResource(
                    "login.cfg").getFile(),
                LocalSchedulerExample.class.getResource("groups.cfg").getFile(),
                imp,
                "org.objectweb.proactive.extra.scheduler.policy.PriorityPolicy");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
