/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.scheduler.common.task.util;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlValue;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * BooleanWrapper is the Scheduler Wrapper object for boolean.
 * It is mostly used for Hibernate when using boolean with parametric type.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
@PublicAPI
@XmlAccessorType(XmlAccessType.FIELD)
public class BooleanWrapper implements Serializable {

    @XmlValue
    private boolean booleanValue;

    /**
     * Create a new instance of BooleanWrapper.
     *
     * @param value the boolean value of this wrapper.
     */
    public BooleanWrapper(boolean value) {
        this.booleanValue = value;
    }

    /**
     * Get the boolean value.
     *
     * @return the boolean value.
     */
    public boolean getBooleanValue() {
        return this.booleanValue;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof BooleanWrapper) ? booleanValue == ((BooleanWrapper) obj).booleanValue : false;
    }

}
