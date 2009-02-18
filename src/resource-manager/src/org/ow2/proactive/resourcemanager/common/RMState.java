/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 * @date 12 mars 08
 *
 */
@PublicAPI
public class RMState implements Serializable {

    private IntWrapper numberOfAllResources;
    private IntWrapper numberOfFreeResources;

    /**
     * Get the number of all resources.
     * 
     * @return the number of all resources.
     */
    public IntWrapper getNumberOfAllResources() {
        return numberOfAllResources;
    }

    /**
     * Get the number of free resources.
     * 
     * @return the number of free resources.
     */
    public IntWrapper getNumberOfFreeResources() {
        return numberOfFreeResources;
    }

    /**
     * Return true if the scheduler has free resources, false if not.
     * 
     * @return true if the scheduler has free resources, false if not.
     */
    public BooleanWrapper hasFreeResources() {
        return new BooleanWrapper(numberOfFreeResources.intValue() != 0);
    }

    /**
     * Sets the number Of All Resources to the given numberOfAllResources value.
     *
     * @param numberOfAllResources the number Of All Resources to set.
     */
    public void setNumberOfAllResources(IntWrapper numberOfAllResources) {
        this.numberOfAllResources = numberOfAllResources;
    }

    /**
     * Sets the number Of Free Resources to the given numberOfFreeResources value.
     *
     * @param numberOfFreeResources the number Of Free Resources to set.
     */
    public void setNumberOfFreeResources(IntWrapper numberOfFreeResources) {
        this.numberOfFreeResources = numberOfFreeResources;
    }

}
