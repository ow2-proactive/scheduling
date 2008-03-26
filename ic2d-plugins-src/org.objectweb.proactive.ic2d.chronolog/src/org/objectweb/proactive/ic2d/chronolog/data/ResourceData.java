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
 * @author The ProActive Team
 */
public class ResourceData {

    /** The descriptor of the ressource */
    protected final IResourceDescriptor ressourceDescriptor;

    /** The data store associated to this ressource data */
    protected final AbstractDataStore dataStore;

    public ResourceData(final IResourceDescriptor ressourceDescriptor, final AbstractDataStore dataStore) {
        this.ressourceDescriptor = ressourceDescriptor;
        this.dataStore = dataStore;
    }

    public IResourceDescriptor getRessourceDescriptor() {
        return ressourceDescriptor;
    }

    public AbstractDataStore getDataStore() {
        return dataStore;
    }

    public MBeanAttributeInfo[] getMBeanAttributeInfoFromRessource() {
        try {
            final MBeanInfo info = this.getRessourceDescriptor().getMBeanServerConnection().getMBeanInfo(
                    getRessourceDescriptor().getObjectName());
            return info.getAttributes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new MBeanAttributeInfo[] {};
    }

    /**
     * 
     * @param attributeName
     *            The name of the attribute
     * @return The value of the attribute
     */
    public Object getAttributeValueByName(final String attributeName) {
        try {
            return this.getRessourceDescriptor().getMBeanServerConnection().getAttribute(
                    this.getRessourceDescriptor().getObjectName(), attributeName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Object();
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
            return this.getRessourceDescriptor().getMBeanServerConnection().getAttribute(objectName,
                    attributeName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Object();
    }

    public Object[] findAndCreateElementModels() {
        // List all available attributes for the current ressource associated
        // mbean
        final MBeanAttributeInfo[] attInfos = this.getMBeanAttributeInfoFromRessource();
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

        // Finnaly add all known models
        res.addAll(this.dataStore.getElements());

        return res.toArray();
    }

    public AbstractTypeModel buildTypeModelFromInfo(final MBeanAttributeInfo in) {
        // First check if array
        if (in.getType().equals("[Ljava.lang.String;")) {
            return new StringArrayBasedTypeModel(this, in);
        } else if (in.getType().equals("byte") || in.getType().equals("java.lang.Byte") ||
            in.getType().equals("short") || in.getType().equals("java.lang.Short") ||
            in.getType().equals("int") || in.getType().equals("java.lang.Integer") ||
            in.getType().equals("float") || in.getType().equals("java.lang.Float") ||
            in.getType().equals("double") || in.getType().equals("java.lang.Double") ||
            in.getType().equals("long") || in.getType().equals("java.lang.Long")) {
            return new NumberBasedTypeModel(this, in);
        } else {
            return new UnknownBasedTypeModel(this, in);
        }
    }

    public AbstractTypeModel buildTypeModelFromProvider(IDataProvider provider) {
        // First check if array
        if (provider.getType().equals("[Ljava.lang.String;")) {
            return new StringArrayBasedTypeModel(this, provider);
        } else if (provider.getType().equals("byte") || provider.getType().equals("java.lang.Byte") ||
            provider.getType().equals("short") || provider.getType().equals("java.lang.Short") ||
            provider.getType().equals("int") || provider.getType().equals("java.lang.Integer") ||
            provider.getType().equals("float") || provider.getType().equals("java.lang.Float") ||
            provider.getType().equals("double") || provider.getType().equals("java.lang.Double") ||
            provider.getType().equals("long") || provider.getType().equals("java.lang.Long")) {
            return new NumberBasedTypeModel(this, provider);
        } else {
            return new UnknownBasedTypeModel(this, provider);
        }
    }

    public String[] getKnownAttributeNames() {
        final String[] knownAttributeNames = new String[this.dataStore.getElements().size()];
        int i = 0;
        for (final AbstractTypeModel knownModel : this.dataStore.getElements()) {
            knownAttributeNames[i++] = knownModel.getDataProvider().getName();
        }
        return knownAttributeNames;
    }
}