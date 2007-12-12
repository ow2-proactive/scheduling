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
package org.objectweb.proactive.extensions.resourcemanager.nodesource.frontend;

import java.util.HashMap;

import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.extensions.resourcemanager.nodesource.pad.PADNodeSource;


/**
 * Interface for {@link PADNodeSource} objects.
 *
 * /TODO gsigety, cdelbe: interface totally useless for the moment
 *
 * @author ProActive team.
 * @version 3.9
 * @since ProActive 3.9
 *
 */
public interface PADNSInterface {

    /**
     * add nodes by deploying a ProActive Descriptor, recover nodes created,
     * adding them to the node Source and register nodes to the RMCore.
     * @param pas ProActive deployment descriptor to deploy.
     */
    public void addNodes(ProActiveDescriptor pad);

    /**
     * @return the number of PADs handled by this {@link PADNodeSource}
     */
    public IntWrapper getSizeListPad();

    /**
     * @return the list of PADs handled by this {@link PADNodeSource}
     */
    public HashMap<String, ProActiveDescriptor> getListPAD();
}
