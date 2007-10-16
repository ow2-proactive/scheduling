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
package org.objectweb.proactive.ic2d.jmxmonitoring.editpart;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.listener.WorldListener;
import org.objectweb.proactive.ic2d.jmxmonitoring.view.MonitoringView;


public class WorldEditPart extends AbstractMonitoringEditPart {
    private FreeformLayer layer;
    private MonitoringView monitoringView;
    private WorldObject castedModel;
    private IFigure castedFigure;
    private Set<IFigure> figuresToUpdate;
    private Set<GraphicalCommunication> communicationsToDraw;
    private boolean shouldRepaint = true;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public WorldEditPart(WorldObject model, MonitoringView monitoringView) {
        super(model);
        this.monitoringView = monitoringView;

        this.figuresToUpdate = Collections.synchronizedSet(new HashSet<IFigure>());
        this.communicationsToDraw = Collections.synchronizedSet(new HashSet<GraphicalCommunication>());

        new Thread() {
                @Override
                public void run() {
                    while (shouldRepaint) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        Control control = getViewer()
                                              .getControl();
                        if (control == null) {
                            return;
                        }
                        control.getDisplay().asyncExec(new Runnable() {
                                public void run() {
                                    for (GraphicalCommunication communication : communicationsToDraw) {
                                        communication.draw();
                                    }
                                    communicationsToDraw.clear();

                                    /*for (IFigure figure : figuresToUpdate) {
                                            figure.repaint();
                                    }
                                    figuresToUpdate.clear();*/
                                    getFigure().repaint();
                                }
                            });
                    }
                }
            }.start();
    }

    //
    // -- PUBLICS METHODS -----------------------------------------------
    //
    @Override
    public void addGraphicalCommunication(GraphicalCommunication communication) {
        communicationsToDraw.add(communication);
    }

    @Override
    public void addFigureToUpdtate(IFigure figure) {
        figuresToUpdate.add(figure);
    }

    /**
     * Convert the result of EditPart.getModel()
     * to WorldObject (the real type of the model).
     * @return the casted model
     */
    @SuppressWarnings("unchecked")
    @Override
    public WorldObject getCastedModel() {
        if (castedModel == null) {
            castedModel = (WorldObject) getModel();
        }
        return castedModel;
    }

    @SuppressWarnings("unchecked")
    @Override
    public IFigure getCastedFigure() {
        if (castedFigure == null) {
            castedFigure = getFigure();
        }
        return castedFigure;
    }

    @Override
    public IFigure getContentPane() {
        return layer;
    }

    @Override
    public MonitoringView getMonitoringView() {
        return this.monitoringView;
    }

    @Override
    public WorldEditPart getWorldEditPart() {
        return this;
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    /**
     * Returns a new view associated
     * with the type of model object the
     * EditPart is associated with. So here, it returns a new FreeFormLayer.
     * @return a new FreeFormLayer view associated with the WorldObject model.
     */
    @Override
    protected IFigure createFigure() {
        layer = new FreeformLayer();
        FlowLayout layout =  /*new FlowLayout()*/new MonitoringLayout();
        layout.setMajorAlignment(FlowLayout.ALIGN_CENTER);
        layout.setMajorSpacing(50);
        layout.setMinorSpacing(50);
        layer.setLayoutManager(layout);

        WorldListener listener = new WorldListener(monitoringView);
        layer.addMouseListener(listener);
        layer.addMouseMotionListener(listener);

        return layer;
    }

    /**
     * Returns a List containing the children model objects.
     * @return the List of children
     */
    @Override
    protected List<AbstractData> getModelChildren() {
        return getCastedModel().getMonitoredChildrenAsList();
    }

    /**
     * Creates the initial EditPolicies and/or reserves slots for dynamic ones.
     */
    @Override
    protected void createEditPolicies() { /* Do nothing */
    }

    //
    // -- INNER CLASS -----------------------------------------------
    //
    private class MonitoringLayout extends FlowLayout {
        @Override
        protected void setBoundsOfChild(IFigure parent, IFigure child,
            Rectangle bounds) {
            parent.getClientArea(Rectangle.SINGLETON);
            bounds.translate(Rectangle.SINGLETON.x, Rectangle.SINGLETON.y +
                100);
            child.setBounds(bounds);
        }
    }
}
