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
package org.objectweb.proactive;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * <P>
 * RunActive is related to the activity of an active object.
 * When an active object is started, which means that its
 * active thread starts and serves the requests being sent
 * to its request queue, it is possible to define exactly how
 * the activity (the serving of requests amongst others) will
 * be done.
 * </P><P>
 * An object implementing this interface is invoked to run the
 * activity until an event trigger its end. The object being
 * reified as an active object can directly implement this interface
 * or an external class can also be used.
 * </P>
 * <P>
 * It is the role of the body of the active object to perform the
 * call on the object implementing this interface. For an active object
 * to run an activity, the method <code>runActivity</code> must not end
 * before the end of the activity. When the method <code>runActivity</code>
 * ends, the activity ends too and the <code>endActivity</code> can be invoked.
 * </P>
 * <P>
 * Here is an example of a simple implementation of <code>runActivity</code> method
 * doing a FIFO service of the request queue :
 * </P>
 * <pre>
 * public void runActivity(Body body) {
 *   Service service = new Service(body);
 *   while (body.isActive()) {
 *     service.blockingServeOldest();
 *   }
 * }
 * </pre>
 *
 * @author The ProActive Team
 * @version 1.0,  2002/06
 * @since   ProActive 0.9.3
 */
@PublicAPI
public interface RunActive extends Active {

    /**
     * Runs the activity of the active object.
     * @param body the body of the active object being started
     */
    public void runActivity(Body body);
}
