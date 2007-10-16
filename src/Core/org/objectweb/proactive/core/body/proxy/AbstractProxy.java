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
package org.objectweb.proactive.core.body.proxy;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public abstract class AbstractProxy implements Proxy, java.io.Serializable {
    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public AbstractProxy() {
    }

    //
    // -- METHODS ----------------------------------------------------
    //

    /**
     * Checks if the given <code>Call</code> object <code>c</code> can be
     * processed with a future semantics, i-e if its returned object
     * can be a future object.
     *
     * Two conditions must be met : <UL>
     * <LI> The returned object is reifiable
     * <LI> The invoked method does not throw any exceptions
     * </UL>
     * @return true if and only if the method call can be asynchronous
     */
    protected static boolean isAsynchronousCall(MethodCall mc) {
        return mc.isAsynchronousWayCall();
    }
}
