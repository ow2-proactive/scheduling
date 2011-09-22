package org.ow2.proactive.scheduler.gui.actions;

import org.ow2.proactive.scheduler.Activator;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.gui.Internal;
import org.ow2.proactive.scheduler.gui.data.ActionsManager;
import org.ow2.proactive.scheduler.gui.data.SchedulerProxy;


public class StartSchedulerAction extends SchedulerGUIAction {

    public StartSchedulerAction() {
        this.setText("Start scheduler");
        this.setToolTipText("Start the scheduler (this will start or restart the scheduler)");
        this.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(
                Internal.IMG_SCHEDULERSTART));
    }

    @Override
    public void setEnabled(boolean connected, SchedulerStatus schedulerStatus, boolean admin,
            boolean jobSelected, boolean owner, boolean jobInFinishQueue) {
        //        if (connected && admin && (schedulerStatus == SchedulerStatus.STOPPED)) {
        //        	super.setEnabled(true);
        //        } else
        //        	super.setEnabled(false);
        //allways enabled, will only be visible when needed:
        //super.setEnabled(true);

    }

    @Override
    public void run() {
        SchedulerProxy.getInstance().start();
        getParent().getDisplay().asyncExec(new Runnable() {
            public void run() {
                ActionsManager.getInstance().update();
            }
        });
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
