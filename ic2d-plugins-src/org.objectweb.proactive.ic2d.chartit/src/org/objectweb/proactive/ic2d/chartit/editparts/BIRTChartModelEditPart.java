package org.objectweb.proactive.ic2d.chartit.editparts;

import java.beans.PropertyChangeEvent;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.chartit.canvas.BIRTChartCanvas;
import org.objectweb.proactive.ic2d.chartit.data.ChartModel;


/**
 * Series based edit part.
 * Graphical representation is based on various BIRT charts
 * @author vbodnart
 *
 */
public final class BIRTChartModelEditPart extends AbstractChartItEditPart<BIRTChartCanvas> {

    public BIRTChartModelEditPart(final ChartModel chartModel) {
        super(chartModel);
    }

    @Override
    public void fillSWTCompositeClient(final Composite client, final int style) {
        super.canvas = BIRTChartBuilder.build(client, style, super.chartModel);

        // Once the canvas has been created initialize this edit part
        super.init();
    }

    public void propertyChange(final PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(ChartModel.MODEL_CHANGED)) {
            Display.getDefault().asyncExec(this);
        }
    }

    public void run() {
        if (this.canvas.isDisposed())
            return;
        // Redraw the canvas
        this.canvas.refreshChartAndRedrawCanvas();
    }
}