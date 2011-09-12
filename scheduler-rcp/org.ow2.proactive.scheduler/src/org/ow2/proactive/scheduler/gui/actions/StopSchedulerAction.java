package org.ow2.proactive.scheduler.gui.actions;

import org.ow2.proactive.scheduler.Activator;
import org.ow2.proactive.scheduler.common.SchedulerStatus;
import org.ow2.proactive.scheduler.gui.Internal;
import org.ow2.proactive.scheduler.gui.data.ActionsManager;
import org.ow2.proactive.scheduler.gui.data.SchedulerProxy;

public class StopSchedulerAction  extends SchedulerGUIAction  {

	public StopSchedulerAction() {
		this.setText("Stop scheduler");
        this.setToolTipText("Stop the scheduler (this will finish all pending and running jobs)");
        this.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(
                Internal.IMG_SCHEDULERSTOP));
	}

	@Override
	public void setEnabled(boolean connected, SchedulerStatus schedulerStatus,
			boolean admin, boolean jobSelected, boolean owner,
			boolean jobInFinishQueue) {
		
//		if (connected && admin && (schedulerStatus != SchedulerStatus.KILLED) &&
//	            (schedulerStatus != SchedulerStatus.UNLINKED) &&
//	            (schedulerStatus != SchedulerStatus.SHUTTING_DOWN) && (schedulerStatus != SchedulerStatus.STOPPED)) {
//	            super.setEnabled(true);
//		} else
//			super.setEnabled(false);
	}

	
	 @Override
   public void run() {
	            SchedulerProxy.getInstance().stop();
	            getParent().getDisplay().asyncExec(new Runnable() {

                    public void run() {
                    	ActionsManager.getInstance().update();
                    }});

    }

	 
	 @Override
	public boolean isEnabled()
	{
		return true;
	}
	 
	 
}
