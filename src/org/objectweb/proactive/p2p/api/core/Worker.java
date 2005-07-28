/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.p2p.api.core;

import org.apache.log4j.Logger;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.ServeException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.p2p.api.exception.IsAlreadyComputingException;

import java.io.Serializable;


/**
 * @author Alexandre di Costanzo
 *
 * Created on Apr 25, 2005
 */
public class Worker implements Serializable {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.P2P_SKELETONS_WORKER);
    private String name = null;
    private boolean isComputing = false;

    /**
     * The active object empty constructor
     */
    public Worker() {
        // The emplty constructor
    }

    /**
     * Construct a new Worker with its name.
     * @param name the Worker's name.
     */
    public Worker(String name) {
        this.name = name;
        logger.debug("Worker " + this.name + " successfully created");
    }

    /**
     * @param task
     * @return the result or a result with the exception.
     */
    public Result execute(Task task) {
        if (this.isComputing == false) {
            this.isComputing = true;
        } else {
            return new Result(new IsAlreadyComputingException());
        }
        Task activedTask = null;
        Exception exception = null;
        Body body = ProActive.getBodyOnThis();
        Service serviceQueue = new Service(body);
        try {
            activedTask = (Task) ProActive.turnActive(task);
        } catch (ActiveObjectCreationException e) {
            logger.fatal("Couldn't actived the task", e);
            exception = e;
        } catch (NodeException e) {
            logger.fatal("A problem with the task's node", e);
            exception = e;
        }
        if (activedTask != null) {
            Result result = activedTask.execute();
            while (ProActive.isAwaited(result)) {
                Request r = serviceQueue.blockingRemoveOldest(100);
                try {
                    r.serve(body);
                } catch (ServeException e) {
                    logger.warn("Problem with serving a request", e);
                }
            }
            this.isComputing = false;
            return result;
        } else {
            logger.fatal("The task was not actived");
            this.isComputing = false;
            return new Result(exception);
        }
    }

    /**
     * @return
     */
    public StringWrapper getName() {
        return new StringWrapper(this.name);
    }
}
