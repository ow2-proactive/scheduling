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
package org.objectweb.proactive.api;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;


public class ProLauncher {

    /**
     * Launches the main method of the main class through the node node
     * @param classname classname of the main method to launch
     * @param mainParameters parameters
     * @param node node in which launch the main method
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws ProActiveException
     */
    public static void newMain(String classname, String[] mainParameters,
        Node node)
        throws ClassNotFoundException, NoSuchMethodException, ProActiveException {
        ProActiveRuntime part = node.getProActiveRuntime();
        part.launchMain(classname, mainParameters);
    }

    /**
     * Creates an instance of the remote class. This instance is
     * created with the default constructor
     * @param classname
     * @param node
     * @throws ClassNotFoundException
     * @throws ProActiveException
     */
    public static void newRemote(String classname, Node node)
        throws ClassNotFoundException, ProActiveException {
        ProActiveRuntime part = node.getProActiveRuntime();
        part.newRemote(classname);
    }
}
