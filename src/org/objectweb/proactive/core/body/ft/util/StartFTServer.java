package org.objectweb.proactive.core.body.ft.util;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;

import org.objectweb.proactive.core.rmi.RegistryHelper;


/**
 * This class is a main that creates and starts a ft.util.GlobalFTServer.
 * Usage : ~>startGlobalFTServer [-name name] [-port portnumber] [-fdperiod faultDetectionPeriod (sec)]
 * @author cdelbe
 * @since ProActive 2.2
 */
public class StartFTServer {
    public static void main(String[] args) {
        try {
            int port = 0;
            int fdPeriod = 0;
            String name = "";

            if (args.length == 0) {
                System.out.println(
                    "Usage startGlobalFTServer [-name name] [-port portnumber] [-fdperiod faultDetectionPeriod (sec)]");
            } else {
                for (int i = 0; i < args.length; i++) {
                    if (args[i].equals("-port")) {
                        port = Integer.parseInt(args[i + 1]);
                    }
                    if (args[i].equals("-fdperiod")) {
                        fdPeriod = Integer.parseInt(args[i + 1]);
                    }
                    if (args[i].equals("-name")) {
                        name = args[i + 1];
                    }
                }
            }

            if (port == 0) {
                port = GlobalFTServer.DEFAULT_PORT;
            }
            if ("".equals(name)) {
                name = GlobalFTServer.DEFAULT_SERVER_NAME;
            }

            // rmi registry
            RegistryHelper registryHelper = new RegistryHelper();
            registryHelper.setRegistryPortNumber(port);
            registryHelper.initializeRegistry();

            System.setSecurityManager(new RMISecurityManager());
            GlobalFTServer cs = new GlobalFTServer(fdPeriod);
            String host = InetAddress.getLocalHost().getHostName();
            Naming.rebind("rmi://" + host + ":" + port + "/" + name, cs);
            System.out.println("CheckpointServer is bound on rmi://" + host +
                ":" + port + "/" + name);
        } catch (RemoteException e) {
            System.err.println("** ERROR ** Unable to launch FT server : ");
            e.printStackTrace();
        } catch (MalformedURLException e) {
            System.err.println("** ERROR ** Unable to launch FT server : ");
            e.printStackTrace();
        } catch (UnknownHostException e) {
            System.err.println("** ERROR ** Unable to launch FT server : ");
            e.printStackTrace();
        }
    }
}
