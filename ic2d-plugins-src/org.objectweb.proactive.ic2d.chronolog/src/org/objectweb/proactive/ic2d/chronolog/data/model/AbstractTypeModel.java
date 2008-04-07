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
public abstract class AbstractTypeModel extends PropertyChangeSupport {
    public static final String ELEMENT_CHANGED = "0";
    /**
     * The parent resource
     */
    protected final ResourceData ressourceData;
    /**
     * The associated provider
     */
    protected final IDataProvider dataProvider;
    /**
     * A boolean variable to know if this model is in a data store
     */
    protected boolean used; // false by default

    /**
     * Creates a new instance of <code>AbstractTypeModel</code>.
     * 
     * @param ressourceData
     *            The parent resource
     * @param dataProvider
     *            The provider associated to this model
     */
    public AbstractTypeModel(final ResourceData ressourceData, final IDataProvider dataProvider) {
        super(new Object());
        this.ressourceData = ressourceData;
        this.dataProvider = dataProvider;
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
     * Adds this model into the resource data store.
     */
    public void addToRessource() {
        this.used = true;
        this.ressourceData.getDataStore().addElement(this);
    }

    /**
     * Removes this model from the resource data store.
     */
    public void removeFromRessource() {
        this.ressourceData.getDataStore().removeElement(this);
        this.used = false;
    }

    /**
     * @return The parent resource
     */
    public ResourceData getRessourceData() {
        return ressourceData;
    }

    /**
     * @return The current provider
     */
    public IDataProvider getDataProvider() {
        return dataProvider;
    }

    /**
     * @return <code>true</code> if this model is in a data store;
     *         <code>false</code> otherwise
     */
    public boolean isUsed() {
        return used;
    }
}