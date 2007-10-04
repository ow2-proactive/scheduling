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
package org.objectweb.proactive.infrastructuremanager;

import org.eclipse.jface.action.GroupMarker;
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


public class ApplicationActionBarAdvisor extends ActionBarAdvisor {
    // Actions - important to allocate these only in makeActions, and then use them
    // in the fill methods.  This ensures that the actions aren't recreated
    // when fillActionBars is called with FILL_PROXY.
    private IWorkbenchAction exitAction;
    private IWorkbenchAction newWindowAction;
    private IWorkbenchAction aboutAction;
    private IWorkbenchAction saveAction;
    private IContributionItem perspectiveList;
    private IContributionItem viewList;
    private IWorkbenchWindow window;

    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
    }

    protected void makeActions(IWorkbenchWindow window) {
        // Creates the actions and registers them.
        // Registering is needed to ensure that key bindings work.
        // The corresponding commands keybindings are defined in the plugin.xml file.
        // Registering also provides automatic disposal of the actions when
        // the window is closed.
        this.window = window;

        exitAction = ActionFactory.QUIT.create(window);
        register(exitAction);

        newWindowAction = ActionFactory.OPEN_NEW_WINDOW.create(window);
        register(newWindowAction);

        aboutAction = ActionFactory.ABOUT.create(window);
        register(aboutAction);

        saveAction = ActionFactory.SAVE.create(window);
        register(saveAction);

        perspectiveList = ContributionItemFactory.PERSPECTIVES_SHORTLIST.create(window);
        viewList = ContributionItemFactory.VIEWS_SHORTLIST.create(window);
    }

    protected void fillMenuBar(IMenuManager menuBar) {
        MenuManager fileMenu = new MenuManager("&File",
                IWorkbenchActionConstants.M_FILE);
        MenuManager windowMenu = new MenuManager("&Window",
                IWorkbenchActionConstants.M_WINDOW);
        MenuManager helpMenu = new MenuManager("&Help",
                IWorkbenchActionConstants.M_HELP);

        MenuManager perspectiveMenu = new MenuManager("Open Perspective");
        MenuManager viewMenu = new MenuManager("Show View");

        menuBar.add(fileMenu);
        // Add a group marker indicating where action set menus will appear.
        menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menuBar.add(windowMenu);
        menuBar.add(helpMenu);

        // File
        fileMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        fileMenu.add(new Separator());
        fileMenu.add(ActionFactory.SAVE.create(window));
        fileMenu.add(new Separator());
        fileMenu.add(exitAction);

        // Window
        windowMenu.add(newWindowAction);
        fileMenu.add(new Separator());
        perspectiveMenu.add(perspectiveList);
        windowMenu.add(perspectiveMenu);
        viewMenu.add(viewList);
        windowMenu.add(viewMenu);

        // Help
        helpMenu.add(aboutAction);
    }

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
        coolBar.add(new ToolBarContributionItem(fileToolBar,
                IWorkbenchActionConstants.TOOLBAR_FILE));
    }
}
