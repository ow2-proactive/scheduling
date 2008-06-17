package org.objectweb.proactive.ic2d.chartit.editpart;

import java.awt.Color;
import java.util.Random;

import org.eclipse.swt.widgets.Composite;
import org.objectweb.proactive.ic2d.chartit.canvas.RRD4JChartCanvas;
import org.objectweb.proactive.ic2d.chartit.data.ChartModel;
import org.objectweb.proactive.ic2d.chartit.data.store.IDataStore;
import org.objectweb.proactive.ic2d.chartit.util.Utils;
import org.rrd4j.ConsolFun;
import org.rrd4j.graph.RrdGraphDef;


/**
 * The implementation of this edit part uses RRD4J chronological, time line
 * based charts.
 * 
 * @author vbodnart
 * 
 */
public class RRD4JChartModelEditPart extends AbstractChartItEditPart<RRD4JChartCanvas> {

    /**
     * The RRD4J graph definition
     */
    private final RrdGraphDef graphDef;

    private final IDataStore dataStore;

    public RRD4JChartModelEditPart(final ChartModel dataElementModel, final IDataStore dataStore) {
        super(dataElementModel);
        this.dataStore = dataStore;
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
        newGraphDef.setShowSignature(false);
        newGraphDef.setColor(RrdGraphDef.COLOR_BACK, Color.white);
        final String[] runtimeNames = super.chartModel.getRuntimeNames();
        // If only one data provider
        if (runtimeNames.length == 1) {
            final String dataSourceName = runtimeNames[0];
            newGraphDef.datasource(dataSourceName, this.dataStore.getDataStoreName(), dataSourceName,
                    ConsolFun.AVERAGE);
            newGraphDef.area(dataSourceName, Color.LIGHT_GRAY, null);
            newGraphDef.line(dataSourceName, Color.BLACK, dataSourceName, 2);
        } else {
            // Set logarithmic to see all values
            newGraphDef.setAltAutoscale(true); // setLogarithmic(true);
            // A random generator is used to 
            final Random generator = new Random(Utils.SEED);
            // Stack all other data sources
            String dataSourceName;
            Color lineColor;
            for (int i = runtimeNames.length; --i >= 0;) {
                dataSourceName = runtimeNames[i];
                newGraphDef.datasource(dataSourceName, this.dataStore.getDataStoreName(), dataSourceName,
                        ConsolFun.AVERAGE);
                lineColor = new Color(generator.nextInt(Utils.MAX_RGB_VALUE), generator
                        .nextInt(Utils.MAX_RGB_VALUE), generator.nextInt(Utils.MAX_RGB_VALUE));
                newGraphDef.line(dataSourceName, lineColor, dataSourceName, 2);
            }
        }
        return newGraphDef;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        if (!super.canvas.isVisible())
            return;
        // Update time span of the graph definition
        this.canvas.updateTimeSpan(this.dataStore.getLeftBoundTime(), this.dataStore.getRightBoundTime());

        // Redraw the canvas
        this.canvas.redraw();
    }
}