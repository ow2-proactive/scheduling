/*
 * Created on Aug 8, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package testsuite.test;

import org.objectweb.proactive.core.node.Node;

import java.io.Serializable;


/**
 * @author adicosta
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class ProActiveFunctionalTest extends FunctionalTest
    implements Serializable, InterfaceProActiveTest {
    private Node node = null;

    public ProActiveFunctionalTest() {
    }

    /**
     *
     */
    public ProActiveFunctionalTest(Node node) {
        super("Remote Functional AbstractTest",
            "This test is executed in remote host.");
        this.node = node;
    }

    /**
     * @param logger
     * @param name
     */
    public ProActiveFunctionalTest(Node node, String name) {
        super(name, "This test is executed in remote host.");
        this.node = node;
    }

    /**
     * @param name
     * @param description
     */
    public ProActiveFunctionalTest(Node node, String name, String description) {
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
}
