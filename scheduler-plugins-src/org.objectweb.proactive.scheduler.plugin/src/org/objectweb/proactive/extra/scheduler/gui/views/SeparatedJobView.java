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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extra.scheduler.common.task.util.ResultDescriptorTool.SimpleTextPanel;
import org.objectweb.proactive.extra.scheduler.gui.actions.ChangeViewModeAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.ConnectDeconnectSchedulerAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.FreezeSchedulerAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.KillRemoveJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.KillSchedulerAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.ObtainJobOutputAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PauseResumeJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PauseSchedulerAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityHighJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityHighestJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityIdleJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityLowJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityLowestJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.PriorityNormalJobAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.ResumeSchedulerAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.ShutdownSchedulerAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.StartStopSchedulerAction;
import org.objectweb.proactive.extra.scheduler.gui.actions.SubmitJobAction;
import org.objectweb.proactive.extra.scheduler.gui.composite.AbstractJobComposite;
import org.objectweb.proactive.extra.scheduler.gui.composite.FinishedJobComposite;
import org.objectweb.proactive.extra.scheduler.gui.composite.JobComposite;
import org.objectweb.proactive.extra.scheduler.gui.composite.PendingJobComposite;
import org.objectweb.proactive.extra.scheduler.gui.composite.RunningJobComposite;
import org.objectweb.proactive.extra.scheduler.gui.composite.StatusLabel;
import org.objectweb.proactive.extra.scheduler.gui.data.JobsController;
import org.objectweb.proactive.extra.scheduler.gui.data.JobsOutputController;
import org.objectweb.proactive.extra.scheduler.gui.data.SchedulerProxy;
import org.objectweb.proactive.extra.scheduler.gui.data.TableManager;


/**
 * This class display the state of the scheduler in real time
 *
 * @author FRADJ Johann
 * @version 1.0, Jul 12, 2007
 * @since ProActive 3.2
 */
public class SeparatedJobView extends ViewPart {

    /** the view part id */
    public static final String ID = "org.objectweb.proactive.extra.scheduler.gui.views.SeparatedJobView";
    private static final long serialVersionUID = -6958852991395601640L;
    private static JobComposite jobComposite = null;
    private static AbstractJobComposite pendingJobComposite = null;
    private static AbstractJobComposite runningJobComposite = null;
    private static AbstractJobComposite finishedJobComposite = null;
    private static Action connectSchedulerAction = null;
    private static Action changeViewModeAction = null;
    private static Action obtainJobOutputAction = null;
    private static Action submitJob = null;
    private static Action pauseResumeJobAction = null;
    private static Action killJobAction = null;
    private static Action priorityJobAction = null;
    private static Action priorityIdleJobAction = null;
    private static Action priorityLowestJobAction = null;
    private static Action priorityLowJobAction = null;
    private static Action priorityNormalJobAction = null;
    private static Action priorityHighJobAction = null;
    private static Action priorityHighestJobAction = null;
    private static Action startStopSchedulerAction = null;
    private static Action freezeSchedulerAction = null;
    private static Action pauseSchedulerAction = null;
    private static Action resumeSchedulerAction = null;
    private static Action shutdownSchedulerAction = null;
    private static Action killSchedulerAction = null;
    private static Composite parent = null;

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    /**
     * The constructor.
     */
    public SeparatedJobView() {
    }

    // -------------------------------------------------------------------- //
    // ----------------------------- private ------------------------------ //
    // -------------------------------------------------------------------- //
    private void hookContextMenu(Composite parent) {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
                public void menuAboutToShow(IMenuManager manager) {
                    fillContextMenu(manager);
                }
            });

        Menu menu = menuMgr.createContextMenu(parent);
        parent.setMenu(menu);
        pendingJobComposite.setMenu(menu);
        runningJobComposite.setMenu(menu);
        finishedJobComposite.setMenu(menu);
    }

    private void fillContextMenu(IMenuManager manager) {
        manager.add(connectSchedulerAction);
        manager.add(changeViewModeAction);
        manager.add(new Separator());
        manager.add(submitJob);
        manager.add(pauseResumeJobAction);
        IMenuManager subMenu = new MenuManager("Change job priority") {
            };
        manager.add(subMenu);
        if (SchedulerProxy.getInstance() != null) {
            if (SchedulerProxy.getInstance().isAnAdmin()) {
                subMenu.add(priorityIdleJobAction);
            }
        }
        subMenu.add(priorityLowestJobAction);
        subMenu.add(priorityLowJobAction);
        subMenu.add(priorityNormalJobAction);
        manager.add(obtainJobOutputAction);
        manager.add(killJobAction);
        if (SchedulerProxy.getInstance() != null) {
            if (SchedulerProxy.getInstance().isAnAdmin()) {
                subMenu.add(priorityHighJobAction);
                subMenu.add(priorityHighestJobAction);
                manager.add(new Separator());
                manager.add(startStopSchedulerAction);
                manager.add(freezeSchedulerAction);
                manager.add(pauseSchedulerAction);
                manager.add(resumeSchedulerAction);
                manager.add(shutdownSchedulerAction);
                manager.add(killSchedulerAction);
            }
        }

        // // Other plug-ins can contribute there actions here
        // manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        // toolBarManager = manager;
        manager.add(connectSchedulerAction);
        manager.add(changeViewModeAction);
        manager.add(new Separator());
        manager.add(submitJob);
        manager.add(pauseResumeJobAction);
        manager.add(priorityJobAction);
        manager.add(obtainJobOutputAction);
        manager.add(killJobAction);

        manager.add(new Separator());
        manager.add(startStopSchedulerAction);
        manager.add(freezeSchedulerAction);
        manager.add(pauseSchedulerAction);
        manager.add(resumeSchedulerAction);
        manager.add(shutdownSchedulerAction);
        manager.add(killSchedulerAction);
    }

    private void makeActions() {
        Shell shell = parent.getShell();

        connectSchedulerAction = ConnectDeconnectSchedulerAction.newInstance(parent);
        changeViewModeAction = ChangeViewModeAction.newInstance(jobComposite);

        obtainJobOutputAction = ObtainJobOutputAction.newInstance();
        submitJob = SubmitJobAction.newInstance(parent);
        pauseResumeJobAction = PauseResumeJobAction.newInstance();
        killJobAction = KillRemoveJobAction.newInstance(shell);

        priorityJobAction = PriorityJobAction.newInstance();
        priorityIdleJobAction = PriorityIdleJobAction.newInstance();
        priorityLowestJobAction = PriorityLowestJobAction.newInstance();
        priorityLowJobAction = PriorityLowJobAction.newInstance();
        priorityNormalJobAction = PriorityNormalJobAction.newInstance();
        priorityHighJobAction = PriorityHighJobAction.newInstance();
        priorityHighestJobAction = PriorityHighestJobAction.newInstance();

        startStopSchedulerAction = StartStopSchedulerAction.newInstance();
        freezeSchedulerAction = FreezeSchedulerAction.newInstance();
        pauseSchedulerAction = PauseSchedulerAction.newInstance();
        resumeSchedulerAction = ResumeSchedulerAction.newInstance();
        shutdownSchedulerAction = ShutdownSchedulerAction.newInstance(shell);
        killSchedulerAction = KillSchedulerAction.newInstance(shell);
    }

    // -------------------------------------------------------------------- //
    // ------------------------------ public ------------------------------ //
    // -------------------------------------------------------------------- //
    /**
     * To display or not the view
     *
     * @param visible
     */
    public static void setVisible(boolean visible) {
        pendingJobComposite.setVisible(visible);
        runningJobComposite.setVisible(visible);
        finishedJobComposite.setVisible(visible);
        // JobInfo jobInfo = JobInfo.getInstance();
        // if(jobInfo != null)
        // jobInfo.setVisible(visible);
        // TaskView taskView = TaskView.getInstance();
        // if(taskView != null)
        // taskView.setVisible(visible);
    }

    /**
     * Returns the pending job composite
     *
     * @return the pending job composite
     */
    public static AbstractJobComposite getPendingJobComposite() {
        return pendingJobComposite;
    }

    /**
     * Returns the running job composite
     *
     * @return the running job composite
     */
    public static AbstractJobComposite getRunningJobComposite() {
        return runningJobComposite;
    }

    /**
     * Returns the finished job composite
     *
     * @return the finished job composite
     */
    public static AbstractJobComposite getFinishedJobComposite() {
        return finishedJobComposite;
    }

    public static void clearOnDisconnection(Boolean sendDisconnectMessage) {
        setVisible(false);
        ConnectDeconnectSchedulerAction.getInstance().setDisconnectionMode();

        if (pendingJobComposite != null) {
            pendingJobComposite.clear();
        }

        if (runningJobComposite != null) {
            runningJobComposite.clear();
        }

        if (finishedJobComposite != null) {
            finishedJobComposite.clear();
        }

        TaskView taskView = TaskView.getInstance();
        if (taskView != null) {
            taskView.clear();
        }

        JobInfo jobInfo = JobInfo.getInstance();
        if (jobInfo != null) {
            jobInfo.clear();
        }

        ResultPreview resultPreview = ResultPreview.getInstance();
        if (resultPreview != null) {
            resultPreview.update(new SimpleTextPanel("No selected task"));
        }

        JobsOutputController jobsOutputController = JobsOutputController.getInstance();
        if (jobsOutputController != null) {
            jobsOutputController.removeAllJobOutput();
        }

        if (sendDisconnectMessage) {
            SchedulerProxy.getInstance().disconnect();
        }
        PAActiveObject.terminateActiveObject(SchedulerProxy.getInstance(), false);
        SchedulerProxy.clearInstance();

        ChangeViewModeAction.getInstance().setEnabled(false);
        KillRemoveJobAction.getInstance().setEnabled(false);
        ObtainJobOutputAction.getInstance().setEnabled(false);
        PauseResumeJobAction.getInstance().setEnabled(false);
        SubmitJobAction.getInstance().setEnabled(false);

        PriorityJobAction.getInstance().setEnabled(false);
        PriorityIdleJobAction.getInstance().setEnabled(false);
        PriorityLowestJobAction.getInstance().setEnabled(false);
        PriorityLowJobAction.getInstance().setEnabled(false);
        PriorityNormalJobAction.getInstance().setEnabled(false);
        PriorityHighJobAction.getInstance().setEnabled(false);
        PriorityHighestJobAction.getInstance().setEnabled(false);

        FreezeSchedulerAction.getInstance().setEnabled(false);
        KillSchedulerAction.getInstance().setEnabled(false);
        PauseSchedulerAction.getInstance().setEnabled(false);
        ResumeSchedulerAction.getInstance().setEnabled(false);
        ShutdownSchedulerAction.getInstance().setEnabled(false);
        StartStopSchedulerAction.getInstance().setEnabled(false);
    }

    // -------------------------------------------------------------------- //
    // ------------------------- extends viewPart ------------------------- //
    // -------------------------------------------------------------------- //
    /**
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite theParent) {
        parent = theParent;

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        parent.setLayout(gridLayout);

        jobComposite = new JobComposite(parent);

        pendingJobComposite = new PendingJobComposite(jobComposite, "Pending",
                JobsController.getLocalView());
        runningJobComposite = new RunningJobComposite(jobComposite, "Running",
                JobsController.getLocalView());
        finishedJobComposite = new FinishedJobComposite(jobComposite,
                "Finished", JobsController.getLocalView());

        GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        jobComposite.setLayoutData(gridData);

        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;

        StatusLabel.newInstance(theParent, gridData,
            JobsController.getLocalView());

        // I must turn active the jobsController after create
        // pendingJobComposite, runningJobComposite, finishedJobComposite
        // and before call newInstance on StatusLabel.
        JobsController.turnActive();

        makeActions();
        hookContextMenu(parent);
        contributeToActionBars();
        setVisible(false);
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
    }

    @Override
    public void dispose() {
        TableManager.clearInstance();
        TaskView taskView = TaskView.getInstance();
        if (taskView != null) {
            taskView.clear();
        }
        JobInfo jobInfo = JobInfo.getInstance();
        if (jobInfo != null) {
            jobInfo.clear();
        }

        ResultPreview resultPreview = ResultPreview.getInstance();
        if (resultPreview != null) {
            resultPreview.update(new SimpleTextPanel("No selected task"));
        }

        JobsOutputController.clearInstance();
        PAActiveObject.terminateActiveObject(JobsController.getActiveView(),
            false);
        SchedulerProxy.getInstance().disconnect();
        PAActiveObject.terminateActiveObject(SchedulerProxy.getInstance(), false);
        JobsController.clearInstances();
        SchedulerProxy.clearInstance();
        super.dispose();
    }
}
