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
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.masterslave;

import java.io.Serializable;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extra.masterslave.core.AOMaster;
import org.objectweb.proactive.extra.masterslave.core.TaskException;
import org.objectweb.proactive.extra.masterslave.interfaces.Master;
import org.objectweb.proactive.extra.masterslave.interfaces.Task;


public class ProActiveMaster<T extends Task<R>, R extends Serializable>
    implements Master<T, R>, Serializable {
    protected AOMaster aomaster = null;

    /**
     * An empty master (you can add resources afterwards)
     */
    public ProActiveMaster() {
    }

    /**
     * Creates a master with a collection of nodes
     * @param nodes
     */
    public ProActiveMaster(Collection<Node> nodes) {
        try {
            aomaster = (AOMaster) ProActive.newActive(AOMaster.class.getName(),
                    new Object[] { nodes });
        } catch (ActiveObjectCreationException e) {
            throw new IllegalArgumentException(e);
        } catch (NodeException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Creates a master with a descriptorURL and an array of virtual node names
     * @param descriptorURL
     * @param virtualNodeNames
     */
    public ProActiveMaster(URL descriptorURL, String virtualNodeName) {
        try {
            aomaster = (AOMaster) ProActive.newActive(AOMaster.class.getName(),
                    new Object[] { descriptorURL, virtualNodeName });
        } catch (ActiveObjectCreationException e) {
            throw new IllegalArgumentException(e);
        } catch (NodeException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Creates a master with the given virtual node
     * @param virtualNode
     */
    public ProActiveMaster(VirtualNode virtualNode) {
        try {
            aomaster = (AOMaster) ProActive.newActive(AOMaster.class.getName(),
                    new Object[] { virtualNode });
        } catch (ActiveObjectCreationException e) {
            throw new IllegalArgumentException(e);
        } catch (NodeException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#addResources(java.util.Collection)
     */
    public void addResources(Collection<Node> nodes) {
        aomaster.addResources(nodes);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#addResources(java.net.URL, java.lang.String)
     */
    public void addResources(URL descriptorURL, String virtualNodeName) {
        aomaster.addResources(descriptorURL, virtualNodeName);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#addResources(org.objectweb.proactive.core.descriptor.data.VirtualNode)
     */
    public void addResources(VirtualNode virtualnode) {
        aomaster.addResources(virtualnode);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#areAllResultsAvailable()
     */
    public boolean areAllResultsAvailable() {
        return aomaster.areAllResultsAvailable();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#isOneResultAvailable()
     */
    public boolean isOneResultAvailable() {
        return aomaster.isOneResultAvailable();
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#isResultAvailable(org.objectweb.proactive.extra.masterslave.interfaces.Task)
     */
    public boolean isResultAvailable(T task) throws IllegalArgumentException {
        return aomaster.isResultAvailable(task);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#solve(org.objectweb.proactive.extra.masterslave.interfaces.Task)
     */
    public void solve(T task) {
        aomaster.solve(task);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#solveAll(java.util.Collection)
     */
    public void solveAll(Collection<T> tasks) {
        aomaster.solveAll(tasks);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#terminate(boolean)
     */
    public void terminate(boolean freeResources) {
        // we use here the synchronous version
        aomaster.terminateIntern(freeResources);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#waitAllResults()
     */
    public Collection<R> waitAllResults() throws TaskException {
        Collection<R> results = (Collection<R>) aomaster.waitAllResults();
        ProActive.waitFor(results);
        return results;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#waitOneResult()
     */
    public R waitOneResult() throws TaskException {
        R res = (R) aomaster.waitOneResult();
        ProActive.waitFor(res);
        return res;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#waitResultOf(org.objectweb.proactive.extra.masterslave.interfaces.Task)
     */
    public R waitResultOf(T task)
        throws IllegalArgumentException, TaskException {
        R res = (R) aomaster.waitResultOf(task);
        ProActive.waitFor(res);
        return res;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.extra.masterslave.interfaces.Master#waitResultsOf(java.util.List)
     */
    public List<R> waitResultsOf(List<T> tasks)
        throws IllegalArgumentException, TaskException {
        List<R> results = (List<R>) aomaster.waitResultsOf(tasks);
        ProActive.waitFor(results);
        return results;
    }
}
