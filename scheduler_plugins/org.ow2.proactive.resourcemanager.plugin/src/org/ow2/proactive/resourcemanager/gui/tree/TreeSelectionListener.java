package org.ow2.proactive.resourcemanager.gui.tree;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeElementType;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeLeafElement;
import org.ow2.proactive.resourcemanager.gui.data.model.TreeParentElement;
import org.ow2.proactive.resourcemanager.gui.handlers.RemoveNodesHandler;


public class TreeSelectionListener implements ISelectionChangedListener {

    public void selectionChanged(SelectionChangedEvent event) {
        List<TreeLeafElement> list;
        ArrayList<String> selectionList = new ArrayList<String>();
        if (event != null && event.getSelectionProvider() != null) {
            Object selection = event.getSelectionProvider().getSelection();
            if (selection != null) {
                list = (List<TreeLeafElement>) ((IStructuredSelection) selection).toList();
                for (TreeLeafElement leaf : list) {
                	getSubTreeNodesList(leaf, selectionList);
                }
            }
        }
        //normally RM is connected if I can select something...
        if (RMStore.isConnected()) {
            RemoveNodesHandler.getInstance().setSelectedNodes(selectionList);
        }
    }

    private void getSubTreeNodesList(TreeLeafElement leaf, ArrayList<String> selectList) {
        if (leaf.getType().equals(TreeElementType.NODE)) {
            if (!selectList.contains(leaf.getName()))
                selectList.add(leaf.getName());
        } else if (((TreeParentElement) leaf).hasChildren()) {
            for (TreeLeafElement element : ((TreeParentElement) leaf).getChildren()) {
            	getSubTreeNodesList(element, selectList);
            }
        }
    }
}
