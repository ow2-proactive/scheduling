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
import org.objectweb.proactive.ic2d.monitoring.data.State;
import org.objectweb.proactive.ic2d.monitoring.data.HostObject;
import org.objectweb.proactive.ic2d.monitoring.figures.HostFigure;
import org.objectweb.proactive.ic2d.monitoring.figures.listeners.HostListener;

public class HostEditPart extends AbstractMonitoringEditPart {

	//
	// -- CONSTRUCTORS -----------------------------------------------
	//

	public HostEditPart(HostObject model) {
		super(model);
	}

	//
	// -- PUBLICS METHODS -----------------------------------------------
	//

	/**
	 * Convert the result of EditPart.getModel()
	 * to HostObject (the real type of the model).
	 * @return the casted model
	 */
	public HostObject getCastedModel(){
		return (HostObject)getModel();
	}


	@Override
	public void update(Observable o, Object arg) {
		final Object param = arg;

		if(param instanceof State && (State)param == State.NOT_MONITORED) {
			deactivate();
		}
		// OS have been updated
		else if((param instanceof String) && (o instanceof HostObject)){
			((HostFigure)getFigure()).changeTitle((String)param);
		}
		Display.getDefault().asyncExec(this);
	}

	@Override
	public void run(){
		refresh();
	}

	//
	// -- PROTECTED METHODS -----------------------------------------------
	//

	/**
	 * Returns a new view associated
	 * with the type of model object the
	 * EditPart is associated with. So here, it returns a new HostFigure.
	 * @return a new HostFigure view associated with the HostObject model.
	 */
	protected IFigure createFigure() {
		HostFigure figure = new HostFigure(getCastedModel().getFullName());
		HostListener listener = new HostListener(getCastedModel(), figure, getMonitoringView());
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
	 * Creates the initial EditPolicies and/or reserves slots for dynamic ones.
	 */
	protected void createEditPolicies() {/* Do nothing */}
}
