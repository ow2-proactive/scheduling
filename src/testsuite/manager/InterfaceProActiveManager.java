/*
 * Created on Aug 20, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package testsuite.manager;

import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;


/**
 * @author adicosta
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface InterfaceProActiveManager {
    public String getRemoteHostname();

    public void setRemoteHostname(String remoteHostName);

    /**
     * @return
     */
    public String getXmlFileLocation();

    /**
     * @param xmlFileLocation
     */
    public void setXmlFileLocation(String xmlFileLocation);

    /**
       * @return
       */
    public Node getLocalVMNode();

    /**
     * @return
     */
    public Node getRemoteVMNode();

    /**
     * @return
     */
    public Node getSameVMNode();

    /**
     * @return
     */
    public VirtualNode[] getVirtualNodes();
}
