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

import java.io.Serializable;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


/**
 * RMState represents information about RM activity.
 *
 * @author The ProActive Team
 *
 * $Id$
 */
@PublicAPI
@XmlRootElement
public class RMState implements Serializable {

    private final RMStateNodeUrls rmNodeUrls;

    private final Long maxNumberOfNodes;

    public RMState(RMStateNodeUrls rmNodeUrls, Long maxNumberOfNodes) {
        this.rmNodeUrls = rmNodeUrls;
        this.maxNumberOfNodes = maxNumberOfNodes;
    }

    public Long getMaxNumberOfNodes() {
        return maxNumberOfNodes;
    }

    protected RMStateNodeUrls getRmNodeUrls() {
        return rmNodeUrls;
    }

    /**
     * Return true if the scheduler has free resources, false if not.
     *
     * @return true if the scheduler has free resources, false if not.
     */
    public BooleanWrapper hasFreeResources() {
        return new BooleanWrapper(!getFreeNodes().isEmpty());
    }

    /**
     * Returns free nodes number in the resource manager.
     *
     * @return free nodes number in the resource manager
     */
    public int getFreeNodesNumber() {
        return getFreeNodes().size();
    }

    /**
     * Returns a set containing all free nodes urls in the resource manager.
     *
     * @return free nodes urls in the resource manager
     */
    public Set<String> getFreeNodes() {
        return getRmNodeUrls().getFreeNodesUrls();
    }

    /**
     * Returns total alive nodes number in the resource manager.
     *
     * @return total alive nodes number in the resource manager
     */
    public int getTotalAliveNodesNumber() {
        return getAliveNodes().size();
    }

    /**
     * Returns a set containing all alive nodes urls in the resource manager.
     *
     * @return all alive nodes urls in the resource manager
     */
    public Set<String> getAliveNodes() {
        return getRmNodeUrls().getAliveNodesUrls();
    }

    /**
     * Returns total nodes number (including dead nodes) in the resource manager.
     *
     * @return total nodes number in the resource manager
     */
    public int getTotalNodesNumber() {
        return getAllNodes().size();
    }

    /**
     * Returns a set containing all nodes urls (including dead nodes) in the resource manager.
     *
     * @return all nodes urls in the resource manager
     */
    public Set<String> getAllNodes() {
        return getRmNodeUrls().getAllNodesUrls();
    }

}
