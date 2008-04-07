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
 * A model that represents any unknown type.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class UnknownBasedTypeModel extends AbstractTypeModel {
    /**
     * The serialVersionUID of this class
     */
    private static final long serialVersionUID = -1031039516362923294L;

    /**
     * Creates a new instance of <code>UnknownBasedTypeModel</code>.
     * 
     * @param ressourceData The parent resource
     * @param provider The provider associated to this model
     */
    public UnknownBasedTypeModel(final ResourceData ressourceData, final IDataProvider provider) {
        super(ressourceData, provider);
    }

    /**
     * Creates a new instance of <code>UnknownBasedTypeModel</code>.
     * 
     * @param ressourceData The parent resource
     * @param provider The provider associated to this model
     */
    public UnknownBasedTypeModel(final ResourceData ressourceData, final MBeanAttributeInfo attributeInfo) {
        super(ressourceData, attributeInfo);
    }

    /**
     * @return An unknown value
     */
    public Object getProvidedValue() {
        return super.dataProvider.provideValue();
    }
}
