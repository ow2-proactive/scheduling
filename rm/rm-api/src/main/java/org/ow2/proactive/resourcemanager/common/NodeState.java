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
package org.ow2.proactive.resourcemanager.common;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Enumeration of all states of a RMNode :<BR>
 * -deploying : node deployment is on going. <BR>
 * -lost : node deployment failed. <BR>
 * -free : node is ready to perform a task.<BR>
 * -configuring: node has been added to the RM but is in configuring state, not ready to perform a task.<BR>
 * -busy : node is executing a task.<BR>
 * -to be removed : node is busy and have to be removed at the end of its current task.<BR>
 * -down : node is broken, and not anymore able to perform tasks.<BR>
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
@PublicAPI
public enum NodeState {

    // WARNING: do not change the order of the fields in this enum or
    // some features will be broken (e.g. SQL queries in RMAccountsManager)
    // As a corollary, new fields must be added at the end!

    /**
     * a node that can be provided to a RM user, and able to perform a task
     */
    FREE("Free"),
    /**
     * a node provided to a RM user.
     */
    BUSY("Busy"),
    /**
     * a node that has been detected down.
     */
    DOWN("Down"),
    /**
     * a busy node which must be removed from resource manager when the user
     * will give back the node.
     */
    TO_BE_REMOVED("To be removed"),
    /**
     * a node for which one the deployment process has been triggered but which
     * is not registered yet.
     */
    DEPLOYING("Deploying"),
    /**
     * a node for which one the deployment process failed
     */
    LOST("Lost"),
    /**
     * a node cannot be provided to a RM user, it is under configuration
     */
    CONFIGURING("Configuring");

    private String desc;

    /**
     * Constructor
     *
     * @param description Human readable description of the state.
     */
    NodeState(String description) {
        this.desc = description;
    }

    /**
     * Returns the enum constant of the specified NodeState with the specified description.
     *
     * @param value the value returned by {@link NodeState#toString()}.
     * @return enum instance corresponding the String representation provided.
     * @throws IllegalArgumentException if the specified enum type has no constant with the specified name, or the specified class object does not represent an enum type.
     * @throws NullPointerException if enumType or name is {@code null}.
     */
    public static NodeState parse(String value) {
        for (NodeState nodeState : values()) {
            if (nodeState.toString().equals(value)) {
                return nodeState;
            }
        }

        throw new IllegalArgumentException("'" + value + "' is not a valid NodeState");
    }

    /**
     * Returns a string representation of the state.
     *
     * @return String representation of the state.
     */
    @Override
    public String toString() {
        return desc;
    }

}
