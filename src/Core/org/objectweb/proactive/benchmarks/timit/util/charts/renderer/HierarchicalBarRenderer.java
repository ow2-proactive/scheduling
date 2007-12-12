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
package org.objectweb.proactive.benchmarks.timit.util.charts.renderer;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Iterator;

import org.jdom.Element;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.GradientPaintTransformer;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ObjectList;


/**
 * This renderer implements a hierarchical bar chart. For example, this kind of
 * chart is adapted to hierarchical timers representation.
 *
 * @author Brian Amedro, Vladimir Bodnartchouk
 */
public class HierarchicalBarRenderer extends BarRenderer {

    /**
     *
     */
    public static final int INCLUSION_MARGIN = 3;
    public static final int CORNER = 2;
    private int alpha;

    /** A list of the width of each series bar. */
    protected ObjectList seriesBarWidthList;
    protected Element[] datasetTree;
    protected Comparable[] series;

    /**
     * Default constructor.
     */
    public HierarchicalBarRenderer() {
        super();
        this.seriesBarWidthList = new ObjectList();
        this.alpha = 255; // Bars are opaque by default
    }

    /**
     * Returns the bar width for a series, or <code>Double.NaN</code> if no
     * width has been set.
     *
     * @param series
     *            the series index (zero based).
     *
     * @return The width for the series (1.0=100%, it is the maximum).
     */
    public double getSeriesBarWidth(int series) {
        double result = Double.NaN;
        Number n = (Number) this.seriesBarWidthList.get(series);
        if (n != null) {
            result = n.doubleValue();
        }
        return result;
    }

    /**
     * Sets the width of the bars of a series.
     *
     * @param series
     *            the series index (zero based).
     * @param width
     *            the width of the series bar in percentage (1.0=100%, it is the
     *            maximum).
     */
    public void setSeriesBarWidth(int series, double width) {
        this.seriesBarWidthList.set(series, new Double(width));
    }

    public Element[] getDatasetTree() {
        return this.datasetTree.clone();
    }

    public void setDatasetTree(Element[] datasetTree) {
        this.datasetTree = datasetTree.clone();

        // Some value in dataset are used as temp variables
        // so we have to init them
        for (int i = 0; i < datasetTree.length; i++) {
            Iterator it = this.datasetTree[i].getDescendants();
            while (it.hasNext()) {
                try {
                    ((Element) it.next()).setAttribute("max", "0");
                } catch (ClassCastException e) {
                    continue;
                }
            }
        }
    }

    public void setSeries(Comparable[] series) {
        this.series = series.clone();
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    /**
     * Calculates the bar width and stores it in the renderer state.
     *
     * @param plot
     *            the plot.
     * @param dataArea
     *            the data area.
     * @param rendererIndex
     *            the renderer index.
     * @param state
     *            the renderer state.
     */
    @Override
    protected void calculateBarWidth(CategoryPlot plot, Rectangle2D dataArea,
        int rendererIndex, CategoryItemRendererState state) {
        // calculate the bar width - this calculation differs from the
        // BarRenderer calculation because the bars are layered on top of one
        // another, so there is effectively only one bar per category for
        // the purpose of the bar width calculation
        CategoryAxis domainAxis = this.getDomainAxis(plot, rendererIndex);
        CategoryDataset dataset = plot.getDataset(rendererIndex);
        if (dataset != null) {
            int columns = dataset.getColumnCount();
            int rows = dataset.getRowCount();
            double space = dataArea.getWidth();
            double maxWidth = space * this.getMaximumBarWidth();
            double categoryMargin = 0.0;
            if (columns > 1) {
                categoryMargin = domainAxis.getCategoryMargin();
            }
            double used = space * (1 - domainAxis.getLowerMargin() -
                domainAxis.getUpperMargin() - categoryMargin);
            if ((rows * columns) > 0) {
                state.setBarWidth(Math.min(used / (dataset.getColumnCount()),
                        maxWidth));
            } else {
                state.setBarWidth(Math.min(used, maxWidth));
            }
        }
    }

    /**
     * Draws the bar for one item in the dataset.
     *
     * @param g2
     *            the graphics device.
     * @param state
     *            the renderer state.
     * @param dataArea
     *            the plot area.
     * @param plot
     *            the plot.
     * @param domainAxis
     *            the domain (category) axis.
     * @param rangeAxis
     *            the range (value) axis.
     * @param data
     *            the data.
     * @param row
     *            the row index (zero-based).
     * @param column
     *            the column index (zero-based).
     * @param pass
     *            the pass index.
     */
    @Override
    public void drawItem(Graphics2D g2, CategoryItemRendererState state,
        Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis,
        ValueAxis rangeAxis, CategoryDataset data, int row, int column, int pass) {
        // nothing is drawn for null values...
        Number dataValue = data.getValue(row, column);
        if (dataValue == null) {
            return;
        }

        // BAR X
        double rectX = domainAxis.getCategoryMiddle(column,
                this.getColumnCount(), dataArea, plot.getDomainAxisEdge()) -
            (state.getBarWidth() / 2.0);

        int seriesCount = this.getRowCount();

        // BAR Y
        double value = dataValue.doubleValue();
        double base = 0.0;
        double lclip = this.getLowerClip();
        double uclip = this.getUpperClip();

        if (uclip <= 0.0) { // cases 1, 2, 3 and 4
            if (value >= uclip) {
                return; // bar is not visible
            }
            base = uclip;
            if (value <= lclip) {
                value = lclip;
            }
        } else if (lclip <= 0.0) { // cases 5, 6, 7 and 8
            if (value >= uclip) {
                value = uclip;
            } else {
                if (value <= lclip) {
                    value = lclip;
                }
            }
        } else { // cases 9, 10, 11 and 12
            if (value <= lclip) {
                return; // bar is not visible
            }
            base = this.getLowerClip();
            if (value >= uclip) {
                value = uclip;
            }
        }

        RectangleEdge edge = plot.getRangeAxisEdge();
        double transY1 = rangeAxis.valueToJava2D(base, dataArea, edge);
        double transY2 = rangeAxis.valueToJava2D(value, dataArea, edge);
        double rectY = Math.min(transY2, transY1);

        double rectWidth = state.getBarWidth();
        double rectHeight = Math.abs(transY2 - transY1);

        // draw the bar...
        double shift = 0.0;
        rectWidth = 0.0;
        double widthFactor = 1.0;
        double seriesBarWidth = this.getSeriesBarWidth(row);
        if (!Double.isNaN(seriesBarWidth)) {
            widthFactor = seriesBarWidth;
        }
        rectWidth = widthFactor * state.getBarWidth();
        rectX = rectX + (((1 - widthFactor) * state.getBarWidth()) / 2.0);
        if (seriesCount > 1) {
            // needs to be improved !!!
            shift = (rectWidth * 0.20) / (seriesCount - 1);
        }

        Rectangle2D bar = new Rectangle2D.Double((rectX +
                ((seriesCount - 1 - row) * shift)), rectY,
                (rectWidth - ((seriesCount - 1 - row) * shift * 2)), rectHeight);

        double rrX;
        double rrY;
        double rrW;
        double rrH;
        if (row == 0) {
            Iterator it = this.datasetTree[column].getDescendants();
            int numElement = -1;
            while (it.hasNext()) {
                try {
                    Element elt = (Element) it.next();
                    numElement++;
                    String name = elt.getAttributeValue("name");
                    dataValue = Double.valueOf(elt.getAttributeValue("avg"));
                    // System.out.println("["+column+"] "+name+" \t-->
                    // "+dataValue);
                    // BAR X
                    rectX = domainAxis.getCategoryMiddle(column,
                            this.getColumnCount(), dataArea,
                            plot.getDomainAxisEdge()) -
                        (state.getBarWidth() / 2.0);

                    seriesCount = this.getRowCount();

                    // BAR Y
                    value = dataValue.doubleValue();
                    base = 0.0;
                    lclip = this.getLowerClip();
                    uclip = this.getUpperClip();

                    if (uclip <= 0.0) { // cases 1, 2, 3 and 4
                        if (value >= uclip) {
                            return; // bar is not visible
                        }
                        base = uclip;
                        if (value <= lclip) {
                            value = lclip;
                        }
                    } else if (lclip <= 0.0) { // cases 5, 6, 7 and 8
                        if (value >= uclip) {
                            value = uclip;
                        } else {
                            if (value <= lclip) {
                                value = lclip;
                            }
                        }
                    } else { // cases 9, 10, 11 and 12
                        if (value <= lclip) {
                            return; // bar is not visible
                        }
                        base = this.getLowerClip();
                        if (value >= uclip) {
                            value = uclip;
                        }
                    }

                    edge = plot.getRangeAxisEdge();
                    transY1 = rangeAxis.valueToJava2D(base, dataArea, edge);
                    transY2 = rangeAxis.valueToJava2D(value, dataArea, edge);
                    rectY = Math.min(transY2, transY1);

                    rectWidth = state.getBarWidth();
                    rectHeight = Math.abs(transY2 - transY1);

                    // draw the bar...
                    shift = 0.0;
                    rectWidth = 0.0;
                    widthFactor = 1.0;
                    seriesBarWidth = this.getSeriesBarWidth(row);
                    if (!Double.isNaN(seriesBarWidth)) {
                        widthFactor = seriesBarWidth;
                    }
                    rectWidth = widthFactor * state.getBarWidth();
                    rectX = rectX +
                        (((1 - widthFactor) * state.getBarWidth()) / 2.0);
                    if (seriesCount > 1) {
                        // needs to be improved !!!
                        shift = (rectWidth * 0.20) / (seriesCount - 1);
                    }
                    rrX = (rectX + ((seriesCount - 1 - row) * shift));
                    rrY = rectY;
                    rrW = (rectWidth - ((seriesCount - 1 - row) * shift * 2));
                    rrH = rectHeight;

                    // IMPORTANT NOTE :
                    // dev attribute is used to save width of the element
                    // min attribute is used to save X position of the element
                    // max attribute is used to save the number of child already
                    // managed
                    if (numElement == 0) {
                        elt.setAttribute("dev", "" + rrW);
                        elt.setAttribute("min", "" + rrX);
                        elt.setAttribute("max", "0");
                    } else {
                        Element parent = elt.getParentElement();

                        // System.out.println(" Parent
                        // "+parent.getAttributeValue("name")
                        // + " rrX/rrW/child -> "
                        // + parent.getAttributeValue("min")+"/"
                        // + parent.getAttributeValue("dev")+"/"
                        // + parent.getAttributeValue("max") );
                        double pW = Double.valueOf(parent.getAttributeValue(
                                    "dev"));
                        double pX = Double.valueOf(parent.getAttributeValue(
                                    "min"));
                        int numChild = Integer.valueOf(parent.getAttributeValue(
                                    "max"));

                        rrW = pW / parent.getChildren().size();
                        rrX = pX + (rrW * numChild);
                        rrX += HierarchicalBarRenderer.INCLUSION_MARGIN;
                        rrW -= (HierarchicalBarRenderer.INCLUSION_MARGIN * 2);
                        elt.setAttribute("dev", "" + rrW);
                        elt.setAttribute("min", "" + rrX);
                        parent.setAttribute("max", "" + (numChild + 1));
                    }

                    RoundRectangle2D rbar = new RoundRectangle2D.Double(rrX,
                            rrY, rrW, rrH, HierarchicalBarRenderer.CORNER,
                            HierarchicalBarRenderer.CORNER);

                    Rectangle2D childSumLine = null;
                    double childSum = Double.valueOf(elt.getAttributeValue(
                                "sum"));
                    transY1 = rangeAxis.valueToJava2D(base, dataArea, edge);
                    transY2 = rangeAxis.valueToJava2D(childSum, dataArea, edge);
                    rectY = Math.min(transY2, transY1);

                    childSum = (childSum / dataValue.doubleValue()) * rrH;
                    if ((childSum < rrH) && (childSum > 0) &&
                            ((childSum / rrH) < 0.95)) {
                        childSumLine = new Rectangle2D.Double(rrX, rectY, rrW, 1);
                    }
                    Paint itemPaint = this.getItemPaintFromName(name,
                            this.series, column);
                    GradientPaintTransformer t = this.getGradientPaintTransformer();
                    if ((t != null) && itemPaint instanceof GradientPaint) {
                        itemPaint = t.transform((GradientPaint) itemPaint, bar);
                    }
                    g2.setPaint(itemPaint);

                    Color c = g2.getColor();
                    g2.setColor(new Color(c.getRed(), c.getGreen(),
                            c.getBlue(), this.alpha));
                    g2.fill(rbar);
                    g2.setColor(Color.DARK_GRAY);
                    if (childSumLine != null) {
                        g2.fill(childSumLine);
                    }

                    // draw the outline...
                    if (this.isDrawBarOutline() &&
                            (state.getBarWidth() > BarRenderer.BAR_OUTLINE_WIDTH_THRESHOLD)) {
                        Stroke stroke = this.getItemOutlineStroke(row, column);
                        Paint paint = this.getItemOutlinePaint(row, column);
                        if ((stroke != null) && (paint != null)) {
                            g2.setStroke(stroke);
                            g2.setPaint(paint);
                            g2.draw(rbar);
                        }
                    }
                } catch (ClassCastException e) {
                    continue;
                }
            }
        }

        // ////////////////////////////

        // draw the item labels if there are any...
        double transX1 = rangeAxis.valueToJava2D(base, dataArea, edge);
        double transX2 = rangeAxis.valueToJava2D(value, dataArea, edge);

        CategoryItemLabelGenerator generator = this.getItemLabelGenerator(row,
                column);
        if ((generator != null) && this.isItemLabelVisible(row, column)) {
            this.drawItemLabel(g2, data, row, column, plot, generator, bar,
                (transX1 > transX2));
        }

        // collect entity and tool tip information...
        if (state.getInfo() != null) {
            EntityCollection entities = state.getEntityCollection();
            if (entities != null) {
                String tip = null;
                CategoryToolTipGenerator tipster = this.getToolTipGenerator(row,
                        column);
                if (tipster != null) {
                    tip = tipster.generateToolTip(data, row, column);
                }
                String url = null;
                if (this.getItemURLGenerator(row, column) != null) {
                    url = this.getItemURLGenerator(row, column)
                              .generateURL(data, row, column);
                }
                CategoryItemEntity entity = new CategoryItemEntity(bar, tip,
                        url, data, row, data.getColumnKey(column), column);
                entities.add(entity);
            }
        }
    }

    private Paint getItemPaintFromName(String name, Comparable[] series,
        int column) {
        for (int i = 0; i < series.length; i++) {
            if (series[i].equals(name)) {
                return this.getItemPaint(i, column);
            }
        }
        return null;
    }
}
