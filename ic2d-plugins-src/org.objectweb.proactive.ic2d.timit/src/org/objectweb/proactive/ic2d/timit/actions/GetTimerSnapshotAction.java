package org.objectweb.proactive.ic2d.timit.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.objectweb.proactive.ic2d.monitoring.data.AOObject;
import org.objectweb.proactive.ic2d.monitoring.data.AbstractDataObject;
import org.objectweb.proactive.ic2d.monitoring.extpoints.IActionExtPoint;
import org.objectweb.proactive.ic2d.timit.data.ChartContainerObject;
import org.objectweb.proactive.ic2d.timit.data.ChartObject;
import org.objectweb.proactive.ic2d.timit.views.TimItView;


/**
 * This class defines an action that will be plugged to the
 * monitoring context menu.
 * After the user perform a click on an figure the associated model
 * reference will be provided to this action.
 * @author vbodnart
 *
 */
public class GetTimerSnapshotAction extends Action implements IActionExtPoint {
	
    public static final String GET_TIMER_SNAPSHOT = "Get timer snapshot";
    
    private AbstractDataObject object;
    
    private ChartContainerObject container;

    public GetTimerSnapshotAction() {
        this.setId(GET_TIMER_SNAPSHOT);
        this.setImageDescriptor(ImageDescriptor.createFromFile(
                this.getClass(), "timer.gif"));
        this.setToolTipText("Get timers snapshot from this object");
        this.setEnabled(false);
    }

    @Override
    public void run() {
        ///IWorkbenchWindow currentWindow = null;
        IWorkbench iworkbench = PlatformUI.getWorkbench();
        IWorkbenchWindow currentWindow = iworkbench.getActiveWorkbenchWindow();
        IWorkbenchPage page = currentWindow.getActivePage();
        try {
            IViewPart part = page.showView(
                    "org.objectweb.proactive.ic2d.timit.views.TimItView");

            if (ChartObject.DEBUG) {
                new ChartObject(((TimItView) part).getChartContainer(), null,
                    null);
                return;
            }

            // Pass the reference of the AbstractDataObject to the ChartContainerObject			
            if ((part != null) && part.getClass().equals(TimItView.class)) {
            	if ( this.container == null ) {
            		this.container = ((TimItView) part).getChartContainer(); 
            	}
                this.container.recognizeAndCreateChart(this.object);
            }

            this.object = null; // free the reference
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Implements IActionExtPoint setAbstractDataObject(AbstractDataObject) method
     */
    public final void setAbstractDataObject(final AbstractDataObject object) {
        this.object = object;
    }

	public final void setActiveSelect(final AOObject ref) {
		if ( this.container != null ){
			ChartObject chartObject = this.container.getChartObjectById(ref.getID());
			if ( chartObject != null ){
				chartObject.getEp().setSelection();
			}
		}		
	}
}
