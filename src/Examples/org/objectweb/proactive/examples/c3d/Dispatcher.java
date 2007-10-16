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

import org.objectweb.proactive.core.util.wrapper.StringMutableWrapper;
import org.objectweb.proactive.examples.c3d.geom.Vec;
import org.objectweb.proactive.examples.c3d.prim.Sphere;


/** Services proposed by a Dispatcher Active Object, without all the GUI stuff */
public interface Dispatcher {

    /** Rotate every object by the given angle */
    public void rotateScene(int i_user, Vec angles);

    public void addSphere(Sphere s);

    public void resetScene();

    /** Register a user, so he can join the fun */
    //SYNCHRONOUS CALL. All [active object calls back to caller] in this method happen AFTER the int is returned
    public int registerUser(User c3duser, String userName);

    public void registerMigratedUser(int userNumber);

    /** removes user from userList, so he cannot receive any more messages or images */
    public void unregisterConsumer(int number);

    /** Get the list of users, entries being separated by \n */
    public StringMutableWrapper getUserList();

    /** Find the name of the machine this Dispatcher is running on */
    public String getMachineName();

    /** Find the name of the OS the Dispatcher is running on */
    public String getOSString();

    /** send message to all users except one */
    public void userWriteMessageExcept(int i_user, String s_message);

    /** Shows a message to a user*/
    public void userWriteMessage(int i_user, String s_message);
}
