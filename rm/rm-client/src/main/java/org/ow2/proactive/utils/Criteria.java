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
package org.ow2.proactive.utils;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    // required number of nodes
    private int size;

    // nodes topology
    private TopologyDescriptor topology;

    // selection scripts
    private List<SelectionScript> scripts;

    // bindings used for selection scripts
    private Map<String, Serializable> bindings;

    // nodes that cannot be in resulting node set
    private NodeSet blackList;

    // if true RM returns up to required number of nodes that satisfy to selection scripts and topology
    // if false it either exact number of empty node set
    private boolean bestEffort = true;

    // optional computation descriptors
    private Collection<String> computationDescriptors;

    // token for accessing nodes
    private String nodeAccessToken;

    // optional set of nodes urls which are acceptable
    private Set<String> setOfAcceptableNodesUrls;

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
     * Sets variable bindings map used by selection scripts
     *
     * @param bindings
     */
    public void setBindings(Map<String, Serializable> bindings) {
        this.bindings = bindings;
    }

    /**
     * returns the bindings map used by selection scripts
     *
     * @return
     */
    public Map<String, Serializable> getBindings() {
        return bindings;
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
     * Sets acceptables nodes that can resulting nodes list.
     */
    public void setAcceptableNodesUrls(Set<String> acceptableNodesUrls) {
        this.setOfAcceptableNodesUrls = acceptableNodesUrls;
    }

    /**
     * @return urls of nodes that can be in resulting nodes list.
     */
    public Set<String> getAcceptableNodesUrls() {
        return setOfAcceptableNodesUrls;
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
     * @param nodeAccessToken
     */
    public void setNodeAccessToken(String nodeAccessToken) {
        this.nodeAccessToken = nodeAccessToken;
    }

}
