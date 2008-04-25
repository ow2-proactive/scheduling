package org.objectweb.proactive.ic2d.chronolog.charting;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.ChartWithoutAxes;
import org.eclipse.birt.chart.model.attribute.AxisType;
import org.eclipse.birt.chart.model.attribute.ChartDimension;
import org.eclipse.birt.chart.model.attribute.DataPoint;
import org.eclipse.birt.chart.model.attribute.DataPointComponentType;
import org.eclipse.birt.chart.model.attribute.IntersectionType;
import org.eclipse.birt.chart.model.attribute.Marker;
import org.eclipse.birt.chart.model.attribute.MarkerType;
import org.eclipse.birt.chart.model.attribute.Position;
import org.eclipse.birt.chart.model.attribute.TickStyle;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.attribute.impl.DataPointComponentImpl;
import org.eclipse.birt.chart.model.attribute.impl.JavaNumberFormatSpecifierImpl;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.NumberDataSet;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.TextDataSet;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.data.impl.TextDataSetImpl;
import org.eclipse.birt.chart.model.impl.ChartWithAxesImpl;
import org.eclipse.birt.chart.model.impl.ChartWithoutAxesImpl;
import org.eclipse.birt.chart.model.layout.Legend;
import org.eclipse.birt.chart.model.layout.Plot;
import org.eclipse.birt.chart.model.type.AreaSeries;
import org.eclipse.birt.chart.model.type.BarSeries;
import org.eclipse.birt.chart.model.type.LineSeries;
import org.eclipse.birt.chart.model.type.PieSeries;
import org.eclipse.birt.chart.model.type.impl.AreaSeriesImpl;
import org.eclipse.birt.chart.model.type.impl.BarSeriesImpl;
import org.eclipse.birt.chart.model.type.impl.LineSeriesImpl;
import org.eclipse.birt.chart.model.type.impl.PieSeriesImpl;


public class BIRTChartBuilder {

    /**
     * Font name for all titles, labels, and values.
     */
    protected final static String FONT_NAME = "Monospaced";

    /**
     * Font size for all titles, labels, and values.
     */
    protected final static int FONT_SIZE = 8;

    String[] seriesNames;
    Double[] seriesValues;

    public BIRTChartBuilder() {
        seriesNames = new String[] { "Empty" };
        seriesValues = new Double[] { 1d };
    }

    public Chart buildFromModel(final ISerieBased model) {
        Chart chart = null;
        seriesNames = model.getSerieNames();
        seriesValues = model.getSerieValues();
        switch (model.getChartChoice()) {
            case PIE:
                chart = this.createPieChart();
                break;
            case BAR:
                chart = this.createBarChart();
                break;
            case AREA:
                chart = this.createAreaChart();
                break;
            case LINE:
                chart = this.createLineChart();
                break;
            default:
                throw new RuntimeException("Unknown chart type : " + model.getChartChoice());
        }
        return chart;
    }

    /**
     * Creates a pie chart model as a reference implementation
     * 
     * @return An instance of the simulated runtime chart model (containing
     *         filled datasets)
     */
    @SuppressWarnings("unchecked")
    public final Chart createPieChart() {
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
        TextDataSet categoryValues = TextDataSetImpl.create(seriesNames);
        NumberDataSet seriesOneValues = NumberDataSetImpl.create(seriesValues);

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
        //sePie.setSeriesIdentifier("id");

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
        //sdCity.getQuery().setDefinition("a.id");
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
    public Chart createBarChart() {
        final ChartWithAxes cwaBar = ChartWithAxesImpl.create();

        // Plot
        cwaBar.getBlock().setBackground(ColorDefinitionImpl.WHITE());
        cwaBar.getBlock().getOutline().setVisible(false);
        Plot p = cwaBar.getPlot();
        //p.getClientArea().setBackground(ColorDefinitionImpl.WHITE());
        p.getOutline().setVisible(false);

        // Title
        // cwaBar.getTitle().getLabel().getCaption().setValue("Bar Chart");
        cwaBar.getTitle().setVisible(false);

        // Legend
        Legend lg = cwaBar.getLegend();
        // lg.getText().getFont().setSize(16);
        // lg.setItemType(LegendItemType.CATEGORIES_LITERAL);
        lg.setVisible(false);

        // X-Axis
        Axis xAxisPrimary = cwaBar.getPrimaryBaseAxes()[0];
        xAxisPrimary.setType(AxisType.TEXT_LITERAL);
        xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
        xAxisPrimary.getOrigin().setType(IntersectionType.VALUE_LITERAL);
        xAxisPrimary.getLabel().getCaption().getFont().setSize(FONT_SIZE);
        xAxisPrimary.getLabel().getCaption().getFont().setName(FONT_NAME);
        xAxisPrimary.getTitle().setVisible(false);

        // Y-Axis
        Axis yAxisPrimary = cwaBar.getPrimaryOrthogonalAxis(xAxisPrimary);
        yAxisPrimary.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);
        yAxisPrimary.setType(AxisType.LINEAR_LITERAL);
        //yAxisPrimary.getLabel().getCaption().getFont().setRotation(90);
        yAxisPrimary.getLabel().getCaption().getFont().setSize(FONT_SIZE);
        yAxisPrimary.getLabel().getCaption().getFont().setName(FONT_NAME);

        // Data Set
        TextDataSet categoryValues = TextDataSetImpl.create(seriesNames);

        NumberDataSet orthoValues = NumberDataSetImpl.create(seriesValues);

        // X-Series
        Series seCategory = SeriesImpl.create();
        seCategory.setDataSet(categoryValues);

        SeriesDefinition sdX = SeriesDefinitionImpl.create();
        sdX.getSeriesPalette().shift(0);
        xAxisPrimary.getSeriesDefinitions().add(sdX);
        sdX.getSeries().add(seCategory);

        // Y-Series
        BarSeries bs = (BarSeries) BarSeriesImpl.create();
        bs.setDataSet(orthoValues);
        bs.setRiserOutline(null);
        bs.getLabel().setVisible(true);
        bs.getLabel().getCaption().getFont().setSize(FONT_SIZE);
        bs.getLabel().getCaption().getFont().setName(FONT_NAME);
        bs.setLabelPosition(Position.INSIDE_LITERAL);

        SeriesDefinition sdY = SeriesDefinitionImpl.create();
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
    public final Chart createAreaChart() {
        ChartWithAxes cwaArea = ChartWithAxesImpl.create();

        // Plot
        cwaArea.getBlock().setBackground(ColorDefinitionImpl.WHITE());
        Plot p = cwaArea.getPlot();
        //p.getClientArea().setBackground(ColorDefinitionImpl.create(255, 255, 225));
        p.getOutline().setVisible(false);

        // Title
        //cwaLine.getTitle().getLabel().getCaption().setValue("Line Chart");
        cwaArea.getTitle().setVisible(false);

        // Legend
        cwaArea.getLegend().setVisible(false);

        // X-Axis
        Axis xAxisPrimary = cwaArea.getPrimaryBaseAxes()[0];
        xAxisPrimary.setType(AxisType.TEXT_LITERAL);
        xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
        xAxisPrimary.getOrigin().setType(IntersectionType.VALUE_LITERAL);
        xAxisPrimary.getLabel().getCaption().getFont().setSize(FONT_SIZE);
        xAxisPrimary.getLabel().getCaption().getFont().setName(FONT_NAME);
        xAxisPrimary.getTitle().setVisible(false);

        // Y-Axis
        Axis yAxisPrimary = cwaArea.getPrimaryOrthogonalAxis(xAxisPrimary);
        yAxisPrimary.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);
        yAxisPrimary.getLabel().getCaption().getFont().setSize(FONT_SIZE);
        yAxisPrimary.getLabel().getCaption().getFont().setName(FONT_NAME);

        // Data Set
        TextDataSet categoryValues = TextDataSetImpl.create(seriesNames);
        NumberDataSet orthoValues = NumberDataSetImpl.create(seriesValues);

        // X-Series
        Series seCategory = SeriesImpl.create();
        seCategory.setDataSet(categoryValues);
        SeriesDefinition sdX = SeriesDefinitionImpl.create();

        xAxisPrimary.getSeriesDefinitions().add(sdX);
        sdX.getSeries().add(seCategory);

        // Y-Series
        AreaSeries as = (AreaSeries) AreaSeriesImpl.create();
        as.setDataSet(orthoValues);
        as.getLineAttributes().setColor(ColorDefinitionImpl.BLUE());
        as.getLabel().setVisible(true);
        as.getLabel().getCaption().getFont().setSize(FONT_SIZE);
        as.getLabel().getCaption().getFont().setName(FONT_NAME);

        SeriesDefinition sssdY = SeriesDefinitionImpl.create();
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
    public final Chart createLineChart() {
        ChartWithAxes cwaLine = ChartWithAxesImpl.create();

        // Plot
        cwaLine.getBlock().setBackground(ColorDefinitionImpl.WHITE());
        Plot p = cwaLine.getPlot();
        //p.getClientArea().setBackground(ColorDefinitionImpl.create(255, 255, 225));
        p.getOutline().setVisible(false);

        // Title
        //cwaLine.getTitle().getLabel().getCaption().setValue("Line Chart");
        cwaLine.getTitle().setVisible(false);

        // Legend
        cwaLine.getLegend().setVisible(false);

        // X-Axis
        Axis xAxisPrimary = cwaLine.getPrimaryBaseAxes()[0];
        xAxisPrimary.setType(AxisType.TEXT_LITERAL);
        xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
        xAxisPrimary.getOrigin().setType(IntersectionType.VALUE_LITERAL);
        xAxisPrimary.getLabel().getCaption().getFont().setSize(FONT_SIZE);
        xAxisPrimary.getLabel().getCaption().getFont().setName(FONT_NAME);
        xAxisPrimary.getTitle().setVisible(false);

        // Y-Axis
        Axis yAxisPrimary = cwaLine.getPrimaryOrthogonalAxis(xAxisPrimary);
        yAxisPrimary.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);
        yAxisPrimary.getLabel().getCaption().getFont().setSize(FONT_SIZE);
        yAxisPrimary.getLabel().getCaption().getFont().setName(FONT_NAME);

        // Data Set
        TextDataSet categoryValues = TextDataSetImpl.create(seriesNames);
        NumberDataSet orthoValues = NumberDataSetImpl.create(seriesValues);

        // X-Series
        Series seCategory = SeriesImpl.create();
        seCategory.setDataSet(categoryValues);
        SeriesDefinition sdX = SeriesDefinitionImpl.create();

        xAxisPrimary.getSeriesDefinitions().add(sdX);
        sdX.getSeries().add(seCategory);

        // Y-Series
        LineSeries ls = (LineSeries) LineSeriesImpl.create();
        ls.setDataSet(orthoValues);
        ls.getLineAttributes().setColor(ColorDefinitionImpl.BLUE());
        for (int i = 0; i < ls.getMarkers().size(); i++) {
            ((Marker) ls.getMarkers().get(i)).setType(MarkerType.TRIANGLE_LITERAL);
        }
        ls.getLabel().setVisible(true);
        ls.getLabel().getCaption().getFont().setSize(FONT_SIZE);
        ls.getLabel().getCaption().getFont().setName(FONT_NAME);

        SeriesDefinition sssdY = SeriesDefinitionImpl.create();
        sssdY.getSeriesPalette().shift(-2);
        yAxisPrimary.getSeriesDefinitions().add(sssdY);
        sssdY.getSeries().add(ls);

        return cwaLine;
    }
}