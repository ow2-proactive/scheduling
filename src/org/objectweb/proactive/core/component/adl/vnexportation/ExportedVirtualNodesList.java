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

import org.apache.log4j.Logger;

import org.objectweb.proactive.core.util.ProActiveLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * A static container of exported / composed virtual nodes.
 *
 * @author Matthieu Morel
 *
 */
public class ExportedVirtualNodesList {
    private Logger logger = ProActiveLogger.getLogger("components.adl");
    private static ExportedVirtualNodesList instance = null;
    static Map exports;

    private ExportedVirtualNodesList() {
        exports = new HashMap();
    }

    /**
     * Returns the only instance
     * @return a unique instance for the vm
     */
    public static ExportedVirtualNodesList instance() {
        if (instance == null) {
            instance = new ExportedVirtualNodesList();
        }
        return instance;
    }

    /**
     * Links virtual nodes to composing nodes and to exporting node
     * @param exportedVNComponent the name of the component of the exported virtual node
     * @param exportedVN the name of the exported virtual node
     * @param baseVNComponent the name of a component of the base (composing) virtual node
     * @param baseVN the name of a base (composing) virtual node
     * @param composingVNIsMultiple true if the virtual node is multiple, false if it is single
     */
    public void compose(String exportedVNComponent, String exportedVN,
        String baseVNComponent, String baseVN, boolean composingVNIsMultiple) {
        LinkedVirtualNode composerNode = getNode(exportedVNComponent,
                exportedVN, true);
        LinkedVirtualNode composingNode = getNode(baseVNComponent, baseVN, true);
        composingNode.setMultiple(composingVNIsMultiple);
        boolean added = composerNode.addComposingVirtualNode(composingNode);
        if (added) {
            if (logger.isDebugEnabled()) {
                logger.debug("COMPOSED " + exportedVNComponent + "." +
                    exportedVN + " from " + baseVNComponent + "." + baseVN);
            }
        }
    }

    /**
     * Links virtual nodes to composing nodes and to exporting node
     * @param componentName the name of the current component defining the exportation
     * @param exportedVN the name of the exported virtual node
     * @param composingVN the name of the component containing a composing virtual node
     * @param composingVNIsMultiple the name of the composing virtual node inside the
     */
    public void compose(String componentName, ExportedVirtualNode exportedVN,
        ComposingVirtualNode composingVN, boolean composingVNIsMultiple) {
        compose(componentName, exportedVN.getName(),
            composingVN.getComponent(),
            "this".equals(composingVN.getName()) ? componentName
                                                 : composingVN.getName(),
            composingVNIsMultiple);
    }

    public LinkedVirtualNode getNode(String componentName,
        String virtualNodeName, boolean createIfNotFound) {
        LinkedVirtualNode lvn = null;
        if (exports.containsKey(componentName)) {
            List exportedVNs = (List) exports.get(componentName);
            Iterator it = exportedVNs.iterator();
            while (it.hasNext()) {
                lvn = (LinkedVirtualNode) it.next();
                if (lvn.getOriginalName().equals(virtualNodeName)) {
                    return lvn;
                }
            }

            // none found : create one
            lvn = new LinkedVirtualNode(componentName, virtualNodeName);
            exportedVNs.add(lvn);
        } else {
            if (createIfNotFound) {
                // component not listed
                List list = new ArrayList();
                list.add(lvn = new LinkedVirtualNode(componentName,
                            virtualNodeName));
                exports.put(componentName, list);
            }
        }
        return lvn;
    }
}
