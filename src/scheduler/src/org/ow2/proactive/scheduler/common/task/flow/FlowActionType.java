/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 *              Nice-Sophia Antipolis/ActiveEon
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */

package org.ow2.proactive.scheduler.common.task.flow;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Control Flow Action types
 *
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 * 
 */
@PublicAPI
public enum FlowActionType {

    /** 
     * Fallback case: no action is performed
     */
    CONTINUE("continue"),

    /** 
     * Exclusive branching with optional join
     */
    IF("if"),

    /** 
     * Parallel split with join
     */
    REPLICATE("replicate"),

    /** 
     * Loop back in the flow to a previously executed task
     */
    LOOP("loop");

    private String str = "";

    /**
     * Default constructor
     * 
     * @param str string representation
     */
    private FlowActionType(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return this.str;
    }

    /**
     * Parses a string containing the textual representation of a FlowActionType
     * 
     * @param str the string to parse
     * @return the type reflected by the string, or continue if none matches
     */
    public static FlowActionType parse(String str) {
        if (str == null) {
            return FlowActionType.CONTINUE;
        }
        if (str.equalsIgnoreCase(FlowActionType.IF.toString())) {
            return FlowActionType.IF;
        } else if (str.equalsIgnoreCase(FlowActionType.REPLICATE.toString())) {
            return FlowActionType.REPLICATE;
        } else if (str.equalsIgnoreCase(FlowActionType.LOOP.toString())) {
            return FlowActionType.LOOP;
        } else {
            return FlowActionType.CONTINUE;
        }
    }

}
