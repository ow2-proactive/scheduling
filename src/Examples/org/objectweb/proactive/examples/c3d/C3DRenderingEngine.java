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
package org.objectweb.proactive.examples.c3d;

import org.objectweb.proactive.examples.c3d.geom.Ray;
import org.objectweb.proactive.examples.c3d.geom.Scene;
import org.objectweb.proactive.examples.c3d.geom.Vec;
import org.objectweb.proactive.examples.c3d.prim.Isect;
import org.objectweb.proactive.examples.c3d.prim.Light;
import org.objectweb.proactive.examples.c3d.prim.Surface;
import org.objectweb.proactive.examples.c3d.prim.View;


/**
 * Rendering Engine used by C3D.
 * When fed a scene and an Interval, returns a 2D picture of the 3D scene.
 */
public class C3DRenderingEngine implements java.io.Serializable, RenderingEngine {
    private static final double INFINITE = 1e6;

    // Alpha channel
    private static final int alpha = 255 << 24;

    // the scene which should be drawn
    private Scene scene;

    // speed up contraptions
    // Current intersection instance (only one is needed!)
    private Isect inter = new Isect();

    // replace new Vec(0,0,0)
    private final Vec voidVec = new Vec();

    //Temporary ray and Vec
    private Ray tRay = new Ray();
    private Vec tmpVec = new Vec();

    // used by toString to show a better value than C3DRenderingEngine@11d2572 
    protected String name = "name not set";

    /** Default constructor needed by ProActive */
    public C3DRenderingEngine() {
    }

    /** Constructor sets the value used by toString()*/
    public C3DRenderingEngine(String name) {
        this.name = name;
    }

    /** Creates the local objects used in the rendering  */
    public void setScene(Scene scene) {
        this.scene = scene;
    }

    /** Trace and send back the result to the dispatcher. <i>Heavily optimized!!!</i>
     * @return the partial Image that was asked for */
    public Image2D render(int engineNb, Interval interval) {
        int[] row = new int[interval.totalImageWidth * (interval.yto - interval.yfrom)];
        int pixCounter = 0; //iterator

        // Rendering variables
        int x;

        // Rendering variables
        int y;
        int red;
        int green;
        int blue;
        double xlen;
        double ylen;

        View view = scene.getView();
        Vec viewVec = Vec.sub(view.at, view.from);
        viewVec.normalize();

        Vec tmpVec = new Vec(viewVec);
        tmpVec.scale(Vec.dot(view.up, viewVec));

        Vec upVec = Vec.sub(view.up, tmpVec);
        upVec.normalize();

        Vec leftVec = Vec.cross(view.up, viewVec);
        leftVec.normalize();

        double frustrumwidth = view.dist * Math.tan(view.angle);

        upVec.scale(-frustrumwidth);
        leftVec.scale(view.aspect * frustrumwidth);

        Ray r = new Ray(view.from, this.voidVec);
        Vec col = new Vec();

        // All loops are reversed for 'speedup' (cf. thinking in java p331)
        // For each line
        for (y = interval.yfrom; y < interval.yto; y++) {
            ylen = ((2.0 * y) / interval.totalImageWidth) - 1.0;

            // For each pixel of the line
            for (x = 0; x < interval.totalImageWidth; x++) {
                xlen = ((2.0 * x) / interval.totalImageWidth) - 1.0;
                r.D = Vec.comb(xlen, leftVec, ylen, upVec);
                r.D.add(viewVec);
                r.D.normalize();
                col = trace(0, 1.0, r);

                // computes the color of the ray
                red = (int) (col.x * 255.0);
                if (red > 255) {
                    red = 255;
                }
                green = (int) (col.y * 255.0);
                if (green > 255) {
                    green = 255;
                }
                blue = (int) (col.z * 255.0);
                if (blue > 255) {
                    blue = 255;
                }

                // Sets the pixels
                row[pixCounter++] = C3DRenderingEngine.alpha | (red << 16) | (green << 8) | (blue);
            } // end for (x)
        } // end for (y)

        // sends the result back to the dispatcher
        return (new Image2D(row, interval, engineNb));
    }

    /** Find closest Ray, return initialized Isect with intersection information.
     * @returns true if intersection is found */
    private boolean intersect(Ray r, double maxt) {
        int nhits = 0;
        this.inter.t = maxt;
        int nbprimitives = this.scene.getNbPrimitives();
        for (int i = 0; i < nbprimitives; i++) {
            // uses global temporary Prim (tp) as temp.object for speedup
            Isect tp = this.scene.getPrimitive(i).intersect(r);
            if ((tp != null) && (tp.t < this.inter.t)) {
                // Fixme : this is a clone, no ? Hide this in Isect.java 
                this.inter.t = tp.t;
                this.inter.prim = tp.prim;
                this.inter.enter = tp.enter;
                nhits++;
            }
        }
        return nhits > 0;
    }

    /** Checks if there is a shadow  */
    private boolean shadow(Ray r, double tmax) {
        return !intersect(r, tmax);
    }

    /** Return the Vector's reflection direction  */
    private Vec specularDirection(Vec I, Vec N) {
        Vec r;
        r = Vec.comb(1.0 / Math.abs(Vec.dot(I, N)), I, 2.0, N);
        r.normalize();
        return r;
    }

    /** Return the Vector's transmission direction */
    private Vec transDir(Surface m1, Surface m2, Vec I, Vec N) {
        double n1 = (m1 == null) ? 1.0 : m1.ior;
        double n2 = (m2 == null) ? 1.0 : m2.ior;
        double eta = n1 / n2;
        double c1 = -Vec.dot(I, N);
        double cs2 = 1.0 - (eta * eta * (1.0 - (c1 * c1)));
        if (cs2 < 0.0) {
            return null;
        }
        Vec r = Vec.comb(eta, I, (eta * c1) - Math.sqrt(cs2), N);
        r.normalize();
        return r;
    }

    /** Returns the shaded color */
    private Vec shade(int level, double weight, Vec P, Vec N, Vec I, Isect hit) {
        Vec tcol;
        Vec R;
        double diff;
        double spec;
        Surface surf;
        Vec col;
        int l;

        col = new Vec();
        surf = hit.prim.getSurface();
        R = new Vec();
        if (surf.shine > 1e-6) {
            R = specularDirection(I, N);
        }

        // Computes the effectof each light
        int nblights = this.scene.getNbLights();
        for (l = 0; l < nblights; l++) {
            Light light = this.scene.getLight(l);
            this.tmpVec.sub2(light.pos, P);
            if (Vec.dot(N, this.tmpVec) >= 0.0) {
                this.tmpVec.normalize();

                this.tRay.P = P;
                this.tRay.D = this.tmpVec;

                // Checks if there is a shadow
                if (shadow(this.tRay, C3DRenderingEngine.INFINITE)) {
                    diff = Vec.dot(N, this.tmpVec) * surf.kd * light.brightness;

                    col.adds(diff, surf.color);
                    if (surf.shine > 1e-6) {
                        spec = Vec.dot(R, this.tmpVec);
                        if (spec > 1e-6) {
                            spec = Math.pow(spec, surf.shine);
                            col.x += spec;
                            col.y += spec;
                            col.z += spec;
                        }
                    }
                }
            } // if
        } // for

        this.tRay.P = P;
        if ((surf.ks * weight) > 1e-3) {
            this.tRay.D = specularDirection(I, N);
            tcol = trace(level + 1, surf.ks * weight, this.tRay);
            col.adds(surf.ks, tcol);
        }
        if ((surf.kt * weight) > 1e-3) {
            if (hit.enter) {
                this.tRay.D = transDir(null, surf, I, N);
            } else {
                this.tRay.D = transDir(surf, null, I, N);
            }
            tcol = trace(level + 1, surf.kt * weight, this.tRay);
            col.adds(surf.kt, tcol);
        }

        // garbaging...
        tcol = null;
        surf = null;

        return col;
    }

    /** Launches a ray. Please note, as return may be this.voidVec,
     * the return value should not be modified, once returned! */
    private Vec trace(int level, double weight, Ray ray) {
        Vec P;
        Vec N;
        boolean hit;

        // Checks the recursion level
        if (level > 6) {
            return new Vec();
        }

        hit = intersect(ray, C3DRenderingEngine.INFINITE);
        if (hit) {
            P = ray.point(this.inter.t);
            N = this.inter.prim.normal(P);
            if (Vec.dot(ray.D, N) >= 0.0) {
                N.negate();
            }
            return shade(level, weight, P, N, ray.D, this.inter);
        }

        // no intersection --> col = 0,0,0
        return this.voidVec;
    }

    /** A textual representation of the renderer */
    @Override
    public String toString() {
        return this.name;
    }
}
