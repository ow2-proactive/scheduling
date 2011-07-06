/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.gui.topology.prefuse;

import prefuse.util.force.AbstractForce;
import prefuse.util.force.ForceItem;
import prefuse.util.force.Spring;


/**
 * Force function that computes the force acting on ForceItems due to a
 * given Spring.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class LatencyForce extends AbstractForce {

    private static String[] pnames = new String[] { "SpringCoefficient", "DefaultSpringLength" };

    public static final float DEFAULT_SPRING_COEFF = 1E-4f;
    public static final float DEFAULT_MAX_SPRING_COEFF = 1E-3f;
    public static final float DEFAULT_MIN_SPRING_COEFF = 1E-5f;
    public static final float DEFAULT_SPRING_LENGTH = 80;
    public static final float DEFAULT_MIN_SPRING_LENGTH = 0;
    public static final float DEFAULT_MAX_SPRING_LENGTH = 2000;
    public static final int SPRING_COEFF = 0;
    public static final int SPRING_LENGTH = 1;

    /**
     * Create a new SpringForce.
     * @param springCoeff the default spring co-efficient to use. This will
     * be used if the spring's own co-efficient is less than zero.
     * @param defaultLength the default spring length to use. This will
     * be used if the spring's own length is less than zero.
     */
    public LatencyForce(float springCoeff, float defaultLength) {
        params = new float[] { springCoeff, defaultLength };
        minValues = new float[] { DEFAULT_MIN_SPRING_COEFF, DEFAULT_MIN_SPRING_LENGTH };
        maxValues = new float[] { DEFAULT_MAX_SPRING_COEFF, DEFAULT_MAX_SPRING_LENGTH };
    }

    /**
     * Constructs a new SpringForce instance with default parameters.
     */
    public LatencyForce() {
        this(DEFAULT_SPRING_COEFF, DEFAULT_SPRING_LENGTH);
    }

    /**
     * Returns true.
     * @see prefuse.util.force.Force#isSpringForce()
     */
    public boolean isSpringForce() {
        return true;
    }

    /**
     * @see prefuse.util.force.AbstractForce#getParameterNames()
     */
    protected String[] getParameterNames() {
        return pnames;
    }

    /**
     * Calculates the force vector acting on the items due to the given spring.
     * @param s the Spring for which to compute the force
     * @see prefuse.util.force.Force#getForce(prefuse.util.force.Spring)
     */
    public void getForce(Spring s) {
        ForceItem item1 = s.item1;
        ForceItem item2 = s.item2;
        float length = (s.length < 0 ? params[SPRING_LENGTH] : s.length);
        float x1 = item1.location[0], y1 = item1.location[1];
        float x2 = item2.location[0], y2 = item2.location[1];
        float dx = x2 - x1, dy = y2 - y1;
        float r = (float) Math.sqrt(dx * dx + dy * dy);
        if (r == 0.0) {
            dx = ((float) Math.random() - 0.5f) / 50.0f;
            dy = ((float) Math.random() - 0.5f) / 50.0f;
            r = (float) Math.sqrt(dx * dx + dy * dy);
        }
        float d = r - length;
        float coeff = (s.coeff < 0 ? params[SPRING_COEFF] : s.coeff) * d / r;
        item1.force[0] += coeff * dx;
        item1.force[1] += coeff * dy;
        item2.force[0] += -coeff * dx;
        item2.force[1] += -coeff * dy;
        //        System.out.println("LatencyForce.enclosing_method() length = " + length + " r = " + r  + " d = " + d + " d/r = " + d/r + " coeff = " + coeff + " dx = " + dx + " dy = " + dy);
    }

} // end of class SpringForce
