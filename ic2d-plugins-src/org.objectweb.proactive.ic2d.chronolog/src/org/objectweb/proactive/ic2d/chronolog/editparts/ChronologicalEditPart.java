package org.objectweb.proactive.ic2d.chronolog.editparts;

import java.awt.Color;
import java.beans.PropertyChangeEvent;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.chronolog.canvas.RRD4JChartCanvas;
import org.objectweb.proactive.ic2d.chronolog.data.model.AbstractTypeModel;
import org.objectweb.proactive.ic2d.chronolog.data.model.NumberBasedTypeModel;
import org.objectweb.proactive.ic2d.chronolog.data.store.IDataStore;
import org.rrd4j.ConsolFun;
import org.rrd4j.graph.RrdGraphDef;


/**
 * The implementation of this edit part uses RRD4J chronological, time line based charts. 
 * @author vbodnart
 *
 */
public class ChronologicalEditPart extends AbstractChronologEditPart<NumberBasedTypeModel, RRD4JChartCanvas> {

    /**
     * The RRD4J graph definition
     */
    private final RrdGraphDef graphDef;

    private final IDataStore dataStore;

    public ChronologicalEditPart(final NumberBasedTypeModel dataElementModel) {
        super(dataElementModel);
        this.dataStore = dataElementModel.getResourceData().getModelsCollector().getDataStore();
        this.graphDef = this.createGraphDef();
    }

    @Override
    public void fillSWTCompositeClient(final Composite client, final int style) {
        super.canvas = new RRD4JChartCanvas(client, style, this.graphDef);

        // Initialize this edit part
        super.init();
    }

    /**
     * @return
     */
    private RrdGraphDef createGraphDef() {
        // Create Graph
        final RrdGraphDef newGraphDef = new RrdGraphDef();
        final String dataSourceName = super.dataElementModel.getDataProvider().getName();
        newGraphDef.datasource(dataSourceName, this.dataStore.getDataStoreName(), dataSourceName,
                ConsolFun.AVERAGE);
        newGraphDef.setShowSignature(false);
        newGraphDef.area(dataSourceName, Color.LIGHT_GRAY, null);
        newGraphDef.line(dataSourceName, Color.BLACK, null, 1); // Warning !! fill AWT Color
        newGraphDef.setColor(RrdGraphDef.COLOR_BACK, Color.white);
        return newGraphDef;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        if (super.canvas.isDisposed())
            return;
        // Update time span of the graph definition        
        this.canvas.updateTimeSpan(this.dataStore.getLeftBoundTime(), this.dataStore.getRightBoundTime());
        // Redraw the canvas
        this.canvas.redraw();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(AbstractTypeModel.ELEMENT_CHANGED)) {
            Display.getDefault().asyncExec(this);
        }
    }
}