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
package org.objectweb.proactive.ic2d.timit.views;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.ic2d.timit.actions.timeline.ClearTimeLineAction;
import org.objectweb.proactive.ic2d.timit.actions.timeline.DecreaseSizeAction;
import org.objectweb.proactive.ic2d.timit.actions.timeline.ExpandSizeAction;
import org.objectweb.proactive.ic2d.timit.actions.timeline.FitSizeAction;
import org.objectweb.proactive.ic2d.timit.actions.timeline.IncreaseSizeAction;
import org.objectweb.proactive.ic2d.timit.data.timeline.TimeLineChartObject;
import org.objectweb.proactive.ic2d.timit.editparts.timeline.TimeLineChartEditPart;
import org.objectweb.proactive.ic2d.timit.editparts.timeline.TimeLineEditPartFactory;


public class TimeLineView extends ViewPart {
    public static final String ID = "org.objectweb.proactive.ic2d.timit.views.TimeLineView";
    protected ScrollingGraphicalViewer viewer;
    protected TimeLineChartObject container;
    protected IToolBarManager toolBarManager;

    public TimeLineView() {
        this.container = new TimeLineChartObject();
    }

    @Override
    public void createPartControl(Composite parent) {
        // Create graphical viewer
        this.viewer = new ScrollingGraphicalViewer();
        this.viewer.createControl(parent);
        // Configure the viewer
        this.viewer.getControl().setBackground(ColorConstants.white);
        ScalableFreeformRootEditPart root = new ScalableFreeformRootEditPart();
        this.viewer.setRootEditPart(root);

        // Activate the viewer as selection provider for Eclipse
        this.getSite().setSelectionProvider(this.viewer);

        // Get the toolbarManager and add all actions
        this.toolBarManager = getViewSite().getActionBars().getToolBarManager();

        // Create all actions for this view        
        toolBarManager.add(new ClearTimeLineAction());
        toolBarManager.add(new Separator());
        toolBarManager.add(new ZoomInAction(root.getZoomManager()));
        toolBarManager.add(new ZoomOutAction(root.getZoomManager()));
        toolBarManager.add(new Separator());
        toolBarManager.add(new IncreaseSizeAction());
        toolBarManager.add(new DecreaseSizeAction());
        toolBarManager.add(new FitSizeAction());
        toolBarManager.add(new ExpandSizeAction());

        // Initialize the viewer with input
        this.viewer.setEditPartFactory(new TimeLineEditPartFactory(this));

        /////////////////////////////////////////////////////
        // Add contents
        this.viewer.setContents(this.container);
    }

    @Override
    public void setFocus() {
        // TODO Auto-generated method stub
    }

    public TimeLineChartObject getContainer() {
        return container;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter == ZoomManager.class) {
            return this.viewer.getProperty(ZoomManager.class.toString());
        }
        return super.getAdapter(adapter);
    }

    /**
     * Call this method after the main edit part is created to
     * set the target of all actions in the toolbar manager
     * @param target The target of all actions in the toolbar
     */
    public void initActionsTarget(TimeLineChartEditPart target) {
        if (this.toolBarManager == null) {
            return;
        }

        // Add the target to all actions
        ((IncreaseSizeAction) ((org.eclipse.jface.action.ActionContributionItem) this.toolBarManager
                .find(IncreaseSizeAction.INCREASE_SIZE_ACTION)).getAction()).setTarget(target);
        ((DecreaseSizeAction) ((org.eclipse.jface.action.ActionContributionItem) this.toolBarManager
                .find(DecreaseSizeAction.DECREASE_SIZE_ACTION)).getAction()).setTarget(target);
        ((FitSizeAction) ((org.eclipse.jface.action.ActionContributionItem) this.toolBarManager
                .find(FitSizeAction.FIT_SIZE_ACTION)).getAction()).setTarget(target);
        ((ExpandSizeAction) ((org.eclipse.jface.action.ActionContributionItem) this.toolBarManager
                .find(ExpandSizeAction.EXPAND_TIMELINE_ACTION)).getAction()).setTarget(target);
        ((ClearTimeLineAction) ((org.eclipse.jface.action.ActionContributionItem) this.toolBarManager
                .find(ClearTimeLineAction.CLEAR_TIMELINE_ACTION)).getAction()).setTarget(target);
    }
}
