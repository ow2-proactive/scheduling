package org.objectweb.proactive.ic2d.timit.data;

import java.util.ArrayList;
import java.util.List;


public class TimerTreeHolder extends AbstractObject {
    private static TimerTreeHolder instance;
    public static final String P_ADD_SOURCE = "_add_source";

    /** List of hold tree's sources */
    protected List<ChartObject> chartObjectSources;
    protected List<TimerObject> dummyRoots;
    protected int currentSourceIndex;

    public static final TimerTreeHolder getInstance() {
        return instance;
    }

    public TimerTreeHolder() {
        this.chartObjectSources = new ArrayList<ChartObject>();
        this.dummyRoots = new ArrayList<TimerObject>();
        this.currentSourceIndex = 0;
        instance = this;
    }

    public final void setSelectedIndex(final ChartObject source) {
        this.currentSourceIndex = this.chartObjectSources.indexOf(source);
        if (this.currentSourceIndex != -1) {
            TimerObject timerToSelect = this.dummyRoots.get(this.currentSourceIndex);
            timerToSelect.firePropertyChange(TimerObject.P_SELECTION, null, null);
        }
    }

    public final void provideChartObject(final ChartObject source) {
        if (!this.chartObjectSources.contains(source)) {
            this.chartObjectSources.add(source);
            // Add dummyRoot to current dummyRoots and attach the total
            TimerObject dummyRoot = new TimerObject(source.aoObject.getFullName(),
                    true);
            dummyRoot.children.add(source.rootTimer);
            dummyRoots.add(dummyRoot);
            this.currentSourceIndex = this.chartObjectSources.size() - 1;
            firePropertyChange(P_ADD_SOURCE, null, null);
            dummyRoot.firePropertyChange(TimerObject.P_SELECTION, null, null);
            dummyRoot.firePropertyChange(TimerObject.P_EXPAND_STATE, null, true);
        } else {
            this.setSelectedIndex(source);
        }
    }

    public final List<TimerObject> getChildren() {
        return this.dummyRoots;
    }

    public final List<ChartObject> getChartObjectSources() {
        return chartObjectSources;
    }

    public final void removeChartObject(final ChartObject source) {
        int index = this.chartObjectSources.indexOf(source);
        this.removeByIndex(index);
    }

    public final void removeDummyRoot(final TimerObject source) {
        int index = this.dummyRoots.indexOf(source);
        this.removeByIndex(index);
    }

    public final void removeByIndex(final int index) {
        this.chartObjectSources.remove(index);
        this.dummyRoots.remove(index);
    }
}
