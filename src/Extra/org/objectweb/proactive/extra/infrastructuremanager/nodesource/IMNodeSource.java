/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.extra.infrastructuremanager.nodesource;

import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extra.infrastructuremanager.imnode.IMNode;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.dynamic.DynamicNodeSource;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.dynamic.P2PNodeSource;
import org.objectweb.proactive.extra.infrastructuremanager.nodesource.frontend.NodeSourceInterface;
import org.objectweb.proactive.extra.scheduler.common.scripting.SelectionScript;


/**
 * A node Source is an entity that can provide nodes
 * to the Infrastructure Manager.
 * The source may be based on ProActive Descriptors deployment, or on dynamic
 * node allocation, with a p2p node source for example
 * @see P2PNodeSource
 * @see DynamicNodeSource
 *
 * @author proActive team
 */
public abstract class IMNodeSource implements NodeSourceInterface {

    /**
     * String identifying the NodeSource. This must be unique.
     * @return
     */
    public abstract String getSourceId();

    @Override
    public int hashCode() {
        return getSourceId().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof IMNodeSource) {
            IMNodeSource o2 = (IMNodeSource) o;

            return getSourceId().equals(o2.getSourceId());
        }

        return false;
    }

    /**
     * Set the {@link IMNode} in a busy state.
     * There is nothing to do more than that, like expressly setting
     * busy state <i>a mano</i>.
     * @param imnode
     */
    public abstract void setBusy(IMNode imnode);

    /**
     * Set the {@link IMNode} in a free state.
     * A free node can be used by IM.
     * @param imnode
     */
    public abstract void setFree(IMNode imnode);

    /**
     * Set the {@link IMNode} in a down state.
     * A Node is down when it's no longer responding.
     * @param imnode
     */
    public abstract void setDown(IMNode imnode);

    /**
     * The way to to get free nodes in the structure, ordered (or not) with the script.
     * The more a Node has chances to verify the script, the less it's far in the list.
     */
    public abstract ArrayList<IMNode> getNodesByScript(SelectionScript script,
        boolean ordered);

    /**
     * Shutting down Node Manager, and everything depending on it.
     */
    public abstract BooleanWrapper shutdown();

    /**
     * That's the way to say to the NodeManager that a Node verifies a script.
     * This will help ordering nodes for future calls to {@link #getNodesByScript(SelectionScript)}.
     * @param imnode
     * @param script
     */
    public void setSelectionScript(IMNode imnode, SelectionScript script) {
        HashMap<SelectionScript, Integer> verifs = imnode.getScriptStatus();

        if (verifs.containsKey(script)) {
            verifs.remove(script);
        }

        verifs.put(script, IMNode.VERIFIED_SCRIPT);
    }

    /**
     * That's the way to say to the NodeManager that a Node doesn't (or no longer) verifie a script.
     * This will help ordering nodes for future calls to {@link #getNodesByScript(SelectionScript)}.
     * @param imnode
     * @param script
     */
    public void setNotSelectionScript(IMNode imnode, SelectionScript script) {
        HashMap<SelectionScript, Integer> verifs = imnode.getScriptStatus();

        if (verifs.containsKey(script)) {
            int status = verifs.remove(script);

            if (status == IMNode.NOT_VERIFIED_SCRIPT) {
                verifs.put(script, IMNode.NOT_VERIFIED_SCRIPT);
            } else {
                verifs.put(script, IMNode.NO_LONGER_VERIFIED_SCRIPT);
            }
        } else {
            verifs.put(script, IMNode.NOT_VERIFIED_SCRIPT);
        }
    }
}
