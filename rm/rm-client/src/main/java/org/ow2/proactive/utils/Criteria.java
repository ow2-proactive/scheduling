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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.utils;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.scripting.SelectionScript;
import org.ow2.proactive.topology.descriptor.TopologyDescriptor;


/**
 *
 * When user searches for nodes in the resource manager he
 * can specify various criteria.
 *
 * This class encapsulates requirements and limitations for
 * resulting node set.
 *
 */
@PublicAPI
public class Criteria implements Serializable {

    private static final long serialVersionUID = 60L;

    // required number of nodes
    private int size;

    // nodes topology
    private TopologyDescriptor topology;
    // selection scripts
    private List<SelectionScript> scripts;
    // nodes that cannot be in resulting node set
    private NodeSet blackList;
    // if true RM returns up to required number of nodes that satisfy to selection scripts and topology
    // if false it either exact number of empty node set
    private boolean bestEffort = true;
    // optional computation descriptors
    private Collection<String> computationDescriptors;
    // token for accessing nodes
    private String nodeAccessToken;

    /**
     * Creates criteria instance
     * 
     * @param size - required number of nodes
     */
    public Criteria(int size) {
        this.size = size;
    }

    /**
     * @return required number of nodes
     */
    public int getSize() {
        return size;
    }

    /**
     * @return a topology descriptor {@link TopologyDescriptor} 
     */
    public TopologyDescriptor getTopology() {
        return topology;
    }

    /**
     * Sets topology criteria for nodes {@link TopologyDescriptor}
     */
    public void setTopology(TopologyDescriptor topology) {
        this.topology = topology;
    }

    /**
     * @return a selection scripts list. All have to pass on every single node.
     */
    public List<SelectionScript> getScripts() {
        return scripts;
    }

    /**
     * Sets a selection scripts list. All have to pass on every single node.
     */
    public void setScripts(List<SelectionScript> scripts) {
        this.scripts = scripts;
    }

    /**
     * @return nodes that have not be in resulting nodes list.
     */
    public NodeSet getBlackList() {
        return blackList;
    }

    /**
     * Sets nodes that have not be in resulting nodes list.
     */
    public void setBlackList(NodeSet blackList) {
        this.blackList = blackList;
    }

    /**
     * @return the mode of selection
     */
    public boolean isBestEffort() {
        return bestEffort;
    }

    /**
     * Sets the mode of selection:
     * if best effort is true RM returns up to required number of nodes that satisfy to selection scripts and topology
     * if false it either exact number of empty node set.
     *
     */
    public void setBestEffort(boolean bestEffort) {
        this.bestEffort = bestEffort;
    }

    /**
     * @return a set of descriptors of computations for which nodes will be reserved (for information purposes). 
     */
    public Collection<String> getComputationDescriptors() {
        return computationDescriptors;
    }

    /**
     * Sets descriptors of computations for which nodes will be reserved (for information purposes).
     */
    public void setComputationDescriptors(Collection<String> computationDescriptors) {
        this.computationDescriptors = computationDescriptors;
    }

    /**
     * If a token is specified the resource manager only looks for nodes having this token.
     * If no token is specified in the criteria, nodes protected by token will not be selected.
     *
     * @return a key for accessing nodes
     */
    public String getNodeAccessToken() {
        return nodeAccessToken;
    }

    /**
     * If key is specified the resource manager only looks for nodes having this token.
     * If no token is specified in the criteria, nodes protected by token will not be selected.
     *
     * @param nodeAccessKey
     */
    public void setNodeAccessToken(String nodeAccessToken) {
        this.nodeAccessToken = nodeAccessToken;
    }

}
