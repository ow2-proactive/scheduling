package org.objectweb.proactive.ic2d.timit.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.objectweb.proactive.ic2d.timit.data.TimerObject;
import org.objectweb.proactive.ic2d.timit.data.TimerTreeHolder;


public class DeleteTreeAction extends Action {
    public static final String DELETE_TREE_ACTION = "Delete Tree";
    private TimerObject target;

    public DeleteTreeAction() {
        super.setId(DELETE_TREE_ACTION);
        super.setImageDescriptor(ImageDescriptor.createFromFile(
                this.getClass(), "delete_obj.gif"));
        super.setToolTipText(DELETE_TREE_ACTION);
        super.setEnabled(false);
    }

    @Override
    public final void run() {
        TimerTreeHolder t = TimerTreeHolder.getInstance();
        if (t != null) {
            t.firePropertyChange(TimerTreeHolder.P_REMOVE_SELECTED, null, target);
            //t.removeDummyRoot(target);
        }
    }

    public final void setTarget(final TimerObject target) {
        if (target == null) {
            this.setEnabled(false);
            return;
        }
        this.target = target;
        this.setEnabled(true);
    }
}
