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
package org.objectweb.proactive.core.body.request;

/**
 * <p>
 * A class implementing this interface can process requests.
 * </p><p>
 * It implements one method <code>processRequest</code> that takes a request
 * and returns an int saying whether
 * the request shall be removed and served, removed without serving or kept.
 * </p><p>
 * It is used as a call back interface allowing a custom processing on request
 * stored in the request queue.
 * </p><p>
 * Typically it can be used to serve requests stored in a request queue in a
 * custom manner. When doing custom processing, a request should
 * be REMOVED from the queue BEFORE serving.
 * </p>
 *
 * @author  ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public interface RequestProcessor {

    /** Constant indicating that the request shall be removed and served. */
    public final static int REMOVE_AND_SERVE = 1;

    /** Constant indicating that the request shall removed without being served. */
    public final static int REMOVE = 2;

    /** Constant indicating that the request shall be kept. */
    public final static int KEEP = 3;

    /**
     * Returns one of the constants indicating the desired treatment for the request.
     * @param request the request to process
     * @return one of the three constants above
     */
    public int processRequest(Request request);
}
