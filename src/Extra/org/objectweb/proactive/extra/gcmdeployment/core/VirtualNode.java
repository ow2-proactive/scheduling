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
package org.objectweb.proactive.extra.gcmdeployment.core;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Careful synchronization is required since Virtual Node can be returned
 * to the user before nodes registration.
 *
 * @author cmathieu
 *
 */
@PublicAPI
public interface VirtualNode {

    /**
     * A magic number to indicate that a Virtual Node is asking
     * for every available nodes
     */
    static final public long MAX_CAPACITY = -2;

    /**
     * Returns the name of this Virtual Node
     * @return name of the Virtual Node as declared inside the GCM Application Descriptor
     */
    public String getName();

    /**
     * Returns the capacity asked by this Virtual Node
     *
     * @return the capacity asked by this Virtual Node. If max is specified
     * in the GCM Application Descriptor then MAX_CAPACITY is returned.
     */
    public long getRequiredCapacity();
}
