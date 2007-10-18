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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.timit.data.ChartObject;
import org.objectweb.proactive.ic2d.timit.data.TimerTreeHolder;
import org.objectweb.proactive.ic2d.timit.figures.ChartFigure;
import org.objectweb.proactive.ic2d.timit.views.TimItView;


public class ChartEditPart extends AbstractGraphicalEditPart
    implements MouseListener, Runnable {
    public static final int CHART_HEIGHT = 280;
    protected TimItView timItView;
    protected ChartObject chartObject;

    public ChartEditPart(final ChartObject chartObject) {
        super.setModel(chartObject);
        this.chartObject = chartObject;
        this.chartObject.setEp(this);
    }

    @Override
    protected final IFigure createFigure() {
        ChartFigure chartFigure = new ChartFigure(this.chartObject.provideChart());
        chartFigure.setPreferredSize(this.getViewer().getControl().getBounds().width -
            20, CHART_HEIGHT);
        chartFigure.setMinimumSize(new Dimension(300, CHART_HEIGHT));
        chartFigure.addMouseListener(this);
        return chartFigure;
    }

    @Override
    protected final void fireSelectionChanged() {
        super.fireSelectionChanged();
        if (this.timItView == null) {
            this.timItView = ((ChartContainerEditPart) this.getParent()).timItView;
        }

        ChartObject model = (ChartObject) this.getModel();
        switch (this.getSelected()) {
        case ChartEditPart.SELECTED_PRIMARY:
            this.timItView.getRefreshSelectedButton().setEnabled(true);
            // Update the text of the timerLevelButton
            this.timItView.getTimerLevelButton()
                          .setText("Switch to " +
                model.getInversedTimerLevel());
            // Enable timerLevelButton
            this.timItView.getTimerLevelButton().setEnabled(true);
            this.timItView.getShowInTreeViewAction().setTarget(model);
            // Delegate the selection to the figure
            ((ChartFigure) this.getFigure()).setSelected();
            // If Tree View is used then set selected the tree view of the this
            // chart
            if (TimerTreeHolder.getInstance() != null) {
                TimerTreeHolder.getInstance().provideChartObject(model, true);
            }
            break;
        case ChartEditPart.SELECTED_NONE:
            ((ChartFigure) this.getFigure()).setUnselected();
        }
    }

    public final void handleSelection(final boolean reveal) {
        if (reveal) {
            this.getViewer().reveal(this);
        }
        if (this.getViewer().getSelectedEditParts().size() != 0) {
            // Only one object can be selected at time
            ChartEditPart lastSelected = (ChartEditPart) this.getViewer()
                                                             .getSelectedEditParts()
                                                             .get(0);
            // deselect it
            lastSelected.getViewer().deselect(lastSelected);
        }

        this.getViewer().select(this);
    }

    public final void asyncRefresh() {
        Display.getDefault().asyncExec(this);
    }

    public final void run() {
        ChartFigure figure = (ChartFigure) getFigure();
        figure.setChart(this.chartObject.provideChart());
        figure.setPreferredSize(this.getViewer().getControl().getBounds().width -
            20, CHART_HEIGHT);
        refresh();
        figure.repaint(); // TODO : check if refresh() calls the
                          // figure.repaint() method
    }

    @Override
    protected final void createEditPolicies() {
    }

    // //////////////////////////////
    // MOUSE LISTENER IMPLEMENTATION
    // //////////////////////////////
    public final void mousePressed(final MouseEvent arg0) {
        this.handleSelection(false);
    }

    public final void mouseDoubleClicked(final MouseEvent arg0) {
    }

    public final void mouseReleased(final MouseEvent arg0) {
    }
}
