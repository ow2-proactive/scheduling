/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * UpdatableProperties allow to know if a specified value has been modified or not.
 * <p>
 * Useful to know if the default value has been kept.
 * <p>
 * Managed parameter entities are RestartMode, BooleanWrapper, IntegerWrapper.
 * If you want to add more entities, just add it in the @anyMetaDef annotation.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
@XmlAccessorType(XmlAccessType.FIELD)
@PublicAPI
public class UpdatableProperties<T> implements Serializable {

    /** The value of this property. */
    @XmlValue
    private T value = null;

    /** If the property has been set. */
    @XmlTransient
    private boolean set = false;

    /** HIBERNATE default constructor */
    @SuppressWarnings("unused")
    private UpdatableProperties() {
    }

    /**
     * Create a new instance of UpdatableProperties using a specified value.
     * <p>
     * This value will be considered has the default one.
     */
    public UpdatableProperties(T defaultValue) {
        this.value = defaultValue;
    }

    /**
     * Get the value of the property.
     * 
     * @return the value of the property.
     */
    public T getValue() {
        return value;
    }

    /**
     * Set the value of the property.
     * This action will remember that the value is not the default one anymore.
     * 
     * @param value the new value to be set.
     */
    public void setValue(T value) {
        this.value = value;
        this.set = true;
    }

    /**
     * Tell if the value has been set or if it is the default one.
     * 
     * @return true if the default value has been changed.
     */
    public boolean isSet() {
        return set;
    }

}
