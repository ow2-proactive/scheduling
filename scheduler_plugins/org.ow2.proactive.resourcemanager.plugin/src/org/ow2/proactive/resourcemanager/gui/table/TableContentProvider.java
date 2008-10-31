package org.ow2.proactive.resourcemanager.gui.table;

import java.util.ArrayList;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.ow2.proactive.resourcemanager.gui.data.model.Node;
import org.ow2.proactive.resourcemanager.gui.data.model.Root;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeElementType;
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

    public ArrayList<NodeTableItem> getAllTableItems(Root root) {
        ArrayList<NodeTableItem> items = new ArrayList<NodeTableItem>();
        synchronized (root) {
            //nodes sources
            for (TreeLeafElement src : root.getChildren()) {
                String nodeSourceName = src.getName();
                TreeParentElement source = (TreeParentElement) src;
                //hosts
                for (TreeLeafElement hst : source.getChildren()) {
                    String hostName = hst.getName();
                    TreeParentElement host = (TreeParentElement) hst;
                    //Vms
                    for (TreeLeafElement jvms : host.getChildren()) {
                        TreeParentElement jvm = (TreeParentElement) jvms;
                        //nodes
                        for (TreeLeafElement node : jvm.getChildren()) {
                            String url = node.getName();
                            items.add(new NodeTableItem(nodeSourceName, hostName, ((Node) node).getState(),
                                url));
                        }
                    }
                }
            }
        }
        return items;
    }
}
