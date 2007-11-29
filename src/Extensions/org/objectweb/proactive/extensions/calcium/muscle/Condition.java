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
package org.objectweb.proactive.extensions.calcium.muscle;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystem;


/**
 * This interface is used to provide evaluation
 * conditions in {@link org.objectweb.proactive.extensions.calcium.skeletons.Skeleton}s like: {@link org.objectweb.proactive.extensions.calcium.skeletons.DaC},
 * {@link org.objectweb.proactive.extensions.calcium.skeletons.If}, and {@link org.objectweb.proactive.extensions.calcium.skeletons.While}.
 *
 * @author The ProActive Team (mleyton)
 *
 */
@PublicAPI
public interface Condition<P> extends Muscle<P, Boolean> {
    public boolean evalCondition(SkeletonSystem system, P param)
        throws Exception;
}
