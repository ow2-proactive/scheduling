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
package org.objectweb.proactive.core.component.adl.vnexportation;

import org.objectweb.proactive.core.component.adl.nodes.VirtualNode;


/**
 * A builder interface for composing exported virtual nodes.
 *
 * @author Matthieu Morel
 *
 */
public interface ExportedVirtualNodesBuilder {

    /**
     * <p> Composes exported virtual nodes. This is a way to reorganize the physical deployment of the components. </p>
     * <p> The exported virtual nodes that do not have parents (they are not themselves exported) *must* correspond
     * to the virtual nodes specified in the proactive deployment descriptor. They must also match cardinalities : if an
     * exported virtual node has a "multiple" cardinality", it must be corresponding to a "multiple" virtual node, i.e. a
     * virtual node that contains several nodes. </p>
     * <p>This method validates and performs the linkage, keeps it in memory, and allows hierarchical composition
     * of exported virtual nodes. </p>
     *
     * @param componentName the name of the component
     * @param exportedVirtualNodes an array of the exported virtual nodes elements for this component
     * @param currentComponentVN the virtual node to export
     * @throws Exception in case of a composition error
     */
    void compose(String componentName, ExportedVirtualNode[] exportedVirtualNodes,
            VirtualNode currentComponentVN) throws Exception;
}
