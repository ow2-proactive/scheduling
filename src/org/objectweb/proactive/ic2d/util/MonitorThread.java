/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.util;

import java.rmi.RemoteException;
import java.util.Iterator;

import javax.swing.DefaultListModel;

import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.ic2d.data.WorldObject;
import org.objectweb.proactive.ic2d.gui.jobmonitor.NodeExploration;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.DataAssociation;
import org.objectweb.proactive.ic2d.gui.jobmonitor.data.MonitoredHost;

/**
 * @author ProActiveTeam
 * @version 1.0, 26 janv. 2005
 * @since ProActive 2.2
 * 
 */
public class MonitorThread extends Thread {
    private String protocol;
    private String host;
    private String depth;
    private WorldObject worldObject;
    private IC2DMessageLogger logger;
    private DataAssociation asso;

    public MonitorThread(String protocol, String host, String depth,
        WorldObject worldObject, IC2DMessageLogger logger) {
        this.asso = new DataAssociation();
        this.protocol = protocol;
        this.host = host;
        this.depth = depth;
        this.logger = logger;
        this.worldObject = worldObject;
    }

    public void run() {
        String hostname = null;
        int port = 0;
        DefaultListModel skippedObjects = new DefaultListModel();

        NodeExploration explorator = new NodeExploration(asso,
                skippedObjects, logger, protocol);
        explorator.setMaxDepth(depth);
        explorator.startExploration();
        if (host != null) {
            hostname = UrlBuilder.removePortFromHost(host);
            port = UrlBuilder.getPortFromUrl(host);
        }
        explorator.exploreHost(hostname, port);
        explorator.endExploration();
        Iterator it = asso.getHosts().iterator(); // iterator de MonitoredHost
        while (it.hasNext()) {
            MonitoredHost monitoredhost = (MonitoredHost) it.next();

            //                String tmphost = monitoredhost.getFullName();
            try {
                worldObject.addHostObject(monitoredhost, asso);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
