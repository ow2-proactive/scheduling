/*
 * ################################################################
 *
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
 * A simple example regarding the usage of the task scheduler, notice that it must implement Proactive task and be serializable!!
 * @author walzouab
 *
 */
package org.objectweb.proactive.examples.scheduler;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.scheduler.ProActiveTask;
import org.objectweb.proactive.scheduler.TaskScheduler;


public class HelloProActiveTask implements ProActiveTask, java.io.Serializable {
    //please use this logger instead of system.out.print
    private static Logger logger = ProActiveLogger.getLogger(Loggers.TASK_SCHEDULER);

    public static void main(String[] args) throws Exception {
        //this is the location of the scheduler
        String schedulerURL = "//localhost:1234/SchedulerNode";

        //here i will create the object to be submitted
        HelloProActiveTask task = new HelloProActiveTask();

        //here Taskscheduler will be created and connected to and the task will be subimtted for scheuling!!
        TaskScheduler scheduler = new TaskScheduler(); //create scheduler object
        scheduler.connectTo(schedulerURL); //bind the object to an actual scheduler
        scheduler.submit(task); //task submission
    }

    public HelloProActiveTask() {
    }

    public void run() {
        foo();
    }

    void foo() {
        logger.info("hi from " + getHostName());
    }

    String getHostName() {
        try {
            return java.net.InetAddress.getLocalHost().toString();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
