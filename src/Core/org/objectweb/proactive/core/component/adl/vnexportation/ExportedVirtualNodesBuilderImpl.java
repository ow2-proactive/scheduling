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

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.proactive.core.component.adl.nodes.VirtualNode;


/**
 * An implementation of the {@link org.objectweb.proactive.core.component.adl.vnexportation.ExportedVirtualNodesBuilder} interface.
 *
 * This class performs a logical composition of the exported virtual nodes of the components ADL.
 *
 *
 * @author Matthieu Morel
 *
 */
public class ExportedVirtualNodesBuilderImpl implements ExportedVirtualNodesBuilder {
    // implementation of ExportedVirtualNodesBuilder
    public void compose(String componentName, ExportedVirtualNode[] exportedVirtualNodes,
            VirtualNode currentComponentVN) throws ADLException {
        for (int i = 0; i < exportedVirtualNodes.length; i++) {
            ComposingVirtualNode[] composing_vns = exportedVirtualNodes[i].getComposedFrom()
                    .getComposingVirtualNodes();

            for (int j = 0; j < composing_vns.length; j++) {
                boolean composing_vn_is_multiple = false;
                if ("this".equals(composing_vns[j].getComponent())) {
                    if (currentComponentVN == null) {
                        throw new ADLException("Trying to compose a virtual node from " +
                            composing_vns[j].getName() + ", which is declared to be in the component " +
                            componentName + " , but there is no virtual node defined in this component", null);
                    }
                    if (!currentComponentVN.getName().equals(composing_vns[j].getName())) {
                        throw new ADLException("Trying to compose a virtual node from " +
                            composing_vns[i].getName() + ", which is declared to be in the component " +
                            componentName + ", but " + currentComponentVN.getName() + " is not defined " +
                            "in this component", null);
                    }

                    // change "this" into the name of the component
                    composing_vns[j].setComponent(componentName);
                }
                if ((currentComponentVN != null) &&
                    currentComponentVN.getCardinality().equals(VirtualNode.MULTIPLE)) {
                    composing_vn_is_multiple = true;
                }

                //                /System.out.println("COMPOSING : " + componentName + "." + exportedVirtualNodes[i].getName() + "--> " + composing_vns[j].getComponent() + "." + composing_vns[j].getName());
                ExportedVirtualNodesList.instance().compose(componentName, exportedVirtualNodes[i],
                        composing_vns[j], composing_vn_is_multiple);
                //System.out.println("COMPOSED VN LIST : " +ExportedVirtualNodesList.instance().toString());
            }
        }
    }
}
