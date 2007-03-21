/*
* ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
package org.objectweb.proactive.extra.scheduler;


/**
 * This interface contains tunable paramters to be used by the scheudler
 * </br><b>WARNING. The conditions mentioned must be respected, otherwise, the scheduler might crash or have an irradical behavior</b>
 * @author walzouab
 *
 */
public interface SchedulerParameters1 {

    /**
     * The default policy to be used by the scheduler.
     * It is recommended to set it to FIFO
     */
    public final String DEFAULT_POLICY = "org.objectweb.proactive.extra.scheduler.policy.FIFOPolicy";

    /**
     * Time for the scheduler to sleep, or its heart beat
     * </br><b>Warning, Must be a positive integer<b>
     */
    public final long SCHEDULER_TIMEOUT = 1000;

    /**
     *        A grace period to wait before returning unused nodes fromt the scheduler to the resource manager
     * </br><b>WARNING, Must be a positive integer<b>
     */
    public final long TIME_BEFORE_RETURNING_NODES_TO_RM = SCHEDULER_TIMEOUT * 3;

    /**
     *        Time before all active objeccts are pinged to make sure they are still alive
     *        </br><b>WARNING, Must be a positive integer<b>
     */
    public final long TIME_BEFORE_TEST_ALIVE = SCHEDULER_TIMEOUT * 3;

    /**
     * Percentage to be returned of resources each time TIME_BEFORE_RETURNING_NODES_TO_RM times out.
     * Zero means to never return any nodes acquired, one means to return all nodes acquired
     * </br><b>Warning, Must be a double between zero and one<b>
     */
    public final double PERCENTAGE_OF_RESOURCES_TO_RETURN = 0.5;
}
