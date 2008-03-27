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
package org.objectweb.proactive.ic2d.chronolog.data;

import java.util.ArrayList;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.ObjectName;

import org.objectweb.proactive.ic2d.chronolog.data.model.AbstractTypeModel;
import org.objectweb.proactive.ic2d.chronolog.data.model.NumberBasedTypeModel;
import org.objectweb.proactive.ic2d.chronolog.data.model.StringArrayBasedTypeModel;
import org.objectweb.proactive.ic2d.chronolog.data.model.UnknownBasedTypeModel;
import org.objectweb.proactive.ic2d.chronolog.data.provider.IDataProvider;
import org.objectweb.proactive.ic2d.chronolog.data.store.AbstractDataStore;


/**
 * This class represents a resource. A resource is any data that can be managed by an MBean.
 *  
 * @author The ProActive Team
 */
public final class ResourceData {

    /** The descriptor of the resource */
    protected final IResourceDescriptor resourceDescriptor;

    /** The data store associated to this resource data */
    protected final AbstractDataStore dataStore;

    public ResourceData(final IResourceDescriptor resourceDescriptor, final AbstractDataStore dataStore) {
        this.resourceDescriptor = resourceDescriptor;
        this.dataStore = dataStore;
    }

    public IResourceDescriptor getResourceDescriptor() {
        return resourceDescriptor;
    }

    public AbstractDataStore getDataStore() {
        return dataStore;
    }

    public MBeanAttributeInfo[] getMBeanAttributeInfoFromResource() {
        try {
            final MBeanInfo info = this.getResourceDescriptor().getMBeanServerConnection().getMBeanInfo(
                    getResourceDescriptor().getObjectName());
            return info.getAttributes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new MBeanAttributeInfo[] {};
    }

    /**
     * Same as the precedent method but for another ObjectName.
     * 
     * @param objectName
     *            The ObjectName (MBean or MXBean object name)
     * @param attributeName
     *            The name of the attribute
     * @return The value of the attribute
     */
    public Object getAttributeValueByName(final ObjectName objectName, final String attributeName) {
        try {
            return this.getResourceDescriptor().getMBeanServerConnection().getAttribute(objectName,
                    attributeName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Object();
    }

    /**
     * Finds all available attributes information from the MBean associated to this resource
     * and creates corresponding models.
     * 
     * @return An array of models created from attribute information
     */
    public Object[] findAndCreateElementModels() {
        // List all available attributes for the current resource associated
        // mbean
        final MBeanAttributeInfo[] attInfos = this.getMBeanAttributeInfoFromResource();
        // Create a temporary arraylist
        final ArrayList<AbstractTypeModel> res = new ArrayList<AbstractTypeModel>(attInfos.length +
            this.dataStore.getElements().size());
        // Iterate through all new names and check for known
        for (final MBeanAttributeInfo in : attInfos) {
            boolean notKnown = true;
            // First check if the attribute name is already known by this
            // ressource data store
            for (final AbstractTypeModel knownModel : this.dataStore.getElements()) {
                if (knownModel.getDataProvider().getName().equals(in.getName())) {
                    notKnown = false;
                    break;
                }
            }
            if (notKnown) {
                res.add(buildTypeModelFromInfo(in));
            }
        }

        // Finally add all known models
        res.addAll(this.dataStore.getElements());

        return res.toArray();
    }

    public String[] getKnownAttributeNames() {
        final String[] knownAttributeNames = new String[this.dataStore.getElements().size()];
        int i = 0;
        for (final AbstractTypeModel knownModel : this.dataStore.getElements()) {
            knownAttributeNames[i++] = knownModel.getDataProvider().getName();
        }
        return knownAttributeNames;
    }

    /**
     * Builds a new model from an attribute info.
     * @param provider The attribute info of the model 
     * @return A new model
     */
    public AbstractTypeModel buildTypeModelFromInfo(final MBeanAttributeInfo in) {
        if (contains(StringArrayBasedTypeModel.types, in.getType())) {
            return new StringArrayBasedTypeModel(this, in);
        } else if (contains(NumberBasedTypeModel.types, in.getType())) {
            return new NumberBasedTypeModel(this, in);
        } else {
            return new UnknownBasedTypeModel(this, in);
        }
    }

    /**
     * Builds a new model from a provider.
     * @param provider The provider of the model 
     * @return A new model
     */
    public AbstractTypeModel buildTypeModelFromProvider(final IDataProvider provider) {
        if (contains(StringArrayBasedTypeModel.types, provider.getType())) {
            return new StringArrayBasedTypeModel(this, provider);
        } else if (contains(NumberBasedTypeModel.types, provider.getType())) {
            return new NumberBasedTypeModel(this, provider);
        } else {
            return new UnknownBasedTypeModel(this, provider);
        }
    }

    /**
     * Checks if a string is contained in an array of string.
     * @param ar The array of string
     * @param str the element 
     * @return Returns <code>true</code> if str is contained in ar; <code>false</code> otherwise
     */
    public static final boolean contains(final String[] ar, final String str) {
        for (final String s : ar) {
            if (s.equals(str))
                return true;
        }
        return false;
    }
}