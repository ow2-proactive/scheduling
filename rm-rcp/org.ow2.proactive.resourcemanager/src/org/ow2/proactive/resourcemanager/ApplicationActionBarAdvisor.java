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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.ow2.proactive.resourcemanager.gui.data.RMStatusBarItem;


/**
 * @author ProActive TEam
 *
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

    // Actions - important to allocate these only in makeActions, and then use them
    // in the fill methods. This ensures that the actions aren't recreated
    // when fillActionBars is called with FILL_PROXY.
    private IWorkbenchAction exitAction;
    private IWorkbenchAction newWindowAction;
    private IWorkbenchAction saveAction;
    private IContributionItem perspectiveList;
    private IContributionItem viewList;

    /**
     * @param configurer the action bar's configurer
     */
    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
    }

    /**
     * @see org.eclipse.ui.application.ActionBarAdvisor#makeActions(org.eclipse.ui.IWorkbenchWindow)
     */
    @Override
    protected void makeActions(IWorkbenchWindow window) {
        exitAction = ActionFactory.QUIT.create(window);
        register(exitAction);

        newWindowAction = ActionFactory.OPEN_NEW_WINDOW.create(window);
        register(newWindowAction);

        saveAction = ActionFactory.SAVE.create(window);
        register(saveAction);

        perspectiveList = ContributionItemFactory.PERSPECTIVES_SHORTLIST.create(window);
        viewList = ContributionItemFactory.VIEWS_SHORTLIST.create(window);
    }

    /**
     * @see org.eclipse.ui.application.ActionBarAdvisor#fillMenuBar(org.eclipse.jface.action.IMenuManager)
     */
    @Override
    protected void fillMenuBar(IMenuManager menuBar) {
        MenuManager fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
        MenuManager windowMenu = new MenuManager("&Window", IWorkbenchActionConstants.M_WINDOW);

        MenuManager perspectiveMenu = new MenuManager("Open Perspective");
        MenuManager viewMenu = new MenuManager("Show View");

        menuBar.add(fileMenu);
        // Add a group marker indicating where action set menus will appear.
        menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menuBar.add(windowMenu);

        // File
        fileMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        fileMenu.add(new Separator());
        fileMenu.add(exitAction);

        // Window
        windowMenu.add(newWindowAction);
        fileMenu.add(new Separator());
        perspectiveMenu.add(perspectiveList);
        windowMenu.add(perspectiveMenu);
        viewMenu.add(viewList);
        windowMenu.add(viewMenu);
    }

    /**
     * @see org.eclipse.ui.application.ActionBarAdvisor#fillCoolBar(org.eclipse.jface.action.ICoolBarManager)
     */
    @Override
    protected void fillCoolBar(ICoolBarManager coolBar) {
        coolBar.add(new GroupMarker("group.file"));

        IToolBarManager fileToolBar = new ToolBarManager(coolBar.getStyle());
        fileToolBar.add(new Separator(IWorkbenchActionConstants.NEW_GROUP));
        fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
        fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.OPEN_EXT));
        fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.SAVE_GROUP));
        fileToolBar.add(saveAction);
        // Add to the cool bar manager
        coolBar.add(new ToolBarContributionItem(fileToolBar, IWorkbenchActionConstants.TOOLBAR_FILE));
    }

    @Override
    protected void fillStatusLine(IStatusLineManager statusLine) {
        RMStatusBarItem statusItem = RMStatusBarItem.getInstance();
        statusItem.setText("disconnected");
        statusLine.add(statusItem);
    }
}
