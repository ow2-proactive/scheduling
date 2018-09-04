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
package org.ow2.proactive.resourcemanager.housekeeping;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.ow2.proactive.resourcemanager.core.RMCore;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;

import it.sauronsoftware.cron4j.Scheduler;


public class NodesHouseKeepingService {

    private static final Logger logger = Logger.getLogger(NodesHouseKeepingService.class);

    private final RMCore rmCoreStub;

    private Scheduler nodesHouseKeepingScheduler;

    private String scheduledCronIdentifier;

    public NodesHouseKeepingService(RMCore rmCoreStub) {
        this.rmCoreStub = rmCoreStub;
    }

    public void start() {
        if (PAResourceManagerProperties.RM_UNAVAILABLE_NODES_MAX_PERIOD.isSet() &&
            PAResourceManagerProperties.RM_UNAVAILABLE_NODES_MAX_PERIOD.getValueAsInt() > 0) {

            this.nodesHouseKeepingScheduler = createOrGetCronScheduler();
            String cronExpression = PAResourceManagerProperties.RM_UNAVAILABLE_NODES_REMOVAL_FREQUENCY.getValueAsString();
            this.scheduledCronIdentifier = this.nodesHouseKeepingScheduler.schedule(cronExpression,
                                                                                    getNodesHouseKeepingHandler());
            this.nodesHouseKeepingScheduler.start();
            logger.info("Nodes housekeeping started with periodic schedule: " +
                        this.nodesHouseKeepingScheduler.getSchedulingPattern(this.scheduledCronIdentifier));
        }

    }

    /*
     * This method is useful to mock the CRON scheduler in unit test.
     */
    Scheduler createOrGetCronScheduler() {
        if (this.nodesHouseKeepingScheduler == null) {
            return new Scheduler();
        } else {
            return this.nodesHouseKeepingScheduler;
        }
    }

    private Runnable getNodesHouseKeepingHandler() {
        return () -> {
            List<String> unavailableNodesUrls = this.rmCoreStub.getToBeRemovedUnavailableNodesUrls();
            if (!unavailableNodesUrls.isEmpty()) {
                logger.info("Remove " + unavailableNodesUrls.size() + " unavailable nodes");
                if (logger.isDebugEnabled() && unavailableNodesUrls.size() < 100) {
                    logger.debug("Nodes URL: " + Arrays.toString(unavailableNodesUrls.toArray()));
                }
                unavailableNodesUrls.forEach(nodeUrl -> this.rmCoreStub.removeNode(nodeUrl, true));
            }
        };
    }

    public void stop() {
        if (this.nodesHouseKeepingScheduler != null) {
            this.nodesHouseKeepingScheduler.deschedule(this.scheduledCronIdentifier);
            this.nodesHouseKeepingScheduler.stop();
        }
    }

}
