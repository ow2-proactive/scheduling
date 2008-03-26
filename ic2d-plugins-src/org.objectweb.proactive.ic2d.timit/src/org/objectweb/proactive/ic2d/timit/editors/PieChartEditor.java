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
package org.objectweb.proactive.ic2d.timit.editors;

import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithoutAxes;
import org.eclipse.birt.chart.model.attribute.ChartDimension;
import org.eclipse.birt.chart.model.attribute.DataPoint;
import org.eclipse.birt.chart.model.attribute.DataPointComponentType;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.attribute.impl.DataPointComponentImpl;
import org.eclipse.birt.chart.model.attribute.impl.JavaNumberFormatSpecifierImpl;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.data.impl.TextDataSetImpl;
import org.eclipse.birt.chart.model.impl.ChartWithoutAxesImpl;
import org.eclipse.birt.chart.model.layout.Plot;
import org.eclipse.birt.chart.model.type.impl.PieSeriesImpl;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.objectweb.proactive.ic2d.timit.editors.canvas.GenericChartCanvas;


/**
 * The editor used to show a pie chart.
 * 
 * @author The ProActive Team
 */
public class PieChartEditor extends EditorPart {

    public static final String ID = "org.objectweb.proactive.ic2d.timit.editors.PieChartEditor";

    /**
     * The size of the label font in the chart. 
     */
    public static final float CHART_FONT_SIZE = 10f;

    public PieChartEditor() {
        super();
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IEditorPart#init(org.eclipse.ui.IEditorSite,
     *      org.eclipse.ui.IEditorInput)
     */
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
        setPartName(input.getName());
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {
        Chart chart = createChart();
        GenericChartCanvas canvas = new GenericChartCanvas(parent, SWT.NONE);
        canvas.setChart(chart);
        canvas.setSize(640, 480);
    }

    /**
     * Creates one chart instance with the chart type.
     * 
     * @return the chart to show
     */
    @SuppressWarnings("unchecked")
    private Chart createChart() {
        IPieChartEditorInput input = (IPieChartEditorInput) this.getEditorInput();

        ChartWithoutAxes cwoaPie = ChartWithoutAxesImpl.create();
        cwoaPie.setDimension(ChartDimension.TWO_DIMENSIONAL_WITH_DEPTH_LITERAL);
        cwoaPie.setSeriesThickness(8);
        //cwoaPie.getInteractivity().setEnable(true); // added
        cwoaPie.getBlock().setBackground(ColorDefinitionImpl.WHITE());

        // Get the plot to customize it
        Plot p = cwoaPie.getPlot();
        p.getClientArea().setBackground(ColorDefinitionImpl.TRANSPARENT());

        // Remove the legend
        cwoaPie.getLegend().setVisible(false);

        // Title
        cwoaPie.getTitle().getLabel().getCaption().setValue(input.getToolTipText());
        cwoaPie.getTitle().getLabel().getCaption().getFont().setSize(PieChartEditor.CHART_FONT_SIZE);
        cwoaPie.getTitle().getLabel().getCaption().getFont().setBold(true);
        cwoaPie.getTitle().getOutline().setVisible(false);

        // Data Set
        TextDataSetImpl categoryValues = (TextDataSetImpl) TextDataSetImpl.create(input.getCategoryNames());
        NumberDataSetImpl seriesOneValues = (NumberDataSetImpl) NumberDataSetImpl.create(input
                .getCategoryValues());

        // Base Series
        Series seCategory = SeriesImpl.create();
        seCategory.setDataSet(categoryValues);
        // Add the base series to the series definition
        SeriesDefinition sd = SeriesDefinitionImpl.create();
        cwoaPie.getSeriesDefinitions().add(sd);
        sd.getSeriesPalette().shift(0);
        sd.getSeries().add(seCategory);

        // Orthogonal Series
        PieSeriesImpl sePie = (PieSeriesImpl) PieSeriesImpl.create();
        sePie.getLabel().getCaption().getFont().setSize(PieChartEditor.CHART_FONT_SIZE); // set series size
        sePie.getLabel().getOutline().setVisible(true);
        sePie.getLabel().setBackground(ColorDefinitionImpl.YELLOW());
        sePie.getLabel().setShadowColor(ColorDefinitionImpl.GREY());
        sePie.setDataSet(seriesOneValues);

        //sePie.setTranslucent(true);

        // Customize the series label name in order to have XXX (XX %)
        DataPoint dp = sePie.getDataPoint();
        dp.getComponents().clear();
        dp.setSeparator("(");
        dp.setSuffix(" %)");
        dp.getComponents().add(
                DataPointComponentImpl.create(DataPointComponentType.BASE_VALUE_LITERAL,
                        JavaNumberFormatSpecifierImpl.create("0.00")));
        dp.getComponents().add(
                DataPointComponentImpl.create(DataPointComponentType.ORTHOGONAL_VALUE_LITERAL,
                        JavaNumberFormatSpecifierImpl.create("0.00")));

        // Add the created series
        SeriesDefinitionImpl seriesDefinition = (SeriesDefinitionImpl) SeriesDefinitionImpl.create();
        sd.getSeriesDefinitions().add(seriesDefinition);
        seriesDefinition.getSeries().add(sePie);

        return cwoaPie;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void doSave(IProgressMonitor monitor) {
        // do nothing
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.ISaveablePart#doSaveAs()
     */
    public void doSaveAs() {
        // do nothing
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {
        // Do nothing.
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.ISaveablePart#isDirty()
     */
    public boolean isDirty() {
        return false;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
     */
    public boolean isSaveAsAllowed() {
        return false;
    }
}
