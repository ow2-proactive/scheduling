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
package org.objectweb.proactive.examples.webservices.c3dWS;

import java.awt.image.ColorModel;

import org.objectweb.proactive.examples.webservices.c3dWS.geom.Ray;
import org.objectweb.proactive.examples.webservices.c3dWS.geom.Vec;
import org.objectweb.proactive.examples.webservices.c3dWS.prim.Isect;
import org.objectweb.proactive.examples.webservices.c3dWS.prim.Light;
import org.objectweb.proactive.examples.webservices.c3dWS.prim.Primitive;
import org.objectweb.proactive.examples.webservices.c3dWS.prim.Surface;


/**
 * Rendering Engine used by Remote 3D
 * @version: 1.1 step 3
 * @author: Florian DOYON
 * @author: Wilfried KLAUSER
 */
public class C3DRenderingEngine implements java.io.Serializable {

    /**
     * Alpha channel
     */
    private static final int alpha = 255 << 24;

    /**
     * Null vector (for speedup, instead of <code>new Vec(0,0,0)</code>
     */
    private static final Vec voidVec = new Vec();

    /**
     * Lights for the rendering scene
     */
    private Light[] lights;

    /**
     * Objects (spheres) for the rendering scene
     */
    private Primitive[] prim;

    /**
     * Default RGB ColorModel for the newPixels(...)
     */
    private transient ColorModel model;

    /**
     * The view for the rendering scene
     */
    private View view;

    /**
     * Interval c3ddispatcher
     */
    private C3DDispatcher c3ddispatcher;

    /**
     * Current intersection instance (only one is needed!)
     */
    private Isect inter = new Isect();

    /**
     * Temporary ray
     */
    private Ray tRay = new Ray();

    /**
     * Temporary vect
     */
    private Vec L = new Vec();

    /**
     * Default constructor
     */
    public C3DRenderingEngine() {
    }

    /**
     * Constructor refernecing the current dispatcher
     */
    public C3DRenderingEngine(C3DDispatcher c3ddispatcher) {
        model = ColorModel.getRGBdefault();
        this.c3ddispatcher = c3ddispatcher;

        //System.out.println("Rendering id "+org.objectweb.proactive.ProActive.getBodyOnThis().getID());
    }

    /**
     * Feature the migration property
     */
    public void migrateTo(String nodeTarget) {
        try {
            org.objectweb.proactive.api.PAMobileAgent.migrateTo(nodeTarget);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Find closest Ray, return initialized Isect
     * with intersection information.
     */
    boolean intersect(Ray r, double maxt) {
        Isect tp;
        int i;
        int nhits;

        nhits = 0;
        inter.t = 1e9;

        for (i = 0; i < prim.length; i++) {
            // uses global temporary Prim (tp) as temp.object for speedup
            tp = prim[i].intersect(r);

            if ((tp != null) && (tp.t < inter.t)) {
                inter.t = tp.t;
                inter.prim = tp.prim;
                inter.surf = tp.surf;
                inter.enter = tp.enter;
                nhits++;
            }
        }

        return (nhits > 0) ? true : false;
    }

    /**
     * Checks if there is a shadow
     */
    int Shadow(Ray r, double tmax) {
        if (intersect(r, tmax)) {
            return 0;
        }

        return 1;
    }

    /**
     * Return the Vector's reflection direction
     */
    Vec SpecularDirection(Vec I, Vec N) {
        Vec r;
        r = Vec.comb(1.0 / Math.abs(Vec.dot(I, N)), I, 2.0, N);
        r.normalize();

        return r;
    }

    /**
     * Return the Vector's transmission direction
     */
    Vec TransDir(Surface m1, Surface m2, Vec I, Vec N) {
        double n1;
        double n2;
        double eta;
        double c1;
        double cs2;
        Vec r;
        n1 = (m1 == null) ? 1.0 : m1.ior;
        n2 = (m2 == null) ? 1.0 : m2.ior;
        eta = n1 / n2;
        c1 = -Vec.dot(I, N);
        cs2 = 1.0 - (eta * eta * (1.0 - (c1 * c1)));

        if (cs2 < 0.0) {
            return null;
        }

        r = Vec.comb(eta, I, (eta * c1) - Math.sqrt(cs2), N);
        r.normalize();

        return r;
    }

    /**
     * Returns the shaded color
     */
    Vec shade(int level, double weight, Vec P, Vec N, Vec I, Isect hit) {
        Vec tcol;
        Vec R;
        double t;
        double diff;
        double spec;
        Surface surf;
        Vec col;
        int l;

        col = new Vec();
        surf = hit.surf;
        R = new Vec();

        if (surf.shine > 1e-6) {
            R = SpecularDirection(I, N);
        }

        // Computes the effectof each light
        for (l = 0; l < lights.length; l++) {
            L.sub2(lights[l].pos, P);

            if (Vec.dot(N, L) >= 0.0) {
                t = L.normalize();

                tRay.P = P;
                tRay.D = L;

                // Checks if there is a shadow
                if (Shadow(tRay, t) > 0) {
                    diff = Vec.dot(N, L) * surf.kd * lights[l].brightness;

                    col.adds(diff, surf.color);

                    if (surf.shine > 1e-6) {
                        spec = Vec.dot(R, L);

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

        tRay.P = P;

        if ((surf.ks * weight) > 1e-3) {
            tRay.D = SpecularDirection(I, N);
            tcol = trace(level + 1, surf.ks * weight, tRay);
            col.adds(surf.ks, tcol);
        }

        if ((surf.kt * weight) > 1e-3) {
            if (hit.enter > 0) {
                tRay.D = TransDir(null, surf, I, N);
            } else {
                tRay.D = TransDir(surf, null, I, N);
            }

            tcol = trace(level + 1, surf.kt * weight, tRay);
            col.adds(surf.kt, tcol);
        }

        // garbaging...
        tcol = null;
        surf = null;

        return col;
    }

    /**
     * Launches a ray
     */
    Vec trace(int level, double weight, Ray r) {
        Vec P;
        Vec N;
        boolean hit;

        // Checks the recursion level
        if (level > 6) {
            return new Vec();
        }

        hit = intersect(r, 1e6);

        if (hit) {
            P = r.point(inter.t);
            N = inter.prim.normal(P);

            if (Vec.dot(r.D, N) >= 0.0) {
                N.negate();
            }

            return shade(level, weight, P, N, r.D, inter);
        }

        // no intersection --> col = 0,0,0
        return voidVec;
    }

    /**
     * Scan all pixels in the image intervals, have them traced
     * and set the result with newPixels(...) on the MemoryImagesource
     * <i>heavily optimized!!!</i>
     */
    public void render(int engine_number, Interval interval) {
        // Screen variables
        int[] row = new int[interval.width * (interval.yto - interval.yfrom)];
        int pixCounter = 0; //iterator

        // Renderding variables
        int x;

        // Renderding variables
        int y;

        // Renderding variables
        int red;

        // Renderding variables
        int green;

        // Renderding variables
        int blue;
        double xlen;
        double ylen;

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

        Ray r = new Ray(view.from, voidVec);
        Vec col = new Vec();

        // All loops are reversed for 'speedup' (cf. thinking in java p331)
        // For each line
        for (y = interval.yfrom; y < interval.yto; y++) {
            ylen = ((2.0 * y) / interval.width) - 1.0;

            // For each pixel of the line
            for (x = 0; x < interval.width; x++) {
                xlen = ((2.0 * x) / interval.width) - 1.0;
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
                row[pixCounter++] = alpha | (red << 16) | (green << 8) |
                    (blue);
            } // end for (x)
        } // end for (y)

        // sends the results to the dispatcher
        c3ddispatcher.setPixels(row, interval, engine_number);
    }

    /**
     * Creates the local objects used in the rendering
     */
    public void setScene(Scene scene) {
        int nLights = scene.getLights();
        int nObjects = scene.getObjects();

        lights = new Light[nLights];
        prim = new Primitive[nObjects];

        for (int l = 0; l < nLights; l++) {
            lights[l] = scene.getLight(l);
        }

        for (int o = 0; o < nObjects; o++) {
            prim[o] = scene.getObject(o);
        }

        this.view = scene.getView();
    }

    /**
     * Destructor
     * @exception Throwable exception requested by RMI
     */
    @Override
    protected void finalize() throws Throwable {
        //System.out.println("Engine halted and released");
        super.finalize();
    }

    /**
     * The pinging function called by <code>C3DDispatcher</code>
     * to get the avg. pinging time
     */
    public int ping() {
        return 0;
    }
}
