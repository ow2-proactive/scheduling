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
package org.objectweb.proactive.examples.c3d;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.migration.MigrationStrategyManagerImpl;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.StringMutableWrapper;
import org.objectweb.proactive.examples.c3d.geom.Vec;
import org.objectweb.proactive.examples.c3d.gui.NameAndHostDialog;
import org.objectweb.proactive.examples.c3d.gui.UserGUI;
import org.objectweb.proactive.examples.c3d.gui.WaitFrame;
import org.objectweb.proactive.examples.c3d.prim.Sphere;
import org.objectweb.proactive.examples.c3d.prim.Surface;


/**
 * The user logic of the C3D application.
 * This class does not do gui related work, which is cast back to UserGUI.
 * It only handles the logic parts, ie specifies the behavior.
 */
public class C3DUser implements InitActive, java.io.Serializable, User,
    UserLogic {

    /** useful for showing information, if no GUI is available, or for error messages*/
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    /** reference to the dispatcher logic, for image generation and message forwarding */
    protected Dispatcher c3ddispatcher;
    protected VirtualNode dispatcherNode;

    /** AsyncRefto self, needed to add method on own queue */
    protected transient User me;

    /** The chosen name of the user*/
    private String userName;

    /**
     * Number of this user in the set of users registered at the
     * <code>c3ddispatcher</Code>, used to distinguish the action requests of
     * several users
     */
    private int i_user;

    /** List of users. Used for private messaging */
    private Hashtable h_users = new Hashtable();

    /** The GUI which makes a nice front-end to th logic-centric class C3DUser */
    private transient UserGUI gui;

    /** The values stored in the GUI are saved here when the GUI needs to migrate */
    private String[] savedGuiValues;
    private String dispMachineAndOS;

    /** ProActive requirement : empty no-arg constructor*/
    public C3DUser() {
    }

    public C3DUser(Dispatcher disp, String name) {
        this.c3ddispatcher = disp;
        this.userName = name;
    }

    /** Returns the C3DUser constructor arguments, after a dialog has popped up */
    private static Object[] getDispatcherAndUserName() {
        // ask user through Dialog for userName & host 
        NameAndHostDialog userAndHostNameDialog = new NameAndHostDialog();
        Dispatcher disp = userAndHostNameDialog.getValidatedDispatcher();

        if (disp == null) {
            logger.error("Could not find a dispatcher. Closing.");
            System.exit(-1);
        }

        return new Object[] { disp, userAndHostNameDialog.getValidatedUserName() };
    }

    /** called after migration, to reconstruct the logic.
     * In the initActivity :  myStrategyManager.onArrival("rebuild"); */

    // shouldn't be called from outside the class. 
    public void rebuild() {
        this.me = (User) org.objectweb.proactive.api.PAActiveObject.getStubOnThis();
        this.c3ddispatcher.registerMigratedUser(i_user);
        createGUI();
    }

    /** Called just before migration, as specified in the initActivity :
     * myStrategyManager.onDeparture("leaveHost");  */

    // shouldn't be called from outside the class. 
    public void leaveHost() {
        this.savedGuiValues = this.gui.getValues();
        this.gui.trash();
    }

    /**
     * Tells what are the operations to perform before starting the activity of the AO.
     * Here, we state that if migration asked, procedure  is : saveData, migrate, rebuild.
     * We also set some other variables.
     */
    public void initActivity(Body body) {
        if (body != null) { // FIXME: this is a component bug: sometimes body is null!    
            MigrationStrategyManagerImpl myStrategyManager = new MigrationStrategyManagerImpl((org.objectweb.proactive.core.body.migration.Migratable) body);
            myStrategyManager.onArrival("rebuild");
            myStrategyManager.onDeparture("leaveHost");
        }

        // register user to dispatcher, while asking user to be patient
        WaitFrame wait = new WaitFrame("C3D : please wait!", "Please wait...",
                "Waiting for information from Dispatcher");
        // get the stub, which is a long operation, while the wait window is displayed 
        this.me = (User) org.objectweb.proactive.api.PAActiveObject.getStubOnThis();

        int user_id = this.c3ddispatcher.registerUser(this.me, this.userName);
        this.i_user = user_id;

        wait.destroy();

        this.savedGuiValues = null;
        // Create user Frame  
        createGUI();
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
        this.c3ddispatcher.unregisterConsumer(i_user);
        this.gui.trash();
        PAActiveObject.terminateActiveObject(true);
    }

    /**
     * Entry point of the program
     */
    public static void main(String[] argv) {
        ProActiveDescriptor proActiveDescriptor = null;

        ProActiveConfiguration.load();

        try {
            proActiveDescriptor = PADeployment.getProactiveDescriptor("file:" +
                    argv[0]);
        } catch (Exception e) {
            logger.error("Trouble loading descriptor file");
            e.printStackTrace();
            System.exit(-1);
        }

        proActiveDescriptor.activateMappings();
        VirtualNode user = proActiveDescriptor.getVirtualNode("User");
        Object[] params = C3DUser.getDispatcherAndUserName();

        try {
            //C3DUser c3duser = (C3DUser) 
            org.objectweb.proactive.api.PAActiveObject.newActive(C3DUser.class.getName(),
                params, user.getNode());
        } catch (Exception e) {
            logger.error("Problemn with C3DUser Active Object creation:");
            e.printStackTrace();
        }
    }

    /** Ask the dispatcher to revert to original scene */
    public void resetScene() {
        this.c3ddispatcher.resetScene();
    }

    /** Ask the dispatcher to add a sphere*/
    public void addSphere() {
        double radius = (Math.random()) * 10.0;
        Sphere sphere = new Sphere(Vec.random(20), radius);
        sphere.setSurface(Surface.random());
        this.c3ddispatcher.addSphere(sphere);
    }

    /** Displays the list of users connected to the dispatcher */
    public void getUserList() {
        StringMutableWrapper list = this.c3ddispatcher.getUserList();
        this.gui.log("List of current users:\n" + list.toString());
    }

    /**  Send a mesage to a given other user, or to all */
    public void sendMessage(String message, String recipientName) {
        Integer talkId = (Integer) h_users.get(recipientName);

        if (talkId == null) {
            // BroadCast
            gui.writeMessage("<to all> " + message + '\n');
            this.c3ddispatcher.userWriteMessageExcept(this.i_user,
                "[from " + this.userName + "] " + message);
        } else {
            // Private message
            gui.writeMessage("<to " + recipientName + "> " + message + '\n');
            this.c3ddispatcher.userWriteMessage(talkId.intValue(),
                "[Private from " + this.userName + "] " + message);
        }
    }

    /**
     * ask for the scene to be rotated by some angle
     * @param rotationAngle = <x y z> means rotate x radians along the x axis,
     *         then y radians along the y axis, and finally  z radians along the z axis
     */
    public void rotateScene(Vec rotationAngle) {
        this.c3ddispatcher.rotateScene(i_user, rotationAngle);
    }

    public void setUserName(String newName) {
        this.userName = newName;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setDispatcherMachine(String machine, String os) {
        this.dispMachineAndOS = "    " + machine + "\n    " + os;

        String guiInfoText = this.gui.getUserInfo();
        int index = guiInfoText.lastIndexOf('\n');

        if (index != -1) {
            index = guiInfoText.lastIndexOf('\n', index);
        }

        if (index != -1) {
            guiInfoText = guiInfoText.substring(0, index + 1) +
                this.dispMachineAndOS;
        } else {
            guiInfoText += this.dispMachineAndOS;
        }

        this.gui.setUserInfo(guiInfoText);
    }

    /** returns the name of the machine on which this active object is currently */
    private String getMachineRelatedValues() {
        String hostName = "unknown";

        try {
            hostName = URIBuilder.getLocalAddress().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return "User\n   " + this.userName + " (" + hostName + ")" +
        "\nDispatcher\n" + this.dispMachineAndOS;
    }

    private void createGUI() {
        this.gui = new UserGUI("C3D user display", (UserLogic) this.me);
        this.gui.setValues(this.savedGuiValues);
        this.gui.setUserInfo(getMachineRelatedValues());
    }
}
