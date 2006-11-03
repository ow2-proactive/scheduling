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
package org.objectweb.proactive.ic2d.monitoring.dnd;

import org.objectweb.proactive.ic2d.monitoring.data.AOObject;
import org.objectweb.proactive.ic2d.monitoring.data.NodeObject;
import org.objectweb.proactive.ic2d.monitoring.figures.AOFigure;

/**
 * This class allows us to make drag and drop.
 */
public class DragAndDrop {

	/** To know if we can drag or not */
	public boolean canDrag = false;
	
	/** To know if we can drop or not */
	public boolean canDrop = false;
	
	/** The source object */
	private AOObject source;
	
	/** The source figure */
	private AOFigure sourceFigure;
	
	/** The target object */
	private NodeObject target;
	
	//
	// -- PUBLICS METHODS -----------------------------------------------
	//
	
	public void setDrag(boolean canDrag){
		this.canDrag = canDrag;
		if(!canDrag && sourceFigure!=null)
			sourceFigure.setHighlight(null);
	}
	
	public boolean canDrag(){
		return this.canDrag;
	}
	
	public void setDrop(boolean canDrop){
		this.canDrop = canDrop;
	}
	
	public boolean canDrop(){
		return this.canDrop;
	}
	
	public AOObject getSource(){
		return this.source;
	}
	
	public void setSource(AOObject source){
		this.source = source;
	}
	
	public void setSourceFigure(AOFigure figure){
		this.sourceFigure = figure;
	}
	
	public NodeObject getTarget(){
		return this.target;
	}
}
