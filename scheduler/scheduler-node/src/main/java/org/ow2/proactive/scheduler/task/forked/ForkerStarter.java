/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.task.forked;

import java.io.IOException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.ow2.proactive.rm.util.process.EnvironmentCookieBasedChildProcessKiller;


/**
 * ForkerStarter...
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 3.0
 */
public class ForkerStarter {

    //args[0]=callbackURL,args[1]=nodeName
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println(ForkerStarter.class.getSimpleName() +
                " does not have the appropriate number of arguments.");
            System.err.println("Arguments must be : callbackURL nodeName");
            System.exit(2);
        }
        EnvironmentCookieBasedChildProcessKiller.registerKillChildProcessesOnShutdown();
        ForkerStarter fs = new ForkerStarter();
        Node n = fs.createLocalNode(args[1]);
        fs.callBack(args[0], n);
    }

    /**
     * callback method to starter runtime at given URL
     * Node reference will be sent through the callback method
     * 
     * @param url the url of the runtime to join
     * @param n the node to return
     * @throws IOException 
     * @throws ActiveObjectCreationException 
     */
    private void callBack(String url, Node n) {
        try {
            ForkerStarterCallback oa = PAActiveObject.lookupActive(ForkerStarterCallback.class, url);
            oa.callback(n);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
            System.exit(3);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(4);
        }
    }

    /**
     * Creates the node with the name given as parameter and returns it.
     * 
     * @param nodeName The expected name of the node
     * @return the newly created node.
     */
    protected Node createLocalNode(String nodeName) {
        Node localNode = null;
        try {
            localNode = NodeFactory.createLocalNode(nodeName, false, null, null);
            if (localNode == null) {
                System.err.println("Cannot create local node !");
                System.exit(5);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(6);
        }
        return localNode;
    }

}
