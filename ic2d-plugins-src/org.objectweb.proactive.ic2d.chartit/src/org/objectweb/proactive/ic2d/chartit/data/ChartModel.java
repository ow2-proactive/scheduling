package org.objectweb.proactive.ic2d.chartit.data;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.ic2d.chartit.data.provider.IDataProvider;


public class ChartModel {

    public static final String DEFAULT_CHART_NAME = "Chart#";

    /**
     * A common source object used as source for all models to create the property change support
     */
    public static final Object COMMON_SOURCE_OBJECT = new Object();

    /**
     * Default period for refreshing cached value (in milliseconds)
     */
    public static final long DEFAULT_REFRESH_PERIOD = 4 * 1000;

    public static final String[] EMPTY_RUNTIME_NAMES = new String[0];

    public static final double[] EMPTY_RUNTIME_VALUES = new double[0];

    /**
     * The available types of chart that can be built from models 
     */
    public static enum ChartType {
        PIE, BAR, AREA, LINE, TIME_SERIES;
        public static String[] names = new String[] { PIE.name(), BAR.name(), AREA.name(), LINE.name(),
                TIME_SERIES.name() };
    }

    /**
     * A common event name for notifications
     */
    public static final String MODEL_CHANGED = "0";

    /**
     * Delegated property change support 
     */
    protected final PropertyChangeSupport propertyChangeSupport;

    protected List<IDataProvider> providers;

    protected String name;

    protected String[] runtimeNames;

    protected double[] runtimeValues;

    /**
     * The period for refreshing cached value (in milliseconds)
     */
    protected long refreshPeriod;

    protected ChartType chartType;

    protected IRuntimeValuesUpdater runtimeValuesUpdater;

    public ChartModel(String name) {
        this(name, ChartType.PIE, ChartModel.DEFAULT_REFRESH_PERIOD);
    }

    public ChartModel(String name, ChartType chartType, long refreshperiod) {
        this.name = name;
        this.chartType = chartType;
        this.refreshPeriod = refreshperiod;

        this.propertyChangeSupport = new PropertyChangeSupport(ChartModel.COMMON_SOURCE_OBJECT);
        this.runtimeNames = ChartModel.EMPTY_RUNTIME_NAMES;
        this.runtimeValues = ChartModel.EMPTY_RUNTIME_VALUES;

        this.providers = new ArrayList<IDataProvider>();
    }

    /**
     * Runs this model
     */
    public void run() {
        // Update runtime values        
        this.runtimeValuesUpdater.updateValues(this.runtimeValues);

        // TODO: handle properly old and new values
        this.propertyChangeSupport.firePropertyChange(ChartModel.MODEL_CHANGED, null, null);
    }

    public boolean addProvider(final IDataProvider provider) {
        if (!this.providers.contains(provider)) {
            this.providers.add(provider);
            return true;
        }
        return false;
    }

    public void removeProvider(final IDataProvider provider) {
        if (this.providers.contains(provider)) {
            this.providers.remove(provider);
        }
    }

    public void removeProviderByName(final String name) {
        IDataProvider providerToRemove = null;
        for (IDataProvider p : this.providers) {
            if (name.equals(p.getName())) {
                providerToRemove = p;
                break;
            }
        }
        this.providers.remove(providerToRemove);
    }

    public void removeProvidersByNames(final String[] names) {
        for (String s : names) {
            this.removeProviderByName(s);
        }
    }

    public List<IDataProvider> getProviders() {
        return providers;
    }

    public void setProviders(List<IDataProvider> providers) {
        this.providers = providers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getRuntimeNames() {
        return runtimeNames;
    }

    public void setRuntimeNames(String[] runtimeNames) {
        this.runtimeNames = runtimeNames;
    }

    public double[] getRuntimeValues() {
        return runtimeValues;
    }

    public void setRuntimeValues(double[] runtimeValues) {
        this.runtimeValues = runtimeValues;
    }

    public long getRefreshPeriod() {
        return refreshPeriod;
    }

    public void setRefreshPeriod(long refreshPeriod) {
        this.refreshPeriod = refreshPeriod;
    }

    public ChartType getChartType() {
        return chartType;
    }

    public void setChartType(ChartType chartType) {
        this.chartType = chartType;
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    public boolean isChronological() {
        return this.chartType == ChartType.TIME_SERIES;
    }

    public String toString() {
        return "Name : " + this.name + " providers : " + this.providers.size();
    }

}
