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

import java.util.List;

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
    public static final boolean DEFAULT_DISPLAY_TOPOLOGY = true;
    private static boolean displayTopology = DEFAULT_DISPLAY_TOPOLOGY;
    /*
     * A repaint will be done each TIME_TO_REFRESH mls 
     */
    private static int TIME_TO_REPAINT = 200;

    /*
     * refreshMode=FULL -> a refresh is asked for each event 
     * refreshMode=OPTIMAL -> a refresh is done by the WorldEditPart each TIME_TO_REPAINT mls
     */
    public enum RefreshMode {
        FULL, OPTIMAL
    };

    private RefreshMode mode = RefreshMode.OPTIMAL;

    //private final Set<IFigure> figuresToUpdate;
    private final java.util.Map<Integer, GraphicalCommunication> communicationsToDraw;
    private boolean shouldRepaint = true;
    private final Runnable drawRunnable = new Runnable() {
        public final void run() {
            for (final GraphicalCommunication communication : communicationsToDraw.values()) {
                communication.draw();
            }
            communicationsToDraw.clear();
            getFigure().repaint();
        }
    };

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public WorldEditPart(WorldObject model, MonitoringView monitoringView) {
        super(model);
        this.monitoringView = monitoringView;

        //this.figuresToUpdate = Collections.synchronizedSet(new HashSet<IFigure>());
        this.communicationsToDraw = new java.util.concurrent.ConcurrentHashMap<Integer, GraphicalCommunication>(); //Collections.synchronizedSet(new HashSet<GraphicalCommunication>());

        new Thread() {
            @Override
            public final void run() {
                try {
                    Control control;
                    while (shouldRepaint) {
                        Thread.sleep(TIME_TO_REPAINT);

                        control = getViewer().getControl();
                        if (control != null) {
                            control.getDisplay().syncExec(WorldEditPart.this.drawRunnable);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //
    // -- PUBLICS METHODS -----------------------------------------------
    //
    @Override
    public final void addGraphicalCommunication(final GraphicalCommunication communication) {
        if (!communicationsToDraw.containsKey(communication.hashCode())) {
            this.communicationsToDraw.put(communication.hashCode(), communication);
        }
    }

    //    @Override
    //    public void addFigureToUpdtate(IFigure figure) {
    //        //figuresToUpdate.add(figure);
    //    }

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

    public void clearCommunicationsAndRepaintFigure() {
        // Clear the communications
        this.communicationsToDraw.clear();
        final IFigure f = getFigure();

        // Since the edit part has changed first force
        // calling threads to execute the repaint one-by-one
        // Each calling thread will execute the repaint at a reasonable opportunity
        synchronized (f) {
            getViewer().getControl().getDisplay().syncExec(new Runnable() {
                public final void run() {
                    f.repaint();
                }
            });
        }
    }

    /**
     * Clears all communications to draw
     */
    public void clearCommunications() {
        this.communicationsToDraw.clear();
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
        FlowLayout layout = /*new FlowLayout()*/new MonitoringLayout();
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
        protected void setBoundsOfChild(IFigure parent, IFigure child, Rectangle bounds) {
            parent.getClientArea(Rectangle.SINGLETON);
            bounds.translate(Rectangle.SINGLETON.x, Rectangle.SINGLETON.y + 100);
            child.setBounds(bounds);
        }
    }

    public RefreshMode getRefreshMode() {
        return mode;
    }

    public void setRefreshMode(RefreshMode m) {
        mode = m;
    }

    /**
     * To choose if you want to show the topology.
     */
    public static void setDisplayTopology(boolean show) {
        displayTopology = show;
    }

    /**
     * Indicates if the topology must be displayed.
     */
    public static boolean displayTopology() {
        return displayTopology;
    }

}
