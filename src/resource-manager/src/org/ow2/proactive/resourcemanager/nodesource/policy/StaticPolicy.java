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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.nodesource.policy;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.nodesource.utils.NamesConvertor;


/**
 *
 * A policy defined static nodes acquisition. Once nodes were added to a node source
 * it initiates nodes acquisition. Releases nodes at node source shutdown or by user request.
 *
 */
public class StaticPolicy extends RMAwareNodeSourcePolicy {

    /**
     * Configure a policy with given parameters.
     * @param policyParameters parameters defined by user
     */
    public void configure(Object... policyParameters) {
    }

    /**
     * Activates static policy. Register a listener in RMMonitoring
     */
    public BooleanWrapper activate() {
        BooleanWrapper activationStatus = super.activate();

        if (!activationStatus.booleanValue()) {
            return activationStatus;
        }

        acquireAllNodes();
        return new BooleanWrapper(true);
    }

    /**
     * Disactivates static policy. Removes listener from RMMonitoring if RM is not shutdown mode.
     */
    public BooleanWrapper disactivate() {
        BooleanWrapper activationStatus = super.disactivate();

        if (!activationStatus.booleanValue()) {
            return activationStatus;
        }
        return new BooleanWrapper(true);
    }

    /**
     * Description for the UI
     */
    public String getDescription() {
        return "Static nodes acquisition.";
    }

    /**
     * Reacts on new nodes acquisition info and requests to acquire all nodes.
     */
    public void nodeSourceNodesAcquisitionInfoAddedEvent(RMNodeSourceEvent event) {
        acquireAllNodes();
    }

    /**
     * Creates string representation of the policy
     */
    public String toString() {
        return NamesConvertor.beautifyName(this.getClass().getSimpleName());
    }
}
