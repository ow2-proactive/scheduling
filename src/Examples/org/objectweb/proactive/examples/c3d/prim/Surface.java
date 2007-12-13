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
package org.objectweb.proactive.examples.c3d.prim;

import java.util.Random;

import org.objectweb.proactive.examples.c3d.geom.Vec;


/**
 * Represents the aspect of the surface of a primitive.
 * As it is currently, a Surface can only be homogeneous.
 */
public class Surface implements java.io.Serializable {
    public Vec color;
    public double kd;
    public double ks;
    public double shine;
    public double kt;
    public double ior;

    /**
     * The array of possible initial colors
     */
    private static Vec[] colors = { new Vec(1, 1, 1), // White
            new Vec(0, 0, 0.5), // Navy
            new Vec(0, 0.5, 0), // darkgreen
            new Vec(0.5, 0, 0), // dark red
            new Vec(1, 0.6, 0), // Orange
            new Vec(1, 0.7, 0.8), // Pink
            new Vec(0.8, 0.8, 1) // Lavender
    };

    /*
     * Create a surface with default values
     */
    public Surface() {
        this.color = new Vec(1, 0, 0);
        this.kd = 1.0;
        this.ks = 0.0;
        this.shine = 0.0;
        this.kt = 0.0;
        this.ior = 1.0;
    }

    /**
     * Create a surface with given parameters set to the internal values
     */
    public Surface(Vec color, double kd, double ks, double shine, double kt, double ior) {
        this.color = color;
        this.kd = kd;
        this.ks = ks;
        this.shine = shine;
        this.kt = kt;
        this.ior = ior;
    }

    /**
     * Generate a random color.
     * @return a Surface with random parameters
     */
    public static Surface random() {
        Random randomGen = new Random();
        return new Surface(colors[randomGen.nextInt(colors.length)], Math.random(), Math.random(), Math
                .random() * 20.0, 0., 1.);
    }

    @Override
    public String toString() {
        return "Surface { color=" + color + "}";
    }
}
