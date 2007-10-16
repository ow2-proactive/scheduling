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
package org.objectweb.proactive.ic2d.timit.editparts;

import java.util.List;

import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.timit.data.ChartContainerObject;
import org.objectweb.proactive.ic2d.timit.data.ChartObject;
import org.objectweb.proactive.ic2d.timit.views.TimItView;


/**
 * This class represents the controller object of the chart container.
 * @author vbodnart
 *
 */
public class ChartContainerEditPart extends AbstractGraphicalEditPart
    implements Runnable {
    protected TimItView timItView;

    /**
     * The constructor of this edit part
     * @param model
     */
    public ChartContainerEditPart(ChartContainerObject model,
        TimItView timItView) {
        this.setModel(model);
        model.setEp(this);
        this.timItView = timItView;
    }

    /**
     * Creates the associated figure object
     * @return The figure
     */
    @Override
    protected IFigure createFigure() {
        FreeformLayer layer = new FreeformLayer();
        ToolbarLayout layout = new ToolbarLayout(false);
        layout.setStretchMinorAxis(false);
        layout.setSpacing(0);
        layer.setLayoutManager(layout);
        return layer;
    }

    @Override
    protected void createEditPolicies() {
    }

    /**
     * Returns a list of children models
     * @return The list of children
     */
    @Override
    protected List<ChartObject> getModelChildren() {
        List<ChartObject> l = ((ChartContainerObject) getModel()).getChildrenList();

        // If the list is not empty the
        if (l.size() != 0) {
            this.timItView.getRefreshAllButton().setEnabled(true);
        }
        return l;
    }

    /**
     * Asynchronous refresh of this edit part.
     */
    public void asyncRefresh() {
        Display.getDefault().asyncExec(this);
    }

    /**
     * The run method performs the refresh
     */
    public void run() {
        refresh();
    }
}
