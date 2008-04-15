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
 * A model that represents an array of string type.
 * <p>
 * Typically such model can be used for pie/bar charts where the array of string
 * can represent the series names ...
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
/**
 * @author vbodnart
 *
 */
public final class StringArrayBasedTypeModel extends AbstractTypeModel {
    /**
     * The serialVersionUID of this class
     */
    private static final long serialVersionUID = -5444235567481335767L;

    /**
     * The accepted types for this class of model
     */
    public static final String[] types = new String[] { "[Ljava.lang.String;" };

    /**
     * The accepted types for the associated values of this class of model  
     */
    public static final String[] associatedValuesTypes = new String[] { "[B", "[Ljava.lang.Byte;", "[S",
            "[Ljava.lang.Short;", "[I", "[Ljava.lang.Integer;", "[F", "[Ljava.lang.Float;", "[D",
            "[Ljava.lang.Double;", "[J", "[Ljava.lang.Long;" };

    /**
     * The available types of chart that can be built from this class of model 
     */
    public static final String[] charts = new String[] { "Pie Chart", "Bar Chart" };

    /**
     * The name of the attribute for the associated values
     */
    private String associatedValuesAttribute;

    /**
     * The selected type of chart 
     */
    private int chartChoice;

    /**
     * Creates a new instance of <code>StringArrayBasedTypeModel</code>.
     * 
     * @param ressourceData
     *            The parent resource
     * @param provider
     *            The provider associated to this model
     */
    public StringArrayBasedTypeModel(final ResourceData ressourceData, final IDataProvider provider) {
        super(ressourceData, provider);
    }

    /**
     * Creates a new instance of <code>StringArrayBasedTypeModel</code>.
     * 
     * @param ressourceData
     *            The parent resource
     * @param provider
     *            The provider associated to this model
     */
    public StringArrayBasedTypeModel(final ResourceData ressourceData, final MBeanAttributeInfo attributeInfo) {
        super(ressourceData, attributeInfo);
    }

    /**
     * @return The array of <code>String</code>
     */
    public String[] getProvidedValue() {
        return (String[]) super.dataProvider.provideValue();
    }

    /**
     * 
     * @return
     */
    public String getAssociatedValuesAttribute() {
        return associatedValuesAttribute;
    }

    /**
     * 
     * @param associatedValuesAttribute
     */
    public void setAssociatedValuesAttribute(final String associatedValuesAttribute) {
        this.associatedValuesAttribute = associatedValuesAttribute;
    }

    /**
     * 
     * @return
     */
    public int getChartChoice() {
        return chartChoice;
    }

    /**
     * 
     * @param chartChoice
     */
    public void setChartChoice(final int chartChoice) {
        this.chartChoice = chartChoice;
    }
}