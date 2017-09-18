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

import java.util.*;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.TaskLauncher;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.JobLogger;


public class SchedulerStateRecoverHelper {

    private static final Logger logger = Logger.getLogger(SchedulerStateRecoverHelper.class);

    private static final JobLogger jobLogger = JobLogger.getInstance();

    private final SchedulerDBManager dbManager;

    public SchedulerStateRecoverHelper(SchedulerDBManager dbManager) {
        this.dbManager = dbManager;
    }

    public RecoveredSchedulerState recover(long loadJobPeriod) {
        List<InternalJob> notFinishedJobs = dbManager.loadNotFinishedJobs(true);

        Vector<InternalJob> pendingJobs = new Vector<>();
        Vector<InternalJob> runningJobs = new Vector<>();

        for (InternalJob job : notFinishedJobs) {
            switch (job.getStatus()) {
                case PENDING:
                    pendingJobs.add(job);
                    break;
                case STALLED:
                case RUNNING:
                case IN_ERROR:
                    runningJobs.add(job);
                    recoverRunningTasksOrResetToPending(job, job.getITasks());
                    break;
                case PAUSED:
                    if ((job.getNumberOfPendingTasks() + job.getNumberOfRunningTasks() +
                         job.getNumberOfFinishedTasks()) == 0) {
                        pendingJobs.add(job);
                    } else {
                        runningJobs.add(job);
                        recoverRunningTasksOrResetToPending(job, job.getITasks());
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected job status: " + job.getStatus());
            }
        }

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

    private void recoverRunningTasksOrResetToPending(InternalJob job, List<InternalTask> tasks) {
        int pendingTasksCount = 0;
        int runningTasksCount = 0;
        for (InternalTask task : tasks) {
            // we only need to take into account the tasks that were running 
            // and recount the number of pending tasks, because if we do not 
            // manage to recover a running task, it will be recovered as a 
            // pending task
            if (task.getStatus() == TaskStatus.RUNNING && task.getExecuterInformation() != null) {
                try {
                    TaskLauncher launcher = task.getExecuterInformation().getLauncher();
                    logger.info("Recover running task " + task.getId() + " (" + task.getName() +
                                ") successfully. Launcher: " + launcher);
                    runningTasksCount++;
                } catch (Throwable e) {
                    logger.info("Recover running task " + task.getId() + " (" + task.getName() +
                                ") failed. Moving back task from status RUNNING TO PENDING.", e);
                    task.setStatus(TaskStatus.PENDING);
                    pendingTasksCount++;
                }
            } else {
                // recount existing pending tasks
                if (task.getStatus().equals(TaskStatus.PENDING)) {
                    task.setStatus(TaskStatus.PENDING);
                    pendingTasksCount++;
                }
            }
            logger.debug("Task " + task.getId() + " status is " + task.getStatus().name());
        }
        // reapply definition of stalled job
        if (runningTasksCount == 0 && job.getStatus() == JobStatus.RUNNING) {
            job.setStatus(JobStatus.STALLED);
        }
        job.setNumberOfPendingTasks(pendingTasksCount);
        job.setNumberOfRunningTasks(runningTasksCount);
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

}
