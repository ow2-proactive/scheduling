package org.ow2.proactive.scheduler.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.job.InternalJob;
import org.ow2.proactive.scheduler.task.internal.InternalTask;
import org.ow2.proactive.scheduler.task.launcher.TaskLauncher;


final class RuntimeJobsState {

    /** list of all running and pending jobs managed by the scheduler */
    private final Map<JobId, InternalJob> jobs;

    /** list of pending jobs among the managed jobs */
    private final Vector<InternalJob> pendingJobs;

    /** list of running jobs among the managed jobs */
    private final Vector<InternalJob> runningJobs;

    /** Currently running tasks for a given jobId*/
    private final ConcurrentHashMap<JobId, Hashtable<TaskId, TaskLauncher>> currentlyRunningTasks;

    RuntimeJobsState(Vector<InternalJob> pendingJobs, Vector<InternalJob> runningJobs) {
        this.pendingJobs = pendingJobs;
        this.runningJobs = runningJobs;
        this.jobs = new HashMap<JobId, InternalJob>(pendingJobs.size() + runningJobs.size());
        this.currentlyRunningTasks = new ConcurrentHashMap<JobId, Hashtable<TaskId, TaskLauncher>>();
        for (InternalJob job : pendingJobs) {
            jobs.put(job.getId(), job);
            currentlyRunningTasks.put(job.getId(), new Hashtable<TaskId, TaskLauncher>());
        }
        for (InternalJob job : runningJobs) {
            jobs.put(job.getId(), job);
            currentlyRunningTasks.put(job.getId(), new Hashtable<TaskId, TaskLauncher>());
        }
    }

    boolean isPendingJob(InternalJob job) {
        return pendingJobs.contains(job);
    }

    boolean isPendingJob(JobId jobId) {
        InternalJob job = jobs.get(jobId);
        return job != null && pendingJobs.contains(job);
    }

    boolean hasJob(JobId jobId) {
        return jobs.containsKey(jobId);
    }

    Collection<InternalJob> runningJobs() {
        return runningJobs;
    }

    Collection<InternalJob> pendingJobs() {
        return pendingJobs;
    }

    Map<JobId, InternalJob> runningAndPendingJobs() {
        return jobs;
    }

    InternalJob getRunningJob(int index) {
        return runningJobs.get(index);
    }

    int runningAndPendingJobsNumber() {
        return runningJobs.size() + pendingJobs.size();
    }

    int runningJobsNumber() {
        return runningJobs.size();
    }

    boolean hasJobOwnedByUser(String user) {
        for (InternalJob job : pendingJobs) {
            if (job.getOwner().equals(user)) {
                return true;
            }
        }
        for (InternalJob job : runningJobs) {
            if (job.getOwner().equals(user)) {
                return true;
            }
        }
        return false;
    }

    TaskLauncher removeRunningTaskLauncher(TaskId taskId) {
        Hashtable<TaskId, TaskLauncher> tasks = currentlyRunningTasks.get(taskId.getJobId());
        if (tasks != null) {
            return tasks.remove(taskId);
        } else {
            return null;
        }
    }

    Map<TaskId, TaskLauncher> getCurrentlyRunningTasks(JobId jobId) {
        return currentlyRunningTasks.get(jobId);
    }

    void jobSubmitted(InternalJob job) {
        jobs.put(job.getId(), job);
        pendingJobs.add(job);
        currentlyRunningTasks.put(job.getId(), new Hashtable<TaskId, TaskLauncher>());
    }

    void jobTerminated(InternalJob job) {
        removeJobData(job);
    }

    void jobRemoved(InternalJob job) {
        removeJobData(job);
    }

    void jobStarted(InternalJob job) {
        pendingJobs.remove(job);
        runningJobs.add(job);
    }

    void taskStarted(InternalTask task, TaskLauncher launcher) {
        currentlyRunningTasks.get(task.getJobId()).put(task.getId(), launcher);
    }

    void clear() {
        jobs.clear();
        pendingJobs.clear();
        runningJobs.clear();
        currentlyRunningTasks.clear();
    }

    private void removeJobData(InternalJob job) {
        jobs.remove(job.getId());
        runningJobs.remove(job);
        pendingJobs.remove(job);
        currentlyRunningTasks.remove(job.getId());
    }

}
