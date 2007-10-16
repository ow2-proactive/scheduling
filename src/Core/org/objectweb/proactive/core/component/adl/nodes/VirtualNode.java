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
package org.objectweb.proactive.core.component.adl.nodes;


/**
 * This interface adds a cardinality attribute to the virtual node (compared to the VirtualNode interface
 * from the standard Fractal ADL). This allows a strict definition of how to use
 * the components deployed on such a virtual node : in case of a multiple virtual node,
 * the reference that we have on a primitive component is actually a reference on a group
 * of primitive components.<br>
 * This implies that method invocations will return groups of results, and this has to be known
 * in advance.
 *
 * @author Matthieu Morel
 */
public interface VirtualNode extends org.objectweb.fractal.adl.nodes.VirtualNode {
    public static String SINGLE = "single";
    public static String MULTIPLE = "multiple";

    /**
     * getter for the cardinality
     * @return the cardinality of the virtual node
     */
    String getCardinality();

    /**
     * setter for the cardinality
     * @param cardinality the cardinality of the virtual node
     */
    void setCardinality(String cardinality);
}
