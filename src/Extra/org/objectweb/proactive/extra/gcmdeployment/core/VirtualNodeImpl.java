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
import java.util.Collection;
import java.util.List;

import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.FileTransferBlock;
import org.objectweb.proactive.extra.gcmdeployment.GCMDeployment.GCMDeploymentDescriptor;


public class VirtualNodeImpl implements VirtualNodeInternal {
    private long requiredCapacity;
    private String id;
    private List<GCMDeploymentDescriptor> providers;

    /** All File Transfer Block associated to this VN */
    private List<FileTransferBlock> fts;

    public VirtualNodeImpl() {
        fts = new ArrayList<FileTransferBlock>();
        providers = new ArrayList<GCMDeploymentDescriptor>();
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

    public List<GCMDeploymentDescriptor> getProviders() {
        return providers;
    }

    public void addProvider(GCMDeploymentDescriptor provider) {
        providers.add(provider);
    }

    public void addProviders(Collection<GCMDeploymentDescriptor> providers) {
        providers.addAll(providers);
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
}
