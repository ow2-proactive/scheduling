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
package org.objectweb.proactive.extra.gcmdeployment.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.FileTransferBlock;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentDescriptor;
import static org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers.GCMA_LOGGER;
public class VirtualNodeImpl implements VirtualNodeInternal {

    /** The, unique, name of this Virtual Node */
    private String id;

    /** The number of Node awaited
     *
     * If 0 the Virtual Node will try to get as many node as possible
     */
    private long requiredCapacity;

    /** Resource providers contributing to this Virtual Node
     *
     * The value indicates how many nodes a resource providers must contribute
     * to the Virtual Node
     */
    private HashSet<GCMDeploymentDescriptor> providers;

    /** All File Transfer Block associated to this VN */
    private List<FileTransferBlock> fts;

    public VirtualNodeImpl() {
        fts = new ArrayList<FileTransferBlock>();
        providers = new HashSet<GCMDeploymentDescriptor>();
    }

    public long getRequiredCapacity() {
        return requiredCapacity;
    }

    public void setRequiredCapacity(long requiredCapacity) {
        this.requiredCapacity = requiredCapacity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void addProvider(GCMDeploymentDescriptor provider) {
        providers.add(provider);
    }

    public Set<GCMDeploymentDescriptor> getProviders() {
        return providers;
    }

    public void addFileTransfertBlock(FileTransferBlock ftb) {
        fts.add(ftb);
    }

    public String getName() {
        return id;
    }

    public void check() throws IllegalStateException {
        if (providers.size() == 0) {
            throw new IllegalStateException("providers is empty in " + this);
        }
    }

    public void checkDirectMode() throws IllegalStateException {
        // TODO Auto-generated method stub
    }

    public DeploymentTree getDeploymentTree() {
        // TODO Auto-generated method stub
        return null;
    }

    public Set<Node> getNodes() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isReady() {
        // TODO Auto-generated method stub
        return false;
    }

    public void addNode(Node node) {
        if (requiredCapacity != MAX_CAPACITY) {
            requiredCapacity--;
            if (requiredCapacity < 0) {
                GCMA_LOGGER.warn("Virtual Node " + id +
                    " received to many node !");
            }
        }
    }

    public boolean needContribution() {
        if (requiredCapacity == MAX_CAPACITY) {
            return false;
        }

        if (requiredCapacity < 0) {
            return false;
        }

        return true;
    }
}
