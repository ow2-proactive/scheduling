/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package org.objectweb.proactive.examples.nbody.barneshut;

import java.io.Serializable;
import java.util.Vector;

import org.objectweb.proactive.examples.nbody.common.Rectangle;


public class Info implements Serializable {
    public int identification;
    double mass = 0;
    double radius = 0;
    double x;
    double y;
    public Planet[] planets;
    private Vector sons;
    private int nbExpectedSons;
    private boolean isLeaf = true;

    public Info() {
    }
    ;
    public Info(Vector planetVector, Rectangle bounds) {
        this.radius = Math.sqrt((bounds.width * bounds.width) +
                (bounds.height * bounds.height)) / 2;

        this.planets = ((Planet[]) planetVector.toArray(new Planet[] {  }));
        recomputeCenterOfMass();

        this.mass = totalMass();
        this.nbExpectedSons = this.planets.length;
    }

    /**
     * Only used by this.copy()
     */
    private Info(Info info) {
        this.identification = info.identification;
        this.mass = info.mass;
        this.radius = info.radius;
        this.x = info.x;
        this.y = info.y;
        this.planets = info.planets;
        this.sons = info.sons;
    }

    /**
     * Compute the total mass contained in this "region".
     * @param bodyList List of Bodies, of type Info Vector, which all have a mass
     * @param numPlanets = bodyList.size()
     * @return the sum of all the masses of the planets within this region
     */
    private double totalMass() {
        double mass = 0;
        for (int i = 0; i < this.planets.length; i++)
            mass += this.planets[i].mass;
        return mass;
    }

    /**
     * Sets the center of the current Info as the center of mass of the Planets contained.
     */
    public void recomputeCenterOfMass() {
        this.x = 0;
        this.y = 0;
        this.mass = 0;
        if (!this.isLeaf) { // then this.sons contains the update, hopefully
            if (this.sons.size() != this.nbExpectedSons) {
                throw new NullPointerException(
                    "Info has not received all of its sons!");
            }

            for (int i = 0; i < this.sons.size(); i++) {
                Info sibling = (Info) sons.get(i);
                this.mass += (sibling).mass;
                this.x += (sibling.x * sibling.mass);
                this.y += (sibling.y * sibling.mass);
            }
        } else {
            //assert planets.length !=0 : "Trying to find the center of an empty set.";
            if (this.planets.length == 0) {
                throw new NullPointerException(
                    "Trying to find the center of an empty set.");
            }

            for (int i = 0; i < this.planets.length; i++) {
                this.mass += planets[i].mass;
                this.x += (planets[i].x * planets[i].mass);
                this.y += (planets[i].y * planets[i].mass);
            }
        }

        this.x /= mass;
        this.y /= mass;
    }

    /**
     * Remove sons and planets, if need be.
     * Sendinf infos is meant to be easier, if there isn't too much information!
     * @param isLeaf
     */
    public void clean(boolean isLeaf) {
        this.isLeaf = isLeaf;
        if (!isLeaf) {
            this.planets = null;
        }
        emptySons();
    }

    public void emptySons() {
        if (!this.isLeaf) {
            this.sons = new Vector();
        }
    }

    public void addSon(Info sibling) {
        this.sons.add(sibling);
        if (this.sons.size() > this.nbExpectedSons) {
            throw new NullPointerException("Adding a son too many!!");
        }
    }

    public String toString() {
        return this.identification + (this.isLeaf ? " leaf " : " not leaf ") +
        this.mass;
    }

    /**
     * A minimum copy of this, ready to be remembered, or sent.
     * Cannot be used as a replacement of the current Info, because velocity is lost.
     */
    public Info copy() {
        return new Info(this);
    }

    /**
     * Method needed to reset the number of sons, because it initially corresponds to the underlying nb of Planets.
     * @param nb
     */
    public void setNbSons(int nb) {
        this.nbExpectedSons = nb;
    }
}
