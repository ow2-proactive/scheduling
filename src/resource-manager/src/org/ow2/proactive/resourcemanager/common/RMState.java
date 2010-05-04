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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.resourcemanager.common;

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;


/**
 * RMState represents informations about RM activity.
 *
 * @author The ProActive Team
 *
 * $Id$
 */
@PublicAPI
public class RMState implements Serializable {

    private int freeNodesNumber;
    private int totalAliveNodesNumber;
    private int totalNodesNumber;

    public RMState(int freeNodesNumber, int totalAliveNodesNumber, int totalNodesNumber) {
        this.freeNodesNumber = freeNodesNumber;
        this.totalAliveNodesNumber = totalAliveNodesNumber;
        this.totalNodesNumber = totalNodesNumber;
    }

    /**
     * Get the number of all resources.
     * 
     * @return the number of all resources.
     */
    @Deprecated
    public IntWrapper getNumberOfAllResources() {
        return new IntWrapper(totalNodesNumber);
    }

    /**
     * Get the number of free resources.
     * 
     * @return the number of free resources.
     */
    @Deprecated
    public IntWrapper getNumberOfFreeResources() {
        return new IntWrapper(freeNodesNumber);
    }

    /**
     * Sets the number Of All Resources to the given numberOfAllResources value.
     *
     * @param numberOfAllResources the number Of All Resources to set.
     */
    @Deprecated
    public void setNumberOfAllResources(IntWrapper numberOfAllResources) {
        this.totalNodesNumber = numberOfAllResources.intValue();
    }

    /**
     * Sets the number Of Free Resources to the given numberOfFreeResources value.
     *
     * @param numberOfFreeResources the number Of Free Resources to set.
     */
    @Deprecated
    public void setNumberOfFreeResources(IntWrapper numberOfFreeResources) {
        this.freeNodesNumber = numberOfFreeResources.intValue();
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
