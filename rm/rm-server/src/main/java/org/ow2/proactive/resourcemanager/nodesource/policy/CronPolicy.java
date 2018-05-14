/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.resourcemanager.nodesource.policy;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;

import it.sauronsoftware.cron4j.Scheduler;


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
     * Initial time for nodes acquisition, default is that nodes are added/removed every two minutes
     */
    @Configurable(description = "Time of the nodes acquisition (crontab format)", dynamic = true)
    private String nodeAcquision = "0-58/2 * * * *";

    @Configurable(description = "Time of the nodes removal (crontab format)", dynamic = true)
    private String nodeRemoval = "1-59/2 * * * *";

    /**
     * The way of nodes removing
     */
    @Configurable(description = "How nodes are removed", dynamic = true)
    private boolean preemptive = false;

    @Configurable(description = "Start deployment immediately", dynamic = true)
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
        configureCronParametersStartingFromIndex(2, policyParameters);
        return new BooleanWrapper(true);
    }

    @Override
    public void reconfigure(Object... updatedPolicyParameters) throws Exception {
        super.reconfigure(updatedPolicyParameters);
        this.cronScheduler.stop();
        configureCronParametersStartingFromIndex(2, updatedPolicyParameters);
        this.scheduleAcquireAndReleaseCron();
    }

    private void configureCronParametersStartingFromIndex(int index, Object[] policyParameters) {
        try {
            this.cronScheduler = new Scheduler();
            this.nodeAcquision = policyParameters[index++].toString();
            this.nodeRemoval = policyParameters[index++].toString();
            this.preemptive = Boolean.parseBoolean(policyParameters[index++].toString());
            this.forceDeployment = Boolean.parseBoolean(policyParameters[index].toString());
        } catch (Throwable t) {
            throw new IllegalArgumentException(t);
        }
    }

    /**
     * Initializes stub to this active object
     */
    public void initActivity(Body body) {
        this.thisStub = (CronPolicy) PAActiveObject.getStubOnThis();
    }

    /**
     * Activates the policy. Schedules acquire/release tasks with specified period.
     */
    @Override
    public BooleanWrapper activate() {
        scheduleAcquireAndReleaseCron();
        return new BooleanWrapper(true);
    }

    private void scheduleAcquireAndReleaseCron() {
        this.cronScheduler.schedule(this.nodeAcquision, () -> {
            logger.info("Acquire nodes");
            this.thisStub.acquireAllNodes();
        });
        this.cronScheduler.schedule(this.nodeRemoval, () -> {
            logger.info("Remove nodes");
            this.thisStub.removeAllNodes(this.preemptive);
        });
        this.cronScheduler.start();

        if (this.forceDeployment) {
            logger.info("Force acquire nodes");
            this.thisStub.acquireAllNodes();
        }
    }

    /**
     * Shutdown the policy and clears the timer.
     */
    @Override
    public void shutdown(Client initiator) {
        this.cronScheduler.stop();
        super.shutdown(initiator);
    }

    /**
     * Policy description for UI
     * @return policy description
     */
    @Override
    public String getDescription() {
        return "Acquire and release nodes at specified time.";
    }

    /**
     * Policy string representation.
     */
    @Override
    public String toString() {
        return super.toString() + " acquisition at [" + this.nodeAcquision + "]" + ", removal at [" + this.nodeRemoval +
               "], preemptive: " + this.preemptive;

    }

}
