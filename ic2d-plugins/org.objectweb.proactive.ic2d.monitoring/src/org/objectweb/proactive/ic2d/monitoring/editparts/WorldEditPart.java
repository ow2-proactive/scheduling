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

import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.objectweb.proactive.ic2d.monitoring.data.AbstractDataObject;
import org.objectweb.proactive.ic2d.monitoring.data.WorldObject;
import org.objectweb.proactive.ic2d.monitoring.figures.listeners.WorldListener;
import org.objectweb.proactive.ic2d.monitoring.views.MonitoringView;

public class WorldEditPart extends AbstractMonitoringEditPart {

	private FreeformLayer layer;

	private MonitoringView monitoringView;
	
	//
	// -- CONSTRUCTORS -----------------------------------------------
	//

	public WorldEditPart(WorldObject model, MonitoringView monitoringView) {
		super(model);
		this.monitoringView = monitoringView;
	}

	//
	// -- PUBLICS METHODS -----------------------------------------------
	//

	/**
	 * Convert the result of EditPart.getModel()
	 * to WorldObject (the real type of the model).
	 * @return the casted model
	 */
	public WorldObject getCastedModel() {
		return (WorldObject)getModel();
	}

	public IFigure getContentPane() {
		return layer;
	}
	
	@Override
	public MonitoringView getMonitoringView(){
		return this.monitoringView;
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
	protected IFigure createFigure() {
		layer = new FreeformLayer();
		FlowLayout layout = /*new FlowLayout()*/new MonitoringLayout();
		layout.setMajorAlignment(FlowLayout.ALIGN_CENTER);
		layout.setMajorSpacing(50);
		layout.setMinorSpacing(50);
//		layout.setSpacing(50);
//		layout.setMinorAlignment(MonitoringLayout.ALIGN_CENTER_CENTER);
		layer.setLayoutManager(layout);
		
		layer.addMouseListener(new WorldListener(monitoringView));
		
		return layer;
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
	protected void createEditPolicies() {
		// TODO Auto-generated method stub

	}

	/*@Override
	public Object getAdapter(Class adapter) {
		if(adapter == MouseWheelHelper.class) {
			return new ViewportMouseWheelHelper(this) { // Classe disponible dans GEF exprès pour cela
				public void handleMouseWheelScrolled(Event event) {
					System.out.println("MouseWheel WorldEditPart");
					super.handleMouseWheelScrolled(event);
				}
			};
		}
		return super.getAdapter(adapter);
	}*/
	
	private class MonitoringLayout extends FlowLayout {
		
		protected void setBoundsOfChild(IFigure parent, IFigure child, Rectangle bounds) {
			parent.getClientArea(Rectangle.SINGLETON);
			bounds.translate(Rectangle.SINGLETON.x, Rectangle.SINGLETON.y+100);
			child.setBounds(bounds);
		}
	}

}
