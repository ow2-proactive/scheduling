package org.ow2.proactive.resourcemanager.gui.stats;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.ow2.proactive.resourcemanager.gui.data.model.RMModel;


public class StatsContentProvider implements IStructuredContentProvider {

    @Override
    public Object[] getElements(Object model) {
        if (model instanceof RMModel) {
            StatsItem freeNodesStat = new StatsItem("# free nodes", Integer.toString(((RMModel) model)
                    .getFreeNodesNumber()));
            StatsItem busyNodesStat = new StatsItem("# busy nodes", Integer.toString(((RMModel) model)
                    .getBusyNodesNumber()));
            StatsItem downNodesStat = new StatsItem("# down nodes", Integer.toString(((RMModel) model)
                    .getDownNodesNumber()));
            return new StatsItem[] { freeNodesStat, busyNodesStat, downNodesStat };
        }
        //should never return this, RMStatsViewer
        return new Object[] {};
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // TODO Auto-generated method stub

    }

}
