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

import org.objectweb.proactive.Body;
import org.objectweb.proactive.examples.c3d.geom.Vec;


public interface UserLogic {

    /** The initialization and linkage is made in this method, instead of using the constructor */
    public abstract void go();

    /** called after migration, to reconstruct the logic.
     * In the initActivity :  myStrategyManager.onArrival("rebuild"); */

    // shouldn't be called from outside the class. 
    public abstract void rebuild();

    /** Called just before migration, as specified in the initActivity :
     * myStrategyManager.onDeparture("leaveHost");  */

    // shouldn't be called from outside the class. 
    public abstract void leaveHost();

    /**
     * Tells what are the operations to perform before starting the activity of the AO.
     * Here, we state that if migration asked, procedure  is : saveData, migrate, rebuild
     */
    public abstract void initActivity(Body body);

    /**
     * Exit the application
     */
    public abstract void terminate();

    /** Displays the list of users connected to the dispatcher */
    public abstract void getUserList();

    /** Ask the dispatcher to revert to original scene*/
    public abstract void resetScene();

    /** Ask the dispatcher to add a sphere*/
    public abstract void addSphere();

    public abstract void showUserInfo();

    /**
     * ask for the scene to be rotated by some angle
     * @param rotationAngle = <x y z> means rotate x radians along the x axis,
     *         then y radians along the y axis, and finally  z radians along the z axis
     */
    public abstract void rotateScene(Vec rotationAngle);

    /**  Send a mesage to a given other user, or to all */
    public abstract void sendMessage(String message, String recipientName);
}
