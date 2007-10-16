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
package org.objectweb.proactive.ic2d.jmxmonitoring.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.objectweb.proactive.ic2d.jmxmonitoring.view.MonitoringView.MonitoringViewer;


public class MonitoringContextMenuProvider extends ContextMenuProvider {
    private List<IAction> actions;

    public MonitoringContextMenuProvider(EditPartViewer viewer) {
        super(viewer);
    }

    @Override
    public void buildContextMenu(IMenuManager manager) {
        GEFActionConstants.addStandardActionGroups(manager); // ???				

        IAction action;
        ActionRegistry registry = ((MonitoringViewer) this.getViewer()).getActionRegistry();

        MenuManager layoutMenu = new MenuManager("Layout");

        if (actions == null) {
            actions = new ArrayList<IAction>();
            // Monitor a new host			
            actions.add(registry.getAction(NewHostAction.NEW_HOST));

            // Set depth control
            actions.add(registry.getAction(SetDepthAction.SET_DEPTH));

            // Refresh
            actions.add(registry.getAction(RefreshAction.REFRESH));

            // Set time to refresh
            actions.add(registry.getAction(SetTTRAction.SET_TTR));

            // Look for new JVM
            actions.add(registry.getAction(RefreshHostAction.REFRESH_HOST));

            // Look for new Nodes
            actions.add(registry.getAction(RefreshJVMAction.REFRESH_JVM));

            // Look for new Active Objects
            actions.add(registry.getAction(RefreshNodeAction.REFRESH_NODE));

            // Stop monitoring this ...
            actions.add(registry.getAction(StopMonitoringAction.STOP_MONITORING));

            // Set update frequence...
            actions.add(registry.getAction(
                    SetUpdateFrequenceAction.SET_UPDATE_FREQUENCE));

            // Kill this VM
            actions.add(registry.getAction(KillVMAction.KILLVM));

            // Zoom In
            actions.add(registry.getAction(GEFActionConstants.ZOOM_IN));

            // Zoom Out
            actions.add(registry.getAction(GEFActionConstants.ZOOM_OUT));

            // Vertical Layout
            actions.add(registry.getAction(VerticalLayoutAction.VERTICAL_LAYOUT));

            // Horizontal Layout
            actions.add(registry.getAction(
                    HorizontalLayoutAction.HORIZONTAL_LAYOUT));
        }

        for (IAction a : this.actions) {
            if (a.isEnabled()) {
                manager.appendToGroup(GEFActionConstants.GROUP_REST, a);
            }
        }

        Iterator<IAction> it = registry.getActions();

        while (it.hasNext()) {
            action = it.next();

            if (!action.isEnabled() || this.actions.contains(action)) {
                continue;
            }

            ///////////////////////////////////////////////////////////////////////
            // HERE GOES ALL ACTIONS PROVIDED BY EXTENSIONS (from external plugins)
            // THEY ARE APPENDED IN A STANDARD WAY
            ///////////////////////////////////////////////////////////////////////
            manager.appendToGroup(GEFActionConstants.GROUP_REST, action);
        }

        // Once the layout menu is filled append it to the manager
        if (!layoutMenu.isEmpty()) {
            manager.appendToGroup(GEFActionConstants.GROUP_REST, layoutMenu);
        }
    }
}
