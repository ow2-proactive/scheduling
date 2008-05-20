package org.objectweb.proactive.ic2d.chartit.data;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.ic2d.chartit.data.provider.IDataProvider;


public class ChartModel {

    public static final String[] EMPTY_RUNTIME_NAMES = new String[0];

    public static final double[] EMPTY_RUNTIME_VALUES = new double[0];

    public static final String DEFAULT_CHART_NAME = "Chart#";

    /**
     * Default period for refreshing cached value (in milliseconds)
     */
    public static final long DEFAULT_REFRESH_PERIOD = 4 * 1000;

    /**
     * The name of this chart
     */
    protected String name;

    /**
     * The chart type
     */
    protected ChartType chartType;

    /**
     * The period for refreshing cached value (in milliseconds)
     */
    protected long refreshPeriod;

    /**
     * The list of data providers used by this chart
     */
    protected List<IDataProvider> providers;

    // /////////////////
    // RUNTIME ONLY //
    // /////////////////

    /**
     * Chart Model listener
     */
    protected IChartModelListener chartModelListener;

    /**
     * The updater of the runtime values
     */
    protected IRuntimeValuesUpdater runtimeValuesUpdater;

    /**
     * The runtime names
     */
    protected String[] runtimeNames;

    /**
     * The runtime values
     */
    protected double[] runtimeValues;

    public ChartModel() {
        this(ChartModel.DEFAULT_CHART_NAME, ChartType.PIE, ChartModel.DEFAULT_REFRESH_PERIOD);
    }

    public ChartModel(final String name) {
        this(name, ChartType.PIE, ChartModel.DEFAULT_REFRESH_PERIOD);
    }

    public ChartModel(String name, ChartType chartType, long refreshperiod) {
        this.name = name;
        this.chartType = chartType;
        this.refreshPeriod = refreshperiod;

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
        if (this.chartModelListener != null)
            this.chartModelListener.modelChanged(IChartModelListener.CHANGED, null, null);
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
        for (final IDataProvider p : this.providers) {
            if (name.equals(p.getName())) {
                providerToRemove = p;
                break;
            }
        }
        this.providers.remove(providerToRemove);
    }

    public void removeProvidersByNames(final String[] names) {
        for (final String s : names) {
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

    public void setChartModelListener(IChartModelListener chartModelListener) {
        this.chartModelListener = chartModelListener;
    }

    public void unSetChartModelListener() {
        this.chartModelListener = null;
    }

    public boolean isChronological() {
        return this.chartType == ChartType.TIME_SERIES;
    }

    public String toString() {
        return "Name : " + this.name + " type : " + this.chartType + " rp : " + this.refreshPeriod +
            " providers : " + this.providers.size();
    }

    public void fillRuntimeNames() {
        this.runtimeNames = new String[this.providers.size()];
        int i = 0;
        for (final IDataProvider provider : this.providers) {
            runtimeNames[i++] = provider.getName();
        }
    }
}
