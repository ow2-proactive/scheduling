package test.getnode;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;


public class Test {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java " + Test.class.getName() + 
                               " node");
            System.exit(-1);
        }
        try {
            System.out.println("Looking for node");

            Node node = NodeFactory.getNode(args[0]);
            System.out.println("Found node " + node);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}