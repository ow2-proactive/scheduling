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

import java.awt.Rectangle;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.objectweb.proactive.benchmarks.timit.config.ConfigChart;
import org.objectweb.proactive.benchmarks.timit.util.BenchmarkStatistics;
import org.objectweb.proactive.benchmarks.timit.util.XMLHelper;


/**
 * This class contains all methods to build a matrix chart from a
 * two-dimensional integer array. Used for communication pattern analysis.
 *
 * @author The ProActive Team
 */
public class Line2dChart implements Chart {

    /**
     *
     */
    private Element[] series;
    private Comparable[] categories;
    private String wantedTag;
    private String[] names;
    private String selectedAttributeName;

    public void generateChart(Element eTimit, BenchmarkStatistics bstats, ConfigChart cChart) {
        Element eTimitClone = (Element) eTimit.clone();

        // Apply filter on elements
        while (true) {
            Iterator<Element> it = eTimitClone.getDescendants(new ElementFilter(cChart.get("tag")));
            try {
                while (it.hasNext()) {
                    XMLHelper.tagFiltering(it.next(), cChart.get("filter").split(","));
                }
            } catch (java.util.ConcurrentModificationException e) {
                continue;
            }
            break;
        }

        // Get values from XML tree (Element)
        List fstats = eTimitClone.getChildren();
        this.series = new Element[fstats.size()];
        this.categories = new Comparable[fstats.size()];

        for (int i = 0; i < fstats.size(); i++) {
            Element fstat = (Element) fstats.get(i);
            fstat.removeContent(new ElementFilter(cChart.get("tag")).negate());
            this.series[i] = fstat.getChild(cChart.get("tag"));
            this.categories[i] = fstat.getAttributeValue("name");
        }
        this.wantedTag = ((Element) this.series[0].getChildren().get(0)).getName();
        this.names = cChart.get("filter").split(",");
        this.selectedAttributeName = cChart.get("attribute");

        this.buildFinalChart(cChart);
    }

    /**
     * Creates a dataset
     *
     * @return The dataset.
     */
    private CategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        int i;
        int k;
        Comparable currentCategory;
        Element currentElement;
        Element iteratedElement;
        Iterator it;
        String currentTagName;
        String attributeNameValue;
        String stringValue;
        double value;

        // Iterate through the categories
        for (i = 0; (i < this.categories.length) && (i < this.series.length); i++) {
            currentCategory = this.categories[i];
            currentElement = this.series[i];
            // Iterate through the selected Element
            it = currentElement.getDescendants();
            while (it.hasNext()) {
                Object o = it.next();
                if (o instanceof Element) {
                    iteratedElement = (Element) o;
                    currentTagName = iteratedElement.getName();
                    if (currentTagName.equals(this.wantedTag)) {
                        for (k = 0; k < this.names.length; k++) {
                            attributeNameValue = iteratedElement.getAttributeValue("name");
                            if ((attributeNameValue != null) && attributeNameValue.equals(this.names[k])) {
                                stringValue = iteratedElement.getAttributeValue(this.selectedAttributeName);
                                if (stringValue != null) {
                                    try {
                                        value = Double.parseDouble(stringValue);
                                        dataset.setValue(value, attributeNameValue + " (" +
                                            this.selectedAttributeName + ")", currentCategory);
                                    } catch (NumberFormatException ex) {
                                        ex.printStackTrace();
                                    }
                                } else {
                                    System.out.println("No attribute " + this.selectedAttributeName +
                                        " for tag " + this.wantedTag + " with name " + this.names[k]);
                                }
                            }
                        }
                    }
                }
            }
        }

        return dataset;
    }

    /**
     *
     * @param chartParameters
     */
    private void buildFinalChart(ConfigChart cChart) {
        this.buildFinalChart(cChart.get("title"), cChart.get("subTitle"), cChart.get("filename"), cChart
                .get("xaxislabel"), cChart.get("yaxislabel"), Integer.valueOf(cChart.get("width")), Integer
                .valueOf(cChart.get("height")));
    }

    /**
     *
     * @param title
     * @param subTitle
     * @param fileName
     * @param domainAxis
     * @param rangeAxis
     * @param width
     * @param height
     */
    private void buildFinalChart(String title, String subTitle, String fileName, String domainAxis,
            String rangeAxis, int width, int height) {
        CategoryDataset dataset = this.createDataset();

        JFreeChart chart = ChartFactory.createLineChart(title, domainAxis, // domain
                // axis
                // label
                rangeAxis, // range axis label
                dataset, // data
                PlotOrientation.VERTICAL, // orientation
                true, // include legend
                true, // tooltips?
                false // URLs?
                );
        chart.addSubtitle(new TextTitle(subTitle));

        final LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());

        CategoryPlot c = chart.getCategoryPlot();
        c.setRenderer(renderer);
        c.setRangeGridlinesVisible(true);

        try {
            ChartUtilities.saveChartAsPNG(XMLHelper.createFileWithDirs(fileName + ".png"), chart, width,
                    height);

            Utilities.saveChartAsSVG(chart, new Rectangle(width, height), XMLHelper
                    .createFileWithDirs(fileName + ".svg"));
        } catch (java.io.IOException e) {
            System.err.println("Error writing image to file");
            e.printStackTrace();
        }
    }
}
