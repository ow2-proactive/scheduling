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
package org.objectweb.proactive.examples.c3d;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.examples.c3d.geom.Vec;
import org.objectweb.proactive.examples.c3d.gui.NameAndHostDialog;
import org.objectweb.proactive.examples.c3d.gui.UserGUI;
import org.objectweb.proactive.examples.c3d.gui.UserGUIImpl;
import org.objectweb.proactive.examples.c3d.gui.WaitFrame;
import org.objectweb.proactive.examples.c3d.prim.Sphere;
import org.objectweb.proactive.examples.c3d.prim.Surface;
import org.objectweb.proactive.ext.migration.MigrationStrategyManagerImpl;


/**
 * The user logic of the C3D application.
 * This class does not do gui related work, which is cast back to UserGUI.
 * It only handles the logic parts, ie specifies the behavior.
 */
public class C3DUser implements org.objectweb.proactive.RunActive,
    java.io.Serializable {

    /** useful for showing information, if no GUI is available, or for error messages*/
    private static final Logger logger = Logger.getLogger(Loggers.EXAMPLES);

    /** reference to the dispatcher logic, for image generation and message forwarding */
    private C3DDispatcher c3ddispatcher;

    /** AsyncRefto self, needed to add method on own queue */
    private transient C3DUser me;

    /** The chosen name of the user*/
    private String userName;

    /**
     * Number of this user in the set of users registered at the
     * <code>C3DDispatcher</Code>, used to distinguish the action requests of
     * several users
     */
    private int i_user;

    /** List of users. Used for private messaging */
    private Hashtable h_users = new Hashtable();

    /** The GUI which makes a nice front-end to th logic-centric class C3DUser */
    private transient UserGUI gui;

    /** The values stored in the GUI are saved here when the GUI needs to migrate */
    private String[] savedGuiValues;

    /** ProActive requirement : empty no-arg constructor*/
    public C3DUser() {
    }

    /** The initialization and linkage is made in this method, instead of using the constructor */
    public void go() {
        // active Object related fields
        this.me = (C3DUser) org.objectweb.proactive.ProActive.getStubOnThis();

        // ask user through Dialog for userName & host 
        String localHost = getLocalHostString();
        NameAndHostDialog userAndHostNameDialog = new NameAndHostDialog(localHost);
        this.c3ddispatcher = userAndHostNameDialog.getValidatedDispatcher();
        this.userName = userAndHostNameDialog.getValidatedUserName();
        if (this.c3ddispatcher == null) {
            logger.error("Could not find a dispatcher. Closing.");
            System.exit(-1);
        }

        // register user to dispatcher, while asking user to be patient
        WaitFrame wait = new WaitFrame("C3D : please wait!", "Please wait...",
                "Waiting for information from Dispatcher");
        int[] initialValues = c3ddispatcher.registerUser(this.me, this.userName);
        this.i_user = initialValues[0];
        int pictureWidth = initialValues[1];
        int pictureHeight = initialValues[2];
        wait.destroy();

        // Create user Frame  
        this.gui = new UserGUIImpl("C3D user display", this.me, pictureWidth,
                pictureHeight);
        String[] values = new String[] {
                "", "", Integer.toString(pictureWidth),
                Integer.toString(pictureHeight)
            };
        this.gui.setValues(getMachineRelatedValues(), values);
    }

    /** Gets the name of the machine this is running on. */
    private String getLocalHostString() {
        String localhost = "";
        try {
            String port = "";
            String protocol = System.getProperty(
                    "proactive.communication.protocol");
            if (!protocol.equals("jini") && !protocol.equals("ibis")) {
                port = ":" +
                    System.getProperty("proactive." + protocol + ".port");
            }
            localhost = UrlBuilder.getHostNameorIP(InetAddress.getLocalHost()) +
                port;
        } catch (UnknownHostException e) {
            localhost = "unknown!";
        }
        return localhost;
    }

    /** called after migration, to reconstruct the logic. In the runActivity :  myStrategyManager.onArrival("rebuild"); */
    private void rebuild() {
        this.me = (C3DUser) org.objectweb.proactive.ProActive.getStubOnThis();
        this.c3ddispatcher.registerMigratedUser(i_user);

        this.gui = new UserGUIImpl("C3D user display", this.me,
                Integer.parseInt(this.savedGuiValues[3]),
                Integer.parseInt(this.savedGuiValues[4]));
        this.gui.setValues(getMachineRelatedValues(), this.savedGuiValues);
    }

    /** Called just before migration, in the runActivity :  myStrategyManager.onDeparture("saveData");  */
    private void saveData() {
        this.savedGuiValues = this.gui.getValues();
        this.gui.trash();
    }

    /** ProActive request queue handling */
    public void runActivity(org.objectweb.proactive.Body body) {
        // globally says : if migration asked, saveData, migrate, rebuild 
        Service service = new Service(body);
        MigrationStrategyManagerImpl myStrategyManager = new MigrationStrategyManagerImpl((org.objectweb.proactive.core.body.migration.Migratable) body);
        myStrategyManager.onArrival("rebuild");
        myStrategyManager.onDeparture("saveData");
        service.fifoServing();
    }

    /** shows a String as a log */
    public void log(String s_message) {
        if (this.gui == null) {
            logger.info(s_message);
        } else {
            this.gui.log(s_message + "\n");
        }
    }

    /** Shows a String as a message to this user*/
    public void message(String s_message) {
        if (this.gui == null) {
            logger.info(s_message);
        } else {
            this.gui.writeMessage(s_message + "\n");
        }
    }

    /**
     * Informs the user that a new user has joined the party!!
     * @param  nUser The new user's ID
     * @param sName The new user's name
     */
    public void informNewUser(int nUser, String sName) {
        this.gui.addUser(sName);
        this.h_users.put(sName, new Integer(nUser));
    }

    /**
     * Informs the user that another user left
     * @param nUser The id of the old user
     */
    public void informUserLeft(String sName) {
        //  remove the user from the users list in the GUI
        this.gui.removeUser(sName);

        // Remove the user from the hash table
        this.h_users.remove(sName);
    }

    /**
     * The pinging function called by <code>C3DDispatcher</code>
     * to get the avg. pinging time
     */
    public int ping() {
        return 0;
    }

    /**
     * Display an interval of newly calculated pixels
     * @param newpix        The pixels as int array
     * @param interval        The interval
     */
    public void setPixels(Image2D image) {
        this.gui.setPixels(image);
    }

    /**
     * Exit the application
     */
    public void terminate() {
        c3ddispatcher.unregisterConsumer(i_user);
        this.gui.trash();
        try {
            org.objectweb.proactive.ProActive.getBodyOnThis().terminate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Entry point of the program
     */
    public static void main(String[] argv) {
        ProActiveDescriptor proActiveDescriptor = null;

        ProActiveConfiguration.load();
        try {
            if (argv.length == 0) {
                proActiveDescriptor = ProActive.getProactiveDescriptor(); //"file:"+args[0]);
            } else {
                proActiveDescriptor = ProActive.getProactiveDescriptor("file:" +
                        argv[0]);
            }			
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        proActiveDescriptor.activateMappings();
        VirtualNode user = proActiveDescriptor.getVirtualNode("User");

        Node node = null;
        try {
            node = user.getNode();
        } catch (NodeException e1) {
            e1.printStackTrace();
            System.exit(-1);
        }
        Object[] params = {  }; // use the no-arg constructor, and then call the go method
        C3DUser c3duser = null;
        try {
            c3duser = (C3DUser) org.objectweb.proactive.ProActive.newActive(C3DUser.class.getName(),
                    params, node);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
        c3duser.go();
    }

    /** Ask the dispatcher to revert to original scene*/
    public void resetScene() {
        c3ddispatcher.resetScene();
    }

    /** Ask the dispatcher to add a sphere*/
    public void addSphere() {
        double radius = (Math.random()) * 10.0;
        Sphere sphere = new Sphere(Vec.random(20), radius);
        sphere.setSurface(Surface.random());
        c3ddispatcher.addSphere(sphere);
    }

    /** Displays the list of users connected to the dispatcher */
    public void getUserList() {
        StringWrapper list = c3ddispatcher.getUserList();
        gui.log("List of current users:\n" + list.toString());
    }

    public void showUserInfo() {
        gui.showUserInfo();
    }

    /**  Send a mesage to a given other user, or to all */
    public void sendMessage(String message, String recipientName) {
        Integer talkId = (Integer) h_users.get(recipientName);
        if (talkId == null) {
            // BroadCast
            gui.writeMessage("<to all> " + message + '\n');
            c3ddispatcher.userWriteMessageExcept(this.i_user,
                "[from " + this.userName + "] " + message);
        } else {
            // Private message
            gui.writeMessage("<to " + recipientName + "> " + message + '\n');
            c3ddispatcher.userWriteMessage(talkId.intValue(),
                "[Private from " + this.userName + "] " + message);
        }
    }

    /**
     * ask for the scene to be rotated by some angle
     * @param rotationAngle = <x y z> means rotate x radians along the x axis,
     *         then y radians along the y axis, and finally  z radians along the z axis
     */
    public void rotateScene(Vec rotationAngle) {
        c3ddispatcher.rotateScene(i_user, rotationAngle);
    }

    /** returns the name of the machine on which this active object is currently */
    private String[] getMachineRelatedValues() {
        String hostName = "unknown";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return new String[] {
            this.userName, hostName, this.c3ddispatcher.getMachineName(),
            this.c3ddispatcher.getOSString(),
        };
    }
}
