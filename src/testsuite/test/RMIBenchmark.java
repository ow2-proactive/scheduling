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
import java.rmi.Naming;
import java.rmi.Remote;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;


/**
 * @author Alexandre di Costanzo
 *
 */
public abstract class RMIBenchmark extends ProActiveBenchmark
    implements Serializable, Remote {
    private String rmiObjectName = "RMIBenchmark" + System.currentTimeMillis();
    private RMIBenchmark rmiObject = null;
    private RMIBenchmark remoteObject = null;

    /**
     *
     */
    public RMIBenchmark() {
        super(null, "RMI Benchmark", "Create benchmark with Java RMI.");
    }

    /**
     * @param name
     * @param description
     */
    public RMIBenchmark(Node node, String name, String description) {
        super(node, name, description);
        initRegistry();
    }

    /**
     * @param name
     * @param description
     */
    public RMIBenchmark(Node node, String name, String description,
        String rmiObjectName) {
        super(node, name, description);
        this.rmiObjectName = rmiObjectName;
        initRegistry();
    }

    private void initRegistry() {
        if (logger.isDebugEnabled()) {
            logger.debug("Begin init rmiregistry ...");
        }
        try {
            RMIBenchmark active = (RMIBenchmark) ProActive.newActive(getClass()
                                                                         .getName(),
                    null, getNode());
            active.setRmiObjectName(rmiObjectName);
            active.bind();
        } catch (ActiveObjectCreationException e) {
            logger.fatal("Can't create an active object", e);
            new RuntimeException(e);
        } catch (NodeException e) {
            logger.fatal("problem with the node", e);
            new RuntimeException(e);
        } catch (Exception e) {
            logger.fatal("Can't bind the class in the rmiregistry", e);
            new RuntimeException(e);
        }
    }

    public void bind() throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        Class c = cl.loadClass(getClass().getName());
        remoteObject = (RMIBenchmark) c.newInstance();
        Naming.rebind("//localhost/" + getRmiObjectName(), remoteObject);
    }

    public void unbind() throws Exception {
        Naming.unbind("//localhost/" + getRmiObjectName());
    }

    /**
     * Don't override this one
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
        rmiObject = (RMIBenchmark) Naming.lookup("//" +
                getNode().getNodeInformation().getInetAddress().getHostAddress() +
                "/" + getRmiObjectName());
    }

    /**
     * @see testsuite.test.AbstractTest#endTest()
     */
    public void endTest() throws Exception {
        // nothing to do
    }

    /**
     * @return
     */
    public String getRmiObjectName() {
        return rmiObjectName;
    }

    /**
     * @param rmiObjectName
     */
    public void setRmiObjectName(String rmiObjectName) {
        this.rmiObjectName = rmiObjectName;
    }

    /**
     * @return
     */
    public RMIBenchmark getRmiObject() {
        return rmiObject;
    }
}
