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
package org.objectweb.proactive.examples.webservices.c3dWS.ws;

public interface Service1Soap extends java.rmi.Remote {
    public void helloWorld(java.lang.String helloWorldName)
        throws java.rmi.RemoteException;

    public void setPixels(java.lang.String setPixelsUserName,
        org.objectweb.proactive.examples.webservices.c3dWS.ws.ArrayOfInt setPixelsPix,
        int setPixelsNumber, int setPixelsNbInt)
        throws java.rmi.RemoteException;

    public void showDialog(java.lang.String showDialogUserName,
        java.lang.String showDialogMessage) throws java.rmi.RemoteException;

    public void showUserMessage(java.lang.String showUserMessageUserName,
        java.lang.String showUserMessageMessage)
        throws java.rmi.RemoteException;

    public void showUserMessageFrom(
        java.lang.String showUserMessageFromUserName,
        java.lang.String showUserMessageFromFrom,
        java.lang.String showUserMessageFromMessage)
        throws java.rmi.RemoteException;

    public void informNewUser(java.lang.String informNewUserMyUserName,
        java.lang.String informNewUserUsernameNew, int informNewUserId)
        throws java.rmi.RemoteException;

    public void informUserLeft(java.lang.String informUserLeftMyUserName,
        java.lang.String informUserLeftUserNameLeft)
        throws java.rmi.RemoteException;
}
