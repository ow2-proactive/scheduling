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

import java.util.ArrayList;
import java.util.List;


/**
 * @author Matthieu Morel
 * <p>
 * This class is a link of a chained list of virtual nodes. An instance contains : <br>
 * - if it is part of a virtual node composition  (i.e. a "composingVirtualNode in the ADL) : 
 * a reference on a "composer" virtual node
 *-  if it is a composer of other virtual nodes (i.e. an "exportedVirtualNode" in the ADL) :
 * references on "composing" virtual nodes</p>
 * <p>LinkedVirtualNode elements inherit from the multiplicity of their composing nodes :
 * if at least one of them is multiple, then this LinkedVirtualNode is multiple</p>
 * 
 */
public class LinkedVirtualNode {
    private List composingVirtualNodes;
    private LinkedVirtualNode composer = null;
    private String componentName;
    private String virtualNodeName;
    private boolean isMultiple = false;

    public LinkedVirtualNode(String componentName, String virtualNodeName) {
        this.componentName = componentName;
        this.virtualNodeName = virtualNodeName;
        composingVirtualNodes = new ArrayList();
    }

    public boolean addComposingVirtualNode(LinkedVirtualNode vn) {
        setMultiple(vn.isMultiple());
        if (!composingVirtualNodes.contains(vn)) {
            composingVirtualNodes.add(vn);
            if (composer != null) {
                vn.composeIn(composer);
            } else {
                vn.composeIn(this);
            }
            return true;
        }
        return false;
    }

    public List getComposingVirtualNodes() {
        return composingVirtualNodes;
    }

    public void composeIn(LinkedVirtualNode vn) {
        composer = vn;
    }

    /**
     * @return the name of the virtual node given at construction time
     */
    public String getOriginalName() {
        return virtualNodeName;
    }

    /**
     *
     * @return the composer's name if there is one, the name of the virtualNode given at construction time otherwise
     */
    public String getExportedName() {
        return ((composer == null) ? virtualNodeName : composer.getExportedName());
    }

    public void setMultiple(boolean yes) {
        if (yes) {
            isMultiple = true;
            System.out.println("CARDINALITY : " + componentName + "." + virtualNodeName + " set to multiple");
        }
    }

    public boolean isMultiple() {
        return isMultiple;
    }
}
