/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.ic2d.gui.components.util;

import org.objectweb.proactive.core.component.adl.vnexportation.ExportedVirtualNodesList;
import org.objectweb.proactive.core.component.adl.vnexportation.LinkedVirtualNode;

import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;


/**
 * @author Matthieu Morel
 *
 */
public class Verifier {
    public static void checkConsistencyOfExportedVirtualNodes(
        boolean displayIfOK) {
        List inconsistent_exported_vns = ExportedVirtualNodesList.instance()
                                                                 .getInconsistentExportedVirtualNodes();
        if (!inconsistent_exported_vns.isEmpty()) {
            Iterator iter = inconsistent_exported_vns.iterator();
            StringBuffer buff = new StringBuffer();
            while (iter.hasNext()) {
                LinkedVirtualNode lvn = (LinkedVirtualNode) iter.next();
                buff.append(lvn.getCompleteNameBeforeComposition() + " composed into " +
                    lvn.getExportedVirtualNodeNameAfterComposition() + "\n");
            }
            JOptionPane.showMessageDialog(null,
                "Inconsistent exported virtual nodes : \n" +
                "the following virtual nodes are defined as exported but they do not not compose virtual nodes :\n" +
                buff.toString());
        } else if (displayIfOK) {
            JOptionPane.showMessageDialog(null,
                "The composition of exported virtual nodes is fine");
        }
    }
}
