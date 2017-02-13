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
package org.ow2.proactive.scheduler.resourcemanager.nodesource.policy;

import java.util.concurrent.atomic.AtomicBoolean;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;

import it.sauronsoftware.cron4j.Scheduler;


/**
 *
 * The policy that triggers new nodes acquisition when scheduler is overloaded within a time slot defined in crontab syntax.
 *
 */
@ActiveObject
public class CronLoadBasedPolicy extends SchedulerLoadingPolicy {
    /**
     * Initial time for nodes acquisition
     */
    @Configurable(description = "Time since the nodes acquisition is allowed (crontab format)")
    private String acquisionAllowed = "* * * * *";

    @Configurable(description = "Time since the nodes acquisition is forbiden (crontab format)")
    private String acquisionForbidden = "* * * * *";

    /**
     * The way of nodes removing
     */
    @Configurable(description = "the mode how nodes are removed")
    private boolean preemptive = false;

    @Configurable(description = "If true acquisition will be immediately allowed")
    private boolean allowed = false;

    private AtomicBoolean isAcquisitionAllowed = new AtomicBoolean(false);

    private Scheduler cronScheduler;

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
            acquisionAllowed = policyParameters[index++].toString();
            acquisionForbidden = policyParameters[index++].toString();
            preemptive = Boolean.parseBoolean(policyParameters[index++].toString());
            allowed = Boolean.parseBoolean(policyParameters[index++].toString());
            isAcquisitionAllowed.set(allowed);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(e);
        }
        return new BooleanWrapper(true);
    }

    @Override
    public BooleanWrapper activate() {
        BooleanWrapper activationStatus = super.activate();
        if (!activationStatus.getBooleanValue()) {
            return activationStatus;
        }

        cronScheduler.schedule(acquisionAllowed, new Runnable() {
            public void run() {
                logger.info("Allowing nodes acquisition");
                isAcquisitionAllowed.set(true);
            }
        });
        cronScheduler.schedule(acquisionForbidden, new Runnable() {
            public void run() {
                logger.info("Forbidding nodes acquisition");
                isAcquisitionAllowed.set(false);
            }
        });
        cronScheduler.start();
        return new BooleanWrapper(true);
    }

    protected void updateNumberOfNodes() {
        if (nodesNumberInNodeSource > 0 && !isAcquisitionAllowed.get()) {
            logger.debug("Policy triggers all nodes removal");
            removeAllNodes(preemptive);
        }
        if (isAcquisitionAllowed.get()) {
            logger.debug("Node acquision allowed");
            super.updateNumberOfNodes();
        } else {
            logger.debug("Node acquision forbidden");
        }
    }

    @Override
    public void shutdown(Client initiator) {
        cronScheduler.stop();
        super.shutdown(initiator);
    }

    @Override
    public String toString() {
        return super.toString() + ", acquisiotion allowed at [" + acquisionAllowed + "]" +
               ", acquisiotion forbidden at [" + acquisionForbidden + "], preemptive: " + preemptive +
               ", allowed initialy: " + allowed;
    }

    @Override
    public String getDescription() {
        return "Triggers new nodes acquisition when scheduler is overloaded within a time slot defined in crontab syntax.";
    }
}
