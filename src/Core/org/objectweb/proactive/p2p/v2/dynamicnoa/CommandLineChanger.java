package org.objectweb.proactive.p2p.v2.dynamicnoa;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.p2p.v2.service.P2PService;
import org.objectweb.proactive.p2p.v2.service.messages.Message;


public class CommandLineChanger {
    public CommandLineChanger() {
        super();
        // TODO Auto-generated constructor stub
    }

    public void changeNOA(String ref, int noa) {
        P2PService p2p = null;
        Node distNode = null;
        try {
            distNode = NodeFactory.getNode(ref);
            p2p = (P2PService) distNode.getActiveObjects(P2PService.class.getName())[0];
            System.out.println("Dumper ready to change NOA");
            Message m = new ChangeMaxNOAMessage(1, noa);
            p2p.message(m);
            System.out.println("Fini!");
            //p2p.message(new ChangeNOAMessage(1,5));
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java " +
                CommandLineChanger.class.getName() + " <URL> <noa> ");
            System.exit(-1);
        }
        CommandLineChanger c = new CommandLineChanger();
        c.changeNOA(args[0], Integer.parseInt(args[1]));
    }
}
