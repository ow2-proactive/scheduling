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

public interface MPIConstants {

    /**
     * <code>MPI_UNSTARTED</code>: constant for MPI process unstarted status.
     */
    public static final String MPI_UNSTARTED = "unstarted";

    /**
     * <code>MPI_RUNNING/code>: constant for MPI process started status.
     */
    public static final String MPI_RUNNING = "running";

    /**
     * <code>MPI_KILLED/code>: constant for MPI process killed status.
     */
    public static final String MPI_KILLED = "killed";

    /**
     * <code>MPI_FINISHED/code>: constant for MPI process finished status.
     */
    public static final String MPI_FINISHED = "finished";

    /**
     * <code>MPI_DEFAULT_STATUS</code>: constant for MPI process default status.
     */
    public static final String MPI_DEFAULT_STATUS = MPI_UNSTARTED;

    /**
     * <code>MPI_DEFAULT_STATUS</code>: constant for MPI process default status.
     */
    public static final String MPI_INCONSISTENT = "inconsistent_status";

    /**
     * <code>MPI_TERM_SUCESS</code>: constant for MPI process successful termination.
     */
    public static final boolean MPI_TERM_SUCCESS = true;

    /**
     * <code>MPI_TERM_FAILURE</code>: constant for MPI process failure termination.
     */
    public static final boolean MPI_TERM_FAILURE = false;
}
