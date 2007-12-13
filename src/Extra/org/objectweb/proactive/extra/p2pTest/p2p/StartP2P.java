/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extra.p2pTest.p2p;

import java.io.Serializable;
import java.util.Random;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extra.p2pTest.utils.ShutdownHook;
import org.objectweb.proactive.p2p.v2.service.StartP2PService;


public class StartP2P {
    private static final String USAGE = "DescriptorFile";

    public static void main(String[] args) {
        if (args.length > 0) {
            StartP2P startP2Ptest = new StartP2P();
            startP2Ptest.startP2PNetworkWithPAD(args[0]);
        } else {
            System.out.println(StartP2P.class.getName() + " Usage: " + USAGE);
        }
    }

    public void startP2PNetworkWithPAD(String proActiveDescriptor) {
        ProActiveDescriptor pad = null;
        try {
            pad = PADeployment.getProactiveDescriptor(proActiveDescriptor);
            Runtime.getRuntime().addShutdownHook(new ShutdownHook(pad));
        } catch (ProActiveException e) {
            e.printStackTrace();
        }

        VirtualNode vn_local = pad.getVirtualNode("VN_LOCAL");
        vn_local.activate();
        VirtualNode vn0 = pad.getVirtualNode("VN0");
        vn0.activate();
        VirtualNode vn1 = pad.getVirtualNode("VN1");
        vn1.activate();
        VirtualNode vn2 = pad.getVirtualNode("VN2");
        vn2.activate();

        String arg0 = "-ttu 1000 -port 1099 -noa 5";
        String[] entryPoint = new String[3];
        entryPoint[0] = " //fiacre.inria.fr:1099";
        entryPoint[1] = " //galpage.inria.fr:1099";
        entryPoint[2] = " //macyavel.inria.fr:1099";

        String s = " -s" + entryPoint[0];

        try {
            System.out.println("���������������������������Starting Entry Node���������������������������");
            Node entryNode = vn0.getNode();

            P2PLauncher entryp2pLauncher = (P2PLauncher) PAActiveObject.newActive(
                    P2PLauncher.class.getName(), new Object[] { arg0 }, entryNode);
            entryp2pLauncher.start();
            Thread.sleep(10000);

            System.out
                    .println("���������������������������Starting Others Entry Nodes���������������������������");
            for (Node othersEntryNodes : vn1.getNodes()) {
                P2PLauncher p2pLauncher = (P2PLauncher) PAActiveObject.newActive(P2PLauncher.class.getName(),
                        new Object[] { arg0 + s }, othersEntryNodes);
                p2pLauncher.start();
                Thread.sleep(10000);
            }

            Random rnd = new Random();
            for (Node node : vn2.getNodes()) {
                int i = rnd.nextInt(entryPoint.length);
                s = " -s" + entryPoint[i] + entryPoint[(i + 1) % entryPoint.length] +
                    entryPoint[(i + 2) % entryPoint.length];
                System.out
                        .println("���������������������������Starting Others Nodes���������������������������");
                P2PLauncher p2pLauncher = (P2PLauncher) PAActiveObject.newActive(P2PLauncher.class.getName(),
                        new Object[] { arg0 + s }, node);
                p2pLauncher.start();
                Thread.sleep(10000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @author cvergoni
     *
     * Inner static class :
     * permit start ProActive P2P via the "main" of StartP2PService
     */
    public static class P2PLauncher implements Serializable {
        private String[] args = null;

        //For ProActive
        public P2PLauncher() {
        }

        public P2PLauncher(String stringArgs) {
            args = string2Tab(stringArgs);
        }

        public void start() {
            StartP2PService.main(args);
        }

        private String[] string2Tab(String stringArgs) {
            String[] tab = new String[100];
            int i = 0;
            stringArgs += " ";
            int pos = 0;
            int pos2;
            while ((pos2 = stringArgs.indexOf(" ", pos)) != -1) {
                tab[i++] = stringArgs.substring(pos, pos2);
                pos = pos2 + 1;
            }
            String[] result = new String[i];
            System.arraycopy(tab, 0, result, 0, i);
            return result;
        }

        @Override
        public String toString() {
            String res = "";
            for (String string : args) {
                res += (string + " ");
            }
            return res;
        }
    } //inner static P2PLauncher class
}
