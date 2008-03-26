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
package org.objectweb.proactive.examples.cs;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
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
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class Server {
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
    protected String messageOfTheDay;
    protected java.util.ArrayList<Client> clients;

    public Server() {
    }

    public Server(String messageOfTheDay) {
        this.clients = new java.util.ArrayList<Client>();
        this.messageOfTheDay = messageOfTheDay;
    }

    public String getMessageOfTheDay() {
        return messageOfTheDay;
    }

    public void setMessageOfTheDay(String messageOfTheDay) {
        logger.info("Server: new message: " + messageOfTheDay);
        this.messageOfTheDay = messageOfTheDay;
        this.notifyClients();
    }

    public void register(Client c) {
        this.clients.add(c);
    }

    public void unregister(Client c) {
        this.clients.remove(c);
    }

    protected void notifyClients() {
        java.util.Iterator<Client> it = this.clients.iterator();
        Client currentClient;

        while (it.hasNext()) {
            currentClient = it.next();
            try {
                currentClient.messageChanged(this.messageOfTheDay);
            } catch (Exception t) {
                it.remove();
            }
        }
    }

    public static void main(String[] args) {
        ProActiveConfiguration.load();
        try {
            // Creates an active object for the server
            Server theServer = (Server) org.objectweb.proactive.api.PAActiveObject.newActive(Server.class
                    .getName(), new Object[] { "This is the first message" });

            //Server theServer = (Server) org.objectweb.proactive.ProActive.newActive(Server.class.getName(), null, null);
            // Binds the server to a specific URL
            org.objectweb.proactive.api.PAActiveObject.register(theServer, "///theServer");

            System.out.println("Server is ready.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
