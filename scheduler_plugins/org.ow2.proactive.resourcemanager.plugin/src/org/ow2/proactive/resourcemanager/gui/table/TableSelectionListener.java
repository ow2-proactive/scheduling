package org.ow2.proactive.resourcemanager.gui.table;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.ow2.proactive.resourcemanager.gui.data.RMStore;
import org.ow2.proactive.resourcemanager.gui.handlers.RemoveNodesHandler;


public class TableSelectionListener implements ISelectionChangedListener {

    public void selectionChanged(SelectionChangedEvent event) {
        Object selection = event.getSelectionProvider().getSelection();
        List<NodeTableItem> list = (List<NodeTableItem>) ((IStructuredSelection) selection).toList();

        ArrayList<String> selectionList = new ArrayList<String>();
        for (NodeTableItem item : list) {
            selectionList.add(item.getNodeUrl());
        }

        //normally RM is connected if I can select something...
        if (RMStore.isConnected()) {
            RemoveNodesHandler.getInstance().setSelectedNodes(selectionList);
        }
    }

}
