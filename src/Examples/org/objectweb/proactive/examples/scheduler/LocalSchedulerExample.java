/*
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */

/**
 * this is how we would run the scheduler
 *
 * @author walzouab
 *
 */
package org.objectweb.proactive.examples.scheduler;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.scheduler.AdminScheduler;
import org.objectweb.proactive.extra.scheduler.resourcemanager.SimpleResourceManager;


public class LocalSchedulerExample {
    //shows how to run the scheduler
    private static Logger logger = ProActiveLogger.getLogger(Loggers.TASK_SCHEDULER);

    public static void main(String[] args) {
        //get the path of the file
        SimpleResourceManager rm;

        try {
            if (args.length == 2) {
                try {
                    rm = (SimpleResourceManager) NodeFactory.getNode(args[1])
                                                            .getActiveObjects(SimpleResourceManager.class.getName())[0];

                    logger.info("Connect to ResourceManager on " + args[1]);
                } catch (Exception e) {
                    throw new Exception("ResourceManager doesn't exist on " +
                        args[1]);
                }
            } else {
                String xmlURL = SimpleResourceManager.class.getResource(
                        "/org/objectweb/proactive/examples.scheduler/test.xml")
                                                           .getPath();
                rm = (SimpleResourceManager) ProActive.newActive(SimpleResourceManager.class.getName(),
                        null);
                logger.info("ResourceManager created on " +
                    ProActive.getActiveObjectNodeUrl(rm));
                rm.addNodes(xmlURL);
            }

            String SNode;
            if (args.length > 0) {
                SNode = args[0];
            } else {
                SNode = "//localhost/SCHEDULER_NODE";
            }

            AdminScheduler adminAPI = AdminScheduler.createLocalScheduler(rm,
                    SNode);

            adminAPI.start();
        } catch (Exception e) {
            logger.error("error creating Scheduler" + e.toString());
            System.exit(1);
        }
    }
}
