/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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
package testsuite.test;

import java.io.Serializable;

import org.objectweb.proactive.core.node.Node;

import testsuite.manager.ProActiveBenchManager;


/**
 * Construct with newActive !!
 * @author Alexandre di Costanzo
 *
 */
public abstract class ProActiveBenchmark extends Benchmark
    implements Serializable, InterfaceProActiveTest {
    private Node node = null;
    private ProActiveBenchmark activeObject = null;

    public ProActiveBenchmark() {
    }

    /**
     *
     */
    public ProActiveBenchmark(Node node) {
        super("Remote Benchmark", "This benchmark is executed in remote host.");
        this.node = node;
    }

    /**
     * @param logger
     * @param name
     */
    public ProActiveBenchmark(Node node, String name) {
        super(name, "This benchmark is executed in remote host.");
        this.node = node;
    }

    /**
     * @param name
     * @param description
     */
    public ProActiveBenchmark(Node node, String name, String description) {
        super(name, description);
        this.node = node;
    }

    public void killVM() {
        if (logger.isDebugEnabled()) {
            logger.debug("kill VM");
        }
        System.exit(0);
    }

    /**
     * @return
     */
    public Node getNode() {
        return node;
    }

    /**
     * @param node
     */
    public void setNode(Node node) {
        this.node = node;
    }

    /**
     * @return
     */
    public ProActiveBenchmark getActiveObject() {
        return activeObject;
    }

    public void setActiveObject(ProActiveBenchmark activeObject) {
        this.activeObject = activeObject;
    }

    public Node getSameVMNode() {
        return ((ProActiveBenchManager) manager).getSameVMNode();
    }

    public Node getLocalVMNode() {
        return ((ProActiveBenchManager) manager).getLocalVMNode();
    }

    public Node getRemoteVMNode() {
        return ((ProActiveBenchManager) manager).getRemoteVMNode();
    }
}
