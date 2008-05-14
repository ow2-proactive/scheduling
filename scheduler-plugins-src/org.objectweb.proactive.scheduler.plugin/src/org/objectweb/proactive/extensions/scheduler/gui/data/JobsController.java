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
package org.objectweb.proactive.extensions.scheduler.gui.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.scheduler.common.job.JobEvent;
import org.objectweb.proactive.extensions.scheduler.common.job.JobId;
import org.objectweb.proactive.extensions.scheduler.common.job.JobPriority;
import org.objectweb.proactive.extensions.scheduler.common.job.JobState;
import org.objectweb.proactive.extensions.scheduler.common.job.UserIdentification;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerEventListener;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerInitialState;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerState;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerUsers;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskEvent;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskId;
import org.objectweb.proactive.extensions.scheduler.common.task.TaskResult;
import org.objectweb.proactive.extensions.scheduler.gui.actions.ChangePriorityJobAction;
import org.objectweb.proactive.extensions.scheduler.gui.actions.FreezeSchedulerAction;
import org.objectweb.proactive.extensions.scheduler.gui.actions.PriorityJobAction;
import org.objectweb.proactive.extensions.scheduler.gui.actions.KillRemoveJobAction;
import org.objectweb.proactive.extensions.scheduler.gui.actions.KillSchedulerAction;
import org.objectweb.proactive.extensions.scheduler.gui.actions.ObtainJobOutputAction;
import org.objectweb.proactive.extensions.scheduler.gui.actions.PauseResumeJobAction;
import org.objectweb.proactive.extensions.scheduler.gui.actions.PauseSchedulerAction;
import org.objectweb.proactive.extensions.scheduler.gui.actions.ResumeSchedulerAction;
import org.objectweb.proactive.extensions.scheduler.gui.actions.ShutdownSchedulerAction;
import org.objectweb.proactive.extensions.scheduler.gui.actions.StartStopSchedulerAction;
import org.objectweb.proactive.extensions.scheduler.gui.actions.SubmitJobAction;
import org.objectweb.proactive.extensions.scheduler.gui.composite.AbstractJobComposite;
import org.objectweb.proactive.extensions.scheduler.gui.composite.TaskComposite;
import org.objectweb.proactive.extensions.scheduler.gui.listeners.EventJobsListener;
import org.objectweb.proactive.extensions.scheduler.gui.listeners.EventSchedulerListener;
import org.objectweb.proactive.extensions.scheduler.gui.listeners.EventTasksListener;
import org.objectweb.proactive.extensions.scheduler.gui.listeners.FinishedJobsListener;
import org.objectweb.proactive.extensions.scheduler.gui.listeners.PendingJobsListener;
import org.objectweb.proactive.extensions.scheduler.gui.listeners.RunningJobsListener;
import org.objectweb.proactive.extensions.scheduler.gui.listeners.SchedulerUsersListener;
import org.objectweb.proactive.extensions.scheduler.gui.views.JobInfo;
import org.objectweb.proactive.extensions.scheduler.gui.views.ResultPreview;
import org.objectweb.proactive.extensions.scheduler.gui.views.SeparatedJobView;
import org.objectweb.proactive.extensions.scheduler.gui.views.TaskView;
import org.objectweb.proactive.extensions.scheduler.job.InternalJob;
import org.objectweb.proactive.extensions.scheduler.task.internal.InternalTask;


/**
 * @author The ProActive Team
 * @version 1.0, Jul 12, 2007
 * @since ProActive 3.2
 */
public class JobsController implements SchedulerEventListener<InternalJob> {
    // The shared instance view as a direct reference
    private static JobsController localView = null;

    // The shared instance view as an active object
    private static JobsController activeView = null;
    private static SchedulerState schedulerState = null;

    // jobs
    private Map<JobId, InternalJob> jobs = null;

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
    private void pendingToRunningTaskEventInternal(TaskEvent event) {
        for (EventTasksListener listener : eventTasksListeners)
            listener.runningTaskEvent(event);
    }

    /** call "finishedTaskEvent" method on listeners */
    private void runningToFinishedTaskEventInternal(TaskEvent event) {
        for (EventTasksListener listener : eventTasksListeners)
            listener.finishedTaskEvent(event);
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

    /** call "killedEvent" method on listeners */
    private void jobKilledEventInternal(JobId jobId) {
        for (EventJobsListener listener : eventJobsListeners)
            listener.killedEvent(jobId);
    }

    /** call "pausedEvent" method on listeners */
    private void jobPausedEventInternal(JobEvent event) {
        for (EventJobsListener listener : eventJobsListeners)
            listener.pausedEvent(event);
    }

    /** call "resumedEvent" method on listeners */
    private void jobResumedEventInternal(JobEvent event) {
        for (EventJobsListener listener : eventJobsListeners)
            listener.resumedEvent(event);
    }

    /** call "priorityChangedEvent" method on listeners */
    private void jobPriorityChangedEventInternal(JobEvent event) {
        for (EventJobsListener listener : eventJobsListeners)
            listener.priorityChangedEvent(event);
    }

    /**
     * call "update" method on listeners
     * 
     * synchronized because an object (Users.java) call it by a reference local and not by the active object reference
     */
    private synchronized void usersUpdateInternal() {
        for (SchedulerUsersListener listener : schedulerUsersListeners)
            listener.update(users);
    }

    /** Set actions enable or not on pause events (pause freeze resume start) */
    private void setActionEnabledOnPauseEvents() {
        SubmitJobAction.getInstance().setEnabled(true);
        TableManager tableManager = TableManager.getInstance();
        JobId jobId = tableManager.getLastJobIdOfLastSelectedItem();
        InternalJob job = null;
        if (jobId != null) {
            job = getJobById(jobId);
        }
        if (job == null) {
            ObtainJobOutputAction.getInstance().setEnabled(false);
            ChangePriorityJobAction.getInstance().setEnabled(false);
            PriorityJobAction.getInstance(JobPriority.IDLE).setEnabled(false);
            PriorityJobAction.getInstance(JobPriority.LOWEST).setEnabled(false);
            PriorityJobAction.getInstance(JobPriority.LOW).setEnabled(false);
            PriorityJobAction.getInstance(JobPriority.NORMAL).setEnabled(false);
            PriorityJobAction.getInstance(JobPriority.HIGH).setEnabled(false);
            PriorityJobAction.getInstance(JobPriority.HIGHEST).setEnabled(false);
            PauseResumeJobAction.getInstance().setEnabled(false);
            KillRemoveJobAction.getInstance().setEnabled(false);
        } else if (tableManager.isItTheLastSelectedTable(AbstractJobComposite.FINISHED_TABLE_ID)) {
            ObtainJobOutputAction.getInstance().setEnabled(
                    SchedulerProxy.getInstance().isItHisJob(job.getOwner()));
            ChangePriorityJobAction.getInstance().setEnabled(false);
            PriorityJobAction.getInstance(JobPriority.IDLE).setEnabled(false);
            PriorityJobAction.getInstance(JobPriority.LOWEST).setEnabled(false);
            PriorityJobAction.getInstance(JobPriority.LOW).setEnabled(false);
            PriorityJobAction.getInstance(JobPriority.NORMAL).setEnabled(false);
            PriorityJobAction.getInstance(JobPriority.HIGH).setEnabled(false);
            PriorityJobAction.getInstance(JobPriority.HIGHEST).setEnabled(false);
            PauseResumeJobAction.getInstance().setEnabled(false);
            PauseResumeJobAction.getInstance().setPauseResumeMode();
            KillRemoveJobAction.getInstance().setEnabled(false);
        } else { // The table selected is the pending or the running table
            boolean enabled = SchedulerProxy.getInstance().isItHisJob(job.getOwner());
            // enabling/disabling button permitted with this job
            ObtainJobOutputAction.getInstance().setEnabled(enabled);

            ChangePriorityJobAction.getInstance().setEnabled(enabled);
            PriorityJobAction.getInstance(JobPriority.IDLE).setEnabled(enabled);
            PriorityJobAction.getInstance(JobPriority.LOWEST).setEnabled(enabled);
            PriorityJobAction.getInstance(JobPriority.LOW).setEnabled(enabled);
            PriorityJobAction.getInstance(JobPriority.NORMAL).setEnabled(enabled);
            PriorityJobAction.getInstance(JobPriority.HIGH).setEnabled(enabled);
            PriorityJobAction.getInstance(JobPriority.HIGHEST).setEnabled(enabled);

            PauseResumeJobAction.getInstance().setEnabled(enabled);

            JobState jobState = job.getState();
            if (jobState.equals(JobState.PAUSED)) {
                PauseResumeJobAction.getInstance().setResumeMode();
            } else if (jobState.equals(JobState.RUNNING) || jobState.equals(JobState.PENDING)) {
                PauseResumeJobAction.getInstance().setPauseMode();
            } else {
                PauseResumeJobAction.getInstance().setPauseResumeMode();
            }

            KillRemoveJobAction.getInstance().setEnabled(enabled);
        }
    }

    /** Set actions enable or not on stop events (stop shutdown) */
    private void setActionEnabledOnStopEvents() {
        TableManager tableManager = TableManager.getInstance();
        JobId jobId = tableManager.getLastJobIdOfLastSelectedItem();
        InternalJob job = null;
        if (jobId != null) {
            job = getJobById(jobId);
        }
        if (job == null) {
            ObtainJobOutputAction.getInstance().setEnabled(false);
            KillRemoveJobAction.getInstance().setEnabled(false);
        } else if (tableManager.isItTheLastSelectedTable(AbstractJobComposite.FINISHED_TABLE_ID)) {
            ObtainJobOutputAction.getInstance().setEnabled(
                    SchedulerProxy.getInstance().isItHisJob(job.getOwner()));
            KillRemoveJobAction.getInstance().setEnabled(false);
        } else { // The table selected is the pending or the running table
            boolean enabled = SchedulerProxy.getInstance().isItHisJob(job.getOwner());
            // enabling/disabling button permitted with this job
            ObtainJobOutputAction.getInstance().setEnabled(enabled);
            KillRemoveJobAction.getInstance().setEnabled(enabled);
        }

        SubmitJobAction.getInstance().setEnabled(false);
        ChangePriorityJobAction.getInstance().setEnabled(false);
        PriorityJobAction.getInstance(JobPriority.IDLE).setEnabled(false);
        PriorityJobAction.getInstance(JobPriority.LOWEST).setEnabled(false);
        PriorityJobAction.getInstance(JobPriority.LOW).setEnabled(false);
        PriorityJobAction.getInstance(JobPriority.NORMAL).setEnabled(false);
        PriorityJobAction.getInstance(JobPriority.HIGH).setEnabled(false);
        PriorityJobAction.getInstance(JobPriority.HIGHEST).setEnabled(false);
        PauseResumeJobAction.getInstance().setEnabled(false);
        PauseResumeJobAction.getInstance().setPauseResumeMode();
    }

    /** Set actions enable or not on stop events (stop shutdown) */
    private void setEnabledAdminButtons(Boolean startStopEnabled, Boolean freezeEnabled,
            Boolean pauseEnabled, Boolean resumeEnabled, Boolean shutdownEnabled) {
        if (SchedulerProxy.getInstance().isAnAdmin()) {
            StartStopSchedulerAction.getInstance().setEnabled(startStopEnabled);
            FreezeSchedulerAction.getInstance().setEnabled(freezeEnabled);
            PauseSchedulerAction.getInstance().setEnabled(pauseEnabled);
            ResumeSchedulerAction.getInstance().setEnabled(resumeEnabled);
            ShutdownSchedulerAction.getInstance().setEnabled(shutdownEnabled);
        } else {
            StartStopSchedulerAction.getInstance().setEnabled(false);
            FreezeSchedulerAction.getInstance().setEnabled(false);
            PauseSchedulerAction.getInstance().setEnabled(false);
            ResumeSchedulerAction.getInstance().setEnabled(false);
            ShutdownSchedulerAction.getInstance().setEnabled(false);
        }
    }

    // -------------------------------------------------------------------- //
    // ---------------- implements SchedulerEventListener ----------------- //
    // -------------------------------------------------------------------- //
    /**
     * @see org.objectweb.proactive.extensions.scheduler.core.SchedulerEventListener#newPendingJobEvent(org.objectweb.proactive.extra.scheduler.job.Job)
     */
    public void jobSubmittedEvent(InternalJob job) {
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
     * @see org.objectweb.proactive.extensions.scheduler.core.SchedulerEventListener#pendingToRunningJobEvent(org.objectweb.proactive.extra.scheduler.job.JobEvent)
     */
    public void jobPendingToRunningEvent(JobEvent event) {
        JobId jobId = event.getJobId();
        InternalJob job = getJobById(jobId);
        job.update(event);

        // remember if the job, which changing list, was selected
        boolean remember = TableManager.getInstance().isJobSelectedInThisTable(jobId,
                AbstractJobComposite.PENDING_TABLE_ID);

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
        if (remember) {
            TableManager.getInstance().moveJobSelection(jobId, AbstractJobComposite.RUNNING_TABLE_ID);
            // update the available buttons
            SeparatedJobView.getRunningJobComposite().jobSelected(job);
        }
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.core.SchedulerEventListener#runningToFinishedJobEvent(org.objectweb.proactive.extra.scheduler.job.JobEvent)
     */
    public void jobRunningToFinishedEvent(JobEvent event) {
        JobId jobId = event.getJobId();
        InternalJob job = getJobById(jobId);
        job.update(event);

        // remember if the job, which changing list, was selected
        boolean remember = TableManager.getInstance().isJobSelectedInThisTable(jobId,
                AbstractJobComposite.RUNNING_TABLE_ID);

        // call method on listeners
        removeRunningJobEventInternal(jobId);

        // remove job from the running jobs list
        if (!runningJobsIds.remove(jobId)) {
            throw new IllegalStateException("can't remove the job (id = " + jobId +
                ") from the runningJobsIds list !");
        }

        // add job to finished jobs list
        if (!finishedJobsIds.add(jobId)) {
            throw new IllegalStateException("can't add the job (id = " + jobId +
                ") from the finishedJobsIds list !");
        }

        // call method on listeners
        addFinishedJobEventInternal(jobId);

        // if the job was selected, move its selection to an other table
        if (remember) {
            TableManager.getInstance().moveJobSelection(jobId, AbstractJobComposite.FINISHED_TABLE_ID);
            // update the available buttons
            SeparatedJobView.getFinishedJobComposite().jobSelected(job);
        }
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.core.SchedulerEventListener#removeFinishedJobEvent(org.objectweb.proactive.extra.scheduler.job.JobEvent)
     */
    public void jobRemoveFinishedEvent(JobEvent event) {
        JobId jobId = event.getJobId();
        getJobById(jobId).update(event);

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
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.core.SchedulerEventListener#pendingToRunningTaskEvent(org.objectweb.proactive.extra.scheduler.task.TaskEvent)
     */
    public void taskPendingToRunningEvent(TaskEvent event) {
        JobId jobId = event.getJobId();
        getJobById(jobId).update(event);

        // call method on listeners
        pendingToRunningTaskEventInternal(event);

        final TaskEvent taskEvent = event;

        // if this job is selected in the Running table
        if (TableManager.getInstance().isJobSelectedInThisTable(jobId, AbstractJobComposite.RUNNING_TABLE_ID)) {
            final InternalJob job = getJobById(jobId);
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    // update info
                    JobInfo jobInfo = JobInfo.getInstance();
                    if (jobInfo != null) {
                        jobInfo.updateInfos(job);
                    }

                    TaskView taskView = TaskView.getInstance();
                    if (taskView != null) {
                        taskView.lineUpdate(taskEvent, getTaskDescriptorById(job, taskEvent.getTaskId()));
                    }
                }
            });
        }
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.core.SchedulerEventListener#runningToFinishedTaskEvent(org.objectweb.proactive.extra.scheduler.task.TaskEvent)
     */
    public void taskRunningToFinishedEvent(TaskEvent event) {
        JobId jobId = event.getJobId();
        getJobById(jobId).update(event);
        final TaskEvent taskEvent = event;

        // call method on listeners
        runningToFinishedTaskEventInternal(event);

        // if this job is selected in the Running table
        if (TableManager.getInstance().isJobSelectedInThisTable(jobId, AbstractJobComposite.RUNNING_TABLE_ID)) {
            final InternalJob job = getJobById(jobId);
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    // update info
                    JobInfo jobInfo = JobInfo.getInstance();
                    if (jobInfo != null) {
                        jobInfo.updateInfos(job);
                    }

                    TaskView taskView = TaskView.getInstance();
                    if (taskView != null) {
                        TaskId taskId = taskEvent.getTaskId();
                        taskView.lineUpdate(taskEvent, getTaskDescriptorById(job, taskId));

                        if (taskId.equals(taskView.getIdOfSelectedTask())) {
                            TaskResult tr = TaskComposite.getTaskResult(job.getId(), taskId);
                            if (tr != null) {
                                ResultPreview resultPreview = ResultPreview.getInstance();
                                if (resultPreview != null) {
                                    resultPreview.update(tr.getGraphicalDescription());
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.userAPI.SchedulerEventListener#SchedulerImmediatePausedEvent(org.objectweb.proactive.extra.scheduler.core.SchedulerEvent)
     */
    public void schedulerImmediatePausedEvent() {
        schedulerState = SchedulerState.PAUSED_IMMEDIATE;
        setActionEnabledOnPauseEvents();

        setEnabledAdminButtons(false, false, false, true, false);

        // call method on listeners
        schedulerFreezeEventInternal();
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.userAPI.SchedulerEventListener#SchedulerPausedEvent(org.objectweb.proactive.extra.scheduler.core.SchedulerEvent)
     */
    public void schedulerPausedEvent() {
        schedulerState = SchedulerState.PAUSED;
        setActionEnabledOnPauseEvents();

        setEnabledAdminButtons(false, false, false, true, false);

        // call method on listeners
        schedulerPausedEventInternal();
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.userAPI.SchedulerEventListener#SchedulerResumedEvent(org.objectweb.proactive.extra.scheduler.core.SchedulerEvent)
     */
    public void schedulerResumedEvent() {
        schedulerState = SchedulerState.STARTED;
        setActionEnabledOnPauseEvents();

        setEnabledAdminButtons(true, true, true, false, true);

        // call method on listeners
        schedulerResumedEventInternal();
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.userAPI.SchedulerEventListener#SchedulerShutDownEvent()
     */
    public void schedulerShutDownEvent() {
        schedulerState = SchedulerState.KILLED;
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                SeparatedJobView.clearOnDisconnection(false);
            }
        });

        // call method on listeners
        schedulerShutDownEventInternal();
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.userAPI.SchedulerEventListener#SchedulerShuttingDownEvent()
     */
    public void schedulerShuttingDownEvent() {
        schedulerState = SchedulerState.SHUTTING_DOWN;
        setActionEnabledOnStopEvents();

        setEnabledAdminButtons(false, false, false, false, false);

        // call method on listeners
        schedulerShuttingDownEventInternal();
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.userAPI.SchedulerEventListener#SchedulerStartedEvent()
     */
    public void schedulerStartedEvent() {
        schedulerState = SchedulerState.STARTED;
        setActionEnabledOnPauseEvents();

        StartStopSchedulerAction.getInstance().setStopMode();

        setEnabledAdminButtons(true, true, true, false, true);

        // call method on listeners
        schedulerStartedEventInternal();
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.userAPI.SchedulerEventListener#SchedulerStoppedEvent()
     */
    public void schedulerStoppedEvent() {
        schedulerState = SchedulerState.STOPPED;
        setActionEnabledOnStopEvents();

        StartStopSchedulerAction.getInstance().setStartMode();

        setEnabledAdminButtons(true, false, false, false, true);

        // call method on listeners
        schedulerStoppedEventInternal();
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.userAPI.SchedulerEventListener#SchedulerkilledEvent()
     */
    public void schedulerKilledEvent() {
        schedulerState = SchedulerState.KILLED;
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                SeparatedJobView.clearOnDisconnection(false);
            }
        });

        // disable all buttons
        SubmitJobAction.getInstance().setEnabled(false);
        ObtainJobOutputAction.getInstance().setEnabled(true);
        PauseResumeJobAction.getInstance().setEnabled(false);
        KillRemoveJobAction.getInstance().setEnabled(false);

        ChangePriorityJobAction.getInstance().setEnabled(false);
        PriorityJobAction.getInstance(JobPriority.IDLE).setEnabled(false);
        PriorityJobAction.getInstance(JobPriority.LOWEST).setEnabled(false);
        PriorityJobAction.getInstance(JobPriority.LOW).setEnabled(false);
        PriorityJobAction.getInstance(JobPriority.NORMAL).setEnabled(false);
        PriorityJobAction.getInstance(JobPriority.HIGH).setEnabled(false);
        PriorityJobAction.getInstance(JobPriority.HIGHEST).setEnabled(false);

        StartStopSchedulerAction.getInstance().setEnabled(false);
        FreezeSchedulerAction.getInstance().setEnabled(false);
        PauseSchedulerAction.getInstance().setEnabled(false);
        ResumeSchedulerAction.getInstance().setEnabled(false);
        ShutdownSchedulerAction.getInstance().setEnabled(false);
        KillSchedulerAction.getInstance().setEnabled(false);

        // call method on listeners
        schedulerKilledEventInternal();
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.userAPI.SchedulerEventListener#jobKilledEvent(org.objectweb.proactive.extra.scheduler.job.JobId)
     */
    public void jobKilledEvent(JobId jobId) {
        Vector<JobId> list = null;

        if (pendingJobsIds.contains(jobId)) {
            list = pendingJobsIds;
            removePendingJobEventInternal(jobId);
        } else if (runningJobsIds.contains(jobId)) {
            list = runningJobsIds;
            removeRunningJobEventInternal(jobId);
        } else if (finishedJobsIds.contains(jobId)) {
            list = finishedJobsIds;
            removeFinishedJobEventInternal(jobId);
        }

        // remove job from the jobs map
        if (jobs.remove(jobId) == null) {
            throw new IllegalStateException("can't remove the job (id = " + jobId + ") from the jobs map !");
        }

        // remove job from the specified jobs list
        if (!list.remove(jobId)) {
            throw new IllegalStateException("can't remove the job (id = " + jobId + ") from the list !");
        }

        // remove job's output
        JobsOutputController.getInstance().removeJobOutput(jobId);

        // call method on listeners
        jobKilledEventInternal(jobId);
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.userAPI.SchedulerEventListener#jobPausedEvent(org.objectweb.proactive.extra.scheduler.job.JobEvent)
     */
    public void jobPausedEvent(JobEvent event) {
        final InternalJob job = getJobById(event.getJobId());
        job.update(event);
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                // update info
                JobInfo jobInfo = JobInfo.getInstance();
                if (jobInfo != null) {
                    jobInfo.updateInfos(job);
                }

                TaskView taskView = TaskView.getInstance();
                if (taskView != null) {
                    taskView.fullUpdate(job);
                }
            }
        });

        // call method on listeners
        jobPausedEventInternal(event);
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.userAPI.SchedulerEventListener#jobResumedEvent(org.objectweb.proactive.extra.scheduler.job.JobEvent)
     */
    public void jobResumedEvent(JobEvent event) {
        final InternalJob job = getJobById(event.getJobId());
        job.update(event);
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                // update info
                JobInfo jobInfo = JobInfo.getInstance();
                if (jobInfo != null) {
                    jobInfo.updateInfos(job);
                }

                TaskView taskView = TaskView.getInstance();
                if (taskView != null) {
                    taskView.fullUpdate(job);
                }
            }
        });

        // call method on listeners
        jobResumedEventInternal(event);
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.userAPI.SchedulerEventListener#changeJobPriorityEvent(org.objectweb.proactive.extra.scheduler.job.JobEvent)
     */
    public void jobChangePriorityEvent(JobEvent event) {
        getJobById(event.getJobId()).update(event);
        jobPriorityChangedEventInternal(event);
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerEventListener#schedulerRMDownEvent()
     */
    public void schedulerRMDownEvent() {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerEventListener#schedulerRMUpEvent()
     */
    public void schedulerRMUpEvent() {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerEventListener#usersUpdate(org.objectweb.proactive.extensions.scheduler.common.job.UserIdentification)
     */
    public void usersUpdate(UserIdentification userIdentification) {
        System.out.println("users update");
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

        System.out.println("add sched users listener");
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
        Vector<InternalJob> jobs = new Vector<InternalJob>();
        for (JobId id : pendingJobsIds)
            jobs.add(getJobById(id));
        Collections.sort(jobs);

        Vector<JobId> tmp = new Vector<JobId>();
        for (InternalJob job : jobs)
            tmp.add(job.getId());

        pendingJobsIds = tmp;
    }

    public void sortRunningsJobs() {
        Vector<InternalJob> jobs = new Vector<InternalJob>();
        for (JobId id : runningJobsIds)
            jobs.add(getJobById(id));
        Collections.sort(jobs);

        Vector<JobId> tmp = new Vector<JobId>();
        for (InternalJob job : jobs)
            tmp.add(job.getId());

        runningJobsIds = tmp;
    }

    public void sortFinishedJobs() {
        Vector<InternalJob> jobs = new Vector<InternalJob>();
        for (JobId id : finishedJobsIds)
            jobs.add(getJobById(id));
        Collections.sort(jobs);

        Vector<JobId> tmp = new Vector<JobId>();
        for (InternalJob job : jobs)
            tmp.add(job.getId());

        finishedJobsIds = tmp;
    }

    // -------------------------------------------------------------------- //
    // ------------------------------ others ------------------------------ //
    // -------------------------------------------------------------------- //
    public InternalJob getJobById(JobId id) {
        InternalJob res = jobs.get(id);
        if (res == null)
            throw new IllegalArgumentException("there are no jobs with the id : " + id);
        return res;
    }

    public InternalTask getTaskDescriptorById(InternalJob job, TaskId id) {
        InternalTask taskDescriptor = job.getHMTasks().get(id);
        if (taskDescriptor == null) {
            throw new IllegalArgumentException("there are no taskDescriptor with the id : " + id +
                " in the job : " + job.getId());
        }
        return taskDescriptor;
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
        SchedulerInitialState<InternalJob> state = null;
        state = SchedulerProxy.getInstance().addSchedulerEventListener(
                ((SchedulerEventListener) PAActiveObject.getStubOnThis()));

        if (state == null) { // addSchedulerEventListener failed
            return false;
        }

        schedulerState = state.getState();
        switch (schedulerState) {
            case KILLED:
                schedulerKilledEvent();
                break;
            case PAUSED:
                schedulerPausedEvent();
                break;
            case PAUSED_IMMEDIATE:
                schedulerImmediatePausedEvent();
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

        jobs = new HashMap<JobId, InternalJob>();
        pendingJobsIds = new Vector<JobId>();
        runningJobsIds = new Vector<JobId>();
        finishedJobsIds = new Vector<JobId>();

        Vector<InternalJob> tmp = state.getPendingJobs();
        for (InternalJob job : tmp) {
            jobs.put(job.getId(), job);
            pendingJobsIds.add(job.getId());
        }

        tmp = state.getRunningJobs();
        for (InternalJob job : tmp) {
            jobs.put(job.getId(), job);
            runningJobsIds.add(job.getId());
        }

        tmp = state.getFinishedJobs();
        for (InternalJob job : tmp) {
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
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void clearInstances() {
        localView = null;
        activeView = null;
    }

    public static SchedulerState getSchedulerState() {
        return schedulerState;
    }
}
