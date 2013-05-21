package org.ow2.proactive.scheduler.core.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.task.TaskStatus;
import org.ow2.proactive.scheduler.core.SchedulerStateImpl;
import org.ow2.proactive.scheduler.job.ClientJobState;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;


public class SchedulerStateRecoverHelper {

    private final SchedulerDBManager dbManager;

    public static class RecoveredSchedulerState {

        private final Vector<InternalJob> pendingJobs;

        private final Vector<InternalJob> runningJobs;

        private final Vector<InternalJob> finishedJobs;

        private final SchedulerStateImpl schedulerState;

        public RecoveredSchedulerState(Vector<InternalJob> pendingJobs, Vector<InternalJob> runningJobs,
                Vector<InternalJob> finishedJobs) {
            this.pendingJobs = pendingJobs;
            this.runningJobs = runningJobs;
            this.finishedJobs = finishedJobs;
            schedulerState = new SchedulerStateImpl();
            schedulerState.setPendingJobs(convertToClientJobState(pendingJobs));
            schedulerState.setRunningJobs(convertToClientJobState(runningJobs));
            schedulerState.setFinishedJobs(convertToClientJobState(finishedJobs));
        }

        public Vector<InternalJob> getPendingJobs() {
            return pendingJobs;
        }

        public Vector<InternalJob> getRunningJobs() {
            return runningJobs;
        }

        public Vector<InternalJob> getFinishedJobs() {
            return finishedJobs;
        }

        public SchedulerStateImpl getSchedulerState() {
            return schedulerState;
        }

        private Vector<JobState> convertToClientJobState(List<InternalJob> jobs) {
            Vector<JobState> result = new Vector<JobState>(jobs.size());
            for (InternalJob internalJob : jobs) {
                result.add(new ClientJobState(internalJob));
            }
            return result;
        }

    }

    public SchedulerStateRecoverHelper(SchedulerDBManager dbManager) {
        this.dbManager = dbManager;
    }

    public RecoveredSchedulerState recover(long loadJobPeriod) {
        List<InternalJob> notFinishedJobs = dbManager.loadNotFinishedJobs(true);

        Vector<InternalJob> pendingJobs = new Vector<InternalJob>();
        Vector<InternalJob> runningJobs = new Vector<InternalJob>();

        for (InternalJob job : notFinishedJobs) {
            job.getJobDescriptor();
            switch (job.getStatus()) {
                case PENDING:
                    pendingJobs.add(job);
                    break;
                case STALLED:
                case RUNNING:
                    runningJobs.add(job);
                    runningTasksToPending(job.getITasks());
                    break;
                case PAUSED:
                    if ((job.getNumberOfPendingTasks() + job.getNumberOfRunningTasks() + job
                            .getNumberOfFinishedTasks()) == 0) {
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

        for (InternalJob job : runningJobs) {
            ArrayList<InternalTask> tasksList = copyAndSort(job.getITasks(), true);

            //simulate the running execution to recreate the tree.
            for (InternalTask task : tasksList) {
                job.simulateStartAndTerminate(task.getId());
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
                job.setNumberOfPendingTasks(job.getNumberOfPendingTasks() + job.getNumberOfRunningTasks());
                job.setNumberOfRunningTasks(0);
            }
        }

        for (InternalJob job : pendingJobs) {
            //set the task to pause inside the job if it is paused.
            if (job.getStatus() == JobStatus.PAUSED) {
                job.setStatus(JobStatus.STALLED);
                job.setPaused();
            }
        }

        Vector<InternalJob> finishedJobs = new Vector<InternalJob>(dbManager.loadFinishedJobs(false,
                loadJobPeriod));

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
     * Make a copy of the given argument with the restriction 'onlyFinished'.
     * As no task could be running after recover, this method also move task from RUNNING status to PENDING one.
     * Then sort the array according to finished time order.
     *
     * @param tasks the list of internal tasks to copy.
     * @param onlyFinished true if the copy must contains only the finished task,
     *                                                 false to contains every tasks.
     * @return the sorted copy of the given argument.
     */
    private ArrayList<InternalTask> copyAndSort(ArrayList<InternalTask> tasks, boolean onlyFinished) {
        ArrayList<InternalTask> tasksList = new ArrayList<InternalTask>();

        //copy the list with only the finished task.
        for (InternalTask task : tasks) {
            if (onlyFinished) {
                switch (task.getStatus()) {
                    case ABORTED:
                    case FAILED:
                    case FINISHED:
                    case FAULTY:
                    case SKIPPED:
                        tasksList.add(task);
                }
            } else {
                tasksList.add(task);
            }
            //if task was running, put it in pending status
            if (task.getStatus() == TaskStatus.RUNNING) {
                task.setStatus(TaskStatus.PENDING);
            }
        }
        //sort the finished task according to their finish time.
        //to be sure to be in the right tree browsing.
        Collections.sort(tasksList, new FinishTimeComparator());

        return tasksList;
    }

    private static class FinishTimeComparator implements Comparator<InternalTask> {
        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         * @param o1 First InternalTask to be compared.
         * @param o2 Second InternalTask to be compared with the first.
         * @return a negative integer, zero, or a positive integer as the
         * 	       first argument is less than, equal to, or greater than the
         *	       second. 
         */
        public int compare(InternalTask o1, InternalTask o2) {
            return (int) (o1.getFinishedTime() - o2.getFinishedTime());
        }
    }

}
