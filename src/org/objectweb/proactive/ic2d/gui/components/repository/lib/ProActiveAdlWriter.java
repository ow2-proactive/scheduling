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
package org.objectweb.proactive.ic2d.gui.components.repository.lib;

import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.objectweb.fractal.gui.model.Component;
import org.objectweb.fractal.gui.repository.api.Storage;
import org.objectweb.fractal.gui.repository.lib.FractalAdlWriter;
import org.objectweb.proactive.core.component.adl.vnexportation.ExportedVirtualNodesList;
import org.objectweb.proactive.core.component.adl.vnexportation.LinkedVirtualNode;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.ic2d.gui.components.model.ProActiveComponent;


/**
 * @author Matthieu Morel
 *
 */
public class ProActiveAdlWriter extends FractalAdlWriter {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_GUI);

    public void saveComponent(final Component c, final AdlNode comp,
        final String name, final boolean internalType, final Map sharing)
        throws Exception {
        saveComponentHeader(c, comp, name, internalType);
        saveExportedVirtualNodes(c, comp);
        if (c.isComposite()) {
            saveCompositeComponent(c, comp, name, sharing);
        } else {
            savePrimitiveComponent(c, comp, name, sharing);
        }
        saveVirtualNode(c, comp);
    }

    public void saveVirtualNode(final Component component, final AdlNode comp) {
        ProActiveComponent c = (ProActiveComponent) component;
        if (c.getVirtualNode().length() > 0) {
            AdlNode vn = new AdlNode("virtual-node");

            // remove * at the end of the virtual node
            vn.astSetAttribute("name",
                c.getVirtualNode().endsWith("*")
                ? c.getVirtualNode()
                   .substring(0, c.getVirtualNode().length() - 1)
                : c.getVirtualNode());
            vn.astSetAttribute("cardinality",
                c.getVirtualNode().endsWith("*") ? "multiple" : "single");
            comp.astAddNode(vn);
        }
    }

    public void saveExportedVirtualNodes(final Component component,
        final AdlNode comp) {
        ProActiveComponent c = (ProActiveComponent) component;
        if (c.getExportedVirtualNodesAfterComposition().length() > 0) {
            AdlNode exportedVNs = new AdlNode("exportedVirtualNodes");
            StringTokenizer tokenizer = new StringTokenizer(c.getExportedVirtualNodesBeforeComposition(),
                    ";");
            Map map = ExportedVirtualNodesList.instance().getList();
            while (tokenizer.hasMoreTokens()) {
                AdlNode exportedVN = new AdlNode("exportedVirtualNode");
                AdlNode composedFrom = new AdlNode("composedFrom");
                String exported_vn_name = tokenizer.nextToken();
                if (exported_vn_name.endsWith("*")) {
                    exported_vn_name = exported_vn_name.substring(0,
                            exported_vn_name.length() - 1);
                }
                Map m = ExportedVirtualNodesList.instance().getList();
                LinkedVirtualNode lvn = findNode(c, exported_vn_name);

                // keep the original name of this exported vn
                exportedVN.astSetAttribute("name", lvn.getVirtualNodeName());

                Iterator it = lvn.getComposingVirtualNodes().iterator();
                while (it.hasNext()) {
                    LinkedVirtualNode linked_composing_vn = (LinkedVirtualNode) it.next();
                    AdlNode composingVN = new AdlNode("composingVirtualNode");
                    boolean self_exportation = linked_composing_vn.getDefiningComponentName()
                                                                  .equals(c.getName());
                    composingVN.astSetAttribute("component",
                        self_exportation ? "this"
                                         : linked_composing_vn.getDefiningComponentName());
                    //if (self_exportation) {
                    composingVN.astSetAttribute("name",
                        linked_composing_vn.getVirtualNodeName());
                    //} else {
                    // composingVN.astSetAttribute("name", linked_composing_vn.getExportedVirtualNodeNameBeforeComposition());
                    //}
                    composedFrom.astAddNode(composingVN);
                }
                exportedVN.astAddNode(composedFrom);
                exportedVNs.astAddNode(exportedVN);
            }
            comp.astAddNode(exportedVNs);
        }
    }

    private LinkedVirtualNode findNode(Component c, String vnName) {
        LinkedVirtualNode lvn = ExportedVirtualNodesList.instance()
                                                        .getNode(c.getName(),
                vnName, true);
        if (lvn == null) {
            logger.error("Not found : " + c.getName() + " , " + vnName);
        }
        try {
            if ((lvn.getComposingVirtualNodes().size() == 0) &&
                    (c.getParent() != null)) {
                // search the exported vn in enclosing components
                LinkedVirtualNode tmp = findNode(c.getParent(), vnName);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return lvn;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public void setForceInternal(boolean forceInternal) {
        this.forceInternal = forceInternal;
    }
}
