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
