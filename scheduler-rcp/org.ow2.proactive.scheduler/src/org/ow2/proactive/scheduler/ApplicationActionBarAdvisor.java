/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.ow2.proactive.scheduler.gui.actions.ConnectAction;
import org.ow2.proactive.scheduler.gui.actions.DisconnectAction;
import org.ow2.proactive.scheduler.gui.actions.ExitAction;
import org.ow2.proactive.scheduler.gui.actions.FreezeSchedulerAction;
import org.ow2.proactive.scheduler.gui.actions.JMXActionsManager;
import org.ow2.proactive.scheduler.gui.actions.KillSchedulerAction;
import org.ow2.proactive.scheduler.gui.actions.PauseSchedulerAction;
import org.ow2.proactive.scheduler.gui.actions.ResumeSchedulerAction;
import org.ow2.proactive.scheduler.gui.actions.ShutdownSchedulerAction;
import org.ow2.proactive.scheduler.gui.actions.StartStopSchedulerAction;
import org.ow2.proactive.scheduler.gui.actions.SubmitFlatFileJobAction;
import org.ow2.proactive.scheduler.gui.actions.SubmitJobAction;
import org.ow2.proactive.scheduler.gui.actions.SubmitListAction;


public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

    // Scheduler
    private IAction connectAction;
    private IAction disConnectAction;

    private SubmitListAction submitList = null;
    private Action submitJob = null;
    private Action submitJobEditVars = null;
    private Action submitFlatJob = null;

    private IAction exitAction;

    // Admin
    private Action startStopSchedulerAction = null;
    private Action freezeSchedulerAction = null;
    private Action pauseSchedulerAction = null;
    private Action resumeSchedulerAction = null;
    private Action shutdownSchedulerAction = null;
    private Action killSchedulerAction = null;

    // Window
    private IContributionItem viewList;
    private IWorkbenchAction resetPerspective;
    private IWorkbenchAction preferenceAction;

    // help
    private IWorkbenchAction aboutAction;

    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
    }

    @Override
    protected void makeActions(IWorkbenchWindow window) {

        // SCHEDULER
        connectAction = new ConnectAction();
        disConnectAction = new DisconnectAction();
        disConnectAction.setEnabled(false);

        submitJob = new SubmitJobAction(false);
        submitJobEditVars = new SubmitJobAction(true);
        submitList = new SubmitListAction();
        submitFlatJob = new SubmitFlatFileJobAction();
        submitList.add(submitJob);
        submitList.add(submitJobEditVars);
        submitList.add(submitFlatJob);

        exitAction = new ExitAction();

        // ADMIN
        startStopSchedulerAction = new StartStopSchedulerAction();
        freezeSchedulerAction = new FreezeSchedulerAction();
        pauseSchedulerAction = new PauseSchedulerAction();
        resumeSchedulerAction = new ResumeSchedulerAction();
        shutdownSchedulerAction = new ShutdownSchedulerAction();
        killSchedulerAction = new KillSchedulerAction();

        // WINDOW
        viewList = ContributionItemFactory.VIEWS_SHORTLIST.create(window);

        resetPerspective = ActionFactory.RESET_PERSPECTIVE.create(window);
        register(resetPerspective);

        preferenceAction = ActionFactory.PREFERENCES.create(window);
        register(preferenceAction);

        // HELP
        aboutAction = ActionFactory.ABOUT.create(window);
        register(aboutAction);

    }

    @Override
    protected void fillMenuBar(IMenuManager menuBar) {

        // SCHEDULER
        MenuManager schedulerMenu = new MenuManager("&Scheduler", IWorkbenchActionConstants.M_FILE);
        schedulerMenu.add(connectAction);
        schedulerMenu.add(disConnectAction);
        schedulerMenu.add(new Separator());

        MenuManager submitMenu = new MenuManager("&Submit Job", Activator.getDefault().getImageRegistry()
                .getDescriptor(org.ow2.proactive.scheduler.gui.Internal.IMG_JOBSUBMIT), null);
        submitMenu.add(submitJob);
        submitMenu.add(submitJobEditVars);
        submitMenu.add(submitFlatJob);

        schedulerMenu.add(submitMenu);
        schedulerMenu.add(new Separator());
        schedulerMenu.add(exitAction);

        // ADMIN
        MenuManager adminMenu = new MenuManager("&Admin", IWorkbenchActionConstants.MB_ADDITIONS);
        adminMenu.add(startStopSchedulerAction);
        adminMenu.add(freezeSchedulerAction);
        adminMenu.add(pauseSchedulerAction);
        adminMenu.add(resumeSchedulerAction);
        adminMenu.add(shutdownSchedulerAction);
        adminMenu.add(killSchedulerAction);

        // WINDOW
        MenuManager windowMenu = new MenuManager("&Window", IWorkbenchActionConstants.M_WINDOW);

        MenuManager viewMenu = new MenuManager("&Show View");
        viewMenu.add(viewList);

        windowMenu.add(viewMenu);
        windowMenu.add(resetPerspective);
        windowMenu.add(new Separator());

        windowMenu.add(new Separator());
        for (final Action action : JMXActionsManager.getInstance().getActions()) {
            windowMenu.add(action);
        }
        windowMenu.add(new Separator());
        windowMenu.add(preferenceAction);

        // HELP
        MenuManager helpMenu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);
        helpMenu.add(aboutAction);

        menuBar.add(schedulerMenu);
        menuBar.add(adminMenu);
        menuBar.add(windowMenu);
        menuBar.add(helpMenu);

        // forces accelerators to be loaded
    }

    @Override
    protected void fillCoolBar(ICoolBarManager coolBar) {
        IToolBarManager bar = new ToolBarManager(coolBar.getStyle());

        bar.add(connectAction);
        bar.add(disConnectAction);
        bar.add(new Separator());

        bar.add(submitList);

        bar.add(new Separator());
        bar.add(startStopSchedulerAction);
        bar.add(freezeSchedulerAction);
        bar.add(pauseSchedulerAction);
        bar.add(resumeSchedulerAction);
        bar.add(shutdownSchedulerAction);
        bar.add(killSchedulerAction);

        bar.add(new Separator());
        for (final Action action : JMXActionsManager.getInstance().getActions()) {
            bar.add(action);
        }

        coolBar.add(new ToolBarContributionItem(bar, IWorkbenchActionConstants.TOOLBAR_FILE));
    }
}
