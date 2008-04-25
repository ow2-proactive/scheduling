package org.objectweb.proactive.ic2d.chronolog.editparts;

import java.beans.PropertyChangeEvent;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.chronolog.canvas.BIRTChartCanvas;
import org.objectweb.proactive.ic2d.chronolog.charting.BIRTChartBuilder;
import org.objectweb.proactive.ic2d.chronolog.data.model.AbstractTypeModel;
import org.objectweb.proactive.ic2d.chronolog.data.model.StringArrayBasedTypeModel;


/**
 * Series based edit part.
 * Graphical representation is based on various BIRT charts
 * @author vbodnart
 *
 */
public final class SeriesEditPart extends
        AbstractChronologEditPart<StringArrayBasedTypeModel, BIRTChartCanvas> {

    private final BIRTChartBuilder chartBuilder;

    public SeriesEditPart(final StringArrayBasedTypeModel dataElementModel) {
        super(dataElementModel);
        this.chartBuilder = new BIRTChartBuilder();
    }

    @Override
    public void fillSWTCompositeClient(final Composite client, final int style) {
        this.canvas = new BIRTChartCanvas(client, style);

        // Create the chart from the model        
        this.canvas.setChart(this.chartBuilder.buildFromModel(super.dataElementModel));

        // Initialize this edit part
        super.init();
    }

    public void propertyChange(final PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(AbstractTypeModel.ELEMENT_CHANGED)) {
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