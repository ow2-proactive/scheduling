import org.objectweb.proactive.api.ProDeployment;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeInformation;


public class TestDeployment {
    public static String URL_PAD1 = "infrastructuremanager/descriptor/3VNodes-4Jvms-10Nodes.xml";

    public static void DeployPAD(String urlPAD) {
        try {
            ProActiveDescriptor pad = ProDeployment.getProactiveDescriptor(urlPAD);
            pad.activateMappings();
            VirtualNode[] vnodes = pad.getVirtualNodes();
            for (int i = 0; i < vnodes.length; i++) {
                vnodes[i].activate();
            }
            VirtualNode vnode;
            Node[] nodes;
            Node node;
            NodeInformation nodeInfo;
            System.out.println("-");
            System.out.println("+--> Nombre de VNodes : " + vnodes.length);
            for (int i = 0; i < vnodes.length; i++) {
                vnode = vnodes[i];
                nodes = vnode.getNodes();
                System.out.println("--");
                System.out.println("+----> " + nodes.length +
                    " Nodes appartiennent au VNode " + vnode.getName());
                for (int j = 0; j < nodes.length; j++) {
                    node = nodes[i];
                    nodeInfo = node.getNodeInformation();
                    String mes = "NodeInformation : \n";
                    mes += "+--------------------------------------------------------------------\n";
                    mes += ("+--> getJobID              : " +
                    nodeInfo.getJobID() + "\n");
                    mes += ("+--> getName               : " +
                    nodeInfo.getName() + "\n");
                    mes += ("+--> getProtocol           : " +
                    nodeInfo.getProtocol() + "\n");
                    mes += ("+--> getURL                : " +
                    nodeInfo.getURL() + "\n");
                    mes += "+--------------------------------------------------------------------\n";
                    System.out.println(mes);
                }
            }
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Descripteur de deploimenent n1 : ");
        DeployPAD(URL_PAD1);
    }
}
