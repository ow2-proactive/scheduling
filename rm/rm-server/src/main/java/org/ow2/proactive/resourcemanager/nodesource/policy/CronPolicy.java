/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.nodesource.policy;

import it.sauronsoftware.cron4j.Scheduler;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;


/**
 *
 * Allocated nodes for specified time slot: from "acquire time" to "release time" with specified period.
 * Remove nodes preemptively if "preemptive" parameter is set to true. <br>
 * If period is zero then acquisition will be performed once. <br>
 *
 * NOTE: difference between acquire and release time have to be bigger than time required
 * to nodes all acquisition. Period have to be enough to release all nodes.
 *
 */
@ActiveObject
public class CronPolicy extends NodeSourcePolicy implements InitActive {
    protected static Logger logger = Logger.getLogger(CronPolicy.class);

    /**
     * Initial time for nodes acquisition
     */
    @Configurable(description = "Time of the nodes acquisition (crontab format)")
    private String nodeAcquision = "* * * * *";

    @Configurable(description = "Time of the nodes removal (crontab format)")
    private String nodeRemoval = "* * * * *";

    /**
     * The way of nodes removing
     */
    @Configurable(description = "the mode how nodes are removed")
    private boolean preemptive = false;

    @Configurable(description = "Start deployment immediately")
    private boolean forceDeployment = false;

    private Scheduler cronScheduler;

    /**
     * Active object stub
     */
    private CronPolicy thisStub;

    /**
     * Proactive default constructor
     */
    public CronPolicy() {
    }

    /**
     * Configure a policy with given parameters.
     * @param policyParameters parameters defined by user
     */
    @Override
    public BooleanWrapper configure(Object... policyParameters) {
        super.configure(policyParameters);
        try {
            cronScheduler = new Scheduler();
            int index = 2;

            nodeAcquision = policyParameters[index++].toString();
            nodeRemoval = policyParameters[index++].toString();
            preemptive = Boolean.parseBoolean(policyParameters[index++].toString());
            forceDeployment = Boolean.parseBoolean(policyParameters[index++].toString());
        } catch (Throwable t) {
            throw new IllegalArgumentException(t);
        }
        return new BooleanWrapper(true);
    }

    /**
     * Initializes stub to this active object
     */
    public void initActivity(Body body) {
        thisStub = (CronPolicy) PAActiveObject.getStubOnThis();
    }

    /**
     * Activates the policy. Schedules acquire/release tasks with specified period.
     */
    @Override
    public BooleanWrapper activate() {
        cronScheduler.schedule(nodeAcquision, new Runnable() {
            public void run() {
                logger.info("Acquiring nodes");
                thisStub.acquireAllNodes();
            }
        });
        cronScheduler.schedule(nodeRemoval, new Runnable() {
            public void run() {
                logger.info("Removing nodes");
                thisStub.removeAllNodes(preemptive);
            }
        });
        cronScheduler.start();

        if (forceDeployment) {
            logger.info("Acquiring nodes");
            thisStub.acquireAllNodes();
        }
        return new BooleanWrapper(true);
    }

    /**
     * Shutdown the policy and clears the timer.
     */
    @Override
    public void shutdown(Client initiator) {
        cronScheduler.stop();
        super.shutdown(initiator);
    }

    /**
     * Policy description for UI
     * @return policy description
     */
    @Override
    public String getDescription() {
        return "Acquires and releases nodes at specified time.";
    }

    /**
     * Policy string representation.
     */
    @Override
    public String toString() {
        return super.toString() + " acquisiotion at [" + nodeAcquision + "]" + ", removal at [" +
            nodeRemoval + "], preemptive: " + preemptive;

    }
}
