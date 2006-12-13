/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.monitoring.editparts;

import java.util.List;
import java.util.Observable;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.monitoring.data.AbstractDataObject;
import org.objectweb.proactive.ic2d.monitoring.data.NodeObject;
import org.objectweb.proactive.ic2d.monitoring.data.State;
import org.objectweb.proactive.ic2d.monitoring.figures.NodeFigure;
import org.objectweb.proactive.ic2d.monitoring.figures.listeners.NodeListener;

public class NodeEditPart extends AbstractMonitoringEditPart{

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
	public NodeObject getCastedModel(){
		return (NodeObject)getModel();
	}

	/**
	 * This method is called whenever the observed object is changed.
	 * It calls the method <code>refresh()</code>.
	 * @param o the observable object (instance of AbstractDataObject).
	 * @param arg an argument passed to the notifyObservers  method.
	 */
	@Override
	public void update(Observable o, Object arg) {
		//System.out.println("NodeEditPart.update()");
		final Object param = arg;
//		final NodeEditPart nodeEditPart = this;

		Display.getDefault().asyncExec(new Runnable() {
			public void run () {
				if(param instanceof State && (State)param == State.NOT_MONITORED) {
					deactivate();
//					((AbstractIC2DEditPart)getParent()).removeChildVisual(nodeEditPart);
				}
				else if(param instanceof State) {
					((NodeFigure)getFigure()).setHighlight(getMonitoringView().getVirtualNodesGroup().getColor(((NodeObject)getModel()).getVNParent()));
					refresh();
					//getWorldEditPart().getGUIRefresher().refresh(this);
				}
				else {
					refresh();
					//getWorldEditPart().getGUIRefresher().refresh(this);
				}
			}
		});
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
	protected IFigure createFigure() {
		NodeFigure figure = new NodeFigure(getCastedModel().getFullName(),getCastedModel().getParentProtocol());
		NodeListener listener = new NodeListener(getCastedModel(), figure, getMonitoringView());
		figure.addMouseListener(listener);
		figure.addMouseMotionListener(listener);
		return figure;
	}

	/**
	 * Returns a List containing the children model objects.
	 * @return the List of children
	 */
	protected List<AbstractDataObject> getModelChildren() {
		return getCastedModel().getMonitoredChildren();
	}


	/**
	 * Fills the view with data extracted from the model object 
	 * associated with the EditPart.
	 * This method will be called just after the creation of 
	 * the figure, and may also be called in response to 
	 * notifications from the model. 
	 */
	/*	protected void refreshVisuals(){ 
		//TODO
	}
	 */

	/**
	 * Creates the initial EditPolicies and/or reserves slots for dynamic ones.
	 */
	protected void createEditPolicies() {
		// TODO Auto-generated method stub

	}



}
