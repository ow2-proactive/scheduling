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
package org.ow2.proactive.scheduler.core.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.core.rmproxies.RMProxy;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.JobLogger;
import org.ow2.proactive.utils.NodeSet;


public class SchedulerStateRecoverHelper {

    private static final Logger logger = Logger.getLogger(SchedulerStateRecoverHelper.class);

    private static final JobLogger jobLogger = JobLogger.getInstance();

    public static final String FAIL_TO_RECOVER_RUNNING_TASK_STRING = "Fail to recover running task ";

    private final SchedulerDBManager dbManager;

    private final Map<JobId, TaskStatusCounter> jobsToUpdate = new HashMap<>();

    public SchedulerStateRecoverHelper(SchedulerDBManager dbManager) {
        this.dbManager = dbManager;
    }

    public RecoveredSchedulerState recover(long loadJobPeriod) {
        return recover(loadJobPeriod, null);
    }

    public RecoveredSchedulerState recover(long loadJobPeriod, RMProxy rmProxy) {
        List<InternalJob> notFinishedJobs = dbManager.loadNotFinishedJobs(true);

        Vector<InternalJob> pendingJobs = new Vector<>();
        Vector<InternalJob> runningJobs = new Vector<>();

        ExecutorService recoverRunningTasksThreadPool = Executors.newFixedThreadPool(PASchedulerProperties.SCHEDULER_PARALLEL_SCHEDULER_STATE_RECOVER_NBTHREAD.getValueAsInt());

        for (InternalJob job : notFinishedJobs) {
            recoverJob(rmProxy, pendingJobs, runningJobs, job, recoverRunningTasksThreadPool);
        }

        recoverRunningTasksThreadPool.shutdown();

        boolean terminatedWithoutTimeout;

        try {
            terminatedWithoutTimeout = recoverRunningTasksThreadPool.awaitTermination(PASchedulerProperties.SCHEDULER_PARALLEL_SCHEDULER_STATE_RECOVER_TIMEOUT.getValueAsInt(),
                                                                                      TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for the Scheduler state to be recovered", e);
            Thread.currentThread().interrupt();
            throw new SchedulerStateNotRecoveredException(e);
        }

        failIfSchedulerStateRecoveryTimeout(terminatedWithoutTimeout);

        applyJobUpdates(notFinishedJobs);

        Vector<InternalJob> finishedJobs = new Vector<>();

        for (Iterator<InternalJob> iterator = runningJobs.iterator(); iterator.hasNext();) {
            InternalJob job = iterator.next();
            try {
                List<InternalTask> tasksList = copyAndSort(job.getITasks());

                //simulate the running execution to recreate the tree.
                for (InternalTask task : tasksList) {
                    job.recoverTask(task.getId());
                }

                if (job.getStatus() == JobStatus.PAUSED) {
                    job.setStatus(JobStatus.STALLED);
                    job.setPaused();

                    //update the count of pending and running task.
                    job.setNumberOfPendingTasks(job.getNumberOfPendingTasks() + job.getNumberOfRunningTasks());
                    job.setNumberOfRunningTasks(0);
                }
            } catch (Exception e) {
                logger.error("Failed to recover job " + job.getId() + " " + job.getName() +
                             " job might be in a inconsistent state", e);
                jobLogger.error(job.getId(), "Failed to recover job, job might be in an inconsistent state", e);
                // partially cancel job (not tasks) and move it to finished jobs to avoid running it
                iterator.remove();
                job.setStatus(JobStatus.CANCELED);
                finishedJobs.add(job);
                dbManager.updateJobAndTasksState(job);
            }
        }

        finishedJobs.addAll(dbManager.loadFinishedJobs(false, loadJobPeriod));
        logger.info("[Recovering counters] " + " Pending: " + pendingJobs.size() + " Running: " + runningJobs.size() +
                    " Finished: " + finishedJobs.size());

        return new RecoveredSchedulerState(pendingJobs, runningJobs, finishedJobs);
    }

    private void applyJobUpdates(List<InternalJob> notFinishedJobs) {
        for (InternalJob job : notFinishedJobs) {
            if (this.jobsToUpdate.containsKey(job.getId())) {
                updateJobWithCounters(job, this.jobsToUpdate.get(job.getId()));
            }
        }
    }

    private void failIfSchedulerStateRecoveryTimeout(boolean terminatedWithoutTimeout) {
        if (!terminatedWithoutTimeout) {
            String errorMessage = "Timeout while waiting for the Scheduler state to be recovered";
            logger.error(errorMessage);
            throw new SchedulerStateNotRecoveredException(errorMessage);
        }
    }

    private void recoverJob(RMProxy rmProxy, Vector<InternalJob> pendingJobs, Vector<InternalJob> runningJobs,
            InternalJob job, ExecutorService recoverRunningTasksThreadPool) {
        switch (job.getStatus()) {
            case PENDING:
                pendingJobs.add(job);
                break;
            case STALLED:
            case RUNNING:
            case IN_ERROR:
                runningJobs.add(job);
                recoverJobTasks(job, job.getITasks(), rmProxy, recoverRunningTasksThreadPool);
                break;
            case PAUSED:
                if ((job.getNumberOfPendingTasks() + job.getNumberOfRunningTasks() +
                     job.getNumberOfFinishedTasks()) == 0) {
                    pendingJobs.add(job);
                } else {
                    runningJobs.add(job);
                    recoverJobTasks(job, job.getITasks(), rmProxy, recoverRunningTasksThreadPool);
                }
                break;
            default:
                throw new IllegalStateException("Unexpected job status: " + job.getStatus());
        }
    }

    private void recoverJobTasks(InternalJob job, List<InternalTask> tasks, RMProxy rmProxy,
            ExecutorService recoverRunningTasksThreadPool) {
        TaskStatusCounter counter = new TaskStatusCounter();
        for (InternalTask task : tasks) {
            // we only need to take into account the tasks that were running
            // and recount the number of pending tasks, because if we do not
            // manage to recover a running task, it will be recovered as a
            // pending task
            if ((task.getStatus() == TaskStatus.RUNNING || task.getStatus() == TaskStatus.PAUSED) &&
                task.getExecuterInformation() != null) {
                recoverRunningTaskAsynchronously(rmProxy, recoverRunningTasksThreadPool, counter, task);
            } else {
                recoverTaskOtherThanRunning(counter, task);
            }
            logger.debug("Task " + task.getId() + " status is " + task.getStatus().name());
        }
        this.jobsToUpdate.put(job.getId(), counter);
    }

    private void recoverRunningTaskAsynchronously(RMProxy rmProxy, ExecutorService recoverRunningTasksThreadPool,
            TaskStatusCounter counter, InternalTask task) {
        recoverRunningTasksThreadPool.submit(() -> recoverRunningTask(rmProxy, counter, task));
    }

    private void recoverTaskOtherThanRunning(TaskStatusCounter counter, InternalTask task) {
        // recount existing pending tasks. We base this number on the
        // definition provided in SchedulerDBManager#PENDING_TASKS
        if (task.getStatus().equals(TaskStatus.PENDING) || task.getStatus().equals(TaskStatus.SUBMITTED) ||
            task.getStatus().equals(TaskStatus.NOT_STARTED)) {
            counter.incrementPendingTasks();
        } else {
            // recount existing running tasks that are not recoverable
            // These tasks are "running" by the definition provided in
            // SchedulerDBManager#RUNNING_TASKS
            if (task.getStatus() == TaskStatus.IN_ERROR || task.getStatus() == TaskStatus.WAITING_ON_ERROR ||
                task.getStatus() == TaskStatus.WAITING_ON_FAILURE) {
                counter.incrementRunningTasks();
            }
        }
    }

    private void recoverRunningTask(RMProxy rmProxy, TaskStatusCounter counter, InternalTask task) {
        try {
            if (runningTaskMustBeResetToPending(task, rmProxy)) {
                setTaskStatusAndIncrementPendingCounter(counter, task);
            } else {
                counter.incrementRunningTasks();
            }
        } catch (Throwable e) {
            logger.warn(FAIL_TO_RECOVER_RUNNING_TASK_STRING + task.getId() + " (" + task.getName() + ")", e);
            setTaskStatusAndIncrementPendingCounter(counter, task);
        }
    }

    private void updateJobWithCounters(InternalJob job, TaskStatusCounter counter) {
        // reapply definition of stalled job
        if (counter.getRunningTaskNumber() == 0 && job.getStatus().equals(JobStatus.RUNNING)) {
            job.setStatus(JobStatus.STALLED);
        }
        job.setNumberOfPendingTasks(counter.getPendingTaskNumber());
        job.setNumberOfRunningTasks(counter.getRunningTaskNumber());
    }

    private boolean runningTaskMustBeResetToPending(InternalTask task, RMProxy rmProxy) {
        boolean resetToPending;
        if (rmProxy != null) {

            NodeSet nodes = task.getExecuterInformation().getNodes();
            boolean taskNodesKnownByRM = rmProxy.areNodesKnown(nodes);

            if (taskNodesKnownByRM) {
                TaskLauncher launcher = task.getExecuterInformation().getLauncher();
                logger.debug("Checking whether task launcher has called its doTask method: " +
                             launcher.isTaskStarted());
                if (launcher.isTaskStarted()) {
                    logger.info("Recover running task " + task.getId() + " (" + task.getName() +
                                ") successfully with task launcher " + launcher);
                    resetToPending = false;
                } else {
                    logger.info(FAIL_TO_RECOVER_RUNNING_TASK_STRING + task.getId() + " (" + task.getName() +
                                ") because its task launcher has not started to execute the task");
                    resetToPending = true;
                }
            } else {
                logger.info(FAIL_TO_RECOVER_RUNNING_TASK_STRING + task.getId() + " (" + task.getName() +
                            ") because the task's node is not known by the resource manager");
                resetToPending = true;
            }

        } else {
            logger.info(FAIL_TO_RECOVER_RUNNING_TASK_STRING + task.getId() + " (" + task.getName() +
                        ") because the resource manager is not reachable");
            resetToPending = true;
        }

        return resetToPending;
    }

    private void setTaskStatusAndIncrementPendingCounter(TaskStatusCounter counter, InternalTask task) {
        logger.info("Changing task status to " + TaskStatus.PENDING);
        task.setStatus(TaskStatus.PENDING);
        counter.incrementPendingTasks();
    }

    /**
     * Make a copy of the given argument
     * As no task could be running after recover, this method also move task from RUNNING status to PENDING one.
     * Then sort the array according to finished time order.
     *
     * @param tasks the list of internal tasks to copy.
     * @return the sorted copy of the given argument.
     */
    protected List<InternalTask> copyAndSort(List<InternalTask> tasks) {
        ArrayList<InternalTask> tasksList = new ArrayList<>();

        //copy the list with only the finished task.
        for (InternalTask task : tasks) {
            switch (task.getStatus()) {
                case ABORTED:
                case FAILED:
                case FINISHED:
                case FAULTY:
                case SKIPPED:
                    tasksList.add(task);
            }
        }

        //sort parents before children
        return TopologicalTaskSorter.sortInternalTasks(tasksList);
    }

    private class TaskStatusCounter {

        private AtomicInteger pendingTasks = new AtomicInteger(0);

        private AtomicInteger runningTasks = new AtomicInteger(0);

        private void incrementPendingTasks() {
            this.pendingTasks.incrementAndGet();
        }

        private void incrementRunningTasks() {
            this.runningTasks.incrementAndGet();
        }

        private int getPendingTaskNumber() {
            return this.pendingTasks.get();
        }

        private int getRunningTaskNumber() {
            return this.runningTasks.get();
        }

    }

}
