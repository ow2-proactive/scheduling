package org.objectweb.proactive.ic2d.chartit.editpart;

import java.util.Random;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.ChartWithoutAxes;
import org.eclipse.birt.chart.model.DialChart;
import org.eclipse.birt.chart.model.attribute.AxisType;
import org.eclipse.birt.chart.model.attribute.ChartDimension;
import org.eclipse.birt.chart.model.attribute.DataPoint;
import org.eclipse.birt.chart.model.attribute.DataPointComponentType;
import org.eclipse.birt.chart.model.attribute.IntersectionType;
import org.eclipse.birt.chart.model.attribute.LegendItemType;
import org.eclipse.birt.chart.model.attribute.LineDecorator;
import org.eclipse.birt.chart.model.attribute.Marker;
import org.eclipse.birt.chart.model.attribute.MarkerType;
import org.eclipse.birt.chart.model.attribute.Position;
import org.eclipse.birt.chart.model.attribute.RiserType;
import org.eclipse.birt.chart.model.attribute.TickStyle;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.attribute.impl.DataPointComponentImpl;
import org.eclipse.birt.chart.model.attribute.impl.JavaNumberFormatSpecifierImpl;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.BaseSampleData;
import org.eclipse.birt.chart.model.data.DataFactory;
import org.eclipse.birt.chart.model.data.NumberDataSet;
import org.eclipse.birt.chart.model.data.OrthogonalSampleData;
import org.eclipse.birt.chart.model.data.SampleData;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.TextDataSet;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.data.impl.TextDataSetImpl;
import org.eclipse.birt.chart.model.impl.ChartWithAxesImpl;
import org.eclipse.birt.chart.model.impl.ChartWithoutAxesImpl;
import org.eclipse.birt.chart.model.impl.DialChartImpl;
import org.eclipse.birt.chart.model.layout.Legend;
import org.eclipse.birt.chart.model.layout.Plot;
import org.eclipse.birt.chart.model.type.AreaSeries;
import org.eclipse.birt.chart.model.type.BarSeries;
import org.eclipse.birt.chart.model.type.DialSeries;
import org.eclipse.birt.chart.model.type.LineSeries;
import org.eclipse.birt.chart.model.type.PieSeries;
import org.eclipse.birt.chart.model.type.impl.AreaSeriesImpl;
import org.eclipse.birt.chart.model.type.impl.BarSeriesImpl;
import org.eclipse.birt.chart.model.type.impl.DialSeriesImpl;
import org.eclipse.birt.chart.model.type.impl.LineSeriesImpl;
import org.eclipse.birt.chart.model.type.impl.PieSeriesImpl;
import org.eclipse.swt.widgets.Composite;
import org.objectweb.proactive.ic2d.chartit.canvas.BIRTChartCanvas;
import org.objectweb.proactive.ic2d.chartit.data.ChartModel;
import org.objectweb.proactive.ic2d.chartit.util.Utils;


/**
 * 
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>. 
 */
public class BIRTChartBuilder {

    public static final String[] SINGLE_CATEGORY = new String[] { "Category" };

    /**
     * Font name for all titles, labels, and values.
     */
    protected final static String FONT_NAME = "Monospaced";

    /**
     * Font size for all titles, labels, and values.
     */
    protected final static int FONT_SIZE = 8;

    public static final BIRTChartCanvas build(final Composite client, final int style, final ChartModel model) {
        Chart chart = null;
        switch (model.getChartType()) {
            case PIE:
                chart = createPieChart(model);
                break;
            case BAR:
                chart = createBarChart(model, RiserType.RECTANGLE_LITERAL);
                break;
            case TUBE:
                chart = createBarChart(model, RiserType.TUBE_LITERAL);
                break;
            case CONE:
                chart = createBarChart(model, RiserType.CONE_LITERAL);
                break;
            case PYRAMID:
                chart = createBarChart(model, RiserType.TRIANGLE_LITERAL);
                break;
            case AREA:
                chart = createAreaChart(model);
                break;
            case LINE:
                chart = createLineChart(model);
                break;
            case METER:
                chart = createMeterChart(model);
                break;
            default:
                throw new RuntimeException("Unknown chart type : " + model.getChartType());
        }

        // Create the canvas
        return new BIRTChartCanvas(client, style, chart);
    }

    /**
     * Creates a pie chart model as a reference implementation
     * 
     * @return An instance of the simulated runtime chart model (containing
     *         filled datasets)
     */
    @SuppressWarnings("unchecked")
    public static final Chart createPieChart(final ChartModel model) {
        ChartWithoutAxes cwoaPie = ChartWithoutAxesImpl.create();
        cwoaPie.setDimension(ChartDimension.TWO_DIMENSIONAL_LITERAL);

        // Plot
        cwoaPie.setSeriesThickness(0);
        cwoaPie.getBlock().setBackground(ColorDefinitionImpl.WHITE());
        Plot p = cwoaPie.getPlot();
        // p.getClientArea().setBackground(null);
        p.getClientArea().getOutline().setVisible(false);
        p.getOutline().setVisible(false);

        // Legend
        Legend lg = cwoaPie.getLegend();
        lg.setVisible(false);
        // lg.getText().getFont().setSize(16);
        // lg.setBackground(null);
        // lg.getOutline().setVisible(false);

        // Title
        cwoaPie.getTitle().getOutline().setVisible(false);
        cwoaPie.getTitle().setVisible(false);

        // Data Set
        TextDataSet categoryValues = TextDataSetImpl.create(model.getRuntimeNames());
        NumberDataSet seriesOneValues = NumberDataSetImpl.create(model.getRuntimeValues());

        // Base Series
        Series seCategory = SeriesImpl.create();
        seCategory.setDataSet(categoryValues);

        SeriesDefinition sd = SeriesDefinitionImpl.create();
        cwoaPie.getSeriesDefinitions().add(sd);
        sd.getSeriesPalette().shift(0);
        sd.getSeries().add(seCategory);

        // Orthogonal Series
        PieSeries sePie = (PieSeries) PieSeriesImpl.create();
        sePie.setExplosion(0);
        sePie.setDataSet(seriesOneValues);
        sePie.getLabel().getCaption().getFont().setSize(FONT_SIZE);
        sePie.getLabel().getCaption().getFont().setName(FONT_NAME);
        // sePie.setSeriesIdentifier("id");

        // Customize the series label name in order to have XXX (XX %)
        DataPoint dp = sePie.getDataPoint();
        dp.getComponents().clear();
        dp.setSeparator("(");
        dp.setSuffix(")");
        dp.getComponents().add(
                DataPointComponentImpl.create(DataPointComponentType.BASE_VALUE_LITERAL,
                        JavaNumberFormatSpecifierImpl.create("##")));
        dp.getComponents().add(
                DataPointComponentImpl.create(DataPointComponentType.PERCENTILE_ORTHOGONAL_VALUE_LITERAL,
                        JavaNumberFormatSpecifierImpl.create("##.##%")));

        SeriesDefinition sdCity = SeriesDefinitionImpl.create();
        // sdCity.getQuery().setDefinition("a.id");
        sd.getSeriesDefinitions().add(sdCity);
        sdCity.getSeries().add(sePie);

        return cwoaPie;
    }

    /**
     * Creates a bar chart model as a reference implementation
     * 
     * @return An instance of the simulated runtime chart model (containing
     *         filled datasets)
     */
    @SuppressWarnings("unchecked")
    public static Chart createBarChart(final ChartModel model, final RiserType riserType) {
        final ChartWithAxes cwaBar = ChartWithAxesImpl.create();

        // Plot
        cwaBar.getBlock().setBackground(ColorDefinitionImpl.WHITE());
        cwaBar.getBlock().getOutline().setVisible(false);

        // Title				
        cwaBar.getTitle().setVisible(false);

        // Legend
        final Legend lg = cwaBar.getLegend();
        // IMPORTANT : setting legend item type allows each bar be colored uniquely 
        lg.setItemType(LegendItemType.CATEGORIES_LITERAL);
        lg.setVisible(false);

        // X-Axis
        final Axis xAxisPrimary = cwaBar.getPrimaryBaseAxes()[0];
        xAxisPrimary.setType(AxisType.TEXT_LITERAL);
        xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
        xAxisPrimary.getOrigin().setType(IntersectionType.VALUE_LITERAL);
        xAxisPrimary.getLabel().getCaption().getFont().setSize(FONT_SIZE);
        xAxisPrimary.getLabel().getCaption().getFont().setName(FONT_NAME);
        xAxisPrimary.getTitle().setVisible(false);

        // Y-Axis
        final Axis yAxisPrimary = cwaBar.getPrimaryOrthogonalAxis(xAxisPrimary);
        yAxisPrimary.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);
        yAxisPrimary.setType(AxisType.LINEAR_LITERAL);
        // yAxisPrimary.getLabel().getCaption().getFont().setRotation(90);
        yAxisPrimary.getLabel().getCaption().getFont().setSize(FONT_SIZE);
        yAxisPrimary.getLabel().getCaption().getFont().setName(FONT_NAME);

        // Data Set
        final TextDataSet categoryValues = TextDataSetImpl.create(model.getRuntimeNames());
        final NumberDataSet orthoValues = NumberDataSetImpl.create(model.getRuntimeValues());

        final SampleData sd = DataFactory.eINSTANCE.createSampleData();
        final BaseSampleData sdBase = DataFactory.eINSTANCE.createBaseSampleData();
        sdBase.setDataSetRepresentation(Utils.EMPTY_STRING);
        sd.getBaseSampleData().add(sdBase);

        final OrthogonalSampleData sdOrthogonal = DataFactory.eINSTANCE.createOrthogonalSampleData();
        sdOrthogonal.setDataSetRepresentation("");
        sdOrthogonal.setSeriesDefinitionIndex(0);
        sd.getOrthogonalSampleData().add(sdOrthogonal);

        cwaBar.setSampleData(sd);

        // X-Series
        final Series seCategory = SeriesImpl.create();
        seCategory.setDataSet(categoryValues);

        final SeriesDefinition sdX = SeriesDefinitionImpl.create();
        sdX.getSeriesPalette().shift(0);
        xAxisPrimary.getSeriesDefinitions().add(sdX);
        sdX.getSeries().add(seCategory);

        // Y-Series
        final BarSeries bs = (BarSeries) BarSeriesImpl.create();
        bs.setDataSet(orthoValues);
        bs.setRiser(riserType);
        bs.getLabel().setVisible(true);
        bs.getLabel().getCaption().getFont().setSize(FONT_SIZE);
        bs.getLabel().getCaption().getFont().setName(FONT_NAME);
        bs.setLabelPosition(Position.INSIDE_LITERAL);

        final SeriesDefinition sdY = SeriesDefinitionImpl.create();
        yAxisPrimary.getSeriesDefinitions().add(sdY);
        sdY.getSeries().add(bs);

        return cwaBar;
    }

    /**
     * Creates a Area chart model as a reference implementation
     * 
     * @return An instance of the simulated runtime chart model (containing
     *         filled datasets)
     */
    @SuppressWarnings("unchecked")
    public static final Chart createAreaChart(final ChartModel model) {
        final ChartWithAxes cwaArea = ChartWithAxesImpl.create();

        // Plot
        cwaArea.getBlock().setBackground(ColorDefinitionImpl.WHITE());

        // Title
        cwaArea.getTitle().setVisible(false);

        // Legend
        cwaArea.getLegend().setVisible(false);

        // X-Axis
        final Axis xAxisPrimary = cwaArea.getPrimaryBaseAxes()[0];
        xAxisPrimary.setType(AxisType.TEXT_LITERAL);
        xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
        xAxisPrimary.getOrigin().setType(IntersectionType.VALUE_LITERAL);
        xAxisPrimary.getLabel().getCaption().getFont().setSize(FONT_SIZE);
        xAxisPrimary.getLabel().getCaption().getFont().setName(FONT_NAME);
        xAxisPrimary.getTitle().setVisible(false);

        // Y-Axis
        final Axis yAxisPrimary = cwaArea.getPrimaryOrthogonalAxis(xAxisPrimary);
        yAxisPrimary.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);
        yAxisPrimary.getLabel().getCaption().getFont().setSize(FONT_SIZE);
        yAxisPrimary.getLabel().getCaption().getFont().setName(FONT_NAME);

        // Data Set
        final TextDataSet categoryValues = TextDataSetImpl.create(model.getRuntimeNames());
        final NumberDataSet orthoValues = NumberDataSetImpl.create(model.getRuntimeValues());

        // X-Series
        final Series seCategory = SeriesImpl.create();
        seCategory.setDataSet(categoryValues);
        final SeriesDefinition sdX = SeriesDefinitionImpl.create();

        xAxisPrimary.getSeriesDefinitions().add(sdX);
        sdX.getSeries().add(seCategory);

        // Y-Series
        final AreaSeries as = (AreaSeries) AreaSeriesImpl.create();
        as.setDataSet(orthoValues);
        as.getLineAttributes().setColor(ColorDefinitionImpl.BLUE());
        as.getLabel().setVisible(true);
        as.getLabel().getCaption().getFont().setSize(FONT_SIZE);
        as.getLabel().getCaption().getFont().setName(FONT_NAME);

        final SeriesDefinition sssdY = SeriesDefinitionImpl.create();
        sssdY.getSeriesPalette().shift(-2);
        yAxisPrimary.getSeriesDefinitions().add(sssdY);
        sssdY.getSeries().add(as);

        return cwaArea;
    }

    /**
     * Creates a line chart model as a reference implementation
     * 
     * @return An instance of the simulated runtime chart model (containing
     *         filled datasets)
     */
    @SuppressWarnings("unchecked")
    public static final Chart createLineChart(final ChartModel model) {
        final ChartWithAxes cwaLine = ChartWithAxesImpl.create();

        // Plot
        cwaLine.getBlock().setBackground(ColorDefinitionImpl.WHITE());

        // Title
        cwaLine.getTitle().setVisible(false);

        // Legend
        cwaLine.getLegend().setVisible(false);

        // X-Axis
        final Axis xAxisPrimary = cwaLine.getPrimaryBaseAxes()[0];
        xAxisPrimary.setType(AxisType.TEXT_LITERAL);
        xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
        xAxisPrimary.getOrigin().setType(IntersectionType.VALUE_LITERAL);
        xAxisPrimary.getLabel().getCaption().getFont().setSize(FONT_SIZE);
        xAxisPrimary.getLabel().getCaption().getFont().setName(FONT_NAME);
        xAxisPrimary.getTitle().setVisible(false);

        // Y-Axis
        final Axis yAxisPrimary = cwaLine.getPrimaryOrthogonalAxis(xAxisPrimary);
        yAxisPrimary.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);
        yAxisPrimary.getLabel().getCaption().getFont().setSize(FONT_SIZE);
        yAxisPrimary.getLabel().getCaption().getFont().setName(FONT_NAME);

        // Data Set
        final TextDataSet categoryValues = TextDataSetImpl.create(model.getRuntimeNames());
        final NumberDataSet orthoValues = NumberDataSetImpl.create(model.getRuntimeValues());

        // X-Series
        final Series seCategory = SeriesImpl.create();
        seCategory.setDataSet(categoryValues);
        final SeriesDefinition sdX = SeriesDefinitionImpl.create();

        xAxisPrimary.getSeriesDefinitions().add(sdX);
        sdX.getSeries().add(seCategory);

        // Y-Series
        final LineSeries ls = (LineSeries) LineSeriesImpl.create();
        ls.setDataSet(orthoValues);
        ls.getLineAttributes().setColor(ColorDefinitionImpl.BLUE());
        for (int i = 0; i < ls.getMarkers().size(); i++) {
            ((Marker) ls.getMarkers().get(i)).setType(MarkerType.TRIANGLE_LITERAL);
        }
        ls.getLabel().setVisible(true);
        ls.getLabel().getCaption().getFont().setSize(FONT_SIZE);
        ls.getLabel().getCaption().getFont().setName(FONT_NAME);

        final SeriesDefinition sssdY = SeriesDefinitionImpl.create();
        sssdY.getSeriesPalette().shift(-2);
        yAxisPrimary.getSeriesDefinitions().add(sssdY);
        sssdY.getSeries().add(ls);

        return cwaLine;
    }

    /**
     * Creates a meter chart model as a reference implementation
     * 
     * @return An instance of the simulated runtime chart model
     */
    @SuppressWarnings("unchecked")
    public static final Chart createMeterChart(final ChartModel chartModel) {
        final DialChart dChart = (DialChart) DialChartImpl.create();
        dChart.setType("Meter Chart");
        dChart.setSubType("Superimposed Meter Chart");

        dChart.setDialSuperimposition(true);
        dChart.setGridColumnCount(2);
        dChart.setSeriesThickness(25);

        // Title/Plot
        dChart.getBlock().setBackground(ColorDefinitionImpl.WHITE());
        dChart.getTitle().setVisible(false);

        // Legend
        final Legend lg = dChart.getLegend();
        //LineAttributes lia = lg.getOutline( );
        lg.getText().getFont().setSize(FONT_SIZE);
        //lia.setStyle( LineStyle.SOLID_LITERAL );
        lg.getOutline().setVisible(false);
        lg.setShowValue(true);
        //lg.getClientArea( ).setBackground( ColorDefinitionImpl.PINK( ) );
        //lg.getClientArea( ).getOutline( ).setVisible( true );
        //lg.getTitle( ).getCaption( ).getFont( ).setSize( FONT_SIZE );
        //lg.getTitle( ).setInsets( InsetsImpl.create( 10, 10, 10, 10 ) );
        //lg.setTitlePosition( Position.ABOVE_LITERAL );
        lg.setItemType(LegendItemType.SERIES_LITERAL);

        // Data Set
        final TextDataSet categoryValues = TextDataSetImpl.create(SINGLE_CATEGORY);

        final SampleData sd = DataFactory.eINSTANCE.createSampleData();
        final BaseSampleData base = DataFactory.eINSTANCE.createBaseSampleData();

        base.setDataSetRepresentation(Utils.EMPTY_STRING);

        sd.getBaseSampleData().add(base);

        dChart.setSampleData(sd);

        final SeriesDefinition sdBase = SeriesDefinitionImpl.create();
        dChart.getSeriesDefinitions().add(sdBase);

        final Series seCategory = (Series) SeriesImpl.create();
        seCategory.setDataSet(categoryValues);
        sdBase.getSeries().add(seCategory);

        final SeriesDefinition sdOrth = SeriesDefinitionImpl.create();
        sdBase.getSeriesDefinitions().add(sdOrth);

        final int length = chartModel.getRuntimeNames().length;

        final Random generator = new Random(19580427);
        // Clear series palette
        sdOrth.getSeriesPalette().getEntries().clear();

        int r, g, b;
        OrthogonalSampleData sdOrthogonal;
        DialSeries seDial;
        NumberDataSetImpl t;
        for (int i = 0; i < length; i++) {
            r = generator.nextInt(Utils.MAX_RGB_VALUE);
            g = generator.nextInt(Utils.MAX_RGB_VALUE);
            b = generator.nextInt(Utils.MAX_RGB_VALUE);
            sdOrth.getSeriesPalette().getEntries().add(ColorDefinitionImpl.create(r, g, b));

            // Create sample data
            sdOrthogonal = DataFactory.eINSTANCE.createOrthogonalSampleData();
            sdOrthogonal.setDataSetRepresentation(Utils.EMPTY_STRING);
            sdOrthogonal.setSeriesDefinitionIndex(i);
            sd.getOrthogonalSampleData().add(sdOrthogonal);

            // Create dial series
            seDial = (DialSeries) DialSeriesImpl.create();
            t = new T(chartModel.getRuntimeValues(), i);
            seDial.setDataSet(t);
            seDial.getNeedle().setDecorator(LineDecorator.ARROW_LITERAL);
            seDial.getDial().getMinorGrid().getTickAttributes().setVisible(true);
            seDial.getDial().getMinorGrid().getTickAttributes().setColor(ColorDefinitionImpl.RED());
            seDial.getDial().getMinorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
            seDial.getDial().getLabel().getCaption().getFont().setSize(FONT_SIZE);
            // Set series identifier 
            seDial.setSeriesIdentifier(chartModel.getRuntimeNames()[i]);
            sdOrth.getSeries().add(seDial);
        }
        return dChart;
    }

    /**
     * A custom number data set used to simulate a pointer in an array of numbers 
     * 
     * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>. 
     */
    static final class T extends NumberDataSetImpl {
        final double[] array;
        final int indexInArray;
        final double[] res;

        /**
         * Creates a new instance of this class.
         * 
         * @param array The array used to retrieve an element in
         * @param indexInArray The index of the element in the array
         */
        public T(final double[] array, final int indexInArray) {
            this.array = array;
            this.indexInArray = indexInArray;
            this.res = new double[1];
            this.initialize();
            super.setValues(this.res);
        }

        @Override
        public Object getValues() {
            this.res[0] = this.array[this.indexInArray];
            return this.res;
        }
    }

}