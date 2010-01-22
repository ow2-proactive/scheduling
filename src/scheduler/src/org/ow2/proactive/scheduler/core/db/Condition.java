/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
package org.ow2.proactive.scheduler.core.db;

/**
 * Condition is the way to define the field to check and the value that must match.
 * It is used by DataBaseManager to restrict recover request to the Hibernate DB.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public class Condition {

    /** The field full name to check. */
    private String fieldFullName;
    /** The value of the field that must match. */
    private Object value;
    /** Comparator for the condition */
    private ConditionComparator comparator;

    /**
     * Create a new instance of Condition.<br/>
     * The given object value must match the same type of the field to be compared to.<br/>
     * ie: if 'fieldFullName' is a long, 'value' must be cast as a long : (long)32.
     *
     * @param fieldFullName the field of the condition.
     * @param comparator the comparator that will be used in the request.
     * @param value the value to set.
     */

    public Condition(String fieldFullName, ConditionComparator comparator, Object value) {
        this.fieldFullName = fieldFullName;
        this.value = value;
        this.comparator = comparator;
    }

    /**
     * Get the field of the condition.
     *
     * @return the field of the condition.
     */
    public String getField() {
        return fieldFullName;
    }

    /**
     * Set the field value to the given field value.
     *
     * @param fieldFullName the field to set.
     */
    public void setField(String fieldFullName) {
        this.fieldFullName = fieldFullName;
    }

    /**
     * Get the value of the condition.
     *
     * @return the value of the condition.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Set the value to the given value.
     *
     * @param value the value to set.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Get the comparator.
     *
     * @return the comparator.
     */
    public ConditionComparator getComparator() {
        return comparator;
    }

    /**
     * Set the comparator to the given comparator value.
     *
     * @param comparator the comparator to set.
     */
    public void setComparator(ConditionComparator comparator) {
        this.comparator = comparator;
    }

}
