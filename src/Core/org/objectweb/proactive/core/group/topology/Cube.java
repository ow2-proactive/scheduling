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
package org.objectweb.proactive.core.group.topology;

import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProxyForGroup;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;


/**
 * This class represents a group by a three-dimensional topology.
 *
 * @author Laurent Baduel
 */
public class Cube extends Plan { // implements Topology3D {

    /** depth of the three-dimensional topology group */
    protected int depth; //  => Z => number of plans

    /**
     * Construtor. The members of <code>g</code> are used to fill the topology group.
     * @param g - the group used a base for the new group (topology)
     * @param height - the heigth of the three-dimensional topology group
     * @param width - the width of the three-dimensional topology group
     * @param depth - the depth of the three-dimensional topology group
     * @throws ConstructionOfReifiedObjectFailedException
     */
    public Cube(Group g, int height, int width, int depth) throws ConstructionOfReifiedObjectFailedException {
        super(g, height * width * depth);
        this.height = height;
        this.width = width;
        this.depth = depth;
    }

    /**
     * Returns the width of the three-dimensional topology group (number of cloumns)
     * @return the width of the three-dimensional topology group
     */
    @Override
    public int getWidth() {
        return this.width;
    }

    /**
     * Returns the height of the three-dimensional topology group (number of lines)
     * @return the height of the three-dimensional topology group
     */
    @Override
    public int getHeight() {
        return this.height;
    }

    /**
     * Returns the height of the three-dimensional topology group (number of lines)
     * @return the height of the three-dimensional topology group
     */
    public int getDepth() {
        return this.depth;
    }

    /**
     * Returns the coordonate X for the specified position, according to the dimension
     * @param position
     * @return the coordonate X
     */
    private int getX(int position) {
        return (position % (this.width * this.height)) % this.width;
    }

    /**
     * Returns the coordonate Y for the specified position, according to the dimension
     * @param position
     * @return the coordonate Y
     */
    private int getY(int position) {
        return (position / this.width) % this.height;
    }

    /**
     * Returns the coordonate Z for the specified position, according to the dimension
     * @param position
     * @return the coordonate Z
     */
    private int getZ(int position) {
        return position / (this.width * this.height);
    }

    /**
     * Returns the object at the left of the specified object in the three-dimensional topology group
     * @param o - the specified object
     * @return the object at the left of <code>o<code>. If there is no object at the left of <code>o</code>, return <code>null</code>
     */
    @Override
    public Object left(Object o) {
        int position = this.indexOf(o);
        int positionX = this.getX(position);
        if (positionX != 0) {
            return this.get(position - 1);
        } else {
            return null;
        }
    }

    /**
     * Returns the object at the right of the specified object in the three-dimensional topology group
     * @param o - the specified object
     * @return the object at the right of <code>o<code>. If there is no object at the right of <code>o</code>, return <code>null</code>
     */
    @Override
    public Object right(Object o) {
        int position = this.indexOf(o);
        int positionX = this.getX(position);
        if (positionX != (this.getWidth() - 1)) {
            return this.get(position + 1);
        } else {
            return null;
        }
    }

    /**
     * Returns the object at the up of the specified object in the three-dimensional topology group
     * @param o - the specified object
     * @return the object at the up of <code>o<code>. If there is no object at the up of <code>o</code>, return <code>null</code>
     */
    @Override
    public Object up(Object o) {
        int position = this.indexOf(o);
        int positionY = this.getY(position);
        if (positionY != 0) {
            return this.get(position - this.getWidth());
        } else {
            return null;
        }
    }

    /**
     * Returns the object at the down of the specified object in the three-dimensional topology group
     * @param o - the specified object
     * @return the object at the down of <code>o<code>. If there is no object at the down of <code>o</code>, return <code>null</code>
     */
    @Override
    public Object down(Object o) {
        int position = this.indexOf(o);
        int positionY = this.getY(position);
        if (positionY != (this.getHeight() - 1)) {
            return this.get(position + this.getWidth());
        } else {
            return null;
        }
    }

    /**
     * Returns the object ahead the specified object in the three-dimensional topology group
     * @param o - the specified object
     * @return the object at the down of <code>o<code>. If there is no object at the down of <code>o</code>, return <code>null</code>
     */
    public Object ahead(Object o) {
        int position = this.indexOf(o);
        int positionZ = this.getZ(position);
        if (positionZ != 0) {
            return this.get(position - (this.getWidth() * this.getHeight()));
        } else {
            return null;
        }
    }

    /**
     * Returns the object behind the specified object in the three-dimensional topology group
     * @param o - the specified object
     * @return the object at the down of <code>o<code>. If there is no object at the down of <code>o</code>, return <code>null</code>
     */
    public Object behind(Object o) {
        int position = this.indexOf(o);
        int positionZ = this.getZ(position);
        if (positionZ != (this.getDepth() - 1)) {
            return this.get(position + (this.getWidth() * this.getHeight()));
        } else {
            return null;
        }
    }

    /**
     * Returns the horizontal line (one-dimensional topology group) that contains the object
     * @param o - the object
     * @return the one-dimensional topology group formed by the horizontal line that contains the object in the three-dimensional topology group
     */
    public Line lineX(Object o) {
        int position = this.indexOf(o);
        int posY = this.getY(position);
        int posZ = this.getZ(position);

        ProxyForGroup tmp = null;
        try {
            tmp = new ProxyForGroup(this.getTypeName());
        } catch (ConstructionOfReifiedObjectFailedException e) {
            e.printStackTrace();
        }

        int begining = (posZ * (this.getHeight() * this.getWidth())) + (posY * this.getWidth());
        for (int i = begining; i < (begining + this.getWidth()); i++) {
            tmp.add(this.get(i));
        }
        Line result = null;
        try {
            result = new Line(tmp, this.getWidth());
        } catch (ConstructionOfReifiedObjectFailedException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Returns the vertical line (kind of column)(one-dimensional topology group) that contains the object
     * @param o - the object
     * @return the one-dimensional topology group formed by the vertical line that contains the object in the three-dimensional topology group
     */
    public Line lineY(Object o) {
        int position = this.indexOf(o);
        int posX = this.getX(position);
        int posZ = this.getZ(position);

        ProxyForGroup tmp = null;
        try {
            tmp = new ProxyForGroup(this.getTypeName());
        } catch (ConstructionOfReifiedObjectFailedException e) {
            e.printStackTrace();
        }

        int begining = (posZ * (this.getHeight() * this.getWidth())) + posX;
        for (int i = 0; i < this.getHeight(); i++) {
            tmp.add(this.get(begining + (i * this.getWidth())));
        }
        Line result = null;
        try {
            result = new Line(tmp, this.getWidth());
        } catch (ConstructionOfReifiedObjectFailedException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Returns the line in depth (one-dimensional topology group) that contains the object
     * @param o - the object
     * @return the one-dimensional topology group formed by the line in depth that contains the object in the three-dimensional topology group
     */
    public Line lineZ(Object o) {
        int position = this.indexOf(o);
        int posY = this.getY(position);
        int posZ = this.getZ(position);

        ProxyForGroup tmp = null;
        try {
            tmp = new ProxyForGroup(this.getTypeName());
        } catch (ConstructionOfReifiedObjectFailedException e) {
            e.printStackTrace();
        }

        int begining = (posY * this.getWidth()) + posY;
        for (int i = 0; i < this.getDepth(); i++) {
            tmp.add(this.get(begining + (i * (this.getWidth() * this.getHeight()))));
        }
        Line result = null;
        try {
            result = new Line(tmp, this.getWidth());
        } catch (ConstructionOfReifiedObjectFailedException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Returns the plan in X (two-dimensional topology group) that contains the object
     * @param o - the object
     * @return the two-dimensional topology group formed by the plan in X that contains the object in the three-dimensional topology group
     */
    public Plan planX(Object o) {
        ProxyForGroup tmp = null;
        try {
            tmp = new ProxyForGroup(this.getTypeName());
        } catch (ConstructionOfReifiedObjectFailedException e) {
            e.printStackTrace();
        }

        int begining = this.getX(this.indexOf(o));
        for (int i = 0; i < this.getHeight(); i++) {
            for (int j = 0; j < this.getDepth(); j++) {
                tmp.add(this.get((begining + (i * this.getWidth())) +
                    (j * (this.getWidth() * this.getHeight()))));
            }
        }
        Plan result = null;
        try {
            result = new Plan(tmp, this.getWidth(), this.getDepth());
        } catch (ConstructionOfReifiedObjectFailedException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Returns the plan in Y (two-dimensional topology group) that contains the object
     * @param o - the object
     * @return the two-dimensional topology group formed by the plan in Y that contains the object in the three-dimensional topology group
     */
    public Plan planY(Object o) {
        ProxyForGroup tmp = null;
        try {
            tmp = new ProxyForGroup(this.getTypeName());
        } catch (ConstructionOfReifiedObjectFailedException e) {
            e.printStackTrace();
        }

        int begining = this.getY(this.indexOf(o)) * this.getWidth();
        for (int i = 0; i < this.getWidth(); i++) {
            for (int j = 0; j < this.getDepth(); j++) {
                tmp.add(this.get((begining + (i * this.getWidth())) +
                    (j * (this.getWidth() * this.getHeight()))));
            }
        }
        Plan result = null;
        try {
            result = new Plan(tmp, this.getWidth(), this.getDepth());
        } catch (ConstructionOfReifiedObjectFailedException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Returns the plan in Z (two-dimensional topology group) that contains the object
     * @param o - the object
     * @return the two-dimensional topology group formed by the plan in Z that contains the object in the three-dimensional topology group
     */
    public Plan planZ(Object o) {
        ProxyForGroup tmp = null;
        try {
            tmp = new ProxyForGroup(this.getTypeName());
        } catch (ConstructionOfReifiedObjectFailedException e) {
            e.printStackTrace();
        }

        int begining = this.getZ(this.indexOf(o)) * (this.getWidth() * this.getHeight());
        for (int i = 0; i < (this.getWidth() * this.getHeight()); i++) {
            tmp.add(this.get(begining + i));
        }
        Plan result = null;
        try {
            result = new Plan(tmp, this.getWidth(), this.getDepth());
        } catch (ConstructionOfReifiedObjectFailedException e) {
            e.printStackTrace();
        }
        return result;
    }
}
