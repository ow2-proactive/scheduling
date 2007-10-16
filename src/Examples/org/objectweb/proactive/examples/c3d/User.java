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


/** Methods which are to be implemented by users.
 * Represents which services are available for Objects unrelated with User GUIs. */
public interface User {

    /** shows a String as a log */
    public void log(String s_message);

    /** Shows a String as a message to this user*/
    public void message(String s_message);

    /**
     * Informs the user that a new user has joined the party!!
     * @param  nUser The new user's ID
     * @param sName The new user's name
     */
    public void informNewUser(int nUser, String sName);

    /**
     * Informs the user that another user left
     * @param nUser The id of the old user
     */
    public void informUserLeft(String sName);

    /**
     * Display an interval of newly calculated pixels
     * @param newpix        The pixels as int array
     * @param interval        The interval
     */
    public void setPixels(Image2D image);

    /**
     * Reflect a change on the dispatcher host.
     * @param os the Name of the OS supporting the dispatcher
     * @param machine the name of the physical machine hosting the dispatcher
     */
    public void setDispatcherMachine(String machine, String os);
}
