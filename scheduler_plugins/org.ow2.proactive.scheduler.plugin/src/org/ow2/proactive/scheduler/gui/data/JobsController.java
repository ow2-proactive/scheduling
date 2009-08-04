/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.ow2.proactive.scheduler.gui.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.scheduler.common.NotificationData;
import org.ow2.proactive.scheduler.common.SchedulerEvent;
import org.ow2.proactive.scheduler.common.SchedulerEventListener;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.common.SchedulerUsers;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobInfo;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.UserIdentification;
import org.ow2.proactive.scheduler.common.task.TaskId;
import org.ow2.proactive.scheduler.common.task.TaskInfo;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.TaskState;
import org.ow2.proactive.scheduler.common.task.util.ResultPreviewTool.SimpleTextPanel;
import org.ow2.proactive.scheduler.gui.Activator;
import org.ow2.proactive.scheduler.gui.composite.AbstractJobComposite;
import org.ow2.proactive.scheduler.gui.composite.TaskComposite;
import org.ow2.proactive.scheduler.gui.listeners.EventJobsListener;
import org.ow2.proactive.scheduler.gui.listeners.EventSchedulerListener;
import org.ow2.proactive.scheduler.gui.listeners.EventTasksListener;
import org.ow2.proactive.scheduler.gui.listeners.FinishedJobsListener;
import org.ow2.proactive.scheduler.gui.listeners.PendingJobsListener;
import org.ow2.proactive.scheduler.gui.listeners.RunningJobsListener;
import org.ow2.proactive.scheduler.gui.listeners.SchedulerUsersListener;
import org.ow2.proactive.scheduler.gui.views.ResultPreview;
import org.ow2.proactive.scheduler.gui.views.SeparatedJobView;
import org.ow2.proactive.scheduler.gui.views.TaskView;


/**
 * JobsController...
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class JobsController implements SchedulerEventListener {
    // The shared instance view as a direct reference
    private static JobsController localView = null;

    // The shared instance view as an active object
    private static JobsController activeView = null;

    // jobs
    private Map<JobId, JobState> jobs = null;

    // jobs id
    private Vector<JobId> pendingJobsIds = null;
    private Vector<JobId> runningJobsIds = null;
    private Vector<JobId> finishedJobsIds = null;

    //Scheduler users
    private SchedulerUsers users = null;

    // listeners
    private Vector<PendingJobsListener> pendingJobsListeners = null;
    private Vector<RunningJobsListener> runningJobsListeners = null;
    private Vector<FinishedJobsListener> finishedJobsListeners = null;
    private Vector<EventTasksListener> eventTasksListeners = null;
    private Vector<EventJobsListener> eventJobsListeners = null;
    private Vector<EventSchedulerListener> eventSchedulerListeners = null;
    private Vector<SchedulerUsersListener> schedulerUsersListeners = null;

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    public JobsController() {
        pendingJobsListeners = new Vector<PendingJobsListener>();
        runningJobsListeners = new Vector<RunningJobsListener>();
        finishedJobsListeners = new Vector<FinishedJobsListener>();
        eventTasksListeners = new Vector<EventTasksListener>();
        eventJobsListeners = new Vector<EventJobsListener>();
        eventSchedulerListeners = new Vector<EventSchedulerListener>();
        schedulerUsersListeners = new Vector<SchedulerUsersListener>();
    }

    // -------------------------------------------------------------------- //
    // ----------------------------- private ------------------------------ //
    // -------------------------------------------------------------------- //
    /** call "addPendingJob" method on listeners */
    private void addPendingJobEventInternal(JobId jobId) {
        for (PendingJobsListener listener : pendingJobsListeners)
            listener.addPendingJob(jobId);
    }

    /** call "removePendingJob" method on listeners */
    private void removePendingJobEventInternal(JobId jobId) {
        for (PendingJobsListener listener : pendingJobsListeners)
            listener.removePendingJob(jobId);
    }

    /** call "addRunningJob" method on listeners */
    private void addRunningJobEventInternal(JobId jobId) {
        for (RunningJobsListener listener : runningJobsListeners)
            listener.addRunningJob(jobId);
    }

    /** call "removeRunningJob" method on listeners */
    private void removeRunningJobEventInternal(JobId jobId) {
        for (RunningJobsListener listener : runningJobsListeners)
            listener.removeRunningJob(jobId);
    }

    /** call "addFinishedJob" method on listeners */
    private void addFinishedJobEventInternal(JobId jobId) {
        for (FinishedJobsListener listener : finishedJobsListeners)
            listener.addFinishedJob(jobId);
    }

    /** call "removeFinishedJob" method on listeners */
    private void removeFinishedJobEventInternal(JobId jobId) {
        for (FinishedJobsListener listener : finishedJobsListeners)
            listener.removeFinishedJob(jobId);
    }

    /** call "runningTaskEvent" method on listeners */
    private void pendingToRunningTaskEventInternal(TaskInfo info) {
        for (EventTasksListener listener : eventTasksListeners)
            listener.runningTaskEvent(info);
    }

    /** call "finishedTaskEvent" method on listeners */
    private void runningToFinishedTaskEventInternal(TaskInfo info) {
        for (EventTasksListener listener : eventTasksListeners)
            listener.finishedTaskEvent(info);
    }

    /** call "startedEvent" method on listeners */
    private void schedulerStartedEventInternal() {
        for (EventSchedulerListener listener : eventSchedulerListeners)
            listener.startedEvent();
    }

    /** call "stoppedEvent" method on listeners */
    private void schedulerStoppedEventInternal() {
        for (EventSchedulerListener listener : eventSchedulerListeners)
            listener.stoppedEvent();
    }

    /** call "pausedEvent" method on listeners */
    private void schedulerPausedEventInternal() {
        for (EventSchedulerListener listener : eventSchedulerListeners)
            listener.pausedEvent();
    }

    /** call "freezeEvent" method on listeners */
    private void schedulerFreezeEventInternal() {
        for (EventSchedulerListener listener : eventSchedulerListeners)
            listener.freezeEvent();
    }

    /** call "resumedEvent" method on listeners */
    private void schedulerResumedEventInternal() {
        for (EventSchedulerListener listener : eventSchedulerListeners)
            listener.resumedEvent();
    }

    /** call "shuttingDownEvent" method on listeners */
    private void schedulerShuttingDownEventInternal() {
        for (EventSchedulerListener listener : eventSchedulerListeners)
            listener.shuttingDownEvent();
    }

    /** call "shutDownEvent" method on listeners */
    private void schedulerShutDownEventInternal() {
        for (EventSchedulerListener listener : eventSchedulerListeners)
            listener.shutDownEvent();
    }

    /** call "killedEvent" method on listeners */
    private void schedulerKilledEventInternal() {
        for (EventSchedulerListener listener : eventSchedulerListeners)
            listener.killedEvent();
    }

    /** call "pausedEvent" method on listeners */
    private void jobPausedEventInternal(JobInfo info) {
        for (EventJobsListener listener : eventJobsListeners)
            listener.pausedEvent(info);
    }

    /** call "resumedEvent" method on listeners */
    private void jobResumedEventInternal(JobInfo info) {
        for (EventJobsListener listener : eventJobsListeners)
            listener.resumedEvent(info);
    }

    /** call "priorityChangedEvent" method on listeners */
    private void jobPriorityChangedEventInternal(JobInfo info) {
        for (EventJobsListener listener : eventJobsListeners)
            listener.priorityChangedEvent(info);
    }

    /**
     * call "update" method on listeners
     * 
     * synchronized because an object (Users.java) call it by a reference local and not by the
     * active object reference
     */
    private synchronized void usersUpdateInternal() {
        for (SchedulerUsersListener listener : schedulerUsersListeners)
            listener.update(users.getUsers());
    }

    // -------------------------------------------------------------------- //
    // ---------------- implements SchedulerEventListener ----------------- //
    // -------------------------------------------------------------------- //

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#schedulerStateUpdatedEvent(org.ow2.proactive.scheduler.common.SchedulerEvent)
     */
    public void schedulerStateUpdatedEvent(SchedulerEvent eventType) {
        switch (eventType) {
            case FROZEN:
                schedulerFrozenEvent();
                break;
            case PAUSED:
                schedulerPausedEvent();
                break;
            case RESUMED:
                schedulerResumedEvent();
                break;
            case SHUTDOWN:
                schedulerShutDownEvent();
                break;
            case SHUTTING_DOWN:
                schedulerShuttingDownEvent();
                break;
            case STARTED:
                schedulerStartedEvent();
                break;
            case STOPPED:
                schedulerStoppedEvent();
                break;
            case KILLED:
                schedulerKilledEvent();
                break;
            case RM_DOWN:
                schedulerRMDownEvent();
                break;
            case RM_UP:
                schedulerRMUpEvent();
                break;
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobSubmittedEvent(org.ow2.proactive.scheduler.common.job.JobState)
     */
    public void jobSubmittedEvent(JobState job) {
        // add job to the global jobs map
        jobs.put(job.getId(), job);

        // add job to the pending jobs list
        if (!pendingJobsIds.add(job.getId())) {
            throw new IllegalStateException("can't add the job (id = " + job.getJobInfo() +
                ") from the pendingJobsIds list !");
        }

        // call method on listeners
        addPendingJobEventInternal(job.getId());
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#jobStateUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void jobStateUpdatedEvent(NotificationData<JobInfo> notification) {
        switch (notification.getEventType()) {
            case JOB_PENDING_TO_RUNNING:
                jobPendingToRunningEvent(notification.getData());
                break;
            case JOB_RUNNING_TO_FINISHED:
                jobRunningToFinishedEvent(notification.getData());
                break;
            case JOB_REMOVE_FINISHED:
                jobRemoveFinishedEvent(notification.getData());
                break;
            case JOB_PAUSED:
                jobPausedEvent(notification.getData());
                break;
            case JOB_RESUMED:
                jobResumedEvent(notification.getData());
                break;
            case JOB_CHANGE_PRIORITY:
                jobChangePriorityEvent(notification.getData());
                break;
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#taskStateUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void taskStateUpdatedEvent(NotificationData<TaskInfo> notification) {
        switch (notification.getEventType()) {
            case TASK_PENDING_TO_RUNNING:
                taskPendingToRunningEvent(notification.getData());
                break;
            case TASK_RUNNING_TO_FINISHED:
                taskRunningToFinishedEvent(notification.getData());
                break;
            case TASK_WAITING_FOR_RESTART:
                taskWaitingForRestart(notification.getData());
                break;
        }
    }

    /**
     * @see org.ow2.proactive.scheduler.common.SchedulerEventListener#usersUpdatedEvent(org.ow2.proactive.scheduler.common.NotificationData)
     */
    public void usersUpdatedEvent(NotificationData<UserIdentification> notification) {
        usersUpdate(notification.getData());
    }

    private void jobPendingToRunningEvent(JobInfo info) {
        final JobId jobId = info.getJobId();
        final JobState job = getJobById(jobId);
        job.update(info);

        // remember if the job, which changing list, was selected
        boolean rememberIsOnly = TableManager.getInstance().isOnlyJobSelected(jobId);
        boolean rememberIsSelectedButNotOnly = TableManager.getInstance().isJobSelected(jobId);

        // call method on listeners
        removePendingJobEventInternal(jobId);

        // remove job from the pending jobs list
        if (!pendingJobsIds.remove(jobId)) {
            throw new IllegalStateException("can't remove the job (id = " + jobId +
                ") from the pendingJobsIds list !");
        }

        // add job to running jobs list
        if (!runningJobsIds.add(jobId)) {
            throw new IllegalStateException("can't add the job (id = " + jobId +
                ") from the runningJobsIds list !");
        }

        // call method on listeners
        addRunningJobEventInternal(jobId);

        // if the job was selected, move its selection to an other table
        if (rememberIsOnly) {
            TableManager.getInstance().moveJobSelection(jobId, AbstractJobComposite.RUNNING_TABLE_ID);
        } else if (rememberIsSelectedButNotOnly) {

            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    // remove this job if it was selected
                    TableManager.getInstance().removeJobSelection(jobId);

                    // this job is not the only selected job
                    List<JobId> jobsId = TableManager.getInstance().getJobsIdOfSelectedItems();
                    List<JobState> jobs = getJobsByIds(jobsId);

                    // update info
                    org.ow2.proactive.scheduler.gui.views.JobInfo jobInfo = org.ow2.proactive.scheduler.gui.views.JobInfo
                            .getInstance();
                    if (jobInfo != null) {
                        if (jobsId.size() == 1)
                            jobInfo.updateInfos(job);
                        else
                            jobInfo.updateInfos(jobs);
                    }

                    TaskView taskView = TaskView.getInstance();
                    if (taskView != null) {
                        if (jobsId.size() == 1)
                            taskView.fullUpdate(job);
                        else
                            taskView.fullUpdate(jobs);
                    }
                }
            });
        }
    }

    private void jobRunningToFinishedEvent(JobInfo info) {
        final JobId jobId = info.getJobId();
        final JobState job = getJobById(jobId);
        job.update(info);

        // remember if the job, which changing list, was selected
        boolean rememberIsOnly = TableManager.getInstance().isOnlyJobSelected(jobId);
        boolean rememberIsSelectedButNotOnly = TableManager.getInstance().isJobSelected(jobId);

        if (info.getStartTime() == -1) {
            // call method on listeners
            removePendingJobEventInternal(jobId);

            // remove job from the pendinig jobs list
            if (!pendingJobsIds.remove(jobId)) {
                throw new IllegalStateException("can't remove the job (id = " + jobId +
                    ") from the runningJobsIds list !");
            }
        } else {
            // call method on listeners
            removeRunningJobEventInternal(jobId);

            // remove job from the running jobs list
            if (!runningJobsIds.remove(jobId)) {
                throw new IllegalStateException("can't remove the job (id = " + jobId +
                    ") from the runningJobsIds list !");
            }
        }

        // add job to finished jobs list
        if (!finishedJobsIds.add(jobId)) {
            throw new IllegalStateException("can't add the job (id = " + jobId +
                ") from the finishedJobsIds list !");
        }

        // call method on listeners
        addFinishedJobEventInternal(jobId);

        // if the job was selected, move its selection to an other table
        if (rememberIsOnly) {
            TableManager.getInstance().moveJobSelection(jobId, AbstractJobComposite.FINISHED_TABLE_ID);
        } else if (rememberIsSelectedButNotOnly) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    // remove this job if it was selected
                    TableManager.getInstance().removeJobSelection(jobId);

                    // this job is not the only selected job
                    List<JobId> jobsId = TableManager.getInstance().getJobsIdOfSelectedItems();
                    List<JobState> jobs = getJobsByIds(jobsId);

                    // update info
                    org.ow2.proactive.scheduler.gui.views.JobInfo jobInfo = org.ow2.proactive.scheduler.gui.views.JobInfo
                            .getInstance();
                    if (jobInfo != null) {
                        if (jobsId.size() == 1)
                            jobInfo.updateInfos(job);
                        else
                            jobInfo.updateInfos(jobs);
                    }

                    TaskView taskView = TaskView.getInstance();
                    if (taskView != null) {
                        if (jobsId.size() == 1)
                            taskView.fullUpdate(job);
                        else
                            taskView.fullUpdate(jobs);
                    }
                }
            });
        }
    }

    private void jobRemoveFinishedEvent(JobInfo info) {
        final JobId jobId = info.getJobId();
        boolean rememberIsSelected = TableManager.getInstance().isJobSelected(jobId);

        // call method on listeners
        removeFinishedJobEventInternal(jobId);

        // remove job from the jobs map
        if (jobs.remove(jobId) == null) {
            throw new IllegalStateException("can't remove the job (id = " + jobId + ") from the jobs map !");
        }

        // remove job from the finished jobs list
        if (!finishedJobsIds.remove(jobId)) {
            throw new IllegalStateException("can't remove the job (id = " + jobId +
                ") from the finishedJobsIds list !");
        }

        // remove job's output
        JobsOutputController.getInstance().removeJobOutput(jobId);

        if (rememberIsSelected) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    // remove this job if it was selected
                    TableManager.getInstance().removeJobSelection(jobId);

                    // this job is not the only selected job
                    List<JobId> jobsId = TableManager.getInstance().getJobsIdOfSelectedItems();
                    List<JobState> jobs = getJobsByIds(jobsId);

                    // update info
                    org.ow2.proactive.scheduler.gui.views.JobInfo jobInfo = org.ow2.proactive.scheduler.gui.views.JobInfo
                            .getInstance();
                    if (jobInfo != null) {
                        if (jobsId.isEmpty())
                            jobInfo.clear();
                        else if (jobsId.size() == 1)
                            jobInfo.updateInfos(jobs.get(0));
                        else
                            jobInfo.updateInfos(jobs);
                    }

                    TaskView taskView = TaskView.getInstance();
                    if (taskView != null) {
                        if (jobsId.isEmpty())
                            taskView.clear();
                        else if (jobsId.size() == 1)
                            taskView.fullUpdate(jobs.get(0));
                        else
                            taskView.fullUpdate(jobs);
                    }
                }
            });
        }
    }

    private void taskPendingToRunningEvent(TaskInfo info) {
        JobId jobId = info.getJobId();
        getJobById(jobId).update(info);

        // call method on listeners
        pendingToRunningTaskEventInternal(info);

        final TaskInfo taskInfo = info;

        // if this job is selected in the Running table
        if (TableManager.getInstance().isJobSelected(jobId)) {
            final JobState job = getJobById(jobId);
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    List<JobId> jobsId = TableManager.getInstance().getJobsIdOfSelectedItems();

                    // update info
                    org.ow2.proactive.scheduler.gui.views.JobInfo jobInfo = org.ow2.proactive.scheduler.gui.views.JobInfo
                            .getInstance();
                    if (jobInfo != null) {
                        if (jobsId.size() == 1)
                            jobInfo.updateInfos(job);
                        else
                            jobInfo.updateInfos(getJobsByIds(jobsId));
                    }

                    TaskView taskView = TaskView.getInstance();
                    if (taskView != null) {
                        taskView.lineUpdate(taskInfo, getTaskStateById(job, taskInfo.getTaskId()));
                    }
                }
            });
        }
    }

    private void taskRunningToFinishedEvent(TaskInfo info) {
        JobId jobId = info.getJobId();
        getJobById(jobId).update(info);
        final TaskInfo taskInfo = info;

        // call method on listeners
        runningToFinishedTaskEventInternal(info);

        // if this job is selected in the Running table
        if (TableManager.getInstance().isJobSelected(jobId)) {
            final JobState job = getJobById(jobId);
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    List<JobId> jobsId = TableManager.getInstance().getJobsIdOfSelectedItems();

                    // update info
                    org.ow2.proactive.scheduler.gui.views.JobInfo jobInfo = org.ow2.proactive.scheduler.gui.views.JobInfo
                            .getInstance();
                    if (jobInfo != null) {
                        if (jobsId.size() == 1)
                            jobInfo.updateInfos(job);
                        else
                            jobInfo.updateInfos(getJobsByIds(jobsId));
                    }

                    TaskView taskView = TaskView.getInstance();
                    if (taskView != null) {
                        TaskId taskId = taskInfo.getTaskId();
                        taskView.lineUpdate(taskInfo, getTaskStateById(job, taskId));

                        if (taskId.equals(taskView.getIdOfSelectedTask())) {
                            TaskResult tr = TaskComposite.getTaskResult(job.getId(), taskId);
                            if (tr != null) {
                                ResultPreview resultPreview = ResultPreview.getInstance();
                                if (resultPreview != null) {
                                    resultPreview.update(new SimpleTextPanel(tr.getTextualDescription()));
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    private void schedulerFrozenEvent() {
        ActionsManager.getInstance().setSchedulerStatus(SchedulerStatus.FROZEN);
        ActionsManager.getInstance().update();

        // call method on listeners
        schedulerFreezeEventInternal();
    }

    private void schedulerPausedEvent() {
        ActionsManager.getInstance().setSchedulerStatus(SchedulerStatus.PAUSED);
        ActionsManager.getInstance().update();

        // call method on listeners
        schedulerPausedEventInternal();
    }

    private void schedulerResumedEvent() {
        ActionsManager.getInstance().setSchedulerStatus(SchedulerStatus.STARTED);
        ActionsManager.getInstance().update();

        // call method on listeners
        schedulerResumedEventInternal();
    }

    private void schedulerShutDownEvent() {
        ActionsManager.getInstance().setSchedulerStatus(SchedulerStatus.KILLED);
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                SeparatedJobView.clearOnDisconnection(false);
            }
        });

        // call method on listeners
        schedulerShutDownEventInternal();
    }

    private void schedulerShuttingDownEvent() {
        ActionsManager.getInstance().setSchedulerStatus(SchedulerStatus.SHUTTING_DOWN);
        ActionsManager.getInstance().update();

        // call method on listeners
        schedulerShuttingDownEventInternal();
    }

    private void schedulerStartedEvent() {
        ActionsManager.getInstance().setSchedulerStatus(SchedulerStatus.STARTED);
        ActionsManager.getInstance().update();

        // call method on listeners
        schedulerStartedEventInternal();
    }

    private void schedulerStoppedEvent() {
        ActionsManager.getInstance().setSchedulerStatus(SchedulerStatus.STOPPED);
        ActionsManager.getInstance().update();

        // call method on listeners
        schedulerStoppedEventInternal();
    }

    private void schedulerKilledEvent() {
        ActionsManager.getInstance().setSchedulerStatus(SchedulerStatus.KILLED);
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                SeparatedJobView.clearOnDisconnection(false);
            }
        });

        // call method on listeners
        schedulerKilledEventInternal();
    }

    private void schedulerRMDownEvent() {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                MessageDialog.openInformation(SeparatedJobView.getSchedulerShell(), "Resource Manager Down",
                        "Resource Manager has failed, the Scheduler has been frozen.\n"
                            + "The Scheduler is now waiting for a new RM, please contact your administrator");
            }
        });
    }

    private void schedulerRMUpEvent() {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                MessageDialog.openInformation(SeparatedJobView.getSchedulerShell(), "Resource Manager Up",
                        "A new Resource Manager has been plugged to the Scheduler.\n"
                            + "Scheduling process has been restarted");
            }
        });
    }

    private void jobPausedEvent(JobInfo info) {
        final JobState job = getJobById(info.getJobId());
        job.update(info);

        // if this job is selected in a table
        if (TableManager.getInstance().isJobSelected(info.getJobId())) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    List<JobId> jobsId = TableManager.getInstance().getJobsIdOfSelectedItems();
                    List<JobState> jobs = getJobsByIds(jobsId);

                    // update info
                    org.ow2.proactive.scheduler.gui.views.JobInfo jobInfo = org.ow2.proactive.scheduler.gui.views.JobInfo
                            .getInstance();
                    if (jobInfo != null) {
                        if (jobsId.size() == 1)
                            jobInfo.updateInfos(job);
                        else
                            jobInfo.updateInfos(jobs);
                    }

                    TaskView taskView = TaskView.getInstance();
                    if (taskView != null) {
                        if (jobsId.size() == 1)
                            taskView.fullUpdate(job);
                        else
                            taskView.fullUpdate(jobs);
                    }
                }
            });
        }
        // call method on listeners
        jobPausedEventInternal(info);
    }

    private void jobResumedEvent(JobInfo info) {
        final JobState job = getJobById(info.getJobId());
        job.update(info);

        // if this job is selected in a table
        if (TableManager.getInstance().isJobSelected(info.getJobId())) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    List<JobId> jobsId = TableManager.getInstance().getJobsIdOfSelectedItems();
                    List<JobState> jobs = getJobsByIds(jobsId);

                    // update info
                    org.ow2.proactive.scheduler.gui.views.JobInfo jobInfo = org.ow2.proactive.scheduler.gui.views.JobInfo
                            .getInstance();
                    if (jobInfo != null) {
                        if (jobsId.size() == 1)
                            jobInfo.updateInfos(job);
                        else
                            jobInfo.updateInfos(jobs);
                    }

                    TaskView taskView = TaskView.getInstance();
                    if (taskView != null) {
                        if (jobsId.size() == 1)
                            taskView.fullUpdate(job);
                        else
                            taskView.fullUpdate(jobs);
                    }
                }
            });
        }

        // call method on listeners
        jobResumedEventInternal(info);
    }

    private void jobChangePriorityEvent(JobInfo info) {
        getJobById(info.getJobId()).update(info);
        jobPriorityChangedEventInternal(info);
    }

    private void taskWaitingForRestart(TaskInfo info) {
        JobId jobId = info.getJobId();
        getJobById(jobId).update(info);

        // call method on listeners
        pendingToRunningTaskEventInternal(info);

        final TaskInfo taskInfo = info;

        // if this job is selected in the Running table
        if (TableManager.getInstance().isJobSelected(jobId)) {
            final JobState job = getJobById(jobId);
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    List<JobId> jobsId = TableManager.getInstance().getJobsIdOfSelectedItems();

                    // update info
                    org.ow2.proactive.scheduler.gui.views.JobInfo jobInfo = org.ow2.proactive.scheduler.gui.views.JobInfo
                            .getInstance();
                    if (jobInfo != null) {
                        if (jobsId.size() == 1)
                            jobInfo.updateInfos(job);
                        else
                            jobInfo.updateInfos(getJobsByIds(jobsId));
                    }

                    TaskView taskView = TaskView.getInstance();
                    if (taskView != null) {
                        taskView.lineUpdate(taskInfo, getTaskStateById(job, taskInfo.getTaskId()));
                    }
                }
            });
        }
    }

    private void usersUpdate(UserIdentification userIdentification) {
        users.update(userIdentification);
        usersUpdateInternal();
    }

    // -------------------------------------------------------------------- //
    // ---------------------- add & remove Listeners ---------------------- //
    // -------------------------------------------------------------------- //
    public void addPendingJobsListener(PendingJobsListener listener) {
        pendingJobsListeners.add(listener);
    }

    public void removePendingJobsListener(PendingJobsListener listener) {
        pendingJobsListeners.remove(listener);
    }

    public void addRunningJobsListener(RunningJobsListener listener) {
        runningJobsListeners.add(listener);
    }

    public void removeRunningJobsListener(RunningJobsListener listener) {
        runningJobsListeners.remove(listener);
    }

    public void addFinishedJobsListener(FinishedJobsListener listener) {
        finishedJobsListeners.add(listener);
    }

    public void removeFinishedJobsListener(FinishedJobsListener listener) {
        finishedJobsListeners.remove(listener);
    }

    public void addEventTasksListener(EventTasksListener listener) {
        eventTasksListeners.add(listener);
    }

    public void removeEventTasksListener(EventTasksListener listener) {
        eventTasksListeners.remove(listener);
    }

    public void addEventJobsListener(EventJobsListener listener) {
        eventJobsListeners.add(listener);
    }

    public void removeEventJobsListener(EventJobsListener listener) {
        eventJobsListeners.remove(listener);
    }

    public void addEventSchedulerListener(EventSchedulerListener listener) {
        eventSchedulerListeners.add(listener);
    }

    public void removeEventSchedulerListener(EventSchedulerListener listener) {
        eventSchedulerListeners.remove(listener);
    }

    public synchronized void addSchedulerUsersListener(SchedulerUsersListener listener) {
        schedulerUsersListeners.add(listener);
    }

    public synchronized void removeSchedulerUsersListener(SchedulerUsersListener listener) {
        schedulerUsersListeners.remove(listener);
    }

    // -------------------------------------------------------------------- //
    // ----------------------------- get jobs ----------------------------- //
    // -------------------------------------------------------------------- //
    public Vector<JobId> getPendingsJobs() {
        return pendingJobsIds;
    }

    public Vector<JobId> getRunningsJobs() {
        return runningJobsIds;
    }

    public Vector<JobId> getFinishedJobs() {
        return finishedJobsIds;
    }

    // -------------------------------------------------------------------- //
    // ---------------------------- sort jobs ----------------------------- //
    // -------------------------------------------------------------------- //
    public void sortPendingsJobs() {
        Vector<JobState> jobs = new Vector<JobState>();
        for (JobId id : pendingJobsIds)
            jobs.add(getJobById(id));
        Collections.sort(jobs);

        Vector<JobId> tmp = new Vector<JobId>();
        for (JobState job : jobs)
            tmp.add(job.getId());

        pendingJobsIds = tmp;
    }

    public void sortRunningsJobs() {
        Vector<JobState> jobs = new Vector<JobState>();
        for (JobId id : runningJobsIds)
            jobs.add(getJobById(id));
        Collections.sort(jobs);

        Vector<JobId> tmp = new Vector<JobId>();
        for (JobState job : jobs)
            tmp.add(job.getId());

        runningJobsIds = tmp;
    }

    public void sortFinishedJobs() {
        Vector<JobState> jobs = new Vector<JobState>();
        for (JobId id : finishedJobsIds)
            jobs.add(getJobById(id));
        Collections.sort(jobs);

        Vector<JobId> tmp = new Vector<JobId>();
        for (JobState job : jobs)
            tmp.add(job.getId());

        finishedJobsIds = tmp;
    }

    // -------------------------------------------------------------------- //
    // ------------------------------ others ------------------------------ //
    // -------------------------------------------------------------------- //
    public JobState getJobById(JobId id) {
        JobState res = jobs.get(id);
        if (res == null) {
            throw new IllegalArgumentException("there are no jobs with the id : " + id);
        }
        return res;
    }

    public List<JobState> getJobsByIds(List<JobId> ids) {
        List<JobState> res = new ArrayList<JobState>();
        for (JobId id : ids) {
            res.add(jobs.get(id));
        }
        return res;
    }

    public TaskState getTaskStateById(JobState job, TaskId id) {
        TaskState taskState = job.getHMTasks().get(id);
        if (taskState == null) {
            throw new IllegalArgumentException("there are no task with the id : " + id + " in the job : " +
                job.getId());
        }
        return taskState;
    }

    public SchedulerUsers getUsers() {
        return users;
    }

    /**
     * Initiate the controller. Warning, this method must be synchronous.
     * 
     * @return true only if no error caught, for synchronous call.
     */
    public boolean init() {
        SchedulerState state = null;
        state = SchedulerProxy.getInstance().addSchedulerEventListener(
                ((SchedulerEventListener) PAActiveObject.getStubOnThis()), false);

        if (state == null) { // addSchedulerEventListener failed
            return false;
        }

        SchedulerStatus SchedulerStatus = state.getStatus();
        ActionsManager.getInstance().setSchedulerStatus(SchedulerStatus);
        switch (SchedulerStatus) {
            case KILLED:
                schedulerKilledEvent();
                break;
            case PAUSED:
                schedulerPausedEvent();
                break;
            case FROZEN:
                schedulerFrozenEvent();
                break;
            case SHUTTING_DOWN:
                schedulerShuttingDownEvent();
                break;
            case STARTED:
                schedulerStartedEvent();
                break;
            case STOPPED:
                schedulerStoppedEvent();
                break;
        }

        jobs = new HashMap<JobId, JobState>();
        pendingJobsIds = new Vector<JobId>();
        runningJobsIds = new Vector<JobId>();
        finishedJobsIds = new Vector<JobId>();

        Vector<JobState> tmp = state.getPendingJobs();
        for (JobState job : tmp) {
            jobs.put(job.getId(), job);
            pendingJobsIds.add(job.getId());
        }

        tmp = state.getRunningJobs();
        for (JobState job : tmp) {
            jobs.put(job.getId(), job);
            runningJobsIds.add(job.getId());
        }

        tmp = state.getFinishedJobs();
        for (JobState job : tmp) {
            jobs.put(job.getId(), job);
            finishedJobsIds.add(job.getId());
        }

        users = state.getUsers();
        usersUpdateInternal();

        // for synchronous call
        return true;
    }

    public static JobsController getLocalView() {
        if (localView == null) {
            localView = new JobsController();
        }
        return localView;
    }

    public static JobsController getActiveView() {
        if (activeView == null) {
            turnActive();
        }
        return activeView;
    }

    public static JobsController turnActive() {
        try {
            activeView = (JobsController) PAActiveObject.turnActive(getLocalView());
            return activeView;
        } catch (NodeException e) {
            e.printStackTrace();
            Activator.log(IStatus.ERROR, "Error in jobs controller ", e);
        } catch (ActiveObjectCreationException e) {
            Activator.log(IStatus.ERROR, "Error in jobs controller ", e);
            e.printStackTrace();
        }
        return null;
    }

    public static void clearInstances() {
        localView = null;
        activeView = null;
    }

}
