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
package org.ow2.proactive.scheduler.core;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.objectweb.proactive.utils.NamedThreadFactory;
import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxiesManager;


public class SchedulingInfrastructureImpl implements SchedulingInfrastructure {

    private final SchedulerDBManager dbManager;

    private final ExecutorService clientExecutorService;

    private final ExecutorService internalExecutorService;

    private final ExecutorService taskPingerService;

    private final ScheduledExecutorService scheduledExecutorService;

    private final RMProxiesManager rmProxiesManager;

    private final DataSpaceServiceStarter dsStarter;

    private final SchedulerSpacesSupport spacesSupport;

    private static class HousekeepingScheduledExecutorLazyHolder {

        private static final ScheduledExecutorService INSTANCE = Executors.newScheduledThreadPool(PASchedulerProperties.SCHEDULER_HOUSEKEEPING_SCHEDULED_POOL_NBTHREAD.getValueAsInt(),
                                                                                                  new NamedThreadFactory("Housekeeping Thread"));

    }

    public SchedulingInfrastructureImpl(SchedulerDBManager dbManager, RMProxiesManager rmProxiesManager,
            DataSpaceServiceStarter dsStarter, ExecutorService clientExecutorService,
            ExecutorService internalExecutorService, ExecutorService taskPingerService,
            ScheduledExecutorService scheduledExecutorService) {
        this.dbManager = dbManager;
        this.rmProxiesManager = rmProxiesManager;
        this.dsStarter = dsStarter;
        this.clientExecutorService = clientExecutorService;
        this.internalExecutorService = internalExecutorService;
        this.scheduledExecutorService = scheduledExecutorService;
        this.taskPingerService = taskPingerService;
        this.spacesSupport = new SchedulerSpacesSupport();
    }

    @Override
    public RMProxiesManager getRMProxiesManager() {
        return rmProxiesManager;
    }

    @Override
    public SchedulerDBManager getDBManager() {
        return dbManager;
    }

    @Override
    public ExecutorService getClientOperationsThreadPool() {
        return clientExecutorService;
    }

    @Override
    public ExecutorService getInternalOperationsThreadPool() {
        return internalExecutorService;
    }

    @Override
    public ExecutorService getTaskPingerThreadPool() {
        return taskPingerService;
    }

    @Override
    public void schedule(final Runnable runnable, long delay) {
        scheduledExecutorService.schedule(new Runnable() {
            public void run() {
                internalExecutorService.submit(runnable);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void schedule(final Callable<?> task, long delay) {
        scheduledExecutorService.schedule(new Runnable() {
            public void run() {
                internalExecutorService.submit(task);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void scheduleHousekeeping(JobRemoveHandler jobRemoveHandler, long delay) {
        HousekeepingScheduledExecutorLazyHolder.INSTANCE.schedule(jobRemoveHandler, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public DataSpaceServiceStarter getDataSpaceServiceStarter() {
        return dsStarter;
    }

    @Override
    public SchedulerSpacesSupport getSpacesSupport() {
        return this.spacesSupport;
    }

    @Override
    public void shutdown() {
        clientExecutorService.shutdownNow();
        taskPingerService.shutdownNow();
        internalExecutorService.shutdownNow();
        scheduledExecutorService.shutdownNow();

        HousekeepingScheduledExecutorLazyHolder.INSTANCE.shutdownNow();

        dsStarter.terminateNamingService();
        rmProxiesManager.terminateAllProxies();
        dbManager.close();
    }

}
