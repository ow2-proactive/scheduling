/*
 * Created on Sep 1, 2003
 *
 */
package testsuite.test;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;

import java.io.Serializable;

import java.rmi.Naming;
import java.rmi.Remote;


/**
 * @author adicosta
 *
 */
public abstract class RMIBenchmark extends ProActiveBenchmark
    implements Serializable, Remote {
    private String rmiObjectName = "RMIBenchmark"+System.currentTimeMillis();
    private RMIBenchmark rmiObject = null;

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
        Naming.rebind("//localhost/" + getRmiObjectName(), this);
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
