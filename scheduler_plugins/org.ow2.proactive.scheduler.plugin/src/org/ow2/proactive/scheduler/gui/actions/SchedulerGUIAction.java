package org.ow2.proactive.scheduler.gui.actions;

import org.eclipse.jface.action.Action;
import org.ow2.proactive.scheduler.common.SchedulerState;
import org.ow2.proactive.scheduler.gui.data.ActionsManager;


/**
 * 
 *
 * @author The ProActive Team
 */
public abstract class SchedulerGUIAction extends Action {

    public SchedulerGUIAction() {
        ActionsManager.getInstance().addAction(this);
    }

    public abstract void setEnabled(boolean connected, SchedulerState schedulerState, boolean admin,
            boolean jobSelected, boolean owner, boolean jobInFinishQueue);
}
