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
package org.objectweb.proactive.benchmarks.timit.util.charts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;

import org.jdom.Element;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.MatrixSeriesCollection;
import org.jfree.data.xy.NormalizedMatrixSeries;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.objectweb.proactive.benchmarks.timit.TimIt;
import org.objectweb.proactive.benchmarks.timit.config.ConfigChart;
import org.objectweb.proactive.benchmarks.timit.util.BenchmarkStatistics;
import org.objectweb.proactive.benchmarks.timit.util.XMLHelper;


/**
 * This class contains all methods to build a matrix chart from a
 * two-dimensional integer array. Used for communication pattern analysis.
 *
 * @author Brian Amedro, Vladimir Bodnartchouk
 */
public class MatrixChart implements Chart {

    /**
     *
     */
    private static final long serialVersionUID = -9045078395503131290L;
    public static final URL logoFile = MatrixChart.class.getResource(
            "/org/objectweb/proactive/benchmarks/timit/pics/TimItProActive.png");
    private Chart.LegendFormat legendFormatMode;
    private Chart.Scale scaleMode;

    /** The array that will be displayed on the chart */
    private int[][] array;

    /** The legendValues */
    private int[] legendValues;

    /** The max value of the array */
    private int maxValue;

    /** The main chart */
    private JFreeChart mainChart;

    /** The legend containing chart */
    private JFreeChart legendChart;

    public void generateChart(Element eTimit, BenchmarkStatistics bstats,
        ConfigChart cChart) {
        String name = cChart.get("eventName");
        int[][] a = (int[][]) bstats.getEventsStatistics()
                                    .getEventDataValue(name);
        this.array = ((a == null) ? MatrixChart.build2DArray(16) : a);
        this.maxValue = MatrixChart.getMaxValue(this.array);
        this.scaleMode = ConfigChart.scaleValue(cChart.get("scaleMode"));
        this.legendFormatMode = ConfigChart.legendValue(cChart.get(
                    "legendFormatMode"));

        if (this.scaleMode == Chart.Scale.DEFAULT) {
            this.scaleMode = Chart.Scale.LOGARITHMIC;
        }
        if (this.legendFormatMode == Chart.LegendFormat.DEFAULT) {
            this.legendFormatMode = Chart.LegendFormat.POW2;
        }

        this.buildFinalChart(cChart);
    }

    /**
     * Creates a two-dim array and returns it.
     *
     * @param size
     *            The size of the array.
     * @return The created array
     */
    private static int[][] build2DArray(int size) {
        int[][] res = new int[size][size];

        for (int i = 0; i < res.length; i++) {
            for (int j = 0; j < res.length; j++) {
                res[i][j] = i * 100000;
            }
        }
        return res;
    }

    private static int getMaxValue(int[][] a) {
        int i;
        int j;
        int max = 0;

        for (i = 0; i < a.length; i++) {
            for (j = 0; j < a.length; j++) {
                if (a[i][j] > max) {
                    max = a[i][j];
                }
            }
        }
        return max;
    }

    private void buildMainChart(String title, String subTitle,
        String xAxisLabel, String yAxisLabel, String fileName) {
        final MatrixSeriesCollection dataset = new MatrixSeriesCollection(this.createMatrixDataSet());

        final JFreeChart chart = ChartFactory.createBubbleChart(title,
                xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL,
                true, true, false);

        chart.addSubtitle(new TextTitle(subTitle));
        chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000,
                Color.WHITE));
        chart.removeLegend();

        // Perform customizations starts here ...
        final XYPlot plot1 = chart.getXYPlot();

        plot1.setDomainGridlinesVisible(false);
        plot1.setRangeGridlinesVisible(false);
        plot1.setForegroundAlpha(0.5f);
        plot1.setDomainAxis(new CustomAxis(plot1.getDomainAxis().getLabel()));
        plot1.setRangeAxis(new CustomAxis(plot1.getRangeAxis().getLabel()));

        // Custumize the domain axis ( y )
        final NumberAxis domainAxis = (NumberAxis) plot1.getDomainAxis();
        domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        domainAxis.setRange(-1, this.array.length);

        // Custumize the range axis ( y )
        final NumberAxis rangeAxis = (NumberAxis) plot1.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setRange(-1, this.array.length);

        // Create custom renderer
        StandardXYItemRenderer ren = new CustomRenderer(false);
        ren.setSeriesItemLabelPaint(0, Color.BLACK);
        plot1.setRenderer(ren);
        this.mainChart = chart;
    }

    private void buildLegendChart(int nbValues) {
        this.legendValues = new int[nbValues + 1];
        this.legendValues[0] = 0;
        int offset = 255 / nbValues;
        int step = offset;

        if (this.scaleMode == Chart.Scale.LOGARITHMIC) {
            double logStep = (Math.log(this.maxValue) / Math.log(2)) / nbValues;
            for (int i = 1; i < (nbValues + 1); i++) {
                this.legendValues[i] = (int) Math.pow(2, logStep * i);
            }
        } else { // Linear scale mode
            for (int i = 1; i < (nbValues + 1); i++) {
                this.legendValues[i] = (step * this.maxValue) / 255;
                step += offset;
            }
        }

        final MatrixSeriesCollection dataset = new MatrixSeriesCollection(this.createLegendDataSet());

        final JFreeChart chart = ChartFactory.createBubbleChart("", "", "",
                dataset, PlotOrientation.VERTICAL, true, true, false);

        chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, 1000,
                Color.WHITE));
        chart.removeLegend();

        // Perform customizations starts here ...
        final XYPlot plot1 = chart.getXYPlot();

        plot1.setDomainGridlinesVisible(false);
        plot1.setRangeGridlinesVisible(false);
        plot1.setForegroundAlpha(0.5f);
        plot1.setDomainAxis(new CustomAxis(plot1.getDomainAxis().getLabel()));
        plot1.setRangeAxis(new CustomAxis(plot1.getRangeAxis().getLabel()));

        // Custumize the domain axis ( x )
        final NumberAxis domainAxis = (NumberAxis) plot1.getDomainAxis();
        domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        domainAxis.setRange(-1, 1);
        domainAxis.setVisible(false);

        // Custumize the range axis ( y )
        final NumberAxis rangeAxis = (NumberAxis) plot1.getRangeAxis();
        rangeAxis.setTickUnit(new CustomTickUnit(rangeAxis.getTickUnit()
                                                          .getSize()));
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setRange(-1, this.legendValues.length);
        rangeAxis.setTickLabelsVisible(true);
        rangeAxis.setTickMarkInsideLength(4);

        // Create custom renderer
        StandardXYItemRenderer ren = new CustomRenderer(true);
        ren.setSeriesItemLabelPaint(0, Color.BLUE);
        plot1.setRenderer(ren);
        plot1.setRangeAxisLocation(AxisLocation.TOP_OR_RIGHT);

        this.legendChart = chart;
    }

    private void buildFinalChart(ConfigChart cChart) {
        this.buildFinalChart(cChart.get("title"), cChart.get("subTitle"),
            cChart.get("filename"), cChart.get("xAxisLabel"),
            cChart.get("yAxisLabel"), Integer.valueOf(cChart.get("width")),
            Integer.valueOf(cChart.get("height")));
    }

    private void buildFinalChart(String title, String subTitle,
        String filename, String xAxisLabel, String yAxisLabel, int width,
        int height) {
        this.buildMainChart(title, subTitle, xAxisLabel, yAxisLabel, filename);

        this.buildLegendChart(5);

        BufferedImage mainChartImage = this.mainChart.createBufferedImage(width,
                height);
        BufferedImage legendChartImage = this.legendChart.createBufferedImage(width / 6,
                height / 3);
        BufferedImage info = null;
        try {
            info = ImageIO.read(MatrixChart.logoFile);
        } catch (IOException ex) {
        }

        BufferedImage total = new BufferedImage(width + (width / 6), height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = total.createGraphics();

        g.drawImage(mainChartImage, 0, 0, null);
        g.drawImage(legendChartImage, width, height / 4, null);
        g.setPaint(Color.WHITE);
        g.fillRect(width, 0, width, height / 4);
        g.fillRect(width, (height / 4) + (height / 3), width, height);
        if (info != null) {
            // g.drawImage(info, (width+(width/6))-info.getWidth(),10, null); //
            // up-right
            g.drawImage(info, (width + (width / 6)) - info.getWidth(),
                height - info.getHeight(), null); // down-right
        }
        g.dispose();

        try {
            javax.imageio.ImageIO.write(total, "png",
                XMLHelper.createFileWithDirs(filename));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Creates a NormalizedMatrixSeries from the array and returns it.
     */
    private NormalizedMatrixSeries createMatrixDataSet() {
        NormalizedMatrixSeries matrix = new NormalizedMatrixSeries("s",
                this.array.length, this.array.length);
        return matrix;
    }

    /**
     * Creates a Legend DataSet and returns it.
     */
    private NormalizedMatrixSeries createLegendDataSet() {
        NormalizedMatrixSeries matrix = new NormalizedMatrixSeries("Legend",
                this.legendValues.length, 1);

        return matrix;
    }

    /**
     * A custom renderer that returns a different color for each item in a
     * single series.
     */
    private class CustomRenderer extends StandardXYItemRenderer {

        /**
         *
         */
        private static final long serialVersionUID = -7987810288979911261L;

        /** The current viewed row */
        private int currentRow = 0;

        /** A boolean for legend mode */
        private boolean legendMode = false;

        /**
         * Creates a new renderer.
         *
         * @param colors
         *            the colors.
         */
        public CustomRenderer(boolean legendMode) {
            super();
            this.legendMode = legendMode;
        }

        /**
         * Returns the paint for an item. Overrides the default behaviour
         * inherited from AbstractSeriesRenderer.
         *
         * @param row
         *            the series.
         * @param column
         *            the category.
         *
         * @return The item color.
         */
        @Override
        public Paint getItemPaint(final int series, final int item) {
            int value = 0;
            if (this.legendMode) {
                if (MatrixChart.this.scaleMode == Chart.Scale.LOGARITHMIC) {
                    value = (255 / (MatrixChart.this.legendValues.length - 1)) * item;
                } else {
                    value = (MatrixChart.this.legendValues[item] * 255) / ((MatrixChart.this.maxValue == 0)
                        ? 1 : MatrixChart.this.maxValue);
                }
            } else {
                if ((item != 0) &&
                        ((item % MatrixChart.this.array.length) == 0)) {
                    this.currentRow++;
                }
                value = (int) ((MatrixChart.this.array[this.currentRow][item % MatrixChart.this.array.length] / (MatrixChart.this.maxValue +
                    0.01)) * 255);

                if (MatrixChart.this.scaleMode == Chart.Scale.LOGARITHMIC) {
                    value = (int) (((Math.log(((value == 255) ? 255 : (value +
                            1))) / Math.log(2)) / (Math.log(255) / Math.log(2))) * 255);
                }
            }

            return new Color(0, 0,
                (int) (255 - (Math.pow(value / 255.0, 6) * 80)), value);
        }

        /**
         * Draws the visual representation of a single data item.
         *
         * @param g2
         *            the graphics device.
         * @param state
         *            the renderer state.
         * @param dataArea
         *            the area within which the data is being drawn.
         * @param info
         *            collects information about the drawing.
         * @param plot
         *            the plot (can be used to obtain standard color information
         *            etc).
         * @param domainAxis
         *            the domain (horizontal) axis.
         * @param rangeAxis
         *            the range (vertical) axis.
         * @param dataset
         *            the dataset.
         * @param series
         *            the series index (zero-based).
         * @param item
         *            the item index (zero-based).
         * @param crosshairState
         *            crosshair information for the plot (<code>null</code>
         *            permitted).
         * @param pass
         *            the pass index.
         */
        @Override
        public void drawItem(Graphics2D g2, XYItemRendererState state,
            Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
            ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
            int series, int item, CrosshairState crosshairState, int pass) {
            PlotOrientation orientation = plot.getOrientation();

            // get the data point...
            double x = dataset.getXValue(series, item);
            double y = dataset.getYValue(series, item);
            double z = 0.97d;

            RectangleEdge domainAxisLocation = plot.getDomainAxisEdge();
            RectangleEdge rangeAxisLocation = plot.getRangeAxisEdge();
            double transX = domainAxis.valueToJava2D(x, dataArea,
                    domainAxisLocation);
            double transY = rangeAxis.valueToJava2D(y, dataArea,
                    rangeAxisLocation);

            double transDomain = 0.0;
            double transRange = 0.0;

            double zero1 = domainAxis.valueToJava2D(0.0, dataArea,
                    domainAxisLocation);
            double zero2 = rangeAxis.valueToJava2D(0.0, dataArea,
                    rangeAxisLocation);
            transDomain = domainAxis.valueToJava2D(z, dataArea,
                    domainAxisLocation) - zero1;
            transRange = zero2 -
                rangeAxis.valueToJava2D(z, dataArea, rangeAxisLocation);

            transDomain = Math.abs(transDomain);
            transRange = Math.abs(transRange);

            RoundRectangle2D.Double rect = new RoundRectangle2D.Double(transX -
                    (transDomain / 2.0), transY - (transRange / 2.0),
                    transDomain, transRange, 10, 10);

            g2.setPaint(this.getItemPaint(series, item));
            g2.fill(rect);
            g2.setStroke(new BasicStroke(1.0f));
            g2.setPaint(Color.LIGHT_GRAY);
            g2.draw(rect);

            this.updateCrosshairValues(crosshairState, x, y, transX, transY,
                orientation);
        }
    }

    private static class CustomAxis extends NumberAxis {

        /**
         *
         */
        private static final long serialVersionUID = -1624475755178533032L;

        public CustomAxis(String labelName) {
            super(labelName);
        }

        /**
         * Calculates the positions of the tick labels for the axis, storing the
         * results in the tick label list (ready for drawing).
         *
         * @param g2
         *            the graphics device.
         * @param state
         *            the axis state.
         * @param dataArea
         *            the area in which the plot should be drawn.
         * @param edge
         *            the location of the axis.
         *
         * @return A list of ticks.
         *
         */
        @Override
        public List<NumberTick> refreshTicks(Graphics2D g2, AxisState state,
            Rectangle2D dataArea, RectangleEdge edge) {
            List<NumberTick> result = new java.util.ArrayList<NumberTick>();
            if (RectangleEdge.isTopOrBottom(edge)) {
                result = this.refreshTicksHorizontal(g2, dataArea, edge);
            } else if (RectangleEdge.isLeftOrRight(edge)) {
                result = this.refreshTicksVertical(g2, dataArea, edge);
            }

            int size = result.size();
            NumberTick first = result.get(0);
            NumberTick last = result.get(size - 1);

            NumberTick newFirst = new NumberTick(0, "", first.getTextAnchor(),
                    first.getRotationAnchor(), first.getAngle());

            NumberTick newLast = new NumberTick(0, "", last.getTextAnchor(),
                    last.getRotationAnchor(), last.getAngle());

            result.set(0, newFirst);
            result.set(size - 1, newLast);
            return result;
        }
    }

    private class CustomTickUnit extends NumberTickUnit {

        /**
         *
         */
        private static final long serialVersionUID = -6476772736783076413L;

        public CustomTickUnit(double size) {
            super(size);
        }

        /**
         * Converts a value to a string.
         *
         * @param value
         *            the value.
         *
         * @return The formatted string.
         */
        @Override
        public String valueToString(double value) {
            if (MatrixChart.this.legendFormatMode == Chart.LegendFormat.POW10) {
                return (((value >= 0) &&
                (value < MatrixChart.this.legendValues.length))
                ? ("" +
                MatrixChart.formatDataSize(MatrixChart.this.legendValues[(int) value],
                    1000)) : "");
            } else if (MatrixChart.this.legendFormatMode == Chart.LegendFormat.POW2) {
                return (((value >= 0) &&
                (value < MatrixChart.this.legendValues.length))
                ? ("" +
                MatrixChart.formatDataSize(MatrixChart.this.legendValues[(int) value],
                    1024)) : "");
            } else {
                return (((value >= 0) &&
                (value < MatrixChart.this.legendValues.length))
                ? ("" + MatrixChart.this.legendValues[(int) value]) : "");
            }
        }
    }

    private static String formatDataSize(int value, int base) {
        int selector = 0;
        double v = value;
        while (v > base) {
            selector++;
            v /= base;
        }
        switch (selector) {
        case 1:
            return "" + TimIt.df.format(v) + "K";
        case 2:
            return "" + TimIt.df.format(v) + "M";
        case 3:
            return "" + TimIt.df.format(v) + "G";
        case 4:
            return "" + TimIt.df.format(v) + "T";
        case 5:
            return "" + TimIt.df.format(v) + "P";
        default:
            return "" + value;
        }
    }
}
