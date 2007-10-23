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

import java.util.ArrayList;
import java.util.List;

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
import org.objectweb.proactive.ic2d.timit.actions.DecreaseSizeAction;
import org.objectweb.proactive.ic2d.timit.actions.ExpandSizeAction;
import org.objectweb.proactive.ic2d.timit.actions.FitSizeAction;
import org.objectweb.proactive.ic2d.timit.actions.IncreaseSizeAction;
import org.objectweb.proactive.ic2d.timit.data.duration.DurationChartObject;
import org.objectweb.proactive.ic2d.timit.editparts.duration.DurationEditPartFactory;


public class TimeLineView extends ViewPart {
    public static final String ID = "org.objectweb.proactive.ic2d.timit.views.TimeLineView";
    protected ScrollingGraphicalViewer viewer;
    protected DurationChartObject container;
    protected IncreaseSizeAction inc;
    protected DecreaseSizeAction dec;
    protected FitSizeAction fit;
    protected ExpandSizeAction exp;

    public IncreaseSizeAction getInc() {
        return inc;
    }

    public DecreaseSizeAction getDec() {
        return dec;
    }

    public TimeLineView() {
        this.container = new DurationChartObject();
    }

    @Override
    public void createPartControl(Composite parent) {
        // create graphical viewer
        this.viewer = new ScrollingGraphicalViewer();
        this.viewer.createControl(parent);
        // configure the viewer
        this.viewer.getControl().setBackground(ColorConstants.white);
        ScalableFreeformRootEditPart root = new ScalableFreeformRootEditPart();

        //System.out.println("DurationStateView.createPartControl() -------> " + root.get);
        //		System.out.println("DurationStateView.createPartControl() ------> " + );
        //		root.getFigure()addEditPartListener(new Test implements EditPartListener {
        //			
        //		}):
        //		List zoomLevels = new ArrayList(3);
        //		zoomLevels.add(ZoomManager.FIT_ALL);
        //		zoomLevels.add(ZoomManager.FIT_WIDTH);
        //		zoomLevels.add(ZoomManager.FIT_HEIGHT);
        //		root.getZoomManager().setZoomLevelContributions(zoomLevels);
        this.viewer.setRootEditPart(root);

        // activate the viewer as selection provider for Eclipse
        this.getSite().setSelectionProvider(this.viewer);

        IToolBarManager toolBarManager = getViewSite()
                                             .getActionBars().getToolBarManager();

        toolBarManager.add(new ZoomInAction(root.getZoomManager()));
        toolBarManager.add(new ZoomOutAction(root.getZoomManager())); //omComboContributionItem(this, zoomStrings));

        toolBarManager.add(new Separator());

        this.inc = new IncreaseSizeAction();
        toolBarManager.add(inc);
        this.dec = new DecreaseSizeAction();
        toolBarManager.add(dec);
        this.fit = new FitSizeAction();
        toolBarManager.add(fit);
        this.exp = new ExpandSizeAction();
        toolBarManager.add(exp);

        // initialize the viewer with input
        this.viewer.setEditPartFactory(new DurationEditPartFactory(this));

        /////////////////////////////////////////////////////
        // Add contents
        this.viewer.setContents(this.container);
    }

    @Override
    public void setFocus() {
        // TODO Auto-generated method stub
    }

    public DurationChartObject getContainer() {
        return container;
    }

    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == ZoomManager.class) {
            return this.viewer.getProperty(ZoomManager.class.toString());
        }
        return super.getAdapter(adapter);
    }

    public FitSizeAction getFit() {
        return fit;
    }

    public ExpandSizeAction getExp() {
        return exp;
    }
}
