package org.ow2.proactive.scheduler.core;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxiesManager;


public interface SchedulingInfrastructure {

    SchedulerClassServers getTaskClassServer();

    RMProxiesManager getRMProxiesManager();

    SchedulerDBManager getDBManager();

    Future<?> submit(Runnable runnable);

    <T> Future<T> submit(Callable<T> task);

    void schedule(Runnable runnable, long delay);

    void schedule(Callable<?> task, long delay);

    DataSpaceServiceStarter getDataSpaceServiceStarter();

    void shutdown();

}
