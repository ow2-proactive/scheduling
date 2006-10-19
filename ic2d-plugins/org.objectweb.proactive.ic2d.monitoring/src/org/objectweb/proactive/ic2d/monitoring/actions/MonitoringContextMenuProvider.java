/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.monitoring.actions;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.objectweb.proactive.ic2d.monitoring.views.MonitoringView.MonitoringViewer;

public class MonitoringContextMenuProvider extends ContextMenuProvider {
	
	public MonitoringContextMenuProvider(EditPartViewer viewer) {
		super(viewer);
	}
	

	@Override
	public void buildContextMenu(IMenuManager manager) {
		GEFActionConstants.addStandardActionGroups(manager); // ???
		
		IAction action;
		ActionRegistry registry = ((MonitoringViewer)this.getViewer()).getActionRegistry();
		
		// Monitor a new host
		action = registry.getAction(NewHostAction.NEW_HOST);
		if (action.isEnabled())
			manager.appendToGroup(GEFActionConstants.GROUP_REST, action);
		
		// Set depth control
		action = registry.getAction(SetDepthAction.SET_DEPTH);
		if (action.isEnabled())
			manager.appendToGroup(GEFActionConstants.GROUP_REST, action);
		
		// Refresh
		action = registry.getAction(RefreshAction.REFRESH);
		if (action.isEnabled())
			manager.appendToGroup(GEFActionConstants.GROUP_REST, action);
		
		// Set time to refresh
		action = registry.getAction(SetTTRAction.SET_TTR);
		if (action.isEnabled())
			manager.appendToGroup(GEFActionConstants.GROUP_REST, action);
		
		// Look for new JVM
		action = registry.getAction(RefreshHostAction.REFRESH_HOST);
		if (action.isEnabled())
			manager.appendToGroup(GEFActionConstants.GROUP_REST, action);
		
		// Look for new Nodes
		action = registry.getAction(RefreshJVMAction.REFRESH_JVM);
		if (action.isEnabled())
			manager.appendToGroup(GEFActionConstants.GROUP_REST, action);
		
		// Look for new Active Objects
		action = registry.getAction(RefreshNodeAction.REFRESH_NODE);
		if (action.isEnabled())
			manager.appendToGroup(GEFActionConstants.GROUP_REST, action);
		
		// Stop monitoring this ...
		action = registry.getAction(StopMonitoringAction.STOP_MONITORING);
		if (action.isEnabled())
			manager.appendToGroup(GEFActionConstants.GROUP_REST, action);
		
		// Set update frequence...
		action = registry.getAction(SetUpdateFrequenceAction.SET_UPDATE_FREQUENCE);
		if (action.isEnabled())
			manager.appendToGroup(GEFActionConstants.GROUP_REST, action);
		
		MenuManager layoutMenu = new MenuManager("Layout");
		
		// Vertical Layout
		action = registry.getAction(VerticalLayoutAction.VERTICAL_LAYOUT);
		if(action.isEnabled())
			layoutMenu.add(action);
		
		// Horizontal Layout
		action = registry.getAction(HorizontalLayoutAction.HORIZONTAL_LAYOUT);
		if(action.isEnabled())
			layoutMenu.add(action);
		
		if (!layoutMenu.isEmpty())
			manager.appendToGroup(GEFActionConstants.GROUP_REST, layoutMenu);
	}

}
