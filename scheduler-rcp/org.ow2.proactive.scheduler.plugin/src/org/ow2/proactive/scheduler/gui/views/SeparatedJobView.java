/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.gui.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.api.PAActiveObject;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.task.util.ResultPreviewTool.SimpleTextPanel;
import org.ow2.proactive.scheduler.gui.actions.ChangeMaximizeListAction;
import org.ow2.proactive.scheduler.gui.actions.ChangePriorityJobAction;
import org.ow2.proactive.scheduler.gui.actions.ChangeViewModeAction;
import org.ow2.proactive.scheduler.gui.actions.ConnectDeconnectSchedulerAction;
import org.ow2.proactive.scheduler.gui.actions.FreezeSchedulerAction;
import org.ow2.proactive.scheduler.gui.actions.JMXChartItAction;
import org.ow2.proactive.scheduler.gui.actions.KillRemoveJobAction;
import org.ow2.proactive.scheduler.gui.actions.KillSchedulerAction;
import org.ow2.proactive.scheduler.gui.actions.MaximizeListAction;
import org.ow2.proactive.scheduler.gui.actions.ObtainJobOutputAction;
import org.ow2.proactive.scheduler.gui.actions.PauseResumeJobAction;
import org.ow2.proactive.scheduler.gui.actions.PauseSchedulerAction;
import org.ow2.proactive.scheduler.gui.actions.PriorityJobAction;
import org.ow2.proactive.scheduler.gui.actions.ResumeSchedulerAction;
import org.ow2.proactive.scheduler.gui.actions.ShutdownSchedulerAction;
import org.ow2.proactive.scheduler.gui.actions.StartStopSchedulerAction;
import org.ow2.proactive.scheduler.gui.actions.SubmitFlatFileJobAction;
import org.ow2.proactive.scheduler.gui.actions.SubmitJobAction;
import org.ow2.proactive.scheduler.gui.composite.AbstractJobComposite;
import org.ow2.proactive.scheduler.gui.composite.FinishedJobComposite;
import org.ow2.proactive.scheduler.gui.composite.PendingJobComposite;
import org.ow2.proactive.scheduler.gui.composite.RunningJobComposite;
import org.ow2.proactive.scheduler.gui.composite.StatusLabel;
import org.ow2.proactive.scheduler.gui.data.ActionsManager;
import org.ow2.proactive.scheduler.gui.data.JobsController;
import org.ow2.proactive.scheduler.gui.data.JobsOutputController;
import org.ow2.proactive.scheduler.gui.data.SchedulerProxy;
import org.ow2.proactive.scheduler.gui.data.TableManager;


/**
 * This class display the state of the scheduler in real time
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class SeparatedJobView extends ViewPart {

    /* the view part id */
    public static final String ID = "org.ow2.proactive.scheduler.gui.views.SeparatedJobView";
    private static SashForm sashForm = null;
    private static AbstractJobComposite pendingJobComposite = null;
    private static AbstractJobComposite runningJobComposite = null;
    private static AbstractJobComposite finishedJobComposite = null;
    private static Action connectSchedulerAction = null;
    private static Action changeViewModeAction = null;

    private static Action changeMaximizeListAction = null;
    private static Action maximizePendingListAction = null;
    private static Action maximizeRunningListAction = null;
    private static Action maximizeFinishedListAction = null;
    private static Action maximizeNoneListAction = null;

    private static Action jmxChartItAction = null;

    private static Action obtainJobOutputAction = null;
    private static Action submitJob = null;
    private static Action submitFlatJob = null;
    private static Action pauseResumeJobAction = null;
    private static Action killJobAction = null;
    private static Action changePriorityJobAction = null;
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

    private static IMenuManager subMenu = null;
    private static IMenuManager subMenuJob = null;
    private static IMenuManager subMenuPriority = null;

    private static Shell schedulerShell = null;

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    /*
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
        // FIXME Maybe call ActionsManager.update() here
        // but I will remove some others calls to this method on others classes
        manager.add(connectSchedulerAction);
        manager.add(changeViewModeAction);
        subMenu = new MenuManager("Maximize list") {
        };
        manager.add(subMenu);
        subMenu.add(maximizeNoneListAction);
        subMenu.add(maximizePendingListAction);
        subMenu.add(maximizeRunningListAction);
        subMenu.add(maximizeFinishedListAction);
        manager.add(jmxChartItAction);

        manager.add(new Separator());

        subMenuJob = new MenuManager("Submit job");
        manager.add(subMenuJob);
        subMenuJob.add(submitJob);
        subMenuJob.add(submitFlatJob);

        manager.add(pauseResumeJobAction);
        subMenuPriority = new MenuManager("Change job priority") {
        };
        manager.add(subMenuPriority);
        if (SchedulerProxy.getInstance() != null) {
            if (SchedulerProxy.getInstance().isAnAdmin()) {
                subMenuPriority.add(priorityIdleJobAction);
            }
        }
        subMenuPriority.add(priorityLowestJobAction);
        subMenuPriority.add(priorityLowJobAction);
        subMenuPriority.add(priorityNormalJobAction);
        manager.add(obtainJobOutputAction);
        manager.add(killJobAction);
        if (SchedulerProxy.getInstance() != null) {
            if (SchedulerProxy.getInstance().isAnAdmin()) {
                subMenuPriority.add(priorityHighJobAction);
                subMenuPriority.add(priorityHighestJobAction);
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
        manager.add(connectSchedulerAction);
        manager.add(changeViewModeAction);
        manager.add(changeMaximizeListAction);
        manager.add(jmxChartItAction);
        manager.add(new Separator());
        manager.add(submitJob);
        manager.add(submitFlatJob);
        manager.add(pauseResumeJobAction);
        manager.add(changePriorityJobAction);
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

        connectSchedulerAction = new ConnectDeconnectSchedulerAction(parent);
        changeViewModeAction = new ChangeViewModeAction();

        changeMaximizeListAction = new ChangeMaximizeListAction();
        maximizeNoneListAction = MaximizeListAction.newInstance(null, MaximizeListAction.NONE);
        maximizePendingListAction = MaximizeListAction.newInstance(pendingJobComposite,
                MaximizeListAction.PENDING);
        maximizeRunningListAction = MaximizeListAction.newInstance(runningJobComposite,
                MaximizeListAction.RUNNING);
        maximizeFinishedListAction = MaximizeListAction.newInstance(finishedJobComposite,
                MaximizeListAction.FINISHED);

        jmxChartItAction = JMXChartItAction.getInstance();

        obtainJobOutputAction = new ObtainJobOutputAction();
        submitJob = new SubmitJobAction(parent);
        submitFlatJob = new SubmitFlatFileJobAction(parent);
        pauseResumeJobAction = new PauseResumeJobAction();
        killJobAction = new KillRemoveJobAction(shell);

        changePriorityJobAction = new ChangePriorityJobAction();
        priorityIdleJobAction = PriorityJobAction.newInstance(JobPriority.IDLE);
        priorityLowestJobAction = PriorityJobAction.newInstance(JobPriority.LOWEST);
        priorityLowJobAction = PriorityJobAction.newInstance(JobPriority.LOW);
        priorityNormalJobAction = PriorityJobAction.newInstance(JobPriority.NORMAL);
        priorityHighJobAction = PriorityJobAction.newInstance(JobPriority.HIGH);
        priorityHighestJobAction = PriorityJobAction.newInstance(JobPriority.HIGHEST);

        startStopSchedulerAction = new StartStopSchedulerAction();
        freezeSchedulerAction = new FreezeSchedulerAction();
        pauseSchedulerAction = new PauseSchedulerAction();
        resumeSchedulerAction = new ResumeSchedulerAction();
        shutdownSchedulerAction = new ShutdownSchedulerAction(shell);
        killSchedulerAction = new KillSchedulerAction(shell);
    }

    // -------------------------------------------------------------------- //
    // ------------------------------ public ------------------------------ //
    // -------------------------------------------------------------------- //
    /*
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

    public static SashForm getSashForm() {
        return sashForm;
    }

    /*
     * Returns the pending job composite
     *
     * @return the pending job composite
     */
    public static AbstractJobComposite getPendingJobComposite() {
        return pendingJobComposite;
    }

    /*
     * Returns the running job composite
     *
     * @return the running job composite
     */
    public static AbstractJobComposite getRunningJobComposite() {
        return runningJobComposite;
    }

    /*
     * Returns the finished job composite
     *
     * @return the finished job composite
     */
    public static AbstractJobComposite getFinishedJobComposite() {
        return finishedJobComposite;
    }

    public static void clearOnDisconnection(Boolean sendDisconnectMessage) {
        setVisible(false);
        TableManager.getInstance().clear();
        ActionsManager.getInstance().setConnected(false);

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

        Users users = Users.getInstance();
        if (users != null) {
            users.clear();
        }

        ResultPreview resultPreview = ResultPreview.getInstance();
        if (resultPreview != null) {
            resultPreview.update(new SimpleTextPanel("No selected task"));
        }

        JobsOutputController jobsOutputController = JobsOutputController.getInstance();
        if (jobsOutputController != null) {
            jobsOutputController.removeAllJobOutput();
        }

        // Disconnect the JMX client of ChartIt
        JMXChartItAction.getInstance().disconnectJMXClient();

        if (sendDisconnectMessage) {
            SchedulerProxy.getInstance().disconnect();
        }
        PAActiveObject.terminateActiveObject(SchedulerProxy.getInstance(), false);
        SchedulerProxy.clearInstance();
        ControllerView.clearInstance();
        ActionsManager.getInstance().update();
    }

    // -------------------------------------------------------------------- //
    // ------------------------- extends viewPart ------------------------- //
    // -------------------------------------------------------------------- //
    /*
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite theParent) {
        parent = theParent;

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        parent.setLayout(gridLayout);

        // Create the SashForm
        Composite sash = new Composite(parent, SWT.NONE);
        sash.setLayout(new FillLayout());
        sash.setLayoutData(new GridData(GridData.FILL_BOTH));
        sashForm = new SashForm(sash, SWT.HORIZONTAL);
        // Change the width of the sashes
        sashForm.SASH_WIDTH = 7;

        pendingJobComposite = new PendingJobComposite(sashForm, JobsController.getLocalView());
        runningJobComposite = new RunningJobComposite(sashForm, JobsController.getLocalView());
        finishedJobComposite = new FinishedJobComposite(sashForm, JobsController.getLocalView());

        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;

        StatusLabel.newInstance(theParent, gridData, JobsController.getLocalView());

        // I must turn active the jobsController after create
        // pendingJobComposite, runningJobComposite, finishedJobComposite
        // and before call newInstance on StatusLabel.
        JobsController.turnActive();

        makeActions();
        hookContextMenu(parent);
        contributeToActionBars();
        setVisible(false);

        if (schedulerShell == null) {
            try {
                schedulerShell = Display.getDefault().getShells()[1];
            } catch (RuntimeException e) {
                schedulerShell = Display.getDefault().getActiveShell();
            }
        }

        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                connectSchedulerAction.run();
            }
        });

    }

    public static Shell getSchedulerShell() {
        return schedulerShell;
    }

    /*
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
    }

    @Override
    public void dispose() {
        TableManager.clearInstance();
        ActionsManager.clearInstance();
        TaskView taskView = TaskView.getInstance();
        if (taskView != null) {
            taskView.clear();
        }
        JobInfo jobInfo = JobInfo.getInstance();
        if (jobInfo != null) {
            jobInfo.clear();
        }

        Users users = Users.getInstance();
        if (users != null) {
            users.clear();
        }

        ResultPreview resultPreview = ResultPreview.getInstance();
        if (resultPreview != null) {
            resultPreview.update(new SimpleTextPanel("No selected task"));
        }

        JobsOutputController.clearInstance();
        PAActiveObject.terminateActiveObject(JobsController.getActiveView(), false);
        SchedulerProxy.getInstance().disconnect();
        PAActiveObject.terminateActiveObject(SchedulerProxy.getInstance(), false);
        JobsController.clearInstances();
        SchedulerProxy.clearInstance();
        ControllerView.clearInstance();
        super.dispose();
    }
}
