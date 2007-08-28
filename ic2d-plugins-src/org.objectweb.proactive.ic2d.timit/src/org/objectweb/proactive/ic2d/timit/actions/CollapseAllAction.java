package org.objectweb.proactive.ic2d.timit.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.objectweb.proactive.ic2d.timit.data.TimerObject;
import org.objectweb.proactive.ic2d.timit.data.TimerTreeHolder;


public class CollapseAllAction extends Action {
    public static final String COLLAPSE_ALL = "Collapse All";
    private TimerTreeHolder timerTreeHolder;

    public CollapseAllAction(TimerTreeHolder t) {
        this.timerTreeHolder = t;
        this.setId(COLLAPSE_ALL);
        this.setImageDescriptor(ImageDescriptor.createFromFile(
                this.getClass(), "collapseall.gif"));
        this.setToolTipText(COLLAPSE_ALL);
        this.setEnabled(true);
    }

    @Override
    public void run() {
        if ((this.timerTreeHolder == null) ||
                (this.timerTreeHolder.getChildren() == null)) {
            return;
        }
        for (TimerObject t : timerTreeHolder.getChildren()) {
            t.firePropertyChange(TimerObject.P_EXPAND_STATE, null, false);
        }
    }
}
