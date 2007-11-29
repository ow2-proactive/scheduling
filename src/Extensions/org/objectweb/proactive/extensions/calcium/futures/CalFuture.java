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
package org.objectweb.proactive.extensions.calcium.futures;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.calcium.exceptions.MuscleException;
import org.objectweb.proactive.extensions.calcium.exceptions.TaskException;
import org.objectweb.proactive.extensions.calcium.statistics.Stats;


/**
 * This class represents a future result on a parameter submitted to the Calcium framework via a {@link org.objectweb.proactive.extensions.calcium.Stream Stream}.
 *
 * The ProActive Team (mleyton)
 */
@PublicAPI
public interface CalFuture<R> {

    /**
     * @return true if the result is available
     */
    public boolean isDone();

    /**
     * Waits if necessary for the computation to complete, and then retrieves its result.
     *
     * @return the computed result
     *
     * @throws InterruptedException
     * @throws MuscleException
     * @throws TaskException
     */
    public R get() throws InterruptedException, MuscleException, TaskException;

    /**
     * Retrieves the computation statistics.
     *
     * @return <code>null</code> if the result is not available. The stats otherwise.
     * @throws InterruptedException
     */
    public Stats getStats();
}
