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

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.ow2.proactive.resourcemanager.authentication.Client;
import org.ow2.proactive.resourcemanager.nodesource.common.Configurable;


/**
 *
 * The policy that triggers new nodes acquisition when scheduler is overloaded within a time slot defined in crontab syntax.
 *
 */
@ActiveObject
public class CronLoadBasedPolicy extends SchedulerLoadingPolicy {

    private static final long serialVersionUID = 61L;
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
