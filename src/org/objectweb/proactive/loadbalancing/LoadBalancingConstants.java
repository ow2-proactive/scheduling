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
package org.objectweb.proactive.loadbalancing;


/**
 * @author Javier.Bustos@sophia.inria.fr
 *
 */
public interface LoadBalancingConstants {

    /**
     * <code>UPDATE_TIME</code>: constant component for UPDATE time.
     */
    public static final long UPDATE_TIME = 20000;

    /**
     * <code>MIGRATION_TIME</code>: constant estimation of MIGRATION time.
     */
    public static final long MIGRATION_TIME = 5000;

    /**
     * <code>OVERLOADED_THREASHOLD</code>: begin of overloaded state.
     */
    public static final double OVERLOADED_THREASHOLD = 0.8;

    /**
     * <code>UNDERLOADED_THREASHOLD</code>: begin of underloaded state.
     */
    public static final double UNDERLOADED_THREASHOLD = 0.3;

    /**
     * <code>RANKING_NORMALIZATION</code>: Ranking of a "reference" CPU.
     */
    public static final double RANKING_NORMALIZATION = 3.3;

    /**
     * <code>RANKING_EPSILON</code>: Used to avoid similar CPUs rejections.
     */
    public static final double RANKING_EPSILON = 0.5;

    /**
     * <code>SUBSET_SIZE</code>: Number of acquaintances requested for balancing.
     */
    public static final int SUBSET_SIZE = 3;
}
