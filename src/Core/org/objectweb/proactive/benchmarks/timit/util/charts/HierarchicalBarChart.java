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

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.objectweb.proactive.benchmarks.timit.config.ConfigChart;
import org.objectweb.proactive.benchmarks.timit.util.BenchmarkStatistics;
import org.objectweb.proactive.benchmarks.timit.util.XMLHelper;
import org.objectweb.proactive.benchmarks.timit.util.charts.renderer.HierarchicalBarRenderer;


public class HierarchicalBarChart implements Chart {

    /**
     *
     */
    private static double CATEGORY_MARGIN = 0.0;
    private Element[] timers;
    private Comparable[] categories;

    public void generateChart(Element eTimit, BenchmarkStatistics bstats, ConfigChart cChart) {
        Element eTimitClone = (Element) eTimit.clone();

        // Apply filter on elements
        Iterator<Element> itTimers = eTimitClone.getDescendants(new ElementFilter("timers"));

        while (itTimers.hasNext()) {
            XMLHelper.tagFiltering(itTimers.next(), cChart.get("filter").split(","));
        }

        // Get values from XML tree (Element)
        List fstats = eTimitClone.getChildren();
        this.timers = new Element[fstats.size()];
        this.categories = new Comparable[fstats.size()];

        for (int i = 0; i < fstats.size(); i++) {
            Element fstat = (Element) fstats.get(i);
            fstat.removeChild("events");
            this.timers[i] = fstat.getChild("timers");
            this.categories[i] = fstat.getAttributeValue("name");
        }

        this.buildFinalChart(cChart);
    }

    private void buildFinalChart(ConfigChart cChart) {
        Chart.Scale scaleMode = ConfigChart.scaleValue(cChart.get("scaleMode"));
        Chart.LegendFormat legendFormatMode = ConfigChart.legendValue(cChart.get("legendFormatMode"));
        int alpha = Integer.valueOf(cChart.get("alpha"));

        if (scaleMode == Chart.Scale.DEFAULT) {
            scaleMode = Chart.Scale.LINEAR;
        }
        if (legendFormatMode == Chart.LegendFormat.DEFAULT) {
            legendFormatMode = Chart.LegendFormat.NONE;
        }
        this.buildFinalChart(cChart.get("title"), cChart.get("subTitle"), cChart.get("xAxisLabel"), cChart
                .get("yAxisLabel"), Integer.valueOf(cChart.get("height")), Integer.valueOf(cChart
                .get("width")), cChart.get("filename"), scaleMode, legendFormatMode, alpha);
    }

    private void buildFinalChart(String title, String subTitle, String xAxisLabel, String yAxisLabel,
            int height, int width, String filename, Chart.Scale scaleMode,
            Chart.LegendFormat legendFormatMode, int alpha) {
        Vector[] vec = new Vector[this.timers.length];
        boolean exist;

        // create the dataset...
        for (int i = 0; i < this.timers.length; i++) {
            vec[i] = new Vector();
            Iterator it = this.timers[i].getDescendants();
            while (it.hasNext()) {
                try {
                    Element elt = (Element) it.next();
                    String name = elt.getAttributeValue("name");
                    double value = Double.valueOf(elt.getAttributeValue("avg"));
                    exist = false;
                    for (int j = 0; j < vec[i].size(); j++) {
                        if (((Counter) vec[i].get(j)).getName().equals(name)) {
                            ((Counter) vec[i].get(j)).addValue(value);
                            exist = true;
                            break;
                        }
                    }
                    if (!exist) {
                        vec[i].add(new Counter(name, value));
                    }
                } catch (ClassCastException e) {
                    continue;
                }
            }
        }

        CategoryDataset dataset = null;
        try {
            dataset = DatasetUtilities.createCategoryDataset(toSeries(vec[0]), this.categories,
                    toDataset(vec));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(
                "Benchmark names must have different names. Be sure that your filter contains correct timers names");
        }

        // create the chart...
        final CategoryAxis categoryAxis = new CategoryAxis(xAxisLabel);
        final ValueAxis valueAxis = new NumberAxis(yAxisLabel);

        final CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis,
            new HierarchicalBarRenderer());

        plot.setOrientation(PlotOrientation.VERTICAL);
        final JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        chart.addSubtitle(new TextTitle(subTitle));

        // set the background color for the chart...
        chart.setBackgroundPaint(Color.white);

        final HierarchicalBarRenderer renderer = (HierarchicalBarRenderer) plot.getRenderer();

        renderer.setItemMargin(0.01);
        renderer.setDatasetTree(this.timers);
        renderer.setSeries(toSeries(vec[0]));
        renderer.setAlpha(alpha);

        final CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryMargin(HierarchicalBarChart.CATEGORY_MARGIN);
        domainAxis.setUpperMargin(0.05);
        domainAxis.setLowerMargin(0.05);

        try {
            if ((filename == null) || "".equals(filename)) {
                throw new RuntimeException(
                    "The output filename for the HierarchicalBarChart cannot be null or empty !");
            }

            ChartUtilities.saveChartAsPNG(XMLHelper.createFileWithDirs(filename + ".png"), chart, width,
                    height);

            Utilities.saveChartAsSVG(chart, new Rectangle(width, height), XMLHelper
                    .createFileWithDirs(filename + ".svg"));
        } catch (java.io.IOException e) {
            System.err.println("Error writing chart image to file");
            e.printStackTrace();
        }
    }

    private static double[][] toDataset(Vector[] counter) {
        try {
            double[][] result = new double[counter[0].size()][counter.length];

            for (int i = 0; i < counter.length; i++) {
                for (int j = 0; j < counter[i].size(); j++) {
                    result[j][i] = ((Counter) counter[i].get(j)).getValue();
                }
            }
            return result;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Can't generate chart with irregular type of values");
        }
    }

    private static Comparable[] toSeries(Vector counter) {
        Comparable[] result = new Comparable[counter.size()];
        for (int i = 0; i < counter.size(); i++) {
            result[i] = ((Counter) counter.get(i)).getName();
        }
        return result;
    }

    private static class Counter {
        private String name;
        private double value;

        public Counter(String name, double value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return this.name;
        }

        public double getValue() {
            return this.value;
        }

        public void addValue(double value) {
            this.value += value;
        }

        @Override
        public String toString() {
            return "[" + this.name + "=" + this.value + "]";
        }
    }
}
