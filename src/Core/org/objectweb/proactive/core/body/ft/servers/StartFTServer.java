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
package org.objectweb.proactive.core.body.ft.servers;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;

import org.objectweb.proactive.core.body.ft.protocols.cic.servers.CheckpointServerCIC;
import org.objectweb.proactive.core.body.ft.protocols.cic.servers.RecoveryProcessCIC;
import org.objectweb.proactive.core.body.ft.protocols.pmlrb.servers.CheckpointServerPMLRB;
import org.objectweb.proactive.core.body.ft.protocols.pmlrb.servers.RecoveryProcessPMLRB;
import org.objectweb.proactive.core.body.ft.servers.faultdetection.FaultDetector;
import org.objectweb.proactive.core.body.ft.servers.faultdetection.FaultDetectorImpl;
import org.objectweb.proactive.core.body.ft.servers.location.LocationServer;
import org.objectweb.proactive.core.body.ft.servers.location.LocationServerImpl;
import org.objectweb.proactive.core.body.ft.servers.recovery.RecoveryProcess;
import org.objectweb.proactive.core.body.ft.servers.resource.ResourceServer;
import org.objectweb.proactive.core.body.ft.servers.resource.ResourceServerImpl;
import org.objectweb.proactive.core.body.ft.servers.storage.CheckpointServer;
import org.objectweb.proactive.core.rmi.RegistryHelper;
import org.objectweb.proactive.core.util.ProActiveInet;


/**
 * This class is a main that creates and starts a ft.util.FTServer.
 * Usage : ~>startGlobalFTServer [-proto {cic|pml}] [-name name] [-port portnumber] [-fdperiod faultDetectionPeriod (sec)]
 * @author cdelbe
 * @since ProActive 2.2
 */
public class StartFTServer {
    public static void main(String[] args) {
        try {
            int port = 0;
            int fdPeriod = 0;
            String name = "";
            String proto = "cic";
            String p2pServer = null;

            if (args.length == 0) {
                System.out.println(
                    "Usage startGlobalFTServer [-proto cic|pml] [-name name] [-port portnumber] [-fdperiod faultDetectionPeriod (sec)] [-p2p serverUrl]");
            } else {
                for (int i = 0; i < args.length; i++) {
                    if (args[i].equals("-port")) {
                        port = Integer.parseInt(args[i + 1]);
                    } else if (args[i].equals("-fdperiod")) {
                        fdPeriod = Integer.parseInt(args[i + 1]);
                    } else if (args[i].equals("-name")) {
                        name = args[i + 1];
                    } else if (args[i].equals("-proto")) {
                        proto = args[i + 1];
                    } else if (args[i].equals("-p2p")) {
                        p2pServer = args[i + 1];
                    }
                }
            }

            if (port == 0) {
                port = FTServer.DEFAULT_PORT;
            }
            if ("".equals(name)) {
                name = FTServer.DEFAULT_SERVER_NAME;
            }
            if (fdPeriod == 0) {
                fdPeriod = FTServer.DEFAULT_FDETECT_SCAN_PERIOD;
            }

            // rmi registry
            RegistryHelper registryHelper = new RegistryHelper();
            registryHelper.setRegistryPortNumber(port);
            registryHelper.initializeRegistry();

            System.setSecurityManager(new RMISecurityManager());

            // server init
            FTServer server = new FTServer();
            LocationServer ls = new LocationServerImpl(server);
            FaultDetector fd = new FaultDetectorImpl(server, fdPeriod);
            ResourceServer rs;

            // protocol specific
            CheckpointServer cs = null;
            RecoveryProcess rp = null;
            if (proto.equals("cic")) {
                cs = new CheckpointServerCIC(server);
                rp = new RecoveryProcessCIC(server);
            } else if (proto.equals("pml")) {
                cs = new CheckpointServerPMLRB(server);
                rp = new RecoveryProcessPMLRB(server);
            } else {
                System.err.println("ERROR: " + proto +
                    " is not a valid protocol. Aborting.");
                System.exit(1);
            }

            // resource server with p2p or not
            if (p2pServer != null) {
                // resource server is launched on p2p network
                rs = new ResourceServerImpl(server, p2pServer);
            } else {
                rs = new ResourceServerImpl(server);
            }

            // init
            server.init(fd, ls, rp, rs, cs);
            server.startFailureDetector();

            String host = ProActiveInet.getInstance().getInetAddress()
                                       .getHostName();
            Naming.rebind("rmi://" + host + ":" + port + "/" + name, server);
            System.out.println("Fault-tolerance server is bound on rmi://" +
                host + ":" + port + "/" + name);
        } catch (RemoteException e) {
            System.err.println("** ERROR ** Unable to launch FT server : ");
            e.printStackTrace();
        } catch (MalformedURLException e) {
            System.err.println("** ERROR ** Unable to launch FT server : ");
            e.printStackTrace();
        }
    }
}
