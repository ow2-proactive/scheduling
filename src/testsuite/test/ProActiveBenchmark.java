/*
 * Created on Jul 25, 2003
 *
 */
package testsuite.test;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.node.Node;


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
     */
    public ProActiveBenchmark(Node node, Logger logger) {
        super(logger, "Remote Benchmark",
            "This benchmark is executed in remote host.");
        this.node = node;
    }

    /**
     * @param logger
     * @param name
     */
    public ProActiveBenchmark(Node node, Logger logger, String name) {
        super(logger, name, "This benchmark is executed in remote host.");
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

    /**
     * @param logger
     * @param name
     * @param description
     */
    public ProActiveBenchmark(Node node, Logger logger, String name,
        String description) {
        super(logger, name, description);
        this.node = node;
    }

    public void killVM() {
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
}
