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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * <p>
 * This class is a link of a chained list of virtual nodes. An instance specifies : <br>
 * - the name of the component which defines it<br>
 * - the name of this virtual node <br>
 * - if it is part of a virtual node composition  (i.e. a "composingVirtualNode in the ADL) :
 * a reference on a "composer" virtual node<br>
 *-  if it is a composer of other virtual nodes (i.e. an "exportedVirtualNode" in the ADL) :
 * references on "composing" virtual nodes</p>
 * <p>The highest virtual node in the hierarchy of composed virtual nodes gives its name to all underlying
 * virtual nodes. This name can be retreived by the method {@link #getExportedVirtualNodeNameAfterComposition()}.</p>
 * <p>LinkedVirtualNode elements inherit from the multiplicity of their composing nodes :
 * if at least one of them is multiple, then this LinkedVirtualNode is multiple</p>
 *
 *
 * @author Matthieu Morel
 */
public class LinkedVirtualNode {
    private List composingVirtualNodes;
    private LinkedVirtualNode composer = null;
    private String componentName;
    private String virtualNodeName;
    private boolean isMultiple = false;
    boolean isLeaf = false;

    //    /private boolean selfExported = false;
    public final static String EMPTY_COMPONENT_NAME = "component_name";
    public final static String EMPTY_VIRTUAL_NODE_NAME = "virtual_node_name";

    /**
     * Constructor
     * @param componentName the name of the component which defines this virtual node
     * @param virtualNodeName the name of the virtual node
     */
    public LinkedVirtualNode(String componentName, String virtualNodeName) {
        this.componentName = componentName;
        this.virtualNodeName = virtualNodeName;
        composingVirtualNodes = new ArrayList();
    }

    void setComposer(LinkedVirtualNode composer) {
        this.composer = composer;
    }

    /**
     * Adds a composing virtual node
     * @param vn a composing virtual node
     * @return true if the virtual node was added, false if it was already present as a composing virtual node
     */
    public boolean addComposingVirtualNode(LinkedVirtualNode vn) {
        //        System.out.println("ADDING " + vn.toString() + " \nTO : " + toString());
        setMultiple(vn.isMultiple());
        if (!composingVirtualNodes.contains(vn)) {
            composingVirtualNodes.add(vn);
            vn.setComposer(this);
            return true;
        }
        return false;
    }

    /**
     * Getter for the composing virtual nodes
     * @return the list of the composing virtual nodes for this virtual node
     */
    public List getComposingVirtualNodes() {
        return composingVirtualNodes;
    }

    /**
     * Returns a String representation of the composing virtual nodes
     * @return a String representation of the composing virtual nodes
     */
    public String getComposingVirtualNodesAsString() {
        Iterator iter = getComposingVirtualNodes().iterator();
        StringBuffer buff = new StringBuffer();
        while (iter.hasNext()) {
            LinkedVirtualNode element = (LinkedVirtualNode) iter.next();
            if (element.getDefiningComponentName().equals(EMPTY_COMPONENT_NAME) ||
                element.getVirtualNodeName().equals(EMPTY_VIRTUAL_NODE_NAME)) {
                continue;
            }
            buff.append(element.getDefiningComponentName() + "." + element.getVirtualNodeName());
            buff.append(";");
        }
        return buff.toString();
    }

    /**
     *
     * @return the name of the virtual node given at construction time
     */
    public String getVirtualNodeName() {
        return virtualNodeName;
    }

    /**
     *
     * @return name_of_the_defining_component.name_of_the_virtual_node_at_construction_time
     */
    public String getCompleteNameBeforeComposition() {
        return componentName + '.' + virtualNodeName;
    }

    /**
     * @return the name resulting from the composition. It corresponds to the name of the highest
     * virtual node in the hierarchy of composed virtual nodes
     */
    public String getExportedVirtualNodeNameAfterComposition() {
        return ((composer == null) ? virtualNodeName : composer.getExportedVirtualNodeNameAfterComposition());
    }

    /**
     *
     * @return the name
     */
    public String getExportedVirtualNodeNameBeforeComposition() {
        return ((composer == null) ? virtualNodeName : composer.getVirtualNodeName());
    }

    /**
     * Setter for cardinality
     * @param yes if true the cardinality is set to multiple
     */
    public void setMultiple(boolean yes) {
        if (yes) {
            isMultiple = true;
        }
    }

    /**
     * Getter for cardinality
     * @return true if this virtual node is multiple
     */
    public boolean isMultiple() {
        return isMultiple;
    }

    /**
     * Checks whether this virtual node is exported
     * @return true if this virtual node is an exported virtual node (i.e. if it is holds composing virtual nodes)
     */
    public boolean isExported() {
        return composingVirtualNodes.size() > 0;
    }

    /**
     * Returns the component that defined this linked virtual node
     * @return the component that defined this linked virtual node
     */
    public String getDefiningComponentName() {
        return componentName;
    }

    /**
     * Indicates that the composing virtual node and the exported virtual node are defined in the same component.
     *
     */

    //    public void setSelfExported() {
    //        selfExported=true;
    //    }
    /**
     * @return true if the composing virtual node and the exported virtual node are defined in the same component.
     */
    public boolean isSelfExported() {
        if (composer == null) {
            return false;
        }

        // FIXME find a better way
        return (isLeaf || composer.getDefiningComponentName().equals(getDefiningComponentName()));
    }

    /**
     *
     * @param componentName name of the component
     * @param virtualNodeName name of the virtual node
     * @return true if the current component contains the specified virtual node as a composing element.
     */
    public boolean isComposedFrom(String componentName, String virtualNodeName) {
        Iterator it = composingVirtualNodes.iterator();
        while (it.hasNext()) {
            LinkedVirtualNode lvn = (LinkedVirtualNode) it.next();
            if (lvn.getDefiningComponentName().equals(componentName)) {
                if (lvn.getVirtualNodeName().equals(virtualNodeName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * @return the composer linked virtual node. null if this virtual node is not exported
     */
    public LinkedVirtualNode getComposer() {
        return composer;
    }

    public void setIsLeaf() {
        isLeaf = true;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        if (composer == this) {
            buffer.append(getDefiningComponentName() + "." + getVirtualNodeName() + "<--" + componentName +
                "." + virtualNodeName + "-->{");
        } else if (composer != null) {
            buffer.append(composer.getDefiningComponentName() + "." + composer.getVirtualNodeName() + "<--" +
                componentName + "." + virtualNodeName + "-->{");
        }
        Iterator it = composingVirtualNodes.iterator();
        while (it.hasNext()) {
            LinkedVirtualNode lvn = (LinkedVirtualNode) it.next();
            if (lvn == this) {
                continue;
            }
            buffer.append(lvn.toString());
        }
        return buffer.append("}").toString();
    }
}
