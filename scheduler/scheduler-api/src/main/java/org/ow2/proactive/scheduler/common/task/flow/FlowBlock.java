/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
 * Possible values for Task Block declaration
 * <p>
 * Each Task can hold a FlowBlock element;
 * at least two tasks are needed to create a Task Block
 * 
 * 
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 * 
 */
@PublicAPI
public enum FlowBlock {

    /**
     * No specific block information
     */
    NONE("none"),

    /**
     * Marks the beginning of a new block
     */
    START("start"),

    /**
     * Marks the ending of the last opened block
     */
    END("end");

    private String str = "";

    /**
     * Default constructor
     * 
     * @param str string representation
     */
    private FlowBlock(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return this.str;
    }

    /**
     * Parses a string containing the textual representation of a FlowBlock
     * 
     * @param str the string to parse
     * @return the type reflected by the string, or NONE if none matches
     */
    public static FlowBlock parse(String str) {
        if (str == null) {
            return FlowBlock.NONE;
        }
        if (str.equalsIgnoreCase(FlowBlock.START.toString())) {
            return FlowBlock.START;
        } else if (str.equalsIgnoreCase(FlowBlock.END.toString())) {
            return FlowBlock.END;
        } else {
            return FlowBlock.NONE;
        }
    }

}
