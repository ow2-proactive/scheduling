package org.objectweb.proactive.ic2d.timit.figures.listeners;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartListener;
import org.objectweb.proactive.ic2d.timit.data.ChartObject;
import org.objectweb.proactive.ic2d.timit.editparts.ChartEditPart;
import org.objectweb.proactive.ic2d.timit.figures.ChartFigure;
import org.objectweb.proactive.ic2d.timit.views.TimItView;


public class ChartListener extends EditPartListener.Stub
    implements MouseListener {
    public static final int SELECTED_STATE = 1;
    public static final int UNSELECTED_STATE = 0;
    public static ChartEditPart lastSelected;
    protected ChartEditPart chartEditPart;

    public ChartListener(ChartEditPart chartEditPart) {
        this.chartEditPart = chartEditPart;
    }

    public final void mouseDoubleClicked(final MouseEvent arg0) {
//		IWorkbench iworkbench = PlatformUI.getWorkbench();
//		IWorkbenchWindow currentWindow = iworkbench.getActiveWorkbenchWindow();
//		IWorkbenchPage page = currentWindow.getActivePage();		
//		try {
//			IViewPart part = page.showView("org.objectweb.proactive.ic2d.timit.views.TimerTreeView");
//			TimerTreeHolder.getInstance().addChartObject((ChartObject)this.chartEditPart.getModel());					
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
    }

    public final void mousePressed(final MouseEvent arg0) {
        // Left click selection
        this.chartEditPart.setSelected(SELECTED_STATE);
        //TimerTreeHolder.getInstance().setSelectedIndex((ChartObject)this.chartEditPart.getModel());
    }

    public final void mouseReleased(final MouseEvent arg0) {
    }

    @Override
    public final void selectedStateChanged(final EditPart editPart) {
        if (this.chartEditPart.getSelected() == SELECTED_STATE) {
            if (lastSelected != null) {
                // Set lastSelected Unselected
                lastSelected.setSelected(UNSELECTED_STATE);
            }
            // Save last selected editpart
            lastSelected = this.chartEditPart;
            // Perform button and figure selection
            TimItView.refreshSelectedButton.setEnabled(true);
            ChartObject model = (ChartObject)this.chartEditPart.getModel();            
            if ( model != null && model.getTimerLevel().equals("Basic") ){
            	TimItView.timerLevelButton.setText("Switch to Detailed");
            } else {
            	TimItView.timerLevelButton.setText("Switch to Basic   ");
            }
            TimItView.timerLevelButton.setEnabled(true); 
            ((ChartFigure) this.chartEditPart.getFigure()).setSelected();
        } else {
            ((ChartFigure) this.chartEditPart.getFigure()).setUnselected();
        }
    }
}
