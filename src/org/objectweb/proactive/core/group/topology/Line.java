/*
 * Created on Mar 16, 2004
 */
package org.objectweb.proactive.core.group.topology;

import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;

/**
 * This class represents a group by a one-dimensional topology.  
 * 
 * @author Laurent Baduel
 */
public class Line extends TopologyGroup { // implements Topology1D {

	/** size of the one-dimensional topology group */ 
	protected int width;
	
	/**
	 * Construtor. The members of <code>g</code> are used to fill the topology group.
	 * @param g - the group used a base for the new group (topology) 
	 * @param size - the dimension (max number of member in the topolody group)
	 * @throws ConstructionOfReifiedObjectFailedException
	 */
	public Line (Group g, int size) throws ConstructionOfReifiedObjectFailedException {
		super(g,size);
		this.width = size;
	}

	/**
	 * Return the max size of the line
	 * @return the max size of the one-dimensional topology group (i.e. the line)
	 */
	public int getWidth() {
		return this.width;
	}

	/**
	 * Returns the position of the specified object
	 * @param o - the object
	 * @return the position of the object in the line
	 */
	public int getX(Object o) {
		return this.indexOf(o);
	}
	
	/**
	 * Returns the object at the left of the specified object in the one-dimensional topology group 
	 * @param o - the specified object
	 * @return the object at the left of <code>o<code>. If there is no object at the left of <code>o</code>, return <code>null</code>
	 */
	public Object left (Object o) {
		int position = this.indexOf(o);
		if (position != 0) {
			return this.get(position-1);
		}
		else {
			return null;
		}
	}

	/**
	 * Returns the object at the right of the specified object in the one-dimensional topology group 
	 * @param o - the specified object
	 * @return the object at the right of <code>o<code>. If there is no object at the right of <code>o</code>, return <code>null</code>
	 */
	public Object right (Object o) {
		int position = this.indexOf(o);
		if (position != this.getWidth()) {
			return this.get(position+1);
		}
		else {
			return null;
		}
	}

}
