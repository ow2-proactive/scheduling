package org.objectweb.proactive.ext.closedworldlauncher;

import org.objectweb.proactive.StartNode;


public class NodeLauncher extends AbstractLauncher {
    public void run(int i) {
        System.out.println(wi);
        if (i == 0) {
            StartNode.main(new String[0]);
        } else {
            System.err.println("Not on node 0, not starting anything");
        }
    }

    public static void main(String[] args) {
        NodeLauncher nl = new NodeLauncher();
        new Thread(nl).start();
    }
}
