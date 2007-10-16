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
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.objectweb.proactive.benchmarks.timit.util.XMLHelper;


/**
 * A class used to generate comparative charts based on the xml output of
 * basic timers.
 *
 * @author vbodnart
 */
public class BasicComparativeChartBuilder {
    public static final String TIME_ATTRIBUTE_NAME = "totalTimeInMillis";
    public static final String CLASSNAME_ATTRIBUTE_NAME = "className";
    public static final String DEFAULT_Y_AXIS_NAME = "Time in milliseconds";
    private String chartTitle;
    private String subtitle;
    private File[] series;
    private DefaultCategoryDataset dataset;
    private Map<String, Document> documentCache;
    private java.util.List<String> classNamesList;

    /**
     * Constructor of this class
     * @param file1 The first file containing the results
     * @param file2 The second file containing the resutls that will be compared with the first one
     * @param chartTitle The title of the chart
     * @param subtitle The subtitle of the chart
     */
    public BasicComparativeChartBuilder(File[] files, String chartTitle,
        String subtitle) {
        this.series = files;
        this.chartTitle = chartTitle;
        this.subtitle = subtitle;
        this.documentCache = new HashMap<String, Document>();
    }

    /**
     * Generates in the specified folder a chart based on given data
     *
     * @param outputFolder The output folder
     */
    public void buildComparativeChart(File outputFolder) {
        // Check the series of files 
        if ((this.series == null) || (this.series.length < 2)) {
            throw new RuntimeException(
                "Cannot generate a comparative chart. You must provide at least 2 different files with the same structure.");
        }

        // Build new dataset
        this.dataset = new DefaultCategoryDataset();

        // This list will be used to store in 
        this.classNamesList = new java.util.ArrayList<String>();

        // Collect all possible classNames from the first serie
        this.collectAllClassNamesFrom(this.series[0]);

        // For each found className make a comparison between series
        Iterator<String> classNamesIterator = this.classNamesList.iterator();
        String className;
        int i;
        while (classNamesIterator.hasNext()) {
            className = classNamesIterator.next();
            // Iterate through  all series to get their values
            for (i = 0; i < this.series.length; i++) {
                // First generate chart for classname1
                // Fill the series (ie filename1) values
                fillDataset(this.series[i], className);
            }

            if (classNamesIterator.hasNext()) {
                // Put some white space
                fillWhiteSpace();
            }
        }

        String forChartName = "";

        // Iterate through  all series to get their values
        for (i = 0; i < this.series.length; i++) {
            forChartName += (this.series[i].getName() +
            (((i + 1) >= this.series.length) ? "" : "-"));
        }

        final JFreeChart chart = createChart();
        try {
            javax.imageio.ImageIO.write(chart.createBufferedImage(800, 600),
                "png",
                new java.io.File(outputFolder.getAbsoluteFile() +
                    "/compareChart_" + forChartName + ".png"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Document getDocumentFromCache(File file) {
        Document res = null;

        // Check if the file was already parsed
        if (this.documentCache.containsKey(file.getName())) {
            res = this.documentCache.get(file.getName());
        } else {
            // Read the xml document
            res = XMLHelper.readFile(file);
            this.documentCache.put(file.getName(), res);
        }
        return res;
    }

    private void collectAllClassNamesFrom(File file) {
        Document d = getDocumentFromCache(file);

        // Supposed to be the timit element
        Element root = d.getRootElement();

        if (root == null) {
            throw new RuntimeException("No root element for xml file " +
                file.getAbsolutePath());
        }

        // Get values from XML tree (Element)
        List<Element> children = root.getChildren();

        if (children.size() == 0) {
            throw new RuntimeException("Nothing to collect from " +
                file.getAbsolutePath() + ". There is no children for " +
                root.getName() + " element.");
        }

        // Collect all available classNames
        String val = null;
        for (Element e : children) {
            val = e.getAttributeValue(CLASSNAME_ATTRIBUTE_NAME);
            // If already known className
            if (this.classNamesList.contains(val)) {
                throw new RuntimeException("Found a className repetition " +
                    val + " in the file " + file.getAbsolutePath());
            }
            this.classNamesList.add(val);
        }
    }

    private void fillDataset(File file, String className) {
        Document d = getDocumentFromCache(file);

        // Supposed to be the timit element
        Element root = d.getRootElement();

        // Get values from XML tree (Element)
        List<Element> aos = root.getChildren();

        // Search for the element with the specified className attribute value
        Element ao = null;
        for (Element e : aos) {
            String val = "";
            if ((val = e.getAttributeValue(CLASSNAME_ATTRIBUTE_NAME)).equals(
                        className) || (val.indexOf(className) >= 0)) {
                ao = e;
                break;
            }
        }

        // If not found return
        if (ao == null) {
            throw new RuntimeException("The following classname : " +
                className + " was not found in the file " +
                file.getAbsolutePath());
        }

        String[] splittedClassname = className.split("\\.");
        String classNameWithoutPackage = splittedClassname[splittedClassname.length -
            1];

        // Search for asked ao

        // Get timers
        Element timers = ao.getChild("timers");
        Iterator<Element> it = timers.getDescendants();

        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof Element) {
                Element current = (Element) o;
                String name = current.getAttributeValue("name") + "_" +
                    classNameWithoutPackage;
                double timeInMillis = Double.parseDouble(current.getAttributeValue(
                            TIME_ATTRIBUTE_NAME));
                // Add information to the datasets
                this.dataset.addValue(timeInMillis, file.getName(), name);
            }
        }
    }

    /**
     * Puts some white space do separate bars on the charts.
     */
    private void fillWhiteSpace() {
        // Add some dummy infromation 
        this.dataset.addValue(0, this.series[0].getName(), "");
    }

    /**
     * Creates a sample chart.
     *
     * @param dataset  a dataset.
     *
     * @return The chart.
     */
    private JFreeChart createChart() {
        // create the chart...
        final JFreeChart chart = ChartFactory.createBarChart(this.chartTitle, // chart title        		
                "", // x axis label              
                DEFAULT_Y_AXIS_NAME, // y axis label
                this.dataset, // data
                PlotOrientation.VERTICAL, true, // include legend
                true, // tooltips
                false // urls
            );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);
        chart.addSubtitle(new TextTitle(this.subtitle + "Time used is : " +
                TIME_ATTRIBUTE_NAME));

        // get a reference to the plot for further customisation...
        final CategoryPlot plot = chart.getCategoryPlot();
        plot.getDomainAxis()
            .setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.BLACK);
        plot.setRangeGridlinePaint(Color.black);
        plot.getRangeAxis().setLabelAngle((0 * Math.PI) / 2.0);
        ((BarRenderer) plot.getRenderer()).setItemMargin(0);

        // OPTIONAL CUSTOMISATION COMPLETED.
        return chart;
    }
}
