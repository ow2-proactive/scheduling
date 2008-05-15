package org.objectweb.proactive.ic2d.chartit.data;

public interface IChartModelListener {

    public static final int CHANGED = 1;

    public void modelChanged(int type, Object oldValue, Object newValue);

}
