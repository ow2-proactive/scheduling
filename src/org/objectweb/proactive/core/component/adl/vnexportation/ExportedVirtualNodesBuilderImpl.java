/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2004 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.core.component.adl.vnexportation;

import org.objectweb.proactive.core.component.adl.nodes.VirtualNode;




/**
 * @author Matthieu Morel
 *
 */
public class ExportedVirtualNodesBuilderImpl
    implements ExportedVirtualNodesBuilder {

    /**
     *
     */
    public void compose(String componentName,
        ExportedVirtualNode[] exportedVirtualNodes,
        VirtualNode currentComponentVN) throws Exception {

        for (int i = 0; i < exportedVirtualNodes.length; i++) {
            ComposingVirtualNode[] composing_vns = exportedVirtualNodes[i].getComposedFrom()
                                                                          .getComposingVirtualNodes();
            for (int j = 0; j < composing_vns.length; j++) {
                boolean composing_vn_is_multiple = false;
                if ("this".equals(composing_vns[j].getComponent())) {
                    if (currentComponentVN == null) {
                        throw new Exception(
                            "Trying to compose a virtual node from " +
                            composing_vns[j].getName() +
                            ", which is declared to be in the component " +
                            componentName +
                            " , but there is no virtual node defined in this component");
                    }
                    if (!currentComponentVN.getName().equals(composing_vns[j].getName())) {
                        throw new Exception(
                            "Trying to compose a virtual node from " +
                            composing_vns[i].getName() +
                            ", which is declared to be in the component " +
                            componentName + ", but " +
                            currentComponentVN.getName() + " is not defined " +
                            "in this component");
                    }
                    if (currentComponentVN.getCardinality().equals(VirtualNode.MULTIPLE)) {
                        composing_vn_is_multiple = true;
                    }
                    // change "this"
                    composing_vns[j].setComponent(componentName);    
                }
                ExportedVirtualNodesList.instance().compose(componentName,
                    exportedVirtualNodes[i], composing_vns[j],
                    composing_vn_is_multiple);
            }
        }
    }


    //       for (int i = path.size() - 1; i >= 0; --i) {
    //           List new_path = path.subList(0, path.size() -1 -i);
    //           if (path.get(i) instanceof ComponentContainer) {
    //               parseExportedVirtualNodes((ComponentContainer)path.get(i), new_path);
    //           }
    //       }           
}
