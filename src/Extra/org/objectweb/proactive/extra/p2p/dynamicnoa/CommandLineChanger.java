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
package org.objectweb.proactive.extra.p2p.dynamicnoa;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.extra.p2p.service.P2PService;
import org.objectweb.proactive.extra.p2p.service.messages.Message;


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
            System.err.println("Usage: java " + CommandLineChanger.class.getName() + " <URL> <noa> ");
            System.exit(-1);
        }
        CommandLineChanger c = new CommandLineChanger();
        c.changeNOA(args[0], Integer.parseInt(args[1]));
    }
}
