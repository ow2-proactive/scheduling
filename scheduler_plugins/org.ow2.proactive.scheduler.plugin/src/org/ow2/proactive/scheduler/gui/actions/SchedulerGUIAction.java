package org.ow2.proactive.scheduler.gui.actions;

import org.eclipse.jface.action.Action;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
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

    public abstract void setEnabled(boolean connected, SchedulerStatus schedulerStatus, boolean admin,
            boolean jobSelected, boolean owner, boolean jobInFinishQueue);
}
