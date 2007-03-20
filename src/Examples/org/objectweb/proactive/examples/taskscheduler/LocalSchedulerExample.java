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
package org.objectweb.proactive.examples.taskscheduler;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.taskscheduler.AdminScheduler;
import org.objectweb.proactive.extra.taskscheduler.resourcemanager.SimpleResourceManager;


public class LocalSchedulerExample {

    /**
     * @param args
     */

    //shows how to run the scheduler
    private static Logger logger = ProActiveLogger.getLogger(Loggers.TASK_SCHEDULER);

    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub

        //get the path of the file
        String xmlURL = SimpleResourceManager.class.getResource(
                "/org/objectweb/proactive/examples/taskscheduler/test.xml")
                                                   .getPath();
        String SNode = args[0];

        try {
            SimpleResourceManager rm = (SimpleResourceManager) ProActive.newActive(SimpleResourceManager.class.getName(),
                    null);

            AdminScheduler adminAPI = AdminScheduler.createLocalScheduler(rm,
                    SNode);

            rm.addNodes(xmlURL);
            adminAPI.start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.error("error creating Scheduler" + e.toString());
        }
    }
}
