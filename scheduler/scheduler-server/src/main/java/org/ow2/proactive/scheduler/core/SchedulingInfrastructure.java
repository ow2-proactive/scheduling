package org.ow2.proactive.scheduler.core;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxiesManager;


public interface SchedulingInfrastructure {

    SchedulerClassServers getTaskClassServer();

    RMProxiesManager getRMProxiesManager();

    SchedulerDBManager getDBManager();

    ExecutorService getClientOperationsThreadPool();

    ExecutorService getInternalOperationsThreadPool();

    void schedule(Runnable runnable, long delay);

    void schedule(Callable<?> task, long delay);

    DataSpaceServiceStarter getDataSpaceServiceStarter();

    void shutdown();

    SchedulerSpacesSupport getSpacesSupport();
}
