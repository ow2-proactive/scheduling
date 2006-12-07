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
package org.objectweb.proactive.ic2d.monitoring.figures.listeners;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Rectangle;

public class Dragger extends MouseMotionListener.Stub implements MouseListener {
	
	private Figure figure;
	private int deltaX;
	private int deltaY;
	
	//
	// -- CONSTRUCTORS -----------------------------------------------
	//
	public Dragger(IFigure figure){
		figure.addMouseMotionListener(this);
		figure.addMouseListener(this);
	}
	
	//
	// -- PUBLIC METHODS ---------------------------------------------
	//
	
	public void mouseReleased(MouseEvent e){
		this.figure = null;
	}
	
	public void mouseClicked(MouseEvent e){/*Do nothing*/}
	
	public void mouseDoubleClicked(MouseEvent e){/*Do nothing*/}
	
	public void mousePressed(MouseEvent e){
		this.figure = ((Figure)e.getSource());
		Rectangle rectangle = figure.getBounds();
		this.deltaX = e.x - rectangle.x;
		this.deltaY = e.y - rectangle.y;
	}
	
	public void mouseDragged(MouseEvent e){
		if(this.figure != null){
			Rectangle rectangle = this.figure.getBounds();
			this.figure.setBounds(new Rectangle(e.x - deltaX, e.y - deltaY, rectangle.width, rectangle.height));
		}
	}
}
