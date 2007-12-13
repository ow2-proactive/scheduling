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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.adl.nodes.VirtualNode;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * <p>A static container of exported / composed virtual nodes. </br>
 * It could be refactored as a standard Fractal component.</p>
 *
 * <p>The virtual nodes are organized in a set of linked list, where the base structural unit
 * is a {@link org.objectweb.proactive.core.component.adl.vnexportation.LinkedVirtualNode}.
 * </p>
 *
 * @author Matthieu Morel
 *
 */
public class ExportedVirtualNodesList {
    public static final String COMPOSING_VIRTUAL_NODES_REGEX = "[^.;]+[.][^.;]+[;]?";
    private Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_ADL);
    private static ExportedVirtualNodesList instance = null;
    static Map linkedVirtualNodes;
    public static final String EMPTY_COMPOSING_VIRTUAL_NODES = "composing_virtual_nodes";

    private ExportedVirtualNodesList() {
        linkedVirtualNodes = new Hashtable();
    }

    /**
     * Returns the unique instance
     * @return a unique instance for the vm
     */
    public static ExportedVirtualNodesList instance() {
        if (instance == null) {
            instance = new ExportedVirtualNodesList();
        }
        return instance;
    }

    public Map getList() {
        return linkedVirtualNodes;
    }

    /**
     * Links virtual nodes to composing nodes and to exporting node
     * @param exportedVNComponent the name of the component of the exported virtual node
     * @param exportedVN the name of the exported virtual node
     * @param baseVNComponent the name of a component of the base (composing) virtual node
     * @param baseVN the name of a base (composing) virtual node
     * @param composingVNIsMultiple true if the virtual node is multiple, false if it is single
     */
    public void compose(String exportedVNComponent, String exportedVN, String baseVNComponent, String baseVN,
            boolean composingVNIsMultiple) throws ADLException {
        LinkedVirtualNode composerNode = getNode(exportedVNComponent, exportedVN, true);
        LinkedVirtualNode composingNode = getNode(baseVNComponent, baseVN, true);

        //        if (exportedVNComponent.equals(baseVNComponent)) {
        //            composingNode.setSelfExported();
        //        }
        composingNode.setMultiple(composingVNIsMultiple);
        checkComposition(composerNode, composingNode);
        if (composingNode != composerNode) {
            boolean added = composerNode.addComposingVirtualNode(composingNode);
            if (added) {
                if (logger.isDebugEnabled()) {
                    logger.debug("COMPOSED " + exportedVNComponent + "." + exportedVN + " from " +
                        baseVNComponent + "." + baseVN);
                }
            }
        }
    }

    private void checkComposition(LinkedVirtualNode composerNode, LinkedVirtualNode composingNode)
            throws ADLException {
        if (composerNode.isMultiple()) {
            if (!composingNode.isMultiple()) {
                throw new ADLException("cannot compose " + composingNode.getDefiningComponentName() + '.' +
                    composingNode.getVirtualNodeName() + " which is SINGLE, whith composer virtual node " +
                    composerNode.getDefiningComponentName() + '.' + composerNode.getVirtualNodeName() +
                    " because it is already composed from a virtual node of cardinality MULTIPLE", null);
            }
        } else {
            if (!composerNode.getComposingVirtualNodes().isEmpty() && composingNode.isMultiple()) {
                throw new ADLException("cannot mix a MULTIPLE virtual node (" +
                    composingNode.getDefiningComponentName() + '.' + composingNode.getVirtualNodeName() +
                    " with SINGLE virtual nodes in composer node " + composerNode.getDefiningComponentName() +
                    '.' + composerNode.getVirtualNodeName(), null);
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
            ComposingVirtualNode composingVN, boolean composingVNIsMultiple) throws ADLException {
        compose(componentName, exportedVN.getName(), composingVN.getComponent(), "this".equals(composingVN
                .getName()) ? componentName : composingVN.getName(), composingVNIsMultiple);
    }

    public boolean addLeafVirtualNode(String componentName, String virtualNodeName, String cardinality) {
        LinkedVirtualNode oldLeaf = getLeafVirtualNode(componentName);
        if (oldLeaf != null) {
            // ensure only 1 leaf per component
            logger.info("removing old leaf virtual node : " + oldLeaf.toString());
            ((List) linkedVirtualNodes.get(componentName)).remove(oldLeaf);
            oldLeaf = null;
        }
        if (cardinality.equals(VirtualNode.MULTIPLE)) {
            // remove *
            virtualNodeName = virtualNodeName.substring(0, virtualNodeName.length() - 1);
        }
        LinkedVirtualNode lvn = getNode(componentName, virtualNodeName, false);

        if (lvn != null) {
            if (lvn.getComposingVirtualNodes().size() != 0) {
                logger.error("Cannot add leaf virtual node " + virtualNodeName +
                    " as it already exists and composes vn");
                return false;
            } else {
                lvn.setIsLeaf();
            }
        }
        lvn = getNode(componentName, virtualNodeName, true);
        lvn.setIsLeaf();
        lvn.setMultiple(VirtualNode.MULTIPLE.equals(cardinality));
        return true;
    }

    public LinkedVirtualNode getLeafVirtualNode(String componentName) {
        LinkedVirtualNode lvn = null;
        if (linkedVirtualNodes.containsKey(componentName)) {
            List exportedVNs = (List) linkedVirtualNodes.get(componentName);
            Iterator it = exportedVNs.iterator();
            while (it.hasNext()) {
                lvn = (LinkedVirtualNode) it.next();
                if (lvn.isLeaf()) {
                    // there is only 1 leaf per component
                    return lvn;
                }
            }
        }
        return null;
    }

    /**
     * Returns the linked virtual node corresponding to the given parameters.
     * @param componentName the name of the component
     * @param virtualNodeName the name of the linked virtual node
     * @param createIfNotFound if true, a {@link LinkedVirtualNode} is created with the given component name
     * and virtual node name. Exportation and composition is still empty for this newly created virtual node.
     * @return a linked virtual node
     */
    public LinkedVirtualNode getNode(String componentName, String virtualNodeName, boolean createIfNotFound) {
        LinkedVirtualNode lvn = null;
        if (linkedVirtualNodes.containsKey(componentName)) {
            List exportedVNs = (List) linkedVirtualNodes.get(componentName);
            Iterator it = exportedVNs.iterator();
            while (it.hasNext()) {
                lvn = (LinkedVirtualNode) it.next();
                if (lvn.getVirtualNodeName().equals(virtualNodeName)) {
                    return lvn;
                }
                if (lvn.getExportedVirtualNodeNameAfterComposition().equals(virtualNodeName)) {
                    return lvn;
                }
            }
            if (createIfNotFound) {
                // vn not listed
                lvn = new LinkedVirtualNode(componentName, virtualNodeName);
                exportedVNs.add(lvn);
                return lvn;
            }
        } else {
            if (createIfNotFound) {
                // component not listed
                List list = new ArrayList();
                list.add(lvn = new LinkedVirtualNode(componentName, virtualNodeName));
                linkedVirtualNodes.put(componentName, list);
                return lvn;
            }
        }

        return null;
    }

    /**
     * Builds a list of the exported (and only exported) linked virtual nodes for the component whose name is given
     * @param componentName the name of the component whose exported virtual nodes we want
     * @return the exported virtual nodes for that named component
     */
    public List getExportedVirtualNodes(String componentName) {
        List list = new ArrayList();
        if (linkedVirtualNodes.containsKey(componentName)) {
            List linked_vns = (List) linkedVirtualNodes.get(componentName);
            Iterator it = linked_vns.iterator();
            while (it.hasNext()) {
                LinkedVirtualNode linked_vn = (LinkedVirtualNode) it.next();
                if (linked_vn.isExported()) {
                    list.add(linked_vn);
                }
            }
        }
        return list;
    }

    public String getExportedVirtualNodesBeforeCompositionAsString(String componentName) {
        List evn = getExportedVirtualNodes(componentName);
        Iterator it = evn.iterator();
        StringBuffer buffer = new StringBuffer();
        LinkedVirtualNode lvn;
        while (it.hasNext()) {
            lvn = (LinkedVirtualNode) it.next();
            buffer.append(lvn.getExportedVirtualNodeNameBeforeComposition() + (lvn.isMultiple() ? "*" : ""));
            if (it.hasNext()) {
                buffer.append(";");
            }
        }
        return buffer.toString();
    }

    /**
     * Returns a String representation of the exported virtual nodes for the named component.
     * @param componentName the name of the component we are interested in
     * @return a String representation of the exported virtual nodes.
     */
    public String getExportedVirtualNodesAfterCompositionAsString(String componentName) {
        List evn = getExportedVirtualNodes(componentName);
        Iterator it = evn.iterator();
        StringBuffer buffer = new StringBuffer();
        LinkedVirtualNode lvn;
        while (it.hasNext()) {
            lvn = (LinkedVirtualNode) it.next();
            buffer.append(lvn.getExportedVirtualNodeNameAfterComposition() + (lvn.isMultiple() ? "*" : ""));
            if (it.hasNext()) {
                buffer.append(";");
            }
        }
        return buffer.toString();
    }

    /**
     * Adds to the composition an exported virtual node with its composing virtual nodes as a String.
     *
     * @param componentName the name of the component
     * @param virtualNode the name of the virtual node
     * @param composingVirtualNodes  a String of composing virtual nodes, which should match the following
     * regular expression : [^.;]+[.][^.;]+[;]? (example : comp1.VN1 ; comp2.VN2)
     */
    public void addExportedVirtualNode(String componentName, String virtualNode, String composingVirtualNodes) {
        if (!composingVirtualNodes.equals(EMPTY_COMPOSING_VIRTUAL_NODES) &&
            !(composingVirtualNodes.replaceAll(COMPOSING_VIRTUAL_NODES_REGEX, "").length() == 0)) {
            throw new IllegalArgumentException(
                "exported virtual nodes can only be made of one or several regular expressions like : [^.;]+[.][^.;]+[;]?\n" +
                    "and this exported virtual node is : " +
                    componentName +
                    "." +
                    virtualNode +
                    "-->" +
                    composingVirtualNodes);
        }
        LinkedVirtualNode exported_vn = getNode(componentName, virtualNode, true);
        if (virtualNode.equals(LinkedVirtualNode.EMPTY_VIRTUAL_NODE_NAME) ||
            composingVirtualNodes.equals(EMPTY_COMPOSING_VIRTUAL_NODES)) {
            exported_vn.addComposingVirtualNode(getNode(LinkedVirtualNode.EMPTY_COMPONENT_NAME,
                    LinkedVirtualNode.EMPTY_VIRTUAL_NODE_NAME, true));
            return;
        }

        //String[] split = composingVirtualNodes.split(COMPOSING_VIRTUAL_NODES_REGEX);
        String[] split = composingVirtualNodes.split(";");
        for (int i = 0; i < split.length; i++) {
            String vn = null;
            if (split[i].indexOf(".") == -1) {
                // incorrect syntax
                vn = "";
            } else {
                String component = split[i].substring(0, split[i].indexOf("."));
                vn = split[i].substring(split[i].indexOf(".") + 1, ((split[i].indexOf(";") == -1) ? split[i]
                        .length() : (split[i].length() - 1)));

                // compose composing vn into composer vn
                exported_vn.addComposingVirtualNode(getNode(component, vn, true));
            }
        }
    }

    /**
     * Removes a virtual node from the composition
     * @param componentName the name of the component
     * @param virtualNodeName the name of the virtual node
     */
    public void removeExportedVirtualNode(String componentName, String virtualNodeName) {
        if (linkedVirtualNodes.containsKey(componentName)) {
            List exportedVNs = (List) linkedVirtualNodes.get(componentName);
            Iterator it = exportedVNs.iterator();

            //List to_remove = new ArrayList();
            LinkedVirtualNode to_remove = null;
            while (it.hasNext()) {
                LinkedVirtualNode lvn = (LinkedVirtualNode) it.next();
                if (lvn.getVirtualNodeName().equals(virtualNodeName) ||
                    lvn.getExportedVirtualNodeNameAfterComposition().equals(virtualNodeName)) {
                    // as the current list should not be modified while iterated, keep a list
                    // of elements to remove
                    to_remove = lvn;
                }
            }
            if (to_remove == null) {
                logger.error("trying to remove virtual node " + componentName + "." + virtualNodeName +
                    ", but could not find it");
                return;
            }

            // remove from composer
            if (to_remove.getComposer() != null) {
                to_remove.getComposer().getComposingVirtualNodes().remove(to_remove);
            }
            exportedVNs.remove(to_remove);
            //            it = to_remove.iterator();
            //            while (it.hasNext()) {
            //                LinkedVirtualNode lvn = (LinkedVirtualNode) it.next();
            //                exportedVNs.remove(lvn);
            //                lvn = null;
            //                System.out.println();
            //            }
        }
    }

    /**
     * Returns the list of composing virtual nodes for the given exported virtual node as a list of linked virtual nodes
     * @param componentName the name of the component
     * @param virtualNodeName the name of the exported virtual node
     * @return a list of linked virtual nodes corresponding to the virtual nodes composing the specified exported virtual node
     */
    public List getComposingVirtualNodes(String componentName, String virtualNodeName) {
        LinkedVirtualNode lvn;
        if (linkedVirtualNodes.containsKey(componentName)) {
            List exportedVNs = (List) linkedVirtualNodes.get(componentName);
            Iterator it = exportedVNs.iterator();
            while (it.hasNext()) {
                lvn = (LinkedVirtualNode) it.next();
                if (lvn.getVirtualNodeName().equals(virtualNodeName)) {
                    return lvn.getComposingVirtualNodes();
                }
                if (lvn.getExportedVirtualNodeNameAfterComposition().equals(virtualNodeName)) {
                    return lvn.getComposingVirtualNodes();
                }
            }
        }
        return null;
    }

    /**
     * Sets the composing virtual nodes for a given exported virtual node
     * @param componentName the name of the component
     * @param virtualNodeName the name of the exported virtual node
     * @param composingVirtualNodes the list of composing virtual nodes  as a String matching the following
     * regular expression : "[^.;]+[.][^.;]+[;]?" (example : comp1.VN1 ; comp2.VN2)
     */
    public void setComposingVirtualNodes(String componentName, String virtualNodeName,
            String composingVirtualNodes) {
        checkComposingVirtualNodesSyntax(composingVirtualNodes);
        LinkedVirtualNode exported_vn = getNode(componentName, virtualNodeName, false);
        if (exported_vn == null) {
            throw new ProActiveRuntimeException("exported virtual node " + componentName + '.' +
                virtualNodeName + " not found");
        }

        // 1. clean up existing composing virtual nodes
        List old_composing_vns = exported_vn.getComposingVirtualNodes();
        Iterator it = old_composing_vns.iterator();
        while (it.hasNext()) {
            ((LinkedVirtualNode) it.next()).setComposer(null);
        }
        exported_vn.getComposingVirtualNodes().clear();

        // 2. add new composing virtual nodes
        String[] split = composingVirtualNodes.split(";");
        for (int i = 0; i < split.length; i++) {
            String component = split[i].substring(0, split[i].indexOf("."));
            String vn = split[i].substring(split[i].indexOf(".") + 1,
                    ((split[i].indexOf(";") == -1) ? split[i].length() : (split[i].length() - 1)));

            // compose composing vn into composer vn
            exported_vn.addComposingVirtualNode(getNode(component, vn, true));
        }
    }

    /**
     * Checks the syntax of the given String of composing virtual nodes
     * @param composingVirtualNodes a String of composing virtual nodes
     */
    public static void checkComposingVirtualNodesSyntax(String composingVirtualNodes) {
        if (!(composingVirtualNodes.replaceAll(ExportedVirtualNodesList.COMPOSING_VIRTUAL_NODES_REGEX, "")
                .length() == 0)) {
            throw new IllegalArgumentException(
                "composing virtual nodes can only be made of one or several regular expressions like : [^.;]+[.][^.;]+[;]?");
        }
    }

    /**
     * This method helps recognizing the exported virtual nodes causing problem in the composition
     * @return a list of exported virtual nodes causing problem
     */
    public List getInconsistentExportedVirtualNodes() {
        List wrong_exported_vns = new ArrayList();

        // 1. get iterator on lists of virtual nodes for each component
        Iterator iterator_on_lists_of_lvns = linkedVirtualNodes.values().iterator();

        // 2. loop : for each list (i.e. for each component), get lvns
        List lvns;
        LinkedVirtualNode lvn;
        while (iterator_on_lists_of_lvns.hasNext()) {
            lvns = (List) iterator_on_lists_of_lvns.next();
            Iterator iterator_on_lvns = lvns.iterator();

            // 3. loop : for each lvn of the list, check consistency
            while (iterator_on_lvns.hasNext()) {
                lvn = (LinkedVirtualNode) iterator_on_lvns.next();
                if (lvn.getComposingVirtualNodes().isEmpty() && !lvn.isSelfExported()) {
                    wrong_exported_vns.add(lvn);
                }
            }
        }
        return wrong_exported_vns;
    }

    /**
     * Empties the current composition
     *
     */
    public void empty() {
        linkedVirtualNodes.clear();
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        Iterator it1 = linkedVirtualNodes.keySet().iterator();
        while (it1.hasNext()) {
            String component_name = (String) it1.next();
            buffer.append(component_name + " : ");
            List list = (List) linkedVirtualNodes.get(component_name);
            Iterator it2 = list.iterator();
            while (it2.hasNext()) {
                LinkedVirtualNode lvn = (LinkedVirtualNode) it2.next();
                buffer.append(lvn.toString());
            }
            buffer.append("\n");
        }
        return buffer.toString();
    }
}
