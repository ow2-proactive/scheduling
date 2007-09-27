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
package org.objectweb.proactive.examples.chat;

import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProGroup;
import org.objectweb.proactive.api.ProMigration;
import org.objectweb.proactive.core.body.migration.Migratable;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.migration.MigrationStrategyManagerImpl;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class represents the Active Object who manages the communication between all the users.
 *
 * @author Laurent Baduel
 */
public class Chat implements java.io.Serializable, RunActive {
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    /** The name of the user */
    private String name = "";

    /** The other chaters */
    private Chat diffusionGroup = null;

    /** An history of all recieved messages */
    private Vector messageLogger;

    /** The graphique interface */
    private transient ChatGUI frame;

    /** The migration strategy manager */
    private MigrationStrategyManagerImpl migrationStrategy = null;

    /** The list of users */
    private String listOfName = "";

    /**
     * Constructor : The default constructor requiered to build Active Objects
     */
    public Chat() {
    }

    /**
     * Constructor : Specify the name of the user to build the Object
     * @param identity - the name of the user
     */
    public Chat(String identity) {
        this.name = identity;
        this.messageLogger = new Vector();
    }

    /**
     * Returns the name of the user
     * @return the name of the user
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the group of diffusion of the Object
     * @return a typed group representing the other users of the chat
     */
    public Chat getDiffusionGroup() {
        return this.diffusionGroup;
    }

    /**
     * Launchs a client without connecting to any other client
     */
    public void startAlone() {
        try {
            this.diffusionGroup = (Chat) ProGroup.newGroup(Chat.class.getName());
        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        this.addIntoDiffusionGroup((Chat) ProActiveObject.getStubOnThis(), this.name);
        this.writeMessage(new Message(" *** " + this.name +
                " has joined the place"));
    }

    /**
     * Launchs a client and connects it to the users known by a speified user (neighbour).
     *
     * A fully peer-to-peer system established with ONLY 4 LINES OF CODES !!!
     * 1 - lookup for the neighbour
     * 2 - copy the diffusion group of the neighbour
     * 3 - register itself to the other users by the diffusion group
     * 4 - add itself to it own diffusion group
     *
     * @param hostname - the name of the host of the neighbour
     * @param userName - the name of user of the neighbour
     */
    public void connect(String hostName, String userName) {
        Chat neighbour = null;
        try {
            neighbour = (Chat) ProActiveObject.lookupActive(Chat.class.getName(),
                    "//" + hostName + "/" + userName); // 1
            this.diffusionGroup = neighbour.getDiffusionGroup(); // 2
            this.writeUsersInTheList();
            this.diffusionGroup.addIntoDiffusionGroup((Chat) ProActiveObject.getStubOnThis(),
                this.name); // 3
            ProGroup.getGroup(this.diffusionGroup)
                          .add((Chat) ProActiveObject.getStubOnThis()); //4
            this.frame.list.append(this.name + "\n");
            this.writeMessage(new Message(" *** " + this.name +
                    " has joined the place"));
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            this.writePrivateMessage(new Message(
                    " *** WARNING : Unable to contact " + userName + "@" +
                    hostName + " !"));
            this.writePrivateMessage(new Message(
                    " *** WARNING : Starting alone !"));
            this.startAlone();
        }
    }

    /**
     * Adds a specified user to the group of diffusion
     * @param c - the user to add
     * @param name - the name of the user to add
     */
    public void addIntoDiffusionGroup(Chat c, String name) {
        ProGroup.getGroup(this.diffusionGroup).add(c);
        this.frame.list.append(name + "\n");
    }

    /**
     * Disconnects the user from all the others (removes it from their diffusion group)
     */
    public void disconnect() {
        this.writeMessage(new Message(" *** " + this.name + " has left"));
        this.diffusionGroup.removeUserFromTheList(this.name);
        this.diffusionGroup.removeFromDiffusionGroup((Chat) ProActiveObject.getStubOnThis());
        ProGroup.getGroup(this.diffusionGroup)
                      .remove((Chat) ProActiveObject.getStubOnThis());
    }

    /**
     * Removes a specified user from the group of diffusion
     * @param c - the user to remove
     */
    public void removeFromDiffusionGroup(Chat c) {
        ProGroup.getGroup(this.diffusionGroup).remove(c);
    }

    /**
     * Specifies the activity of the user
     */
    public void runActivity(Body body) {
        Service service = new Service(body);
        this.register();
        this.rebuildFrame();
        this.initializeMigrationStrategy();
        this.replayMessages();
        while (body.isActive()) {
            service.blockingServeOldest();
        }
    }

    /**
     * Builds the migration strategy manager
     */
    public void initializeMigrationStrategy() {
        if (this.migrationStrategy == null) {
            this.migrationStrategy = new MigrationStrategyManagerImpl((Migratable) ProActiveObject.getBodyOnThis());
            this.migrationStrategy.onDeparture("onDeparture");
        }
    }

    /**
     * To be executed on the departure (when the object begins to migrate to an other location)
     */
    public void onDeparture() {
        this.disposeFrame();
        this.unregister();
    }

    /**
     * Registers the object on it current host
     */
    public void register() {
        try {
            ProActiveObject.register(ProActiveObject.getStubOnThis(),
                "//localhost/" + this.name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unregisters the object on it host
     */
    public void unregister() {
        try {
            ProActiveObject.unregister("//localhost/" + this.name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Destroys the graphic interface
     */
    public void disposeFrame() {
        this.listOfName = this.frame.list.getText();
        if (this.frame != null) {
            this.frame.dispose();
            this.frame = null;
        }
    }

    /**
     * Rebuild the graphic interface
     */
    public void rebuildFrame() {
        this.frame = new ChatGUI((Chat) ProActiveObject.getStubOnThis(), name);
        this.frame.list.setText(this.listOfName);
    }

    /**
     * Replay the messages received by the user
     */
    public void replayMessages() {
        for (int i = 0; i < this.messageLogger.size(); i++) {
            this.frame.text.append(((Message) this.messageLogger.get(i)).toString());
        }
    }

    /**
     * Migrates the object to the specified loaction
     * @param nodeURL - the name of the node to migrate
     */
    public void migrateTo(String nodeURL) {
        this.writePrivateMessage(new Message(" *** I move to " + nodeURL));
        try {
            ProMigration.migrateTo(nodeURL);
        } catch (MigrationException e) {
            this.writePrivateMessage(new Message(
                    " *** WARNING : Unable to move to " + nodeURL + " !"));
        }
    }

    /**
     * Writes a message to the user
     * @param m - the message to write
     */
    public void writePrivateMessage(Message m) {
        this.messageLogger.add(m);
        this.frame.text.append(m.toString());
    }

    /**
     * Writes a message to all the members of the diffusion group
     * @param m - the message to write
     */
    public void writeMessage(Message m) {
        this.diffusionGroup.writePrivateMessage(m);
    }

    /**
     * Writes the name of all connected users in the list
     */
    public void writeUsersInTheList() {
        java.util.Iterator it = ProGroup.getGroup(this.diffusionGroup)
                                              .iterator();
        while (it.hasNext())
            this.frame.list.append(((Chat) it.next()).getName() + "\n");
    }

    /**
     * Removes the specified name of user from the list
     * @param userName - the name to remove
     */
    public void removeUserFromTheList(String userName) {
        this.frame.list.setText(this.frame.list.getText()
                                               .replaceAll(userName + "\n", ""));
    }

    public static void main(String[] args) {
        String userName;
        String neighbourHost = null;
        String neighbourName = null;
        ProActiveConfiguration.load();

        if ((args.length != 1) && (args.length != 3)) {
            logger.info(
                "usage : chat.[sh|bat] UserName [ServerHost ServerName]");
            System.exit(0);
        }

        userName = args[0];
        if (args.length == 3) {
            neighbourHost = args[1];
            neighbourName = args[2];
        }

        Chat chat = null;
        try {
            Object[] param = new Object[1];
            param[0] = new String(userName);
            chat = (Chat) ProActiveObject.newActive(Chat.class.getName(), param,
                    (Node) null);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }

        if ((neighbourHost == null) || (neighbourName == null)) {
            chat.startAlone();
        } else {
            chat.connect(neighbourHost, neighbourName);
        }
    }
}
