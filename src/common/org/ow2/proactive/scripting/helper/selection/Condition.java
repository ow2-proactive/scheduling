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
