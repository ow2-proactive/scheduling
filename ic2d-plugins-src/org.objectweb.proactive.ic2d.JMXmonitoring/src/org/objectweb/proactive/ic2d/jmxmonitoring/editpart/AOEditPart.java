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

import java.util.Iterator;
import java.util.List;
import java.util.Observable;

import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.FreeformLayeredPane;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.editpart.WorldEditPart.RefreshMode;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.AOFigure;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.NodeFigure;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.listener.AOListener;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotification;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotificationTag;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.State;


public class AOEditPart extends AbstractMonitoringEditPart {

    /**
     * An empty list used to feed GEF for all instances of AO edit parts
     * This list is empty beacause AO model has no children !
     */
    public static final List<AbstractData> emptyList = new java.util.ArrayList<AbstractData>(0);

    /**
     * The default color of an arrow used in getArrowColor() method
     */
    private static final Color DEFAULT_ARROW_COLOR = new Color(Display.getCurrent(), 108, 108, 116);
    private Integer length;

    /*private Set<IFigure> figuresToUpdate;
    private Set<GraphicalCommunication> communicationsToDraw;

    private boolean shouldRepaint = true;*/
    private AOFigure castedFigure;
    private ActiveObject castedModel;
    private NodeEditPart castedParentEditPart;
    private NodeFigure castedParentFigure;
    private IFigure globalPanel;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public AOEditPart(ActiveObject model) {
        super(model);
    }

    //
    // -- PUBLICS METHODS -----------------------------------------------
    //

    /**
     * This method is called whenever the observed object is changed. It calls
     * the method <code>refresh()</code>.
     *
     * @param o
     *            the observable object (instance of AbstractDataObject).
     * @param arg
     *            an argument passed to the notifyObservers method.
     */
    @Override
    public void update(Observable o, Object arg) {
        if ((arg != null) && (arg instanceof MVCNotification)) {
            final MVCNotificationTag mvcNotif = ((MVCNotification) arg).getMVCNotification();
            final Object notificationdata = ((MVCNotification) arg).getData();

            // State updated
            switch (mvcNotif) {
                case STATE_CHANGED: {
                    final State state = (State) notificationdata;
                    if (state == State.NOT_MONITORED) {
                        //       getCastedModel().removeAllConnections();
                        // getCastedFigure().removeConnections(getGlobalPanel());

                        // If this clear is too brutal just filter on this (source||tagret) on clear
                        //                        if (getWorldEditPart().getModel() == RefreshMode.FULL)
                        //                            getWorldEditPart().clearCommunicationsAndRepaintFigure();

                    } else {
                        // COMMON REFRESH IE THE AO IS MONITORED JUST DO A REPAINT
                        // Set the new state directly
                        getCastedFigure().setState(state);
                        // The state of this controller has changed repaint at the next 
                        // reasonable opportunity
                        if (getWorldEditPart().getRefreshMode() == RefreshMode.FULL)
                            getViewer().getControl().getDisplay().syncExec(new Runnable() {
                                public final void run() {
                                    //getCastedFigure().refresh();
                                    getCastedFigure().repaint();
                                }
                            });
                    } //if else NOT_MONITORED
                    break;
                } //  case CTATE_CHANGED

                    // Add communication
                    //else if (arg instanceof HashSet) {
                case ACTIVE_OBJECT_RESET_COMMUNICATIONS: {
                    //   final AOFigure destination = getCastedFigure();
                    //   final IFigure panel = getGlobalPanel();
                    //               getViewer().getControl().getDisplay().syncExec(new Runnable() {
                    //                   public final void run() {
                    getCastedModel().removeAllConnections();
                    //     	destination.removeConnections(panel);
                    //                   }
                    //               });
                    //                getWorldEditPart().clearCommunications();
                    break;
                } //case ACTIVE_OBJECT_RESET_COMMUNICATIONS
                case ACTIVE_OBJECT_ADD_COMMUNICATION: {
                    //                     final ActiveObject aoSource = (ActiveObject) notificationdata;
                    //                    final AOFigure destination = getCastedFigure();
                    //                    final IFigure panel = getGlobalPanel();
                    //                    //final IFigure panel=getConnectionLayer();
                    //                    final AbstractMonitoringEditPart editPart = AbstractMonitoringEditPart.registry
                    //                            .get(aoSource);
                    //                    if (editPart != null) {
                    //                        AOFigure source = (AOFigure) editPart.getFigure();
                    //                        if (source != null) {
                    //                            addGraphicalCommunication(new GraphicalCommunication(source, destination, panel,
                    //                                getArrowColor()));
                    //                        } else {
                    //                            System.out.println("[Error] Unable to find the source");
                    //                        }
                    //                    }
                    break;
                } // case ACTIVE_OBJECT_ADD_COMMUNICATION 
                case ACTIVE_OBJECT_REQUEST_QUEUE_LENGHT_CHANGED: {
                    length = (Integer) notificationdata;
                    getCastedFigure().setRequestQueueLength(length);
                    break;
                }
                    //                case SOURCE_CONNECTIONS_CHANGED:
                    //                {
                    //                	//System.out.println("...................refresh source for "+getModel());
                    //                	//this.refreshSourceConnections();
                    //                	//this.refresh();
                    //                }
                    //                case TARGET_CONNECTIONS_CHANGED:
                    //                {
                    //                	//System.out.println("...................refresh target for "+getModel());
                    //                	//this.refreshTargetConnections();
                    //                	//this.refresh();
                    //                }
                    //                
                    //case ACTIVE_OBJECT_REQUEST_QUEUE_LENGHT_CHANGED:
                default:
                    super.update(o, arg);
            } //switch 
        } //if arg is Notification     
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    /**
     * Returns a new view associated with the type of model object the EditPart
     * is associated with. So here, it returns a new NodeFigure.
     * @return a new NodeFigure view associated with the NodeObject model.
     */
    protected IFigure createFigure() {
        AOFigure figure = new AOFigure(getCastedModel().getName() /*getFullName()*/);
        AOListener listener = new AOListener(getCastedModel(), figure, getMonitoringView(),
            getCastedParentFigure());
        figure.addMouseListener(listener);
        figure.addMouseMotionListener(listener);
        return figure;
    }

    protected Color getArrowColor() {
        // Avoid creating a new instance of color by returning a default one
        return DEFAULT_ARROW_COLOR; //new Color(Display.getCurrent(), 108, 108, 116);
    }

    /**
     * Returns a List containing the children model objects.
     * @return the List of children
     */
    protected List<AbstractData> getModelChildren() {
        // NO CHILDREN FOR THIS MODEL !!!
        return emptyList;
    }

    @Override
    protected void createEditPolicies() { /* Do nothing */
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //

    /**
     * Convert the result of EditPart.getModel()
     * to AOObject (the real type of the model).
     * @return the casted model
     */
    @SuppressWarnings("unchecked")
    @Override
    public ActiveObject getCastedModel() {
        if (castedModel == null) {
            castedModel = (ActiveObject) getModel();
        }
        return castedModel;
    }

    /**
     * Convert the result of EditPart.getFigure()
     * to AOFigure (the real type of the figure).
     * @return the casted figure
     */
    @SuppressWarnings("unchecked")
    @Override
    public AOFigure getCastedFigure() {
        if (castedFigure == null) {
            castedFigure = (AOFigure) getFigure();
        }
        return castedFigure;
    }

    private NodeEditPart getCastedParentEditPart() {
        if (castedParentEditPart == null) {
            castedParentEditPart = (NodeEditPart) getParent();
        }
        return castedParentEditPart;
    }

    private NodeFigure getCastedParentFigure() {
        if (castedParentFigure == null) {
            castedParentFigure = (NodeFigure) getCastedParentEditPart().getFigure();
        }
        return castedParentFigure;
    }

    private IFigure getGlobalPanel() {
        if (globalPanel == null) {
            globalPanel = getWorldEditPart().getFigure().getParent();
        }
        return globalPanel;
    }

    private IFigure getConnectionLayer() {
        FreeformLayeredPane flp = (FreeformLayeredPane) getWorldEditPart().getFigure().getParent()
                .getParent();
        @SuppressWarnings("unchecked")
        Iterator c = flp.getChildren().iterator();
        IFigure conLayer = null;
        while (c.hasNext()) {
            IFigure f = (IFigure) c.next();
            if (f instanceof ConnectionLayer)
                conLayer = f;
        }

        return conLayer;

    }

    @Override
    public void deactivate() {
        getCastedModel().resetCommunications();
        super.deactivate();
    }
}
