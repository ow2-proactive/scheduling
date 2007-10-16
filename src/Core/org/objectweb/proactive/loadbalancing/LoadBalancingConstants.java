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
    public static final double UNDERLOADED_THREASHOLD = 0.5;

    /**
     * <code>BALANCE_FACTOR</code>: Used to avoid similar CPUs rejections in load balancing.
     */
    public static final double BALANCE_FACTOR = 0.7;

    /**
     * <code>STEAL_FACTOR</code>: Used to avoid similar CPUs rejections in work stealing.
     */
    public static final double STEAL_FACTOR = 1.2;

    /**
     * <code>SUBSET_SIZE</code>: Number of acquaintances requested for balancing.
     */
    public static final int SUBSET_SIZE = 3;

    /**
     * <code>NEIGHBORS_TO_STEAL</code>: Number of acquaintances requested for work stealing.
     */
    public static final int NEIGHBORS_TO_STEAL = 1;
}
