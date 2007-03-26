package org.objectweb.proactive.extra.infrastructuremanager.test.simple;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.extra.infrastructuremanager.IMFactory;
import org.objectweb.proactive.extra.infrastructuremanager.frontend.IMAdmin;


public class SimpleTestIMAdmin {
    public static String URL_PAD_LOCAL = "/home/ellendir/ProActive/infrastructuremanager/descriptors/3VNodes-3Jvms-10Nodes.xml";
    public static String[] vnodesName = new String[] { "Idefix", "Asterix" };

    public static void main(String[] args) {
        System.out.println("# --oOo-- Test  Admin --oOo-- ");

        try {
            IMAdmin admin = IMFactory.getAdmin("//localhost/IMCORE");
            System.out.println("#[SimpleTestIMAdmin] Echo admin : " +
                admin.echo());

            System.out.println("#[SimpleTestIMAdmin] deployAllVirtualNodes : " +
                URL_PAD_LOCAL);
            admin.deployVirtualNodes(new File(URL_PAD_LOCAL),
                NodeFactory.getDefaultNode(), vnodesName);

            System.out.println("Sleep 12s");
            Thread.sleep(12000);

            HashMap<String, ArrayList<VirtualNode>> deployedVNodesByPad = admin.getDeployedVirtualNodeByPad();
            System.out.println("hashNext : " +
                deployedVNodesByPad.keySet().iterator().hasNext());
            String padName = deployedVNodesByPad.keySet().iterator().next();
            System.out.println("padName : " + padName);

            System.out.println("#[SimpleTestIMAdmin] killPAD : vnode Idefix");
            admin.killPAD(padName, "Idefix");

            System.out.println("#[SimpleTestIMAdmin] redeploy vnode : Asterix");
            admin.redeploy(padName, "Asterix");

            /*
            System.out.println("Sleep 12s");
            Thread.sleep(12000);
            */
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("##[TestIMAdmin] END TEST");
    }
}
