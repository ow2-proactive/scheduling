package org.objectweb.proactive.ic2d.timit.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.objectweb.proactive.ic2d.timit.data.ChartObject;
import org.objectweb.proactive.ic2d.timit.data.TimerTreeHolder;


public class ShowInTreeViewAction extends Action {
    public static final String SHOW_IN_TREE_VIEW_ACTION = "Show in Tree View";
    private ChartObject target;

    public ShowInTreeViewAction() {
        super.setId(SHOW_IN_TREE_VIEW_ACTION);
        super.setImageDescriptor(ImageDescriptor.createFromFile(
                this.getClass(), "treeview.gif"));
        super.setToolTipText(SHOW_IN_TREE_VIEW_ACTION);
        super.setEnabled(false);
    }

    public final void setTarget(final ChartObject target) {
        super.setEnabled(true);
        this.target = target;
    }

    @Override
    public final void run() {
        IWorkbench iworkbench = PlatformUI.getWorkbench();
        IWorkbenchWindow currentWindow = iworkbench.getActiveWorkbenchWindow();
        IWorkbenchPage page = currentWindow.getActivePage();
        try {
            IViewPart part = page.showView(
                    "org.objectweb.proactive.ic2d.timit.views.TimerTreeView");

            if (target != null) {
                TimerTreeHolder.getInstance().provideChartObject(target, false);
                target = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
