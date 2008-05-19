package org.objectweb.proactive.ic2d.chartit.data;

/**
 * The available types of chart that can be built from models
 */
public enum ChartType {
    PIE, BAR, AREA, LINE, TIME_SERIES;
    public static String[] names = new String[] { PIE.name(), BAR.name(), AREA.name(), LINE.name(),
            TIME_SERIES.name() };
}
