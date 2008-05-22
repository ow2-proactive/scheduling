package org.objectweb.proactive.extensions.scheduler.gui.actions;

import org.eclipse.jface.action.Action;
import org.objectweb.proactive.extensions.scheduler.common.scheduler.SchedulerState;
import org.objectweb.proactive.extensions.scheduler.gui.data.ActionsManager;


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
