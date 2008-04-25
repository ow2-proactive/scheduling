package org.objectweb.proactive.ic2d.chronolog.data.model;

import java.util.Arrays;

import org.objectweb.proactive.ic2d.chronolog.charting.ISerieBased;
import org.objectweb.proactive.ic2d.chronolog.data.ResourceData;
import org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider;


public class GroupedNumberBasedTypeModel extends AbstractTypeModel<Double[]> implements ISerieBased {

    /**
     * Authorized chart types for this model
     */
    public static final ChartType[] authorizedChartTypes = new ChartType[] { ChartType.PIE, ChartType.BAR,
            ChartType.AREA, ChartType.LINE };

    protected IDataProvider[] valuesProviders;
    protected String[] valuesProvidersNames;

    /**
     * Creates a new instance of this model based on a group of number based models
     * @param models
     */
    public GroupedNumberBasedTypeModel(final ResourceData resourceData, final Object[] models) {
        // A cast is needed here to avoid ambiguous warning
        super(resourceData, (IDataProvider) null);
        this.valuesProviders = new IDataProvider[models.length];
        this.valuesProvidersNames = new String[models.length];
        super.cachedProvidedValue = new Double[models.length];
        int i = 0;
        for (final Object model : models) {
            final NumberBasedTypeModel numberBasedTypeModel = (NumberBasedTypeModel) model;
            this.valuesProviders[i] = numberBasedTypeModel.dataProvider;
            this.valuesProvidersNames[i++] = numberBasedTypeModel.dataProvider.getName();
        }
        super.chartChoice = authorizedChartTypes[0];
    }

    @Override
    public String getName() {
        return "A group of number based values.";
    }

    @Override
    public String getDescription() {
        return Arrays.toString(this.valuesProvidersNames);
    }

    /**
     * 
     */
    @Override
    public void updateProvidedValue() {
        int i = 0;
        for (IDataProvider provider : this.valuesProviders) {
            Number n = ((Number) provider.provideValue()).doubleValue();
            //System.out.println("GroupedNumberBasedTypeModel.updateProvidedValue() -----> gr provider " + provider.getName()+  " : "+ n.doubleValue() + " raw : " + provider.provideValue());
            super.cachedProvidedValue[i++] = n.doubleValue();
        }
    }

    public IDataProvider[] getValuesProviders() {
        return this.valuesProviders;
    }

    public String[] getValuesProvidersNames() {
        return this.valuesProvidersNames;
    }

    /**
     * 
     */
    @Override
    public ChartType[] getAuthorizedChartTypes() {
        return GroupedNumberBasedTypeModel.authorizedChartTypes;
    }

    public String[] getSerieNames() {
        return this.valuesProvidersNames;
    }

    public Double[] getSerieValues() {
        return super.cachedProvidedValue;
    }

    public String getSeriesLabelFormat() {
        return "";
    }
}