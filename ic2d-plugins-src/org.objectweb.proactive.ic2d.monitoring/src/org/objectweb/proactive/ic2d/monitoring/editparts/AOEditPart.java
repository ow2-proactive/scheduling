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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Set;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.monitoring.data.AOObject;
import org.objectweb.proactive.ic2d.monitoring.data.AbstractDataObject;
import org.objectweb.proactive.ic2d.monitoring.data.State;
import org.objectweb.proactive.ic2d.monitoring.figures.AOFigure;
import org.objectweb.proactive.ic2d.monitoring.figures.NodeFigure;
import org.objectweb.proactive.ic2d.monitoring.figures.listeners.AOListener;

public class AOEditPart extends AbstractMonitoringEditPart{

	private Integer length;

	//
	// -- CONSTRUCTORS -----------------------------------------------
	//

	public AOEditPart(AOObject model) {
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
		if(arg != null){
			// State updated
			if(arg instanceof State){
				final State state = (State)arg;
				final IFigure panel = ((WorldEditPart)getParent().getParent().getParent().getParent()).getFigure().getParent();
				Display.getDefault().asyncExec(new Runnable() {
					public void run () {
						if(state == State.NOT_MONITORED) {
							getCastedFigure().removeConnections(panel);
						}
						else
							getCastedFigure().setState(state);
					}
				});
			}
			// Add communication
			else if(arg instanceof HashSet) {

				final Set<AOObject> communications = (HashSet<AOObject>) arg;
				
				final AOFigure source = getCastedFigure();

				final IFigure panel = getWorldEditPart().getFigure().getParent();

				Display.getDefault().asyncExec(new Runnable() {
					public void run () {
						if (communications.isEmpty())
							source.removeConnections(panel);
						
						for (Iterator<AOObject> it = communications.iterator(); it.hasNext(); )
						{
							AOObject  aoTarget = it.next();
							AbstractMonitoringEditPart editPart = AbstractMonitoringEditPart.registry.get(aoTarget);
							
							if(editPart!=null){
								AOFigure target = (AOFigure) editPart.getFigure();
								if(target!=null){
									source.addConnection(target, panel, getArrowColor());
								}
								else
									System.out.println("[Error] Unable to find the target");
							}
							else{
								System.out.println("[Error] Unable to draw the arrow : "+getCastedModel().getFullName()+" -->  "+aoTarget.getFullName());
							}
						}
						
//						EditPartViewer view = getViewer();
//						if(view==null)
//							return;
//						Map registry = view.getEditPartRegistry();
//						if(registry==null)
//							return;
//						for (java.util.Iterator<AOObject> it = communications.iterator(); it.hasNext(); )
//						{
//							AbstractGraphicalEditPart editPart = (AbstractGraphicalEditPart) registry.get(it.next());
//							if(editPart!=null){
//								AOFigure target = (AOFigure) editPart.getFigure();
//								if(target!=null)
//									source.addConnection(target, panel, getArrowColor());		
//							}
//						}
					}});
			}
			// Request queue length has changed
			else if(arg instanceof Integer) {
				length  = (Integer) arg;
				Display.getDefault().asyncExec(new Runnable() {
					public void run () {
						getCastedFigure().setRequestQueueLength(length);
					}
				});
			}
		}
		else
			super.update(o, arg);
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
		AOFigure figure = new AOFigure(getCastedModel().getFullName());
		AOListener listener = new AOListener(getCastedModel(), figure,  getMonitoringView(), getCastedParentFigure());
		figure.addMouseListener(listener);
		figure.addMouseMotionListener(listener);
		return figure;
	}

	protected Color getArrowColor() {
		return new Color(Display.getCurrent(), 108, 108, 116);
	}

	/**
	 * Returns a List containing the children model objects.
	 * @return the List of children
	 */
	protected List<AbstractDataObject> getModelChildren() {
		return getCastedModel().getMonitoredChildren();
	}

	@Override
	protected void createEditPolicies() {/* Do nothing */}

	//
	// -- PRIVATE METHODS -----------------------------------------------
	//

	/**
	 * Convert the result of EditPart.getModel() to AOObject (the real type of
	 * the model).
	 * @return the casted model
	 */
	public AOObject getCastedModel(){
		return (AOObject)getModel();
	}

	/**
	 * Convert the result of EditPart.getFigure() to AOFigure (the real type of
	 * the figure).
	 * @return the casted figure
	 */
	public AOFigure getCastedFigure(){
		return (AOFigure)getFigure();
	}
	
	private NodeEditPart getCastedParentEditPart(){
		return (NodeEditPart)getParent();
	}
	
	private NodeFigure getCastedParentFigure(){
		return (NodeFigure) getCastedParentEditPart().getFigure();
	}
}
