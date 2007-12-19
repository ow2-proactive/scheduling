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
package org.objectweb.proactive.mpi;

import java.util.ArrayList;
import java.util.Hashtable;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;


@PublicAPI
public interface MPISpmd {

    /**
     * API method for starting MPI program
     * @return MPIResult
     */
    public MPIResult startMPI();

    /**
     * API method for reStarting MPI program - run a new computation independently
     * if the first one is currently running
     * @return MPIResult
     */
    public MPIResult reStartMPI();

    /**
     * API method for killing MPI program -
     * Kills the MPI program. The MPI program represented by this MPISpmd object is forcibly terminated.
     * @return boolean - true if program has been correctly been killed, false otherwise
     */
    public boolean killMPI();

    /**
     * API method for getting MPI program status -
     * @return the status of the MPISspmd object
     */
    public String getStatus();

    /**
     * API method for setting MPI program command arguments
     * @param arguments - the arguments of the MPI program
     */
    public void setMPICommandArguments(String arguments);

    /**
     * API method for getting MPI program name
     * @return String - the name of the MPI program
     */
    public String getName();

    /**
     * API method for getting MPI program virtual Node
     * @return Virtual Node - the name of the MPI program
     */
    public VirtualNode getVn();

    /**
     * API method for adding class that will be instanciate on nodes of applications
     * @param cl - the name of the user class
     */
    public void newActiveSpmd(String cl);

    /**
     * API method for adding class that will be instanciate on nodes of applications
     * @param cl - the name of the user class
     * @param params - the array that contain the parameters
     */
    public void newActiveSpmd(String cl, Object[] params);

    /**
     * API method for adding class that will be instanciate on nodes of applications
     * @param cl - the name of the user class
     * @param params - the array that contain the parameters
     */
    public void newActiveSpmd(String cl, Object[][] params);

    /**
     * API method for adding class that will be instanciate on a specific node of applications
     * @param cl - the name of the user class
     * @param params - the array that contains the parameters
     */
    public void newActive(String cl, Object[] params, int rank);

    /**
     * API method for getting list of SPMD classes
     * @return ArrayList - the list of classes to instanciate
     */
    public ArrayList getSpmdClasses();

    /**
     * API method for getting table of params
     * @return Hashtable - the table of params
     */
    public Hashtable getSpmdClassesParams();

    /**
     * API method for getting list of classes
     * @return ArrayList - the list of classes to instanciate
     */
    public ArrayList getClasses();

    /**
     * API method for getting array of params
     * @return Hashtable - the table of params
     */
    public Hashtable getClassesParams();

    /**
     * API method for getting remote library path
     * @return String - the remote library path
     */
    public String getRemoteLibraryPath();
}
