/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Any;
import org.hibernate.annotations.AnyMetaDef;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.MetaValue;
import org.hibernate.annotations.Proxy;
import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scheduler.common.task.util.BooleanWrapper;
import org.ow2.proactive.scheduler.common.task.util.IntegerWrapper;


/**
 * UpdatableProperties allow to know if a specified value has been modified or not.<br />
 * Useful to know if the default value has been kept.<br/> <br/>
 * Managed parameter entities are RestartMode, BooleanWrapper, IntegerWrapper.
 * If you want to add more entities, just add it in the @anyMetaDef annotation.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
@Entity
@Table(name = "UPDATABLE_PROPERTIES")
@AccessType("field")
@Proxy(lazy = false)
@PublicAPI
public class UpdatableProperties<T> implements Serializable {
    /**  */
	private static final long serialVersionUID = 21L;

	@Id
    @GeneratedValue
    @SuppressWarnings("unused")
    private long hId;

    /** The value of this property. */
    @Any(metaColumn = @Column(name = "VALUE_TYPE", length = 5))
    @AnyMetaDef(idType = "long", metaType = "string", metaValues = {
            @MetaValue(targetEntity = RestartMode.class, value = "RM"),
            @MetaValue(targetEntity = BooleanWrapper.class, value = "BW"),
            @MetaValue(targetEntity = IntegerWrapper.class, value = "IW") })
    @JoinColumn(name = "VALUE_ID")
    @Cascade(CascadeType.ALL)
    private T value = null;

    /** If the property has been set. */
    @Column(name = "SET_")
    private boolean set = false;

    /** HIBERNATE default constructor */
    @SuppressWarnings("unused")
    private UpdatableProperties() {
    }

    /**
     * Create a new instance of UpdatableProperties using a specified value.<br />
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
