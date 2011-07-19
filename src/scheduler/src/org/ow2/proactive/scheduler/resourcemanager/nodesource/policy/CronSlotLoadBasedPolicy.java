/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.ow2.proactive.scheduler.resourcemanager.nodesource.policy;

import it.sauronsoftware.cron4j.Scheduler;

import java.util.concurrent.atomic.AtomicBoolean;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;


/**
 *
 * The policy that keeps all nodes up and running within specified time slot and acquires node on demand when scheduler is overloaded at another time.
 *
 */
@ActiveObject
public class CronSlotLoadBasedPolicy extends SchedulerLoadingPolicy {

    /**  */
    private static final long serialVersionUID = 31L;

    @Configurable(description = "Time when all nodes are deployed (crontab format)")
    private String deployAllAt = "* * * * *";

    @Configurable(description = "Time when all nodes are removed and the policy starts watching the scheduler loading")
    private String undeployAllAt = "* * * * *";

    /**
     * The way of nodes removing
     */
    @Configurable(description = "the mode how nodes are removed")
    private boolean preemptive = false;

    @Configurable(description = "If true the policy will acquire all nodes immediately")
    private boolean acquireNow = false;

    private AtomicBoolean holdingAllNodes = new AtomicBoolean(false);

    private Scheduler cronScheduler;

    /**
     * Active object stub
     */
    private CronSlotLoadBasedPolicy thisStub;

    /**
     * Configure a policy with given parameters.
     * @param policyParameters parameters defined by user
     */
    @Override
    public BooleanWrapper configure(Object... policyParameters) {
        super.configure(policyParameters);

        cronScheduler = new Scheduler();
        try {
            int index = 9;
            deployAllAt = policyParameters[index++].toString();
            undeployAllAt = policyParameters[index++].toString();
            preemptive = Boolean.parseBoolean(policyParameters[index++].toString());
            acquireNow = Boolean.parseBoolean(policyParameters[index++].toString());
            holdingAllNodes.set(acquireNow);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(e);
        }
        return new BooleanWrapper(true);
    }

    @Override
    public BooleanWrapper activate() {

        thisStub = (CronSlotLoadBasedPolicy) PAActiveObject.getStubOnThis();

        BooleanWrapper activationStatus = super.activate();
        if (!activationStatus.getBooleanValue()) {
            return activationStatus;
        }

        // when acquireNow is set
        if (holdingAllNodes.get()) {
            logger.info("Deploying all nodes");
            acquireAllNodes();
        }

        cronScheduler.schedule(deployAllAt, new Runnable() {
            public void run() {
                if (!holdingAllNodes.get()) {
                    logger.info("Deploying all nodes");
                    holdingAllNodes.set(true);
                    thisStub.acquireAllNodes();
                }
            }
        });
        cronScheduler.schedule(undeployAllAt, new Runnable() {
            public void run() {
                if (holdingAllNodes.get()) {
                    logger.info("Removing all nodes");
                    holdingAllNodes.set(false);
                    thisStub.removeAllNodes(preemptive);
                }
            }
        });
        cronScheduler.start();
        return new BooleanWrapper(true);
    }

    protected void updateNumberOfNodes() {
        if (!holdingAllNodes.get()) {
            super.updateNumberOfNodes();
        }
    }

    @Override
    public void shutdown(Client initiator) {
        cronScheduler.stop();
        super.shutdown(initiator);
    }

    @Override
    public String toString() {
        return super.toString() + ", acquire all at [" + deployAllAt + "]" + ", remove all at [" +
            undeployAllAt + "], preemptive: " + preemptive + ", acquired now: " + acquireNow;
    }

    @Override
    public String getDescription() {
        return "Keeps all nodes up and running within specified time slot and acquires node on demand when scheduler is overloaded at another time.";
    }
}
