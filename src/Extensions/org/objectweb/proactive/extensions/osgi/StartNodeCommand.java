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
package org.objectweb.proactive.extensions.osgi;

import java.io.PrintStream;
import java.rmi.AlreadyBoundException;
import java.util.StringTokenizer;

import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.ungoverned.osgi.service.shell.Command;


public class StartNodeCommand implements Command {
    public String getName() {
        return "startNode";
    }

    public String getUsage() {
        return "startNode";
    }

    public String getShortDescription() {
        return "Starts a ProActive Node";
    }

    public void execute(String arg0, PrintStream arg1, PrintStream arg2) {
        System.out.println("Starting a ProActive Node ...");
        StringTokenizer st = new StringTokenizer(arg0);
        st.nextToken();

        String nodeName = st.nextToken();

        try {
            NodeFactory.createNode(nodeName, false, null, null, null);
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
    }
}
