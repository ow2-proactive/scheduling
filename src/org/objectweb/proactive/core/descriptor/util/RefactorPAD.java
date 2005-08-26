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
        List keyList = new ArrayList();
        for (Iterator iter = set.iterator(); iter.hasNext();) {
            String id = (String) iter.next();
            keyList.add(id);
        }

        //System.out.println("*** VN number before refactor : " +
        //    virtualNodesMapping.size());
        // for all nodes, if the current node is a main node, remove it
        for (int i = 0; i < keyList.size(); i++) {
            String id = (String) keyList.get(i);

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

        //System.out.println("*** VN number after refactor : " +
        //    virtualNodesMapping.size());
        return noMain;
    }
}
