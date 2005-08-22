package modelisation.mixed;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;


public class Test {
    public static void main(String[] args) {
        ProActiveDescriptor pad;
        try {
            pad = ProActive.getProactiveDescriptor(args[0]);
            VirtualNode dispatcher = pad.getVirtualNode("Node1"); //------------- Returns the VirtualNode Dispatcher described in the xml file as a java object
            dispatcher.activate(); // ----------------- Activates the VirtualNode
            Node node = dispatcher.getNode(); //----------------Returns the first node available among nodes mapped to the VirtualNode
            System.out.println(pad.getVirtualNodeMappingSize());
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
    }
}
