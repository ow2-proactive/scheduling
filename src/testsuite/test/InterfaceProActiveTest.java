/*
 * Created on Aug 8, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package testsuite.test;

import org.objectweb.proactive.core.node.Node;


/**
 * @author adicosta
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface InterfaceProActiveTest {
    public void killVM();

    /**
     * @return
     */
    public Node getNode();

    /**
     * @param node
     */
    public void setNode(Node node);

	public Node getSameVMNode();

	public Node getLocalVMNode();

	public Node getRemoteVMNode();
	
}
