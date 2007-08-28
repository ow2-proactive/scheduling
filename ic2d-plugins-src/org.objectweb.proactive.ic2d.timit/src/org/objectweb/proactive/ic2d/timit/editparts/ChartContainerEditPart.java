package org.objectweb.proactive.ic2d.timit.editparts;

import java.util.List;

import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.timit.data.ChartContainerObject;
import org.objectweb.proactive.ic2d.timit.data.ChartObject;
import org.objectweb.proactive.ic2d.timit.views.TimItView;


/**
 * This class represents the controller object of the chart container.
 * @author vbodnart
 *
 */
public class ChartContainerEditPart extends AbstractGraphicalEditPart
    implements Runnable {
    protected TimItView timItView;

    /**
     * The constructor of this edit part
     * @param model
     */
    public ChartContainerEditPart(ChartContainerObject model,
        TimItView timItView) {
        this.setModel(model);
        model.setEp(this);
        this.timItView = timItView;
    }

    /**
     * Creates the associated figure object
     * @return The figure
     */
    @Override
    protected IFigure createFigure() {
        FreeformLayer layer = new FreeformLayer();
        ToolbarLayout layout = new ToolbarLayout(false);
        layout.setStretchMinorAxis(false);
        layout.setSpacing(0);
        layer.setLayoutManager(layout);
        return layer;
    }

    @Override
    protected void createEditPolicies() {
    }

    /**
     * Returns a list of children models
     * @return The list of children
     */
    @Override
    protected List<ChartObject> getModelChildren() {
        List<ChartObject> l = ((ChartContainerObject) getModel()).getChildrenList();

        // If the list is not empty the 
        if (l.size() != 0) {
            this.timItView.getRefreshAllButton().setEnabled(true);
        }
        return l;
    }

    /**
     * Asynchronous refresh of this edit part.
     */
    public void asyncRefresh() {
        Display.getDefault().asyncExec(this);
    }

    /**
     * The run method performs the refresh
     */
    @Override
    public void run() {
        refresh();
    }
}
