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
package org.objectweb.proactive.ic2d.monitoring.figures;

import org.eclipse.draw2d.EllipseAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class Anchor extends EllipseAnchor{

	private int orientation = PositionConstants.EAST;

	//
	// -- PUBLIC METHODS -------------------------------------------
	//
	
	/**
	 * @see org.eclipse.draw2d.EllipseAnchor#EllipseAnchor(IFigure)
	 */
	public Anchor(IFigure owner) {
		super(owner);
	}

	/**
	 * @see org.eclipse.draw2d.EllipseAnchor#getLocation(Point)
	 */
	@Override
	public Point getLocation(Point reference) {
		Rectangle r = Rectangle.SINGLETON;
		r.setBounds(getOwner().getBounds());
		r.translate(-1, -1);
		r.resize(1, 1);
		getOwner().translateToAbsolute(r);

		Point ref = r.getCenter().negate().translate(reference);

		if (ref.x == 0)
			return new Point(reference.x, (ref.y > 0) ? r.bottom() : r.y);
		if (ref.y == 0)
			return new Point((ref.x > 0) ? r.right() : r.x, reference.y);

		float dx = (ref.x > 0) ? 0.5f : -0.5f;
		float dy = (ref.y > 0) ? 0.5f : -0.5f;

		float k = (float)(ref.y * r.width) / (ref.x * r.height);
		k = k * k;

		if(orientation == PositionConstants.EAST || orientation == PositionConstants.WEST)
			return r.getCenter().translate((int)(r.width * dx),0);
		else if(orientation == PositionConstants.SOUTH || orientation == PositionConstants.NORTH)
			return r.getCenter().translate(0,(int)(r.height * dy));
		else
			return r.getCenter().translate((int)(r.width * dx / Math.sqrt(1 + k)),
					(int)(r.height * dy / Math.sqrt(1 + 1 / k)));
	}

	public void setOrientation(int orientation){
		this.orientation = orientation;
	}

	public int getOrientation(){
		return this.orientation;
	}

	/**
	 * This is used to update the anchor position.
	 * For example :
	 * Lets A and B 2 figures. We want to draw a connection between this 2 objects.
	 * A is the source, and B is the target.
	 * So you must do : 
	 *     A.getAnchor().useRelativePosition(PositionConstant);
	 *     B.getAnchor().useRelativePosition(PositionConstant);
	 * @param position The target position (a field of PositionConstants)
	 */
	public void useRelativePosition(int position){
		if(position == PositionConstants.NORTH)// Target is in the north of Source
			this.orientation = PositionConstants.EAST;
		else if(position == PositionConstants.SOUTH)// Target is in the south of Source
			this.orientation = PositionConstants.WEST;
		else if (position == PositionConstants.EAST)
			this.orientation = PositionConstants.NORTH;
		else if	(position == PositionConstants.WEST)
			this.orientation = PositionConstants.SOUTH;
		else this.orientation = position;
	}

	
	/**
	 * This is used to calculate the anchor position.
	 */
	public Point getReferencePoint(){
		if (getOwner() == null)
			return null;
		else {
			Rectangle rect = getOwner().getBounds();
			Point ref = rect.getCenter();
			if(orientation == PositionConstants.EAST)
				ref = ref.translate(rect.width/2, 0);
			else if(orientation == PositionConstants.WEST)
				ref = ref.translate(-rect.width/2, 0);
			else if(orientation == PositionConstants.NORTH)
				ref = ref.translate(0, -rect.height/2);
			else if(orientation == PositionConstants.SOUTH)
				ref = ref.translate(0, rect.height/2);
			getOwner().translateToAbsolute(ref);
			return ref;
		}
	}
}
