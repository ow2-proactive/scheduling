package org.ow2.proactive.resourcemanager.examples.windowsagent;

//@snippet-start ProActiveWindowsAgent
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;


public class Test {

    public static void main(String[] args) {

        try {

            // Note that the port is 1100 and not 1099 like those used by the
            // resource manager. The initial port is incremented to ensure that
            // each runtime uses a unique port.
            // Thus, this port corresponds to the port of the first runtime.
            // If a second runtime has been launched, it would use the port 1101,
            // and so on and so forth...

            final Node n = NodeFactory.getNode("rmi://192.168.1.62:1100/toto");

            System.out.println("Nb of active objects on the remote node: " + n.getNumberOfActiveObjects() +
                " local runtime hashcode " + Runtime.getRuntime().hashCode());

            final Test stubOnTest = (Test) PAActiveObject.newActive(Test.class.getName(), null, n);

            final String receivedMessage = stubOnTest.getMessage();

            System.out.println("Nb of active objects on the remote node: " + n.getNumberOfActiveObjects() +
                " received message: " + receivedMessage);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public Test() {
    }

    public String getMessage() {
        return "A message from " + Runtime.getRuntime().hashCode();
    }

}
//@snippet-end ProActiveWindowsAgent
