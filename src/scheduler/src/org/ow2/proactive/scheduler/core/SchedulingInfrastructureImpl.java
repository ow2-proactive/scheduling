package org.ow2.proactive.scheduler.core;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxiesManager;


public class SchedulingInfrastructureImpl implements SchedulingInfrastructure {

    private final SchedulerDBManager dbManager;

    private final SchedulerClassServers classServers;

    private final ScheduledExecutorService scheduledExecutorService;

    private final ExecutorService executorService;

    private final RMProxiesManager rmProxiesManager;

    private final DataSpaceServiceStarter dsStarter;

    public SchedulingInfrastructureImpl(SchedulerDBManager dbManager, RMProxiesManager rmProxiesManager,
            DataSpaceServiceStarter dsStarter, ExecutorService executorService,
            ScheduledExecutorService scheduledExecutorService) {
        this.dbManager = dbManager;
        this.rmProxiesManager = rmProxiesManager;
        this.dsStarter = dsStarter;
        this.executorService = executorService;
        this.scheduledExecutorService = scheduledExecutorService;
        this.classServers = new SchedulerClassServers(dbManager);
    }

    @Override
    public SchedulerClassServers getTaskClassServer() {
        return classServers;
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
    public Future<?> submit(Runnable runnable) {
        return executorService.submit(runnable);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return executorService.submit(task);
    }

    @Override
    public void schedule(Runnable runnable, long delay) {
        scheduledExecutorService.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void schedule(Callable<?> task, long delay) {
        scheduledExecutorService.schedule(task, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public DataSpaceServiceStarter getDataSpaceServiceStarter() {
        return dsStarter;
    }

    @Override
    public void shutdown() {
        executorService.shutdownNow();
        scheduledExecutorService.shutdownNow();
        dsStarter.terminateNamingService();
        rmProxiesManager.terminateAllProxies();
        dbManager.close();
    }

}
