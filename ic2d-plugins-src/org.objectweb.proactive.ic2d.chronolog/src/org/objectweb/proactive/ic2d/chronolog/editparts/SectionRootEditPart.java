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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.chronolog.editparts;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.editparts.SimpleRootEditPart;
import org.eclipse.swt.layout.GridData;
import org.objectweb.proactive.ic2d.chronolog.data.model.AbstractTypeModel;
import org.objectweb.proactive.ic2d.chronolog.data.model.NumberBasedTypeModel;
import org.objectweb.proactive.ic2d.chronolog.figures.ChartFigure;
import org.rrd4j.ConsolFun;
import org.rrd4j.graph.RrdGraphDef;


/**
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class SectionRootEditPart extends SimpleRootEditPart implements PropertyChangeListener, Runnable {

    /**
     * 
     */
    private final RrdGraphDef graphDef;
    /**
     * 
     */
    private final AbstractTypeModel dataElementModel;

    /**
     * @param dataProvider
     */
    public SectionRootEditPart(final AbstractTypeModel dataProvider) {
        this.dataElementModel = dataProvider;
        this.graphDef = this.buildGraphDef();
    }

    /**
     * @return
     */
    private RrdGraphDef buildGraphDef() {
        // Create Graph
        final RrdGraphDef newGraphDef = new RrdGraphDef();
        newGraphDef.datasource(this.dataElementModel.getDataProvider().getName(), this.dataElementModel
                .getRessourceData().getDataStore().getDataStoreName(), this.dataElementModel
                .getDataProvider().getName(), ConsolFun.AVERAGE);
        newGraphDef.setShowSignature(false);
        newGraphDef.area(this.dataElementModel.getDataProvider().getName(), Color.LIGHT_GRAY, null);
        newGraphDef.line(this.dataElementModel.getDataProvider().getName(), Color.BLACK, null, 1); // Warning !! fill AWT Color
        newGraphDef.setColor(RrdGraphDef.COLOR_BACK, Color.white);
        return newGraphDef;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editparts.SimpleRootEditPart#createFigure()
     */
    @Override
    protected IFigure createFigure() {
        final ChartFigure c = new ChartFigure(this.graphDef);

        final GridData gd = new GridData();
        gd.widthHint = c.getBounds().width;
        gd.heightHint = c.getBounds().height;
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;

        super.getViewer().getControl().setLayoutData(gd);
        // DO NOT CALL super.createFigure();
        return c;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(NumberBasedTypeModel.ELEMENT_CHANGED)) {
            getViewer().getControl().getDisplay().asyncExec(this);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        // this.minText.setText("min: " + dataElementModel.getMinValue());
        // this.maxText.setText("max: " + dataElementModel.getMaxValue());
        final ChartFigure f = (ChartFigure) this.getFigure();
        // Update timespan of the graph def
        this.graphDef.setTimeSpan(this.dataElementModel.getRessourceData().getDataStore().getLeftBoundTime(),
                this.dataElementModel.getRessourceData().getDataStore().getRightBoundTime());
        // recompute the image
        f.computeImageFromGraphDef(this.graphDef);
        f.repaint();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
     */
    @Override
    public void activate() {
        super.activate();
        dataElementModel.addPropertyChangeListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
     */
    @Override
    public void deactivate() {
        dataElementModel.removePropertyChangeListener(this);
        super.deactivate();
    }
}