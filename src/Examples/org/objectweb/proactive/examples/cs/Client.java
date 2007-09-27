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
package org.objectweb.proactive.examples.cs;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * <p>
 * A client/server example made using ProActive. In one window you
 * can launch the server using the script cs_server without
 * parameters. Then, in different windows, you can launch several
 * clients using the script cs_client and passing the name of the
 * client as a argument. Each client declares itself to the server
 * and sends messages. The server broadcasts the messages to all
 * referenced clients.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class Client {
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
    protected String myName;
    protected String serverHostName;
    protected Server theServer;
    private java.text.DateFormat dateFormat = new java.text.SimpleDateFormat(
            "dd/MM/yyyy HH:mm:ss");

    public Client() {
    }

    public Client(String clientName, String serverHostName)
        throws Exception {
        this.myName = clientName;
        this.serverHostName = serverHostName;
        // Looks up for the server
        String urlAsString = "//" + serverHostName + "/theServer";
        logger.info("Client " + clientName + " is looking up server at " +
            urlAsString);
        this.theServer = (Server) org.objectweb.proactive.api.ProActiveObject.lookupActive(Server.class.getName(),
                urlAsString);
        logger.info("Client " + this.myName + " successfully found the server");
    }

    public void init() {
        // Registers myself with the server
        Client myself = (Client) org.objectweb.proactive.api.ProActiveObject.getStubOnThis();
        if (myself != null) {
            theServer.register(myself);
        } else {
            logger.info("Cannot get a stub on myself");
        }
    }

    public void doStuff() {
        // Gets the message from the server and prints it out
        //System.out.println (this.myName+": message is "+this.theServer.getMessageOfTheDay());
        // Sets a new message on the server
        theServer.setMessageOfTheDay(this.myName + " is connected (" +
            dateFormat.format(new java.util.Date()) + ")");
        //System.out.println (this.myName+": new message sent to the server");
    }

    public void messageChanged(String newMessage) {
        System.out.println(this.myName + ": message changed: " + newMessage);
    }

    public static void main(String[] args) {
        String clientName;
        String serverHostName;
        if (args.length < 1) {
            System.out.println(
                "Correct syntax is: client <client name> [server host name]");
            return;
        } else if (args.length == 1) {
            clientName = args[0];
            serverHostName = "";
        } else {
            clientName = args[0];
            serverHostName = args[1];
        }

        try {
            // Creates an active object for the client
            Client theClient = (Client) org.objectweb.proactive.api.ProActiveObject.newActive(Client.class.getName(),
                    new Object[] { clientName, serverHostName });
            theClient.init();
            Thread t = new Thread(new RunClient(theClient));
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static class RunClient implements Runnable {
        private Client client;
        private boolean shouldRun = true;

        public RunClient(Client client) {
            this.client = client;
        }

        public void run() {
            while (shouldRun) {
                try {
                    client.doStuff();
                    long l = 500 + (long) (Math.random() * 5000);
                    try {
                        Thread.sleep(l);
                    } catch (InterruptedException e) {
                    }
                } catch (Exception e) {
                    shouldRun = false;
                }
            }
        }
    }
}
