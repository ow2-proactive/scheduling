/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.calcium.environment.proactivescheduler;

import java.net.URI;

import javax.security.auth.login.LoginException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.extensions.calcium.environment.EnvironmentFactory;
import org.objectweb.proactive.extensions.calcium.environment.FileServerClient;
import org.objectweb.proactive.extensions.calcium.environment.proactive.AOTaskPool;
import org.objectweb.proactive.extensions.calcium.environment.proactive.FileServerClientImpl;
import org.objectweb.proactive.extensions.calcium.environment.proactive.Util;
import org.objectweb.proactive.extensions.calcium.task.TaskPool;
import org.objectweb.proactive.extra.scheduler.common.exception.SchedulerException;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerAuthenticationInterface;
import org.objectweb.proactive.extra.scheduler.common.scheduler.SchedulerConnection;


public class ProActiveSchedulerEnvironment implements EnvironmentFactory {
    AOTaskPool taskpool;
    TaskDispatcher dispatcher;
    AOJobListener joblistener;
    FileServerClientImpl fserver;

    public ProActiveSchedulerEnvironment(String host, String user,
        String password)
        throws NodeException, ActiveObjectCreationException, LoginException,
            SchedulerException {
        this(URI.create("//" + host + "/" +
                SchedulerConnection.SCHEDULER_DEFAULT_NAME), user, password);
    }

    public ProActiveSchedulerEnvironment(URI schedUri, String user,
        String password)
        throws NodeException, ActiveObjectCreationException, LoginException,
            SchedulerException {
        this(SchedulerConnection.join(schedUri.toString()), user, password);
    }

    public ProActiveSchedulerEnvironment(
        SchedulerAuthenticationInterface auth, String user, String password)
        throws NodeException, ActiveObjectCreationException {
        Node localnode = NodeFactory.getDefaultNode();

        this.fserver = Util.createFileServer(localnode);
        this.taskpool = Util.createActiveTaskPool(localnode);

        this.joblistener = AOJobListener.createAOJobListener(localnode,
                taskpool, auth, user, password);
        this.dispatcher = new TaskDispatcher(taskpool, fserver, joblistener,
                auth, user, password);
    }

    /*
            public ProActiveSchedulerEnvironment(UserSchedulerInterface scheduler) throws NodeException, ActiveObjectCreationException, SchedulerException{
            Node localnode = NodeFactory.getDefaultNode();

                    this.fserver = Util.createFileServer(localnode);
                    this.taskpool = Util.createActiveTaskPool(localnode);

                    this.scheduler=scheduler;
                    this.joblistener = Util.createAOJobListener(localnode);

                    this.scheduler.addSchedulerEventListener(joblistener, SchedulerEvent.JOB_KILLED);              //JOB_KILLED("jobKilledEvent"),
                    this.scheduler.addSchedulerEventListener(joblistener, SchedulerEvent.RUNNING_TO_FINISHED_JOB); //RUNNING_TO_FINISHED_JOB("runningToFinishedJobEvent")
                    this.scheduler.addSchedulerEventListener(joblistener, SchedulerEvent.KILLED);                  //KILLED("schedulerKilledEvent"),
                    this.scheduler.addSchedulerEventListener(joblistener, SchedulerEvent.SHUTDOWN);                //SHUTDOWN("schedulerShutDownEvent"),
                    this.scheduler.addSchedulerEventListener(joblistener, SchedulerEvent.SHUTTING_DOWN);           //SHUTTING_DOWN("schedulerShuttingDownEvent"),
            }
            */
    public FileServerClient getFileServer() {
        return fserver;
    }

    public TaskPool getTaskPool() {
        return taskpool;
    }

    public void shutdown() {
        dispatcher.shutdown();
        fserver.shutdown();
    }

    public void start() {
        this.dispatcher.start();
    }
}
