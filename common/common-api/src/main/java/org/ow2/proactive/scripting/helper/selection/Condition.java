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
package org.ow2.proactive.scripting.helper.selection;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * A Condition object is a structure which defines
 * - the name of the property
 * - the value which will compare
 * - the operator which will be used to operate this
 *
 * In order to compare some values with a local property file, create one or
 * several Condition object and call CheckProperty or CheckProperties method
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
@PublicAPI
public class Condition {

    /** Name of the condition */
    private String name;

    /** Operator */
    private int operator;

    /** Value to compare */
    private String value;

    /**
     * Create a new condition using its name, operator and value.
     *
     * @param name of the property to check
     * @param operator the operator on which to base the comparison
     * @param value the value to be compared
     */
    public Condition(String name, int operator, String value) {
        this.name = name;
        this.operator = operator;
        this.value = value;
    }

    /**
     * Get the name of the condition
     *
     * @return the name of the condition
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the operator of the condition
     *
     * @return the operator of the condition
     */
    public int getOperator() {
        return this.operator;
    }

    /**
     * Get the value of the condition
     *
     * @return the value of the condition as a String
     */
    public String getValue() {
        return this.value;
    }

}
