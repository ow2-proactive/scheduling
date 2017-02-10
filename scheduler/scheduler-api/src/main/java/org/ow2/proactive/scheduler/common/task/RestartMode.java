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
package org.ow2.proactive.scheduler.common.task;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.objectweb.proactive.annotation.PublicAPI;

import com.google.common.annotations.VisibleForTesting;


/**
 * This class represents the different restart mode for a task if an error occurred during its execution.<br>
 * !! It is no longer an Enum since the rise of Hibernate.
 *
 * @author The ProActive Team
 * @since ProActive 4.0
 */
@PublicAPI
@XmlRootElement(name = "restartmode")
@XmlAccessorType(XmlAccessType.FIELD)
public class RestartMode implements java.io.Serializable {

    /**
     * The task will be restarted according to its possible resources.
     */
    public static final RestartMode ANYWHERE = new RestartMode(1, "Anywhere");

    /**
     * The task will be restarted on an other node.
     */
    public static final RestartMode ELSEWHERE = new RestartMode(2, "Elsewhere");

    @XmlAttribute
    private int index;

    @XmlAttribute
    private String description;

    public RestartMode() {
    }

    /**
     * Implicit constructor of a restart mode.
     *
     * @param description the name of the restart mode.
     */
    @VisibleForTesting
    protected RestartMode(int index, String description) {
        this.index = index;
        this.description = description;
    }

    /**
     * Return the RestartMode corresponding to the given {@code description}.
     *
     * @param description a string representing the restart mode.
     *
     * @return the RestartMode.
     */
    public static RestartMode getMode(String description) {
        if (ELSEWHERE.description.equalsIgnoreCase(description)) {
            return ELSEWHERE;
        } else {
            return ANYWHERE;
        }
    }

    public static RestartMode getMode(int restartModeId) {
        switch (restartModeId) {
            case 1:
                return RestartMode.ANYWHERE;
            case 2:
                return RestartMode.ELSEWHERE;
            default:
                throw new IllegalArgumentException("Unknown restart mode: " + restartModeId);
        }
    }

    public String getDescription() {
        return description;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RestartMode that = (RestartMode) o;

        return index == that.index;
    }

    @Override
    public int hashCode() {
        return index;
    }

    @Override
    public String toString() {
        return description;
    }

}
