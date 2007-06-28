package org.objectweb.proactive.ic2d.timit.figures.listeners;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartListener;
import org.objectweb.proactive.ic2d.timit.editparts.ChartEditPart;
import org.objectweb.proactive.ic2d.timit.figures.ChartFigure;
import org.objectweb.proactive.ic2d.timit.views.TimItView;


public class ChartListener extends EditPartListener.Stub
    implements MouseListener {
    private static final int SELECTED_STATE = 1;
    private static final int UNSELECTED_STATE = 0;
    public static ChartEditPart lastSelected;
    protected ChartEditPart chartEditPart;

    public ChartListener(ChartEditPart chartEditPart) {
        this.chartEditPart = chartEditPart;
    }

    public final void mouseDoubleClicked(MouseEvent arg0) {
    }

    public final void mousePressed(MouseEvent arg0) {
        // Left click selection
        this.chartEditPart.setSelected(SELECTED_STATE);
    }

    public final void mouseReleased(MouseEvent arg0) {
    }

    @Override
    public final void selectedStateChanged(EditPart editPart) {
        if (this.chartEditPart.getSelected() == SELECTED_STATE) {
            if (lastSelected != null) {
                // Set lastSelected Unselected
                lastSelected.setSelected(UNSELECTED_STATE);
            }
            // Save last selected editpart
            lastSelected = this.chartEditPart;
            // Perform button and figure selection
            TimItView.refreshSelectedButton.setEnabled(true);
            ((ChartFigure) this.chartEditPart.getFigure()).setSelected();
        } else {
            ((ChartFigure) this.chartEditPart.getFigure()).setUnselected();
        }
    }
}
