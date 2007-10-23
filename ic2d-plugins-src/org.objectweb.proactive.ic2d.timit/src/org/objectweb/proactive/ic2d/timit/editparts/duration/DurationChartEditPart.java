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
package org.objectweb.proactive.ic2d.timit.editparts.duration;

import java.util.List;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.timit.data.duration.DurationChartObject;
import org.objectweb.proactive.ic2d.timit.data.duration.SequenceObject;
import org.objectweb.proactive.ic2d.timit.figures.duration.SequenceFigure;
import org.objectweb.proactive.ic2d.timit.figures.duration.TimeAxisFigure;
import org.objectweb.proactive.ic2d.timit.figures.duration.TimeIntervalManager;


public class DurationChartEditPart extends AbstractGraphicalEditPart
    implements Runnable {
    private CustomLayer panel;
    private Panel namesPanel;
    private TimeAxisFigure timeAxisFigure;
    private TimeIntervalManager timeIntervalManager;

    public DurationChartEditPart(DurationChartObject model) {
        model.setEp(this);
        setModel(model);
        // Create the time interval manager
        this.timeIntervalManager = new TimeIntervalManager();
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
        List<SequenceObject> l = ((DurationChartObject) getModel()).getChildrenList();
        return l;
    }

    @Override
    protected IFigure createFigure() {
        this.panel = new CustomLayer();

        return panel;
    }

    @Override
    protected void createEditPolicies() {
    }

    public final void asyncRefresh(long longuestTime) {
        // Init the time interval manager
        this.timeIntervalManager.init(0, longuestTime);
        this.asyncRefresh();
    }

    private final void asyncRefresh() {
        this.setDirtyMode();
        Display.getDefault().asyncExec(this);
    }

    @Override
    public final void run() {
        refresh();
        timeAxisFigure.repaint(); // TODO : check if refresh() calls the
                                  // figure.repaint() method
    }

    public void increaseWidth() {
        if (this.timeIntervalManager.getTimeStep() < TimeIntervalManager.MINIMAL_TIMESTAMP_VALUE_IN_MICROSECONDS) {
            return;
        }
        setDirtyMode();
        Rectangle r = this.panel.getBounds();
        r.width += 20;
        this.panel.setFreeformBounds(r);
        this.panel.revalidate();
    }

    public void decreaseWidth() {
        Rectangle r = this.panel.getBounds();
        if (r.width <= this.getViewer().getControl().getBounds().width) {
            return;
        }
        setDirtyMode();
        r.width -= 20;
        this.panel.setFreeformBounds(r);
        this.panel.revalidate();
    }

    public void fitWidth() {
        Rectangle r = this.panel.getBounds();
        setDirtyMode();
        r.width = this.getViewer().getControl().getBounds().width;
        this.panel.setFreeformBounds(r);
        this.panel.revalidate();
    }

    public void expandWidth() {
        int widthToMatch = Math.round((this.timeIntervalManager.getTimeInterval() / TimeIntervalManager.MINIMAL_TIMESTAMP_VALUE_IN_MICROSECONDS) * TimeAxisFigure.referenceXSize);
        Rectangle r = this.panel.getBounds();
        setDirtyMode();
        r.width = widthToMatch;
        this.panel.setFreeformBounds(r);
        this.panel.revalidate();
    }

    private void setDirtyMode() {
        for (Object o : this.timeAxisFigure.getChildren()) {
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

            // // ADD HEADER
            // HeaderFigure labelFigure = new HeaderFigure("Duration Chart");
            // labelFigure.setBorder(new LineBorder());
            // this.add(labelFigure);

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
                sFig.setBorder(new LineBorder());
                // Add label to the names panel
                namesPanel.add(sFig.getLabel(), ToolbarLayout.ALIGN_CENTER);
                timeAxisFigure.add(arg0, ToolbarLayout.ALIGN_CENTER);
            } else {
                super.add(arg0, arg1, arg2);
            }
        }
    }

    public class HeaderFigure extends Label {
        public HeaderFigure(String s) {
            super(s);
        }

        @Override
        protected Point getTextLocation() {
            Point p = super.getTextLocation();
            int viewerWidth = getViewer().getControl().getBounds().width;
            if (viewerWidth > 20) {
                p.x = (viewerWidth / 2) - (super.getTextSize().width / 2);
            }
            return p;
        }

        @Override
        public Rectangle getBounds() {
            Rectangle r = super.getBounds();
            int viewerWidth = getViewer().getControl().getBounds().width;
            if (viewerWidth > 20) {
                r.width = getViewer().getControl().getBounds().width - 20;
            }
            return r;
        }
    }
}
