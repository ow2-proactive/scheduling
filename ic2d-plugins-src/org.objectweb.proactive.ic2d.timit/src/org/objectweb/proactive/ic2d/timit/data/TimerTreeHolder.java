package org.objectweb.proactive.ic2d.timit.data;

import java.util.ArrayList;
import java.util.List;


public class TimerTreeHolder extends AbstractObject {
    private static TimerTreeHolder instance;
    public static final String P_ADD_SOURCE = "_add_source";
    public static final String P_REMOVE_SELECTED = "_remove_selected";

    /** List of hold tree's sources */
    protected List<ChartObject> chartObjectSources;
    protected List<TimerObject> dummyRoots;

    public static final TimerTreeHolder getInstance() {
        return instance;
    }

    public TimerTreeHolder() {
        this.chartObjectSources = new ArrayList<ChartObject>();
        this.dummyRoots = new ArrayList<TimerObject>();
        instance = this;
    }

    public final void provideChartObject(final ChartObject source,
        final boolean retrieveOnly) {
        int sourceIndex = this.chartObjectSources.indexOf(source);
        if (sourceIndex != -1) {
            if (sourceIndex < this.dummyRoots.size()) {
                this.dummyRoots.get(sourceIndex)
                               .firePropertyChange(TimerObject.P_SELECTION,
                    null, null);
            }
        } else if (!retrieveOnly) {
            this.chartObjectSources.add(source);
            // Add dummyRoot to current dummyRoots and attach the total
            TimerObject dummyRoot = new TimerObject(source.aoObject.getName(),
                    true);
            dummyRoot.children.add(source.rootTimer);
            dummyRoots.add(dummyRoot);
            firePropertyChange(P_ADD_SOURCE, null, null);
            dummyRoot.firePropertyChange(TimerObject.P_SELECTION, null, null);
            dummyRoot.firePropertyChange(TimerObject.P_EXPAND_STATE, null, true);
        }
    }

    /**
     * TimerObject t MUST be a dummy root
     * @param t
     */
    public final void removeDummyRoot(final TimerObject t) {
        this.dummyRoots.remove(t);
        TimerObject rootOfSource = t.getChildren().get(0);
        ChartObject sourceToRemove = null;
        for (ChartObject c : this.chartObjectSources) {
            if (c.getRootTimer().equals(rootOfSource)) {
                sourceToRemove = c;
                break; // unary association therefore equality can happen only once
            }
        }
        if (sourceToRemove != null) {
            this.chartObjectSources.remove(sourceToRemove);
        }
    }

    public final List<TimerObject> getChildren() {
        return this.dummyRoots;
    }

    public final List<ChartObject> getChartObjectSources() {
        return chartObjectSources;
    }

    public List<TimerObject> getDummyRoots() {
        return dummyRoots;
    }
}
