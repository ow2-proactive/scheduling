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
package org.objectweb.proactive.ic2d.gui.components.model;

import java.util.List;

import org.objectweb.fractal.gui.model.Component;


/**
 * @author Matthieu Morel
 *
 */
public interface ProActiveComponent extends Component {
    public static final long VIRTUAL_NODE_INCORRECT_SYNTAX = 1 << 8;

    public String getVirtualNode();

    public void setVirtualNode(String virtualNode);

    public String getExportedVirtualNodesAfterComposition();

    public String getExportedVirtualNodesBeforeComposition();

    public List getExportedVirtualNodesNames();

    public List getComposingVirtualNodes(String exportedVirtualNodeName);

    public String getComposingVirtualNodesAsString(
        String exportedVirtualNodeName);

    public void setComposingVirtualNodes(String virtualNodeName,
        String composingVirtualNodes);

    public void addExportedVirtualNode(String virtualNodeName,
        String composingVirtualNodes);

    public String getExportedVirtualNodeNameAfterComposition(
        String exportedVNName);

    public void removeExportedVirtualNode(String virtualNodeName);

    public boolean isParallel();

    public void setParallel();

    public void setCurrentlyEditedExportedVirtualNodeName(
        String exportedVirtualNodeName);

    public String getCurrentlyEditedExportedVirtualNodeName();

    public void setCurrentlyEditedComposingVirtualNodesNames(
        String composingVirtualNodesNames);

    public String getCurrentlyEditedComposingVirtualNodesNames();
}
