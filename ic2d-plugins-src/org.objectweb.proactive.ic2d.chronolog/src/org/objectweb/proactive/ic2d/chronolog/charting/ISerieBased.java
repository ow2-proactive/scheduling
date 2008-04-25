package org.objectweb.proactive.ic2d.chronolog.charting;

import org.objectweb.proactive.ic2d.chronolog.data.model.AbstractTypeModel.ChartType;


public interface ISerieBased {
    /** 
     * Returns the category names
     * @return An array of category names
     */
    public String[] getSerieNames();

    /**
     * Retuns the category values
     * @return An array of category values
     */
    public Double[] getSerieValues();

    /**
     * Provides the format of the series label
     * @return The format of the series label
     */
    public String getSeriesLabelFormat();

    /**
     * Returns the chart choice
     * @return the chart choice
     */
    public ChartType getChartChoice();

}
