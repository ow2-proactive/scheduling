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
package org.objectweb.proactive.ic2d.timit.editparts.timeline;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.timit.data.timeline.SequenceObject;
import org.objectweb.proactive.ic2d.timit.data.timeline.TimeIntervalManager;
import org.objectweb.proactive.ic2d.timit.data.timeline.TimeLineChartObject;
import org.objectweb.proactive.ic2d.timit.figures.timeline.SequenceFigure;
import org.objectweb.proactive.ic2d.timit.figures.timeline.TimeAxisFigure;


public class TimeLineChartEditPart extends AbstractGraphicalEditPart
    implements Runnable {
    private CustomLayer panel;
    private Panel namesPanel;
    private TimeAxisFigure timeAxisFigure;
    private TimeIntervalManager timeIntervalManager;

    public TimeLineChartEditPart(TimeLineChartObject model) {
        model.setEp(this);
        setModel(model);
        this.timeIntervalManager = model.getTimeIntervalManager();
    }

    @Override
    public EditPolicy getEditPolicy(Object arg0) {
        return super.getEditPolicy(arg0);
    }

    /**
     * Returns a list of children models
     *
     * @return The list of children
     */
    @Override
    protected List<SequenceObject> getModelChildren() {
        List<SequenceObject> l = ((TimeLineChartObject) getModel()).getChildrenList();
        return l;
    }

    @Override
    protected IFigure createFigure() {
        // Attach a horizontal range model listener
        ((FigureCanvas) this.getViewer().getControl()).getViewport()
         .getHorizontalRangeModel()
         .addPropertyChangeListener(new CustomHorizontalRangeModelPropertyChangeListener());
        // Create the whole layer
        this.panel = new CustomLayer();
        return panel;
    }

    @Override
    protected void createEditPolicies() {
    }

    /**
     * Clears the model and removes all sequence figures.
     */
    public final void removeAndClearAll() {
        // Clear model
        ((TimeLineChartObject) getModel()).clearChildrenList();

        // Clear editparts		
        // In order to avoid concurrent exception create a temporary list to be filled with parts to delete
        final List<EditPart> toDelete = new ArrayList<EditPart>(this.getChildren()
                                                                    .size());

        // Deactivate selected parts
        for (final Object o : this.getChildren()) {
            EditPart e = (EditPart) o;
            e.deactivate();
            toDelete.add(e);
        }

        // Remove them from the current root editpart
        for (final EditPart e : toDelete) {
            this.removeChild(e);
        }

        // Clear the interval manager
        this.timeIntervalManager.init(0, 0);

        // Refresh
        this.asyncRefresh(false);
    }

    /**
     * Asynchronous refresh.
     * @param dirtyMode If true all figures will recompute before paint
     */
    public final void asyncRefresh(boolean dirtyMode) {
        if (dirtyMode) {
            this.setDirtyMode();
        }
        Display.getDefault().asyncExec(this);
    }

    
    public final void run() {
        refresh();
    }

    public final void increaseWidth() {
        if (this.timeIntervalManager.getTimeStep() < TimeIntervalManager.MINIMAL_TIMESTAMP_VALUE_IN_MICROSECONDS) {
            return;
        }
        setDirtyMode();
        Rectangle r = this.panel.getBounds();
        r.width += 20;
        this.panel.setFreeformBounds(r);
        this.panel.revalidate();
    }

    public final void decreaseWidth() {
        Rectangle r = this.panel.getBounds();
        if (r.width <= this.getViewer().getControl().getBounds().width) {
            return;
        }
        setDirtyMode();
        r.width -= 20;
        this.panel.setFreeformBounds(r);
        this.panel.revalidate();
    }

    public final void fitWidth() {
        Rectangle r = this.panel.getBounds();
        setDirtyMode();
        r.width = this.getViewer().getControl().getBounds().width;
        this.panel.setFreeformBounds(r);
        this.panel.revalidate();
    }

    public final void expandWidth() {
        int widthToMatch = Math.round((this.timeIntervalManager.getTimeInterval() / TimeIntervalManager.MINIMAL_TIMESTAMP_VALUE_IN_MICROSECONDS) * TimeAxisFigure.referenceXSize);
        Rectangle r = this.panel.getBounds();
        setDirtyMode();
        r.width = widthToMatch;
        this.panel.setFreeformBounds(r);
        this.panel.revalidate();
    }

    private final void setDirtyMode() {
        for (final Object o : this.timeAxisFigure.getChildren()) {
            SequenceFigure f = (SequenceFigure) o;
            f.setBDirty(true);
        }
    }

    public TimeIntervalManager getTimeIntervalManager() {
        return timeIntervalManager;
    }

    public void setTimeIntervalManager(TimeIntervalManager timeIntervalManager) {
        this.timeIntervalManager = timeIntervalManager;
    }

    public CustomLayer getLayer() {
        return this.panel;
    }

    public class CustomLayer extends FreeformLayer {
        public CustomLayer() {
            ToolbarLayout layout = new ToolbarLayout();
            layout.setStretchMinorAxis(true);
            layout.setSpacing(20);
            this.setLayoutManager(layout);

            // CONTAINER PANEL
            Panel containerPanel = new Panel();
            containerPanel.setBorder(new LineBorder(
                    new Color(Display.getCurrent(), 225, 225, 225)));
            this.add(containerPanel);
            BorderLayout containerPanelLayout = new BorderLayout();
            containerPanelLayout.setHorizontalSpacing(10);
            containerPanel.setLayoutManager(containerPanelLayout);

            // INSIDE CONTAINER PANEL
            // ADD PANEL FOR NAMES
            namesPanel = new Panel();
            containerPanel.add(namesPanel, BorderLayout.LEFT);

            ToolbarLayout namesPanelLayout = new ToolbarLayout(false);
            // FlowLayout namesPanelLayout = new FlowLayout(false);
            namesPanel.setLayoutManager(namesPanelLayout);
            namesPanelLayout.setStretchMinorAxis(true);
            namesPanelLayout.setSpacing(10);

            Label dummyLabel = new Label("Empty");
            dummyLabel.setBorder(new LineBorder());
            dummyLabel.setVisible(false);
            namesPanel.add(dummyLabel, ToolbarLayout.ALIGN_CENTER);

            // ////////////////
            // INSIDE CONTAINER PANEL
            // ADD PANEL FOR SERIES
            timeAxisFigure = new TimeAxisFigure(timeIntervalManager);
            containerPanel.add(timeAxisFigure, BorderLayout.CENTER);

            ToolbarLayout seriesPanelLayout = new ToolbarLayout(false);
            // FlowLayout seriesPanelLayout = new FlowLayout(false);
            timeAxisFigure.setLayoutManager(seriesPanelLayout);
            seriesPanelLayout.setStretchMinorAxis(true);
            seriesPanelLayout.setSpacing(10);

            SequenceFigure dummyFig = new SequenceFigure();
            // dummyFig.setBorder(new LineBorder());
            dummyFig.setVisible(false);
            timeAxisFigure.add(dummyFig, ToolbarLayout.ALIGN_CENTER);
        }

        @Override
        public void add(IFigure arg0, Object arg1, int arg2) {
            if (arg0.getClass() == SequenceFigure.class) {
                SequenceFigure sFig = (SequenceFigure) arg0;
                // Add label to the names panel
                namesPanel.add(sFig.getLabel(), ToolbarLayout.ALIGN_CENTER);
                timeAxisFigure.add(arg0, ToolbarLayout.ALIGN_CENTER);
            } else {
                super.add(arg0, arg1, arg2);
            }
        }

        @Override
        public void remove(IFigure arg0) {
            if (arg0.getClass() == SequenceFigure.class) {
                SequenceFigure sFig = (SequenceFigure) arg0;
                // Add label to the names panel
                namesPanel.remove(sFig.getLabel());
                timeAxisFigure.remove(arg0);
            } else {
                super.remove(arg0);
            }
        }
    }

    public class CustomHorizontalRangeModelPropertyChangeListener
        implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            TimeLineChartEditPart.this.timeAxisFigure.setBDirty(true);
            TimeLineChartEditPart.this.setDirtyMode();
        }
    }
}
