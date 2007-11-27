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
import java.util.Observable;

import org.eclipse.draw2d.IFigure;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.NodeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.NodeFigure;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.listener.NodeListener;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotification;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotificationTag;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.State;


public class NodeEditPart extends AbstractMonitoringEditPart {
    private NodeObject castedModel;
    private NodeFigure castedFigure;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public NodeEditPart(NodeObject model) {
        super(model);
    }

    //
    // -- PUBLICS METHODS -----------------------------------------------
    //

    /**
     * Convert the result of EditPart.getModel()
     * to NodeObject (the real type of the model).
     * @return the casted model
     */
    @SuppressWarnings("unchecked")
    @Override
    public NodeObject getCastedModel() {
        if (castedModel == null) {
            castedModel = (NodeObject) getModel();
        }
        return castedModel;
    }

    /**
     * Convert the result of EditPart.getFigure()
     * to NodeFigure (the real type of the figure).
     * @return the casted figure
     */
    @SuppressWarnings("unchecked")
    @Override
    public NodeFigure getCastedFigure() {
        if (castedFigure == null) {
            castedFigure = (NodeFigure) getFigure();
        }
        return castedFigure;
    }

    /**
     * This method is called whenever the observed object is changed.
     * It calls the method <code>refresh()</code>.
     * @param o the observable object (instance of AbstractDataObject).
     * @param arg an argument passed to the notifyObservers  method.
     */
    @Override
    //TODO: this method was taken out from a dedicated Thread 
    //which means that the observable (NodeObject) will be blocked
    //on the notifyObservers method.
    public void update(Observable o, Object arg) {
        //final Object param = arg;
        if (!(arg instanceof MVCNotification)) {
            return;
        }

        final MVCNotification notif = (MVCNotification) arg;
        if ((notif.getMVCNotification() == MVCNotificationTag.STATE_CHANGED) &&
                (notif.getData() == State.NOT_MONITORED)) {
            deactivate();
        } else if (notif.getMVCNotification() == MVCNotificationTag.STATE_CHANGED) //in this case we know we have changed highlight state
         {
            //method VirtualNodesGroup.getColor(virtualNode vn)
            //returns the color for the virtual node if the virtual node is selected
            //or null if it is not. 
            //if the collor is null, setHighlight(null) colors the figure 
            //node to the default color
            getCastedFigure()
                .setHighlight(getMonitoringView().getVirtualNodesGroup()
                                  .getColor(getCastedModel().getVirtualNode()));
            refresh();
        } else {
            refresh();
        }
    }

    @Override
    public void refresh() {
        //TODO: this might be costly. Only when we stop monitoring the parent may be null and 
        //we have npe stak in the logs.  
        //        if (this.getParent() == null) {
        //            return;
        //        }
        super.refresh();
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    /**
     * Returns a new view associated
     * with the type of model object the
     * EditPart is associated with. So here, it returns a new NodeFigure.
     * @return a new NodeFigure view associated with the NodeObject model.
     */
    @Override
    protected IFigure createFigure() {
        //TODO A faire
        NodeObject model = getCastedModel();
        NodeFigure figure = new NodeFigure("Node " + model.getName(),
                URIBuilder.getProtocol(model.getUrl()) /*getCastedModel().getFullName(),getCastedModel().getParentProtocol()*/);

        NodeListener listener = new NodeListener(getCastedModel(), figure,
                getMonitoringView());
        figure.addMouseListener(listener);
        figure.addMouseMotionListener(listener);
        return figure;
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
}
