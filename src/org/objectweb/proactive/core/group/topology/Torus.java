/*
 * Created on Mar 16, 2004
 */
package org.objectweb.proactive.core.group.topology;

import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProxyForGroup;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;

/**
 * This class represents a group by a cycling two-dimensional topology.
 * 
 * @author Laurent Baduel
 */
public class Torus extends Ring { // implements Topology2D {

	/** height of the two-dimensional topology group */ 
	protected int height; //  => Y => number of Rings

	/**
	 * Construtor. The members of <code>g</code> are used to fill the topology group.
	 * @param g - the group used a base for the new group (topology) 
	 * @param height - the heigth of the two-dimensional topology group
	 * @param width - the width of the two-dimensional topology group
	 * @throws ConstructionOfReifiedObjectFailedException
	 */
	public Torus (Group g, int height, int width) throws ConstructionOfReifiedObjectFailedException {
		super(g, height*width);
		this.height = height;
		this.width = width;
	}

	/**
	 * Construtor. The members of <code>g</code> are used to fill the topology group.
	 * @param g - the group used a base for the new group (topology) 
	 * @param height - the heigth of the two-dimensional topology group
	 * @param width - the width of the two-dimensional topology group
	 * @throws ConstructionOfReifiedObjectFailedException
	 */
	protected Torus (Group g, int nbMembers) throws ConstructionOfReifiedObjectFailedException {
		super(g, nbMembers);
	}

	/**
	 * Return the max size of the Ring
	 * @return the max size of the one-dimensional topology group (i.e. the Ring)
	 */
	public int getWidth() {
		return this.width;
	}

	/**
	 * Returns the height of the two-dimensional topology group (number of Rings)
	 * @return the height of the two-dimensional topology group
	 */
	public int getHeight() {
		return this.height;
	}

	/**
	 * Returns the coordonate X for the specified position, according to the dimension
	 * @param position
	 * @return the coordonate X
	 */
	private int getX (int position) {
		return position % this.width;  
	}

	/**
	 * Returns the coordonate Y for the specified position, according to the dimension
	 * @param position
	 * @return the coordonate Y
	 */
	private int getY (int position) {
		return position % this.height;  
	}

	/**
	 * Returns the X-coordonate of the specified object
	 * @param o - the object
	 * @return the position (in X) of the object in the Torus
	 */
	public int getX(Object o) {
		return this.indexOf(o) % this.getWidth();
	}

	/**
	 * Returns the Y-coordonate of the specified object
	 * @param o - the object
	 * @return the position (in Y) of the object in the Torus
	 */
	public int getY(Object o) {
		return this.indexOf(o) / this.getWidth();
	}

	/**
	 * Returns the object at the left of the specified object in the two-dimensional topology group 
	 * @param o - the specified object
	 * @return the object at the left of <code>o<code>. If there is no object at the left of <code>o</code>, return <code>null</code>
	 */
	public Object left (Object o) {
		int positionX = this.getX(this.indexOf(o));
		if (positionX != 0) {
			return this.get(positionX-1);
		}
		else {
			return this.get(positionX+(this.getWidth()-1));
		}
	}

	/**
	 * Returns the object at the right of the specified object in the two-dimensional topology group 
	 * @param o - the specified object
	 * @return the object at the right of <code>o<code>. If there is no object at the right of <code>o</code>, return <code>null</code>
	 */
	public Object right (Object o) {
		int positionX = this.getX(this.indexOf(o));
		if (positionX != this.getWidth()) {
			return this.get(positionX+1);
		}
		else {
			return this.get(positionX-(this.getWidth()-1));
		}
	}

	/**
	 * Returns the object at the up of the specified object in the two-dimensional topology group 
	 * @param o - the specified object
	 * @return the object at the up of <code>o<code>. If there is no object at the up of <code>o</code>, return <code>null</code>
	 */
	public Object up (Object o) {
		int positionY = this.getY(this.indexOf(o));
		if (positionY != 0) {
			return this.get(positionY-this.getWidth());
		}
		else {
			return this.get(positionY+(this.getWidth()*(this.getHeight()-1)));
		}
	}

	/**
	 * Returns the object at the down of the specified object in the two-dimensional topology group 
	 * @param o - the specified object
	 * @return the object at the down of <code>o<code>. If there is no object at the down of <code>o</code>, return <code>null</code>
	 */
	public Object down (Object o) {
		int position = this.getY(this.indexOf(o));
		if (position != this.getHeight()) {
			return this.get(position+this.getWidth());
		}
		else {
			return this.get(position-(this.getWidth()*(this.getHeight()-1)));
		}
	}

	/**
	 * Returns the Ring (one-dimensional topology group) with the specified number   
	 * @param Ring - the number of the Ring
	 * @return the one-dimensional topology group formed by the Ring in the two-dimensional topology group, return <code>null</code> if the the specified Ring is incorrect
	 */
	public Ring Ring (int Ring) {
		if ((Ring < 0) || (Ring > this.getWidth())) {
			return null;
		}
		ProxyForGroup tmp = null;
		try {
			tmp = new ProxyForGroup(this.getTypeName()); }
		catch (ConstructionOfReifiedObjectFailedException e) { e.printStackTrace(); }
		int begining = Ring*this.getWidth();
		for (int i = begining ; i < begining+this.getWidth() ; i++) {
			tmp.add(this.get(i));
		}
		Ring result = null;
		try {
			result = new Ring((Group) tmp, this.getWidth()); }
		catch (ConstructionOfReifiedObjectFailedException e) { e.printStackTrace(); }
		return result;
	}

	/**
	 * Returns the Ring that contains the specified object   
	 * @param o - the object
	 * @return the one-dimensional topology group formed by the Ring of the object in the two-dimensional topology group
	 */
	public Ring Ring (Object o) {
		return this.Ring(this.getY(this.indexOf(o)));
	}

	/**
	 * Returns the column (one-dimensional topology group) with the specified number   
	 * @param column - the number of the Ring
	 * @return the one-dimensional topology group formed by the column in the two-dimensional topology group, return <code>null</code> if the the specified Ring is incorrect
	 */
	public Ring column (int column) {
		if ((column < 0) || (column > this.getHeight())) {
			return null;
		}
		ProxyForGroup tmp = null;
		try {
			tmp = new ProxyForGroup(this.getTypeName()); }
		catch (ConstructionOfReifiedObjectFailedException e) { e.printStackTrace(); }
		int begining = column;
		for (int i = 0 ; i < this.getHeight() ; i++) {
			tmp.add(this.get(column+(i*this.getWidth())));
		}
		Ring result = null;
		try {
			result = new Ring((Group) tmp, this.getWidth()); }
		catch (ConstructionOfReifiedObjectFailedException e) { e.printStackTrace(); }
		return result;
	}

	/**
	 * Returns the column that contains the specified object   
	 * @param o - the object
	 * @return the one-dimensional topology group formed by the column of the object in the two-dimensional topology group
	 */
	public Ring column (Object o) {
		return this.column(this.getX(this.indexOf(o)));
	}
}
