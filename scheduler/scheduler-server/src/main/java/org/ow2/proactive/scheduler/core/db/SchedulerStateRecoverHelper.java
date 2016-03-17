package org.ow2.proactive.scheduler.core.db;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.util.JobLogger;
import org.apache.log4j.Logger;


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
            job.getJobDescriptor();
            switch (job.getStatus()) {
                case PENDING:
                    pendingJobs.add(job);
                    break;
                case STALLED:
                case RUNNING:
                case PAUSED_ON_ERROR:
                    runningJobs.add(job);
                    runningTasksToPending(job.getITasks());
                    break;
                case PAUSED:
                    if ((job.getNumberOfPendingTasks() +
                            job.getNumberOfRunningTasks() +
                            job.getNumberOfFinishedTasks()) == 0) {
                        pendingJobs.add(job);
                    } else {
                        runningJobs.add(job);
                        runningTasksToPending(job.getITasks());
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected job status: " + job.getStatus());
            }
        }

        Vector<InternalJob> finishedJobs = new Vector<>();

        for (Iterator<InternalJob> iterator = runningJobs.iterator(); iterator.hasNext(); ) {
            InternalJob job = iterator.next();
            try {
                List<InternalTask> tasksList = copyAndSort(job.getITasks());

                //simulate the running execution to recreate the tree.
                for (InternalTask task : tasksList) {
                    job.recoverTask(task.getId());
                }

                if ((job.getStatus() == JobStatus.RUNNING) || (job.getStatus() == JobStatus.PAUSED)) {
                    //set the status to stalled because the scheduler start in stopped mode.
                    if (job.getStatus() == JobStatus.RUNNING) {
                        job.setStatus(JobStatus.STALLED);
                    }

                    //set the task to pause inside the job if it is paused.
                    if (job.getStatus() == JobStatus.PAUSED) {
                        job.setStatus(JobStatus.STALLED);
                        job.setPaused();
                    }

                    //update the count of pending and running task.
                    job.setNumberOfPendingTasks(
                            job.getNumberOfPendingTasks() + job.getNumberOfRunningTasks());
                    job.setNumberOfRunningTasks(0);
                }
            } catch (Exception e) {
                logger.error("Failed to recover job " + job.getId() + " " + job.getName() +
                        " job might be in a inconsistent state", e);
                jobLogger
                        .error(job.getId(), "Failed to recover job, job might be in an inconsistent state", e);
                // partially cancel job (not tasks) and move it to finished jobs to avoid running it
                iterator.remove();
                job.setStatus(JobStatus.CANCELED);
                finishedJobs.add(job);
                dbManager.updateJobAndTasksState(job);
            }
        }

        for (InternalJob job : pendingJobs) {
            //set the task to pause inside the job if it is paused.
            if (job.getStatus() == JobStatus.PAUSED) {
                job.setStatus(JobStatus.STALLED);
                job.setPaused();
            }
        }

        finishedJobs.addAll(dbManager.loadFinishedJobs(false, loadJobPeriod));

        return new RecoveredSchedulerState(pendingJobs, runningJobs, finishedJobs);
    }

    private void runningTasksToPending(List<InternalTask> tasks) {
        for (InternalTask task : tasks) {
            if (task.getStatus() == TaskStatus.RUNNING) {
                task.setStatus(TaskStatus.PENDING);
            }
        }
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
            //if task was running, put it in pending status
            if (task.getStatus() == TaskStatus.RUNNING) {
                task.setStatus(TaskStatus.PENDING);
            }
        }

        //sort parents before children
        return TopologicalTaskSorter.sortInternalTasks(tasksList);
    }

}
