/*
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
package org.objectweb.proactive.p2p.core.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;


/**
 * To start a P2P Service on the current host.
 *
 * @author Alexandre di Costanzo
 *
 */
public class StartP2PService {
    private boolean noRegister = true;
    static Logger logger = Logger.getLogger(StartP2PService.class.getName());
    protected String acquisitionMethod;
    protected String portNumber;
    protected Vector serverList;
    protected P2PServiceImpl p2pService;

    public StartP2PService(String[] args) {
        if (args.length == 3) {
            this.noRegister = false;
            this.serverList = parser(args[2]);
        }
        this.acquisitionMethod = args[0];
        if (!this.acquisitionMethod.endsWith(";")) {
            this.acquisitionMethod += ":";
        }
        this.portNumber = args[1];
    }
    
    public StartP2PService(String protocol, String port, Vector serverList){
        this.noRegister = false;
        this.serverList = serverList;
        this.acquisitionMethod = protocol;
        this.portNumber = port;
    }
    /**
     * Usage: java org.objectweb.proactive.p2p.daemon.P2PProActiveRuntime
     * acquisitionMethod portNumber [Servers List File]
     *
     * @param args
     *            acquisitionMethod portNumber Servers List File
     */
    public static void main(String[] args) {
        if ((args.length != 3) && (args.length != 2)) {
            System.err.println(
                "Usage: java org.objectweb.proactive.p2p.daemon.P2PProActiveRuntime acquisitionMethod portNumber [Servers List File]");
            logger.error(
                "Usage: java org.objectweb.proactive.p2p.daemon.P2PProActiveRuntime acquisitionMethod portNumber [Servers List File]");
            System.exit(69);
        }

        ProActiveConfiguration.load();

        try {
            
            logger.info("**** Starting jvm on " +
                java.net.InetAddress.getLocalHost().getHostName());
            if (logger.isDebugEnabled()) {
                logger.debug("**** Starting jvm with classpath " +
                    System.getProperty("java.class.path"));
                logger.debug("****              with bootclasspath " +
                    System.getProperty("sun.boot.class.path"));
            }
        } catch (java.net.UnknownHostException e) {
            e.printStackTrace();
        }
        StartP2PService toto = new StartP2PService(args);
        toto.start();
        while (true) {
            ;
        }

        // Tests:
        // Test getProActiveJVMS
        //        while (true) {
        //            try {
        //                Thread.sleep(15000);
        //                System.out.println("______________________" + new Date());
        //                System.out.println(toto.p2pRuntime.getProActiveJVMs().length);
        //                for (int i = 0; i < toto.p2pRuntime.getProActiveJVMs().length; i++)
        //                    System.out.println(toto.p2pRuntime.getProActiveJVMs()[i]
        //                            .getVMInformation().getName());
        //            } catch (InterruptedException e) {
        //                e.printStackTrace();
        //            } catch (ProActiveException e) {
        //                e.printStackTrace();
        //            }
        //        }
        // Test getProActiveJVMS(n)
        //                try {
        //                    System.out.println(">>>>>>>>>>>>>>>"
        //                            + toto.p2pRuntime.getProActiveJVMs(1).length + "/1");
        //                    System.out.println(">>>>>>>>>>>>>>>"
        //                            + toto.p2pRuntime.getProActiveJVMs(2).length + "/2");
        //                    System.out.println(">>>>>>>>>>>>>>>"
        //                            + toto.p2pRuntime.getProActiveJVMs(3).length + "/3");
        //                    System.out.println(">>>>>>>>>>>>>>>"
        //                            + toto.p2pRuntime.getProActiveJVMs(4).length + "/4");
        //                    System.out.println(">>>>>>>>>>>>>>>"
        //                            + toto.p2pRuntime.getProActiveJVMs(5).length + "/5");
        //                    System.out.println(">>>>>>>>>>>>>>>"
        //                            + toto.p2pRuntime.getProActiveJVMs(6).length + "/6");
        //                    System.out.println(">>>>>>>>>>>>>>>"
        //                            + toto.p2pRuntime.getProActiveJVMs(7).length + "/7");
        //                } catch (Exception e1) {
        //                    e1.printStackTrace();
        //                }
    }

    /**
     * <p>
     * Start the P2P service.
     * </p>
     * <p>
     * <b>Warning: </b> it's not a thread.
     * </p>
     */
    public void start() {
        // Create a ProActiveRuntime in the localhost
        try {
            // ProActiveRuntime creation
            System.setProperty("proactive." +
                this.acquisitionMethod.replace(':', ' ').trim() + ".port",
                this.portNumber);
            
            ProActiveRuntime paRuntime = RuntimeFactory.getProtocolSpecificRuntime(this.acquisitionMethod);

            // Node Creation
            String url = paRuntime.createLocalNode(P2PServiceImpl.P2P_NODE_NAME,
                    false, null, paRuntime.getVMInformation().getName(),
                    paRuntime.getJobID());

            // P2PService Active Object Creation
            Object[] params = new Object[3];
            params[0] = this.acquisitionMethod;
            params[1] = this.portNumber;
            params[2] = paRuntime;
            this.p2pService = (P2PServiceImpl) ProActive.newActive(P2PServiceImpl.class.getName(),
                    params, url);

            if (logger.isInfoEnabled())
                logger.info("/////////////////////////////  STARTING P2P SERVICE //////////////////////////////");
            
            // Record the ProActiveRuntime in other from Servers List File
            if (!this.noRegister) {
                ((P2PServiceImpl) this.p2pService).registerP2PServices(this.serverList);
            }
        } catch (NodeException e) {
            logger.error(e);
        } catch (ActiveObjectCreationException e) {
            logger.error(e);
        } catch (ProActiveException e) {
            logger.error(e);
        }
    }

    /**
     * Parse a text file with one host by line.
     *
     * @param fileURL
     *            URL of the file.
     * @return a vector with all hosts.
     */
    private static Vector parser(String fileURL) {
        Vector list = new Vector();
        try {
            FileReader serverList = new FileReader(fileURL);
            BufferedReader in = new BufferedReader(serverList);
            String line;
            while ((line = in.readLine()) != null) {
                list.add(line);
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * @return Returns the P2P Service.
     */
    public P2PServiceImpl getP2PService() {
        return p2pService;
    }
}
