/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.common;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Set;


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
        return new BooleanWrapper(!getRmNodeUrls().getFreeNodesUrls().isEmpty());
    }

    /**
     * Returns free nodes number in the resource manager.
     *
     * @return free nodes number in the resource manager
     */
    public int getFreeNodesNumber() {
        return getRmNodeUrls().getFreeNodesUrls().size();
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
        return getRmNodeUrls().getAliveNodesUrls().size();
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
        return getRmNodeUrls().getAllNodesUrls().size();
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
