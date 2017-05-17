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
package org.ow2.proactive.scheduler.core.helpers;

import org.apache.log4j.Logger;
import org.hibernate.stat.Statistics;
import org.ow2.proactive.scheduler.common.SchedulerState;


/**
 * JobsMemoryMonitorRunner will ask Hibernate for the statistics metrics.
 *
 * @author ActiveEon Team
 * @since 14/02/17
 */
public class JobsMemoryMonitorRunner implements Runnable {

    private static Statistics stats;

    private static SchedulerState schedulerState;

    private static final Logger logger = Logger.getLogger(JobsMemoryMonitorRunner.class);

    public JobsMemoryMonitorRunner(Statistics statistics, SchedulerState schedulerState) {
        statistics.setStatisticsEnabled(true);
        this.stats = statistics;
        this.schedulerState = schedulerState;
    }

    private void printHibernateStats() {
        long deleteCount = stats.getEntityDeleteCount();
        long updateCount = stats.getEntityUpdateCount();
        long insertCount = stats.getEntityInsertCount();
        long fetchCount = stats.getEntityFetchCount();
        long loadCount = stats.getEntityLoadCount();
        long flushCount = stats.getFlushCount();
        logger.debug("[HibernateStats] deleteCount: " + deleteCount + ", updateCount: " +
                updateCount + ", insertCount: " + insertCount + ", fetchCount: " + fetchCount + ", loadCount:" +
                loadCount + ", flushCount:" + flushCount);
    }

    private void printSchedulerState() {
        int nbPendingJobs = schedulerState.getPendingJobs().size();
        int nbRunningJobs = schedulerState.getRunningJobs().size();
        int nbFinishedJobs = schedulerState.getFinishedJobs().size();
        int allJobsActual = schedulerState.getTotalNbJobs();
        int allJobsComputed = nbPendingJobs + nbRunningJobs + nbFinishedJobs;
        logger.debug("[SchedulerState] pendingJobs:" + nbPendingJobs + ", runningJobs:" + nbRunningJobs +
                    ", finishedJobs:" + nbFinishedJobs + ", allJobsActual:" + allJobsActual + ", allJobsComputed:" +
                    allJobsComputed);
    }

    @Override
    public void run() {
        printHibernateStats();
        printSchedulerState();
    }
}
