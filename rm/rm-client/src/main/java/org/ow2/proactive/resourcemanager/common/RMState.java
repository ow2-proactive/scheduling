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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.common;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


/**
 * RMState represents informations about RM activity.
 *
 * @author The ProActive Team
 *
 * $Id$
 */
@PublicAPI
@XmlRootElement
public class RMState implements Serializable {

    private static final long serialVersionUID = 62L;

    private int freeNodesNumber;
    private int totalAliveNodesNumber;
    private int totalNodesNumber;

    public RMState(int freeNodesNumber, int totalAliveNodesNumber, int totalNodesNumber) {
        this.freeNodesNumber = freeNodesNumber;
        this.totalAliveNodesNumber = totalAliveNodesNumber;
        this.totalNodesNumber = totalNodesNumber;
    }

    /**
     * Return true if the scheduler has free resources, false if not.
     *
     * @return true if the scheduler has free resources, false if not.
     */
    public BooleanWrapper hasFreeResources() {
        return new BooleanWrapper(freeNodesNumber != 0);
    }

    /**
     * Returns free nodes number in the resource manager.
     *
     * @return free nodes number in the resource manager
     */
    public int getFreeNodesNumber() {
        return freeNodesNumber;
    }

    /**
     * Returns total alive nodes number in the resource manager.
     *
     * @return total alive nodes number in the resource manager
     */
    public int getTotalAliveNodesNumber() {
        return totalAliveNodesNumber;
    }

    /**
     * Returns total nodes number (including dead nodes) in the resource manager.
     *
     * @return total nodes number in the resource manager
     */
    public int getTotalNodesNumber() {
        return totalNodesNumber;
    }
}
