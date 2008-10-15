/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.common.task;

import java.io.Serializable;


/**
 * UpdatableProperties allow to know if a specified value has been modified or not.<br />
 * Useful to know if the default value has been kept.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public class UpdatableProperties<T> implements Serializable {

    /** The value of this property. */
    private T value = null;
    /** If the property has been set. */
    private boolean set = false;

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
