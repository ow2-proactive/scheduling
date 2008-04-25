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

import javax.management.MBeanAttributeInfo;

import org.objectweb.proactive.ic2d.chronolog.data.ResourceData;
import org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider;


/**
 * A model that represents any numerical primitive type (byte,char,int,float,double,long).
 * <p>
 * Typically such model is used for any plotable graphical representation with time axis.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class NumberBasedTypeModel extends AbstractTypeModel<Number> {
    /**
     * The serialVersionUID of this class
     */
    private static final long serialVersionUID = -4234915437201904826L;

    /**
     * The types associated to this class of model
     */
    public static final String[] types = new String[] { "byte", "java.lang.Byte", "short", "java.lang.Short",
            "int", "java.lang.Integer", "float", "java.lang.Float", "double", "java.lang.Double", "long",
            "java.lang.Long" };

    /**
     * Authorized chart types for this model
     */
    public static final ChartType[] authorizedChartTypes = new ChartType[] { ChartType.TIME_SERIES };

    /**
     * Creates a new instance of <code>NumberBasedTypeModel</code>.
     * 
     * @param ressourceData The parent resource
     * @param provider The provider associated to this model
     */
    public NumberBasedTypeModel(ResourceData ressourceData, IDataProvider provider) {
        super(ressourceData, provider);
        super.chartChoice = ChartType.TIME_SERIES;
    }

    /**
     * Creates a new instance of <code>NumberBasedTypeModel</code>.
     * 
     * @param ressourceData The parent resource
     * @param provider The provider associated to this model
     */
    public NumberBasedTypeModel(ResourceData ressourceData, MBeanAttributeInfo attributeInfo) {
        super(ressourceData, attributeInfo);
        super.chartChoice = ChartType.TIME_SERIES;
    }

    /**
     * Update the cached provided value
     */
    @Override
    public void updateProvidedValue() {
        super.cachedProvidedValue = (Number) super.dataProvider.provideValue();
        // System.out.println("NumberBasedTypeModel.updateProvidedValue() ----------> "+ this.getDataProvider().getName() +" val updated : " + super.cachedProvidedValue);
    }

    /**
     * 
     */
    @Override
    public ChartType[] getAuthorizedChartTypes() {
        return NumberBasedTypeModel.authorizedChartTypes;
    }
}