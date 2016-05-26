package org.ow2.proactive.scheduler.core;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.ow2.proactive.scheduler.core.db.SchedulerDBManager;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxiesManager;


public interface SchedulingInfrastructure {

    RMProxiesManager getRMProxiesManager();

    SchedulerDBManager getDBManager();

    ExecutorService getClientOperationsThreadPool();

    ExecutorService getInternalOperationsThreadPool();

    /**
     * Delay the execution of the specified {@code runnable}
     * by the given {@code delay}.
     *
     * @param runnable the action to execute.
     * @param delay    the minimum delay to wait in milliseconds
     *                 before executing the runnable.
     */
    void schedule(Runnable runnable, long delay);

    /**
     * Delay the execution of the specified {@code runnable}
     * by the given {@code delay}.
     *
     * @param callable the action to execute.
     * @param delay    the minimum delay to wait in milliseconds
     *                 before executing the callable.
     */
    void schedule(Callable<?> callable, long delay);

    /**
     * Delay the execution of the specified {@code jobRemoveHandler}
     * by the given {@code delay}. This method uses a thread pool
     * dedicated to housekeeping operations. This last is different
     * from the one used by other schedule methods.
     *
     * @param jobRemoveHandler the job removal action to execute.
     * @param delay            the minimum delay to wait in milliseconds
     *                         before executing the action.
     */
    void scheduleHousekeeping(JobRemoveHandler jobRemoveHandler, long delay);

    DataSpaceServiceStarter getDataSpaceServiceStarter();

    SchedulerSpacesSupport getSpacesSupport();

    void shutdown();

}
