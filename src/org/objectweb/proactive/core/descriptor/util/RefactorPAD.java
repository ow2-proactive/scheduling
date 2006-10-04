/* 
 * ################################################################
 * 
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
package org.objectweb.proactive.core.descriptor.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeImpl;
import org.objectweb.proactive.core.mop.Utils;


/**
 * This class provides static methods to manipulate the java representation of XML descriptors
 * @version 1.0,  2005/09/20
 * @since   ProActive 3.0
 * @author ProActive team
 */
public class RefactorPAD {

    /**
     * returns a deep copy of the pad
     * used by all other methods before doing changes
     * @param pad
     * @return ProActiveDescriptor
     * @throws IOException
     */
    private static ProActiveDescriptor makeDeepCopy(ProActiveDescriptor pad)
        throws IOException {
        ProActiveDescriptor padCopy = (ProActiveDescriptor) Utils.makeDeepCopy(pad);
        return padCopy;
    }

    /**
     * return a copy of the pad after having removed all the "main" information
     * @param pad
     * @return refactored pad
     * @throws IOException
     */
    public static ProActiveDescriptor buildNoMainPAD(ProActiveDescriptor pad)
        throws IOException {
        ProActiveDescriptor noMain = makeDeepCopy(pad);

        // first remove all main definitions references by clearing the map
        noMain.getMainDefinitionMapping().clear();

        // then get the virtualnodemapping
        Map virtualNodesMapping = noMain.getVirtualNodeMapping();

        Set set = virtualNodesMapping.keySet();

        // do a copy of the keyList in a list to avoid concurrent problems with iter.next() when removing
        List<String> keyList = new ArrayList<String>();
        for (Iterator iter = set.iterator(); iter.hasNext();) {
            String id = (String) iter.next();
            keyList.add(id);
        }

        //System.out.println("*** VN number before refactor : " +
        //    virtualNodesMapping.size());
        // for all nodes, if the current node is a main node, remove it
        for (int i = 0; i < keyList.size(); i++) {
            String id = keyList.get(i);

            //System.out.println("*** VN searched : " + id);
            VirtualNode vn = (VirtualNode) virtualNodesMapping.get(id);

            //System.out.println("*** VN found : " + vn.getName());
            // test if the node is a virtual node lookup
            if (!vn.isLookup()) {
                // if not so it is a virtual node impl, so downcasting to check if main vn
                if (((VirtualNodeImpl) vn).isMainVirtualNode()) {
                    virtualNodesMapping.remove(id);
                    //System.out.println("*** VN to remove : " + vn.getName());
                }
            } else {
                //System.out.println("a VirtualNode lookup cannot be a main VN");
            }
        }
        noMain.setMainDefined(false);
        //System.out.println("*** VN number after refactor : " +
        //    virtualNodesMapping.size());
        return noMain;
    }
}
