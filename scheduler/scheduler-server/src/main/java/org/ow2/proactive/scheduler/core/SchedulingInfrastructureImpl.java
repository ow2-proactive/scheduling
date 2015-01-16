package org.ow2.proactive.scheduler.core;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxiesManager;


public class SchedulingInfrastructureImpl implements SchedulingInfrastructure {

    private final SchedulerDBManager dbManager;

    private final ScheduledExecutorService scheduledExecutorService;

    private final ExecutorService clientExecutorService;

    private final ExecutorService internalExecutorService;

    private final RMProxiesManager rmProxiesManager;

    private final DataSpaceServiceStarter dsStarter;

    private final SchedulerSpacesSupport spacesSupport;

    public SchedulingInfrastructureImpl(SchedulerDBManager dbManager, RMProxiesManager rmProxiesManager,
            DataSpaceServiceStarter dsStarter, ExecutorService clientExecutorService,
            ExecutorService internalExecutorService, ScheduledExecutorService scheduledExecutorService) {
        this.dbManager = dbManager;
        this.rmProxiesManager = rmProxiesManager;
        this.dsStarter = dsStarter;
        this.clientExecutorService = clientExecutorService;
        this.internalExecutorService = internalExecutorService;
        this.scheduledExecutorService = scheduledExecutorService;
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
        internalExecutorService.shutdownNow();
        scheduledExecutorService.shutdownNow();
        dsStarter.terminateNamingService();
        rmProxiesManager.terminateAllProxies();
        dbManager.close();
    }
}
