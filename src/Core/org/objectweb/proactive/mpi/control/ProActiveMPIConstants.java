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
package org.objectweb.proactive.mpi.control;

public interface ProActiveMPIConstants {
    public static final int COMM_MSG_SEND = 2;
    public static final int COMM_MSG_INIT = 6;
    public static final int COMM_MSG_ALLSEND = 8;
    public static final int COMM_MSG_FINALIZE = 10;
    public static final int COMM_MSG_SEND_PROACTIVE = 12;
    public static final int COMM_MSG_NF = 20;

    public static final int MPI_ANY_TAG = -1;
    public static final int MPI_ANY_SOURCE = -2;

    // Data types
    public static final int MPI_CHAR = 1;
    public static final int MPI_UNSIGNED_CHAR = 2;
    public static final int MPI_BYTE = 3;
    public static final int MPI_SHORT = 4;
    public static final int MPI_UNSIGNED_SHORT = 5;
    public static final int MPI_INT = 6;
    public static final int MPI_UNSIGNED = 7;
    public static final int MPI_LONG = 8;
    public static final int MPI_UNSIGNED_LONG = 9;
    public static final int MPI_FLOAT = 10;
    public static final int MPI_DOUBLE = 11;
    public static final int MPI_LONG_DOUBLE = 12;
    public static final int MPI_LONG_LONG_INT = 13;
}
