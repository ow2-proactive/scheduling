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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.chronolog.data.model;

import java.beans.PropertyChangeSupport;

import javax.management.MBeanAttributeInfo;

import org.objectweb.proactive.ic2d.chronolog.data.ResourceData;
import org.objectweb.proactive.ic2d.chronolog.data.provider.ByAttributeDataProvider;
import org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider;


/**
 * Represents a model for the type of an <code>MBean</code> attribute.
 * <p>
 * Underlying classes may represents any kind of types that can have any
 * graphical representation.
 * <p>
 * Typically each model knows its own way to get the value associated to this
 * type through a provider <code>IDataProvider</code>.
 * <p>
 * A model can be built from an attribute information <code>MBeanAttributeInfo</code>
 * or an <code>IDataProvider</code>.
 * 
 * @see MBeanAttributeInfo
 * @see IDataProvider
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public abstract class AbstractTypeModel<E> {

    /**
     * A common source object used as source for all models to create the property change support
     */
    public static final Object COMMON_SOURCE_OBJECT = new Object();

    /**
     * A common event name for notifications
     */
    public static final String ELEMENT_CHANGED = "0";

    /**
     * The available types of chart that can be built from models 
     */
    public static enum ChartType {
        PIE, BAR, AREA, LINE, TIME_SERIES;
        public static String[] names = new String[] { PIE.name(), BAR.name(), AREA.name(), LINE.name(),
                TIME_SERIES.name() };
    }

    /**
     * Delegated property change support 
     */
    protected final PropertyChangeSupport propertyChangeSupport;
    /**
     * The parent resource
     */
    protected final ResourceData resourceData;
    /**
     * The associated provider
     */
    protected final IDataProvider dataProvider;

    /**
     * Default period for refreshing cached value (in milliseconds)
     */
    public static final long DEFAULT_REFRESH_PERIOD = 4 * 1000;

    /**
     * The period for refreshing cached value (in milliseconds)
     */
    protected long refreshPeriod;

    /**
     * The selected type of chart 
     */
    protected ChartType chartChoice;

    /**
     * The cached provided value, this value is updated by subclasses
     */
    protected E cachedProvidedValue;

    /**
     * A boolean variable to know if this model is in a data store
     * false by default
     */
    protected boolean used;

    /**
     * Creates a new instance of <code>AbstractTypeModel</code>.
     * 
     * @param resourceData
     *            The parent resource
     * @param dataProvider
     *            The provider associated to this model
     */
    public AbstractTypeModel(final ResourceData resourceData, final IDataProvider dataProvider) {
        this.propertyChangeSupport = new PropertyChangeSupport(COMMON_SOURCE_OBJECT);
        this.resourceData = resourceData;
        this.dataProvider = dataProvider;
        this.refreshPeriod = AbstractTypeModel.DEFAULT_REFRESH_PERIOD;
        this.used = false;
    }

    /**
     * Creates a new instance of <code>AbstractTypeModel</code>.
     * 
     * @param ressourceData
     *            The parent resource
     * @param attributeInfo
     *            The attribute information used to build a
     *            <code>ByAttributeDataProvider</code> that will be associated
     *            to this model
     */
    public AbstractTypeModel(final ResourceData ressourceData, final MBeanAttributeInfo attributeInfo) {
        this(ressourceData, new ByAttributeDataProvider(ressourceData.getResourceDescriptor()
                .getMBeanServerConnection(), ressourceData.getResourceDescriptor().getObjectName(),
            attributeInfo));
    }

    /**
     * Subclasses must ask the data provider to update its value
     */
    public abstract void updateProvidedValue();

    /**
     * Subclasses must provide the authorized chart types for the model
     * @return
     */
    public abstract ChartType[] getAuthorizedChartTypes();

    public String getName() {
        return this.dataProvider.getName();
    }

    public String getDescription() {
        return this.dataProvider.getDescription();
    }

    /**
     * Adds this model into the resource models collector.
     */
    public void addToCollector() {
        this.resourceData.getModelsCollector().addModel(this);
        this.used = true;
    }

    /**
     * Removes this model from the resource data store.
     */
    public void removeFromCollector() {
        this.resourceData.getModelsCollector().removeModel(this);
        this.used = false;
    }

    /**
     * @return The parent resource
     */
    public ResourceData getResourceData() {
        return resourceData;
    }

    /**
     * @return The current provider
     */
    public IDataProvider getDataProvider() {
        return dataProvider;
    }

    /**
     * Runs this model
     */
    public void run() {
        this.updateProvidedValue();
        // TODO: handle properly old and new values
        this.getPropertyChangeSupport().firePropertyChange(AbstractTypeModel.ELEMENT_CHANGED, null, null);
    }

    /**
     * 
     * @return
     */
    public final PropertyChangeSupport getPropertyChangeSupport() {
        return this.propertyChangeSupport;
    }

    /**
     * Returns an array of authorized chart type names
     * @return an array of authorized chart type names
     */
    public static final String[] getAuthorizedChartTypeNames(ChartType[] authorizedTypes) {
        final String[] names = new String[authorizedTypes.length];
        int i = 0;
        for (final ChartType type : authorizedTypes) {
            names[i++] = type.name();
        }
        return names;
    }

    /**
     * 
     * @return
     */
    public final ChartType getChartChoice() {
        return chartChoice;
    }

    /**
     * 
     * @param chartChoice
     */
    public final void setChartChoice(final ChartType chartChoice) {
        this.chartChoice = chartChoice;
    }

    /**
     * 
     * @param chartChoice
     */
    public final void setChartChoice(final int chartChoice) {
        this.setChartChoice(this.getAuthorizedChartTypes()[chartChoice]);
    }

    public E getCachedProvidedValue() {
        return this.cachedProvidedValue;
    }

    /**
     * @return <code>true</code> if this model is in a data store;
     *         <code>false</code> otherwise
     */
    public final boolean isUsed() {
        return used;
    }

    /**
     * 
     * @return
     */
    public final long getRefreshPeriod() {
        return refreshPeriod;
    }

    /**
     * 
     * @param refreshPeriod
     */
    public final void setRefreshPeriod(final long refreshPeriod) {
        this.refreshPeriod = refreshPeriod;
    }

    /**
     * 
     * @return
     */
    public final boolean needStorage() {
        return this.chartChoice == ChartType.TIME_SERIES;
    }
}