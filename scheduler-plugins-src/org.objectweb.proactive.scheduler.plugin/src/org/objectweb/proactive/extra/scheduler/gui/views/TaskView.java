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
package org.objectweb.proactive.extra.scheduler.gui.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.extra.scheduler.common.job.JobId;
import org.objectweb.proactive.extra.scheduler.common.task.TaskEvent;
import org.objectweb.proactive.extra.scheduler.common.task.TaskId;
import org.objectweb.proactive.extra.scheduler.gui.composite.TaskComposite;
import org.objectweb.proactive.extra.scheduler.gui.data.JobsController;
import org.objectweb.proactive.extra.scheduler.gui.data.TableManager;
import org.objectweb.proactive.extra.scheduler.job.InternalJob;
import org.objectweb.proactive.extra.scheduler.task.internal.InternalTask;


/**
 * This view display many informations about tasks contains in a job.
 *
 * @author ProActive Team
 * @version 1.0, Jul 11, 2007
 * @since ProActive 3.2
 */
public class TaskView extends ViewPart {

    /** the view part id */
    public static final String ID = "org.objectweb.proactive.extra.scheduler.gui.views.TaskView";

    // the shared instance
    private static TaskView instance = null;
    private static boolean isDisposed = true;
    private TaskComposite taskComposite = null;

    /**
     * This is the default constructor
     */
    public TaskView() {
        instance = this;
    }

    /**
     * This method clear the view
     */
    public void clear() {
        taskComposite.clear();
    }

    /**
     * To update fully the view with the new informations about the given job
     *
     * @param job a job
     */
    public void fullUpdate(InternalJob job) {
        if (!taskComposite.isDisposed()) {
            final InternalJob aJob = job;
            Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        taskComposite.setTasks(aJob.getId(), aJob.getTasks());
                    }
                });
        }
    }

    /**
     * To update only one line of the jobs informations displayed in the view.
     * use this method to avoid flicker
     *
     * @param taskEvent
     * @param taskDescriptor
     */
    public void lineUpdate(TaskEvent taskEvent, InternalTask taskDescriptor) {
        final TaskEvent aTaskEvent = taskEvent;
        final InternalTask aTaskDescriptor = taskDescriptor;
        Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    taskComposite.changeLine(aTaskEvent.getTaskId(),
                        aTaskDescriptor);
                }
            });
    }

    /**
     * To display or not the view
     *
     * @param isVisible
     */
    public void setVisible(boolean isVisible) {
        if (taskComposite != null) {
            taskComposite.setVisible(isVisible);
        }
    }

    /**
     * To enabled or not the view
     *
     * @param isEnabled
     */
    public void setEnabled(boolean isEnabled) {
        if (taskComposite != null) {
            taskComposite.setEnabled(isEnabled);
        }
    }

    public TaskId getIdOfSelectedTask() {
        return taskComposite.getIdOfSelectedTask();
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static TaskView getInstance() {
        if (isDisposed) {
            return null;
        }
        return instance;
    }

    // -------------------------------------------------------------------- //
    // ------------------------ extends ViewPart -------------------------- //
    // -------------------------------------------------------------------- //
    /**
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        isDisposed = false;
        taskComposite = new TaskComposite(parent);
        TableManager tableManager = TableManager.getInstance();
        if (tableManager != null) {
            JobId jobId = tableManager.getLastJobIdOfLastSelectedItem();
            if (jobId != null) {
                JobsController jobsController = JobsController.getLocalView();
                if (jobsController != null) {
                    fullUpdate(jobsController.getJobById(jobId));
                }
            }
        }
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        // TableManager tableManager = TableManager.getInstance();
        // if (tableManager != null) {
        // TableItem item = tableManager.getLastSelectedItem();
        // if (item != null)
        // fullUpdate(JobsController.getInstance().getJobById((IntWrapper)
        // item.getData()));
        // }
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        isDisposed = true;
        taskComposite.dispose();
        super.dispose();
    }
}
