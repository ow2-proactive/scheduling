/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.gui.table;

import java.util.ArrayList;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.ow2.proactive.resourcemanager.gui.data.model.Node;
import org.ow2.proactive.resourcemanager.gui.data.model.Root;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeLeafElement;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeParentElement;


public class TableContentProvider implements IStructuredContentProvider {

    public Object[] getElements(Object inputElement) {
        return (getAllTableItems((Root) inputElement)).toArray();
    }

    public void dispose() {
        // TODO Auto-generated method stub
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // TODO Auto-generated method stub
    }

    public ArrayList<Node> getAllTableItems(Root root) {
        ArrayList<Node> items = new ArrayList<Node>();
        synchronized (root) {
            //nodes sources
            for (TreeLeafElement src : root.getChildren()) {
                // String nodeSourceName = src.getName();
                TreeParentElement source = (TreeParentElement) src;
                //hosts
                for (TreeLeafElement hst : source.getChildren()) {
                    // String hostName = hst.getName();
                    TreeParentElement host = (TreeParentElement) hst;
                    //Vms
                    for (TreeLeafElement jvms : host.getChildren()) {
                        TreeParentElement jvm = (TreeParentElement) jvms;
                        //nodes
                        for (TreeLeafElement node : jvm.getChildren()) {
                            items.add((Node) node);
                        }
                    }
                }
            }
        }
        return items;
    }
}
