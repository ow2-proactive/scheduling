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
 * IntegerWrapper is the Scheduler Wrapper object for Integer.
 * It is mostly used for Hibernate when using Integer with parametric type.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
@PublicAPI
@XmlAccessorType(XmlAccessType.FIELD)
public class IntegerWrapper implements Serializable {

    @XmlValue
    private Integer value;

    /**
     * Create a new instance of IntegerWrapper.
     *
     * @param value the integer value of this wrapper.
     */
    public IntegerWrapper(Integer value) {
        this.value = value;
    }

    /**
     * Get the Integer value.
     *
     * @return the Integer value.
     */
    public Integer getIntegerValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof IntegerWrapper) ? value == ((IntegerWrapper) obj).value : false;
    }

}
