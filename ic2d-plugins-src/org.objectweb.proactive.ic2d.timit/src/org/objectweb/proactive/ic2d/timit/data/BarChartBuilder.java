/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.timit.data;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.attribute.AxisType;
import org.eclipse.birt.chart.model.attribute.ChartDimension;
import org.eclipse.birt.chart.model.attribute.IntersectionType;
import org.eclipse.birt.chart.model.attribute.LegendItemType;
import org.eclipse.birt.chart.model.attribute.LineStyle;
import org.eclipse.birt.chart.model.attribute.Orientation;
import org.eclipse.birt.chart.model.attribute.TickStyle;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
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
import org.eclipse.birt.chart.model.type.BarSeries;
import org.eclipse.birt.chart.model.type.impl.BarSeriesImpl;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.AOFigure;
import org.objectweb.proactive.ic2d.timit.data.tree.TimerTreeNodeObject;


/**
 * The Builder of the bar chart. The Chart is customized through the use of a
 * script.
 *
 * @author The ProActive Team
 *
 */
public class BarChartBuilder {

    /**
     * Font name for all titles, labels, and values.
     */
    protected final static String FONT_NAME = "Courrier New";

    /**
     * Font size for all titles, labels, and values.
     */
    protected final static int FONT_SIZE = 8;

    /**
     *
     */
    protected final static String USER_DEFINED_LABEL_VALUE = "User defined"; // "User

    /**
     *
     */
    protected final static double USER_DEFINED_ELEMENT_VALUE = -1d; // To detect

    /**
     * A formatter for date
     */
    protected final static DateFormat df = DateFormat
            .getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);

    /**
     * The title for generated charts
     */
    protected String title;

    /**
     * X axis.
     */
    protected Axis xAxis;

    /**
     * Y axis.
     */
    protected Axis yAxis;

    /**
     * Chart instance.
     */
    protected Chart chart;

    /**
     * Used to provide the current date
     */
    protected Date now = new Date();

    /**
     * An array of string that represents the series ie the values names
     */
    protected String[] series;

    /**
     * An array of  double that represents the values of a chart
     */
    protected double[] values;

    /**
     * The constructor of the CharChartBuilder.
     *
     * @param title The title that will be used for generated charts.
     */
    public BarChartBuilder(String title) {
        this.title = title;
        this.series = new String[0];
        this.values = new double[0];
        build();
    }

    /**
     * Builds one chart.
     */
    protected void build() {
        this.chart = ChartWithAxesImpl.create();

        ((ChartWithAxes) this.chart).setOrientation(Orientation.HORIZONTAL_LITERAL);

        this.buildTitle();
        this.buildXAxis();
        this.buildYAxis();

        this.chart.getLegend().setItemType(LegendItemType.CATEGORIES_LITERAL);
        this.chart.getLegend().setVisible(false);
        this.chart.setDimension(ChartDimension.TWO_DIMENSIONAL_LITERAL);

        this.chart.setScript( // ///////////////////////////////////////////////////////
                "function adaptTime( timeInMillis ) {"
                    + "form = null;"
                    + "var result = 0;" // Check if seconds is not ok
                    + "if ((timeInMillis / 1000) < 1) {"
                    + "	    form = \"ms\";"
                    + "	    result = timeInMillis;" // use milliseconds
                    + " } else {"
                    + "   timeInSeconds = timeInMillis / 1000;" // Check if
                    // minutes
                    // is not ok
                    + "	   if ((timeInSeconds / 60) < 1) {"
                    + "		   form = \"s\";"
                    + "		   result = timeInSeconds;" // use seconds
                    + "	   } else {"
                    + "       timeInMinutes = timeInSeconds / 60;" // Check
                    // if
                    // hours
                    // is
                    // not
                    // ok
                    + "       if ( (timeInMinutes / 60) < 1 ) {"
                    + "		      form = \"m\";"
                    + "		      result = timeInSeconds / 60;" // use
                    // minutes
                    + "       } else {"
                    + "		      form = \"h\";"
                    + "		      result = timeInMinutes / 60;" // use hours
                    + "       }" + "    } " + " }" + " var res = new java.lang.String(result);"
                    + " var dotIndex = res.indexOf('.');" + " if ( dotIndex != -1 ){"
                    + "     var postDot = res.substring(dotIndex+1, res.length());"
                    + "     if ( postDot != 0 ){" + "         if ( postDot.length() > 2 ){ "
                    + "            res = res.substring(0,dotIndex+3); " + "         }"
                    + "     } else {"
                    + "        res = res.substring(0, dotIndex);"
                    + "     }"
                    + " }"
                    + "return res+form;"
                    + "}" // /////////////////////////////////////////////////////////
                    + "function beforeDrawDataPointLabel(dataPoints, label, scriptContext) {"
                    + " if ( dataPoints.getOrthogonalValue() != -1 ) {"
                    + " 	label.getCaption( ).getColor( ).set( 0, 0, 0 );"
                    + " 	label.getCaption( ).getFont( ).setBold(true);"
                    + "   label.getCaption().getFont().setSize(" +
                    FONT_SIZE +
                    ");" +
                    " 	label.setVisible( true );" +
                    " 	label.getCaption().setValue(adaptTime(dataPoints.getOrthogonalValue()));" +
                    " }" +
                    "}" // //////////////////////////////////////////////////////
                    +
                    "function beforeDrawAxisLabel( axis, label, context )" +
                    "{" +
                    " if ( axis.getType() == 'Logarithmic' ) {" +
                    "   label.getCaption().setValue(adaptTime(label.getCaption().getValue()));" +
                    " } else {" +
                    "   if ( label.getCaption().getValue() == '" +
                    USER_DEFINED_LABEL_VALUE +
                    "' ) {" +
                    " 	label.getCaption( ).getColor( ).set( 0, 0, 0 );" +
                    " 	label.getCaption( ).getFont( ).setBold(true);" +
                    "     label.getCaption().getFont().setSize(" +
                    (FONT_SIZE + 1) +
                    ");" +
                    " 	label.setVisible( true );" +
                    "   } else {" +
                    "     label.getCaption().getFont().setSize(" +
                    FONT_SIZE +
                    ");" +
                    " 	label.getCaption( ).getFont( ).setBold(false);" +
                    "   }" +
                    " } " +
                    "}" // //////////////////////////////////////////////////////
                    +
                    "function beforeDrawElement(dataPointHints, fill) {" // + "
                    // java.lang.System.out.println('________
                    // ' +
                    // fill.getClass().getName());"
                    // Color of Total
                    +
                    " if ( dataPointHints.getBaseValue() == 'Total' ) {" +
                    "  fill.set(225, 225, 255);" // Color of Serve
                    +
                    " } else if ( dataPointHints.getBaseValue() == 'Serve' ) { " +
                    "  fill.set(" +
                    AOFigure.COLOR_WHEN_SERVING_REQUEST.getRed() +
                    "," +
                    AOFigure.COLOR_WHEN_SERVING_REQUEST.getGreen() +
                    "," +
                    AOFigure.COLOR_WHEN_SERVING_REQUEST.getBlue() +
                    ");" // Color of SendRequest and SendReply
                    +
                    " } else if ( dataPointHints.getBaseValue() == 'SendRequest' || dataPointHints.getBaseValue() == 'SendReply' ) {" +
                    "  fill.set(40,255,200);" // Color of WaitByNecessity
                    +
                    " } else if ( dataPointHints.getBaseValue() == 'WaitByNecessity') {" +
                    "  fill.set(" +
                    AOFigure.COLOR_WHEN_WAITING_BY_NECESSITY.getRed() +
                    "," +
                    AOFigure.COLOR_WHEN_WAITING_BY_NECESSITY.getGreen() +
                    "," +
                    AOFigure.COLOR_WHEN_WAITING_BY_NECESSITY.getBlue() +
                    ");" // Color of WaitForRequest
                    +
                    " } else if ( dataPointHints.getBaseValue() == 'WaitForRequest') {" +
                    "  fill.set(" +
                    AOFigure.COLOR_WHEN_WAITING_FOR_REQUEST.getRed() +
                    "," +
                    AOFigure.COLOR_WHEN_WAITING_FOR_REQUEST.getGreen() +
                    "," +
                    AOFigure.COLOR_WHEN_WAITING_FOR_REQUEST.getBlue() + ");" + " }" + "}");
    }

    /**
     * Creates a new chart instance.
     *
     * @param filter Used to filter timers
     * @return The instance of the chart created from the list of tree node objects
     */
    public final Chart createChart(final List<TimerTreeNodeObject> timerTreeNodeObjectList,
            final String[] filter) {
        // That map will contain all collapsed timers except methodTimers
        final Map<String, Double> collapsedTimersMap = new HashMap<String, Double>();
        for (final TimerTreeNodeObject t : timerTreeNodeObjectList) {
            if (BasicChartObject.contains(filter, t.getLabelName()) || t.getCurrentTimer().isUserLevel()) {
                Double d = collapsedTimersMap.get(t.getLabelName());
                if (d == null) {
                    d = new Double(t.getCurrentTotalTimeInMsInDouble());
                } else {
                    d += t.getCurrentTotalTimeInMsInDouble();
                }
                collapsedTimersMap.put(t.getLabelName(), d);
            }
        }

        final int size = collapsedTimersMap.size();
        if (this.series.length != size) {
            this.series = new String[size];
            this.values = new double[size];
        }

        // Fill the series and values arrays
        this.fillDataFromCollapsedMap(collapsedTimersMap, filter);

        this.cleanChart();

        this.buildXSeries(series);
        this.buildYSeries(values);
        this.updateTitle(); // used to update the snapshot time

        return chart;
    }

    /**
     * Fills the data from a collapsed map of times.
     * The data will contain the filtered times with the order defined by the filter.
     *
     * @param collapsedTimesMap The collapsed map of times
     * @param filter An array of timers names
     */
    private final void fillDataFromCollapsedMap(final java.util.Map<String, Double> collapsedTimesMap,
            final String[] filter) {
        String timerName = null;
        Double value = null;
        for (int i = 0; i < filter.length; i++) {
            timerName = filter[i];
            value = collapsedTimesMap.get(timerName);
            this.series[i] = timerName;
            this.values[i] = (((value == null) || (value == 0)) ? (-1) : value);
        }
    }

    /**
     * Updates the title of this chart. Prints the last refresh time.
     */
    public final void updateTitle() {
        this.now.setTime(System.currentTimeMillis());
        this.chart.getTitle().getLabel().getCaption().setValue(
                title + " \nLast Refresh : " + this.getFormattedLastRefreshDate());
    }

    /**
     * Returns the formatted last refresh date as a String.
     * @return A String version of the last refresh date
     */
    public final String getFormattedLastRefreshDate() {
        return BarChartBuilder.df.format(this.now);
    }

    /**
     * Builds X axis.
     */
    protected void buildXAxis() {
        xAxis = ((ChartWithAxes) chart).getPrimaryBaseAxes()[0];
        // Title font customization
        xAxis.getTitle().setVisible(false);
        // xAxis.getTitle().getCaption().setValue(xTitle);
        // xAxis.getTitle().getCaption().getFont().setSize(FONT_SIZE);
        // xAxis.getTitle().getCaption().getFont().setName(FONT_NAME);
        // xAxis.setLabelPosition(Position.ABOVE_LITERAL);
        // Label font customization
        xAxis.getLabel().getCaption().getFont().setSize(FONT_SIZE);
        xAxis.getLabel().getCaption().getFont().setName(FONT_NAME);
        xAxis.getMajorGrid().setTickStyle(TickStyle.BELOW_LITERAL);
        xAxis.setType(AxisType.TEXT_LITERAL);
        xAxis.getOrigin().setType(IntersectionType.VALUE_LITERAL);
    }

    /**
     * Builds Y axis.
     */
    protected void buildYAxis() {
        yAxis = ((ChartWithAxes) chart).getPrimaryOrthogonalAxis(xAxis);
        // Title font customization
        yAxis.getTitle().setVisible(false);
        // yAxis.getTitle().getCaption().setValue(yTitle);
        // yAxis.getTitle().getCaption().getFont().setSize(FONT_SIZE);
        // yAxis.getTitle().getCaption().getFont().setName(FONT_NAME);

        // Label font customization
        yAxis.getLabel().getCaption().getFont().setSize(FONT_SIZE);
        yAxis.getLabel().getCaption().getFont().setName(FONT_NAME);
        yAxis.getMajorGrid().getLineAttributes().setVisible(true);
        yAxis.getMajorGrid().getLineAttributes().setColor(ColorDefinitionImpl.GREY());
        yAxis.getMajorGrid().getLineAttributes().setStyle(LineStyle.DASHED_LITERAL);
        yAxis.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);

        yAxis.setType(AxisType.LOGARITHMIC_LITERAL);
        // yAxis.getOrigin().setType(IntersectionType.VALUE_LITERAL);
        // yAxis.getScale().setMin(NumberDataElementImpl.create(0));
        // yAxis.getScale().setMax(NumberDataElementImpl.create(90));
    }

    /**
     * Builds X series.
     */
    @SuppressWarnings("unchecked")
    protected void buildXSeries(String[] series) {
        // Timer names
        TextDataSet categoryValues = TextDataSetImpl.create(series);

        Series seCategory = SeriesImpl.create();
        seCategory.setDataSet(categoryValues);
        seCategory.getLabel().getCaption().getFont().setSize(FONT_SIZE);
        seCategory.getLabel().getCaption().getFont().setName(FONT_NAME);

        // Apply the color palette
        SeriesDefinition sdX = SeriesDefinitionImpl.create();

        sdX.getSeriesPalette().shift(1);
        xAxis.getSeriesDefinitions().add(sdX);
        sdX.getSeries().add(seCategory);
    }

    /**
     * Builds Y series.
     */
    @SuppressWarnings("unchecked")
    protected void buildYSeries(double[] values) {
        NumberDataSet orthoValuesDataSet1 = NumberDataSetImpl.create(values);
        BarSeries bs1 = (BarSeries) BarSeriesImpl.create();

        bs1.setDataSet(orthoValuesDataSet1);
        bs1.setRiserOutline(null);

        bs1.getLabel().getCaption().getFont().setSize(FONT_SIZE);
        bs1.getLabel().getCaption().getFont().setName(FONT_NAME);
        bs1.getLabel().setVisible(true);

        // Apply the color palette
        SeriesDefinition sdY = SeriesDefinitionImpl.create();
        yAxis.getSeriesDefinitions().add(sdY);
        sdY.getSeries().add(bs1);
    }

    /**
     * Clean in order to reuse
     */
    public void cleanChart() {
        xAxis.getSeriesDefinitions().clear();
        yAxis.getSeriesDefinitions().clear();
    }

    /**
     * Builds the chart title.
     */
    protected void buildTitle() {
        chart.getTitle().getLabel().getCaption().setValue(title);
        chart.getTitle().getLabel().getCaption().getFont().setSize(FONT_SIZE);
        chart.getTitle().getLabel().getCaption().getFont().setName(FONT_NAME);
    }
}
