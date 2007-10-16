/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed, Concurrent
 * computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis Contact:
 * proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Initial developer(s): The ProActive Team
 * http://www.inria.fr/oasis/ProActive/contacts.html Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.extensions.calcium.environment.proactive;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class AOInterpreterPool implements RunActive {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_ENVIRONMENT);
    Vector<AOInterpreter> pool;

    public AOInterpreterPool() {
    }

    /**
     * Add each AOInterpreter once to the AOInterpreterPool
     *
     * @param aoi The AOInterpreter array
     */
    public void init(AOInterpreter[] aoi) {
        pool = new Vector<AOInterpreter>();

        for (AOInterpreter i : aoi) {
            pool.add(i);
        }
    }

    public void registerAsAvailable(AOInterpreter aoi) {
        pool.add(aoi);
    }

    public AOInterpreter getAOInterpreter() {
        return pool.remove(0);
    }

    //Producer-Consumer
    public void runActivity(Body body) {
        Service service = new Service(body);

        while (true) {
            String allowedMethodNames = "init|registerAsAvailable";

            if ((pool != null) && !pool.isEmpty()) {
                allowedMethodNames += "getAOInterpreter";
            }

            service.blockingServeOldest(new RequestFilterOnAllowedMethods(
                    allowedMethodNames));
        }
    }

    protected class RequestFilterOnAllowedMethods implements RequestFilter,
        java.io.Serializable {
        private String allowedMethodNames;

        public RequestFilterOnAllowedMethods(String allowedMethodNames) {
            this.allowedMethodNames = allowedMethodNames;
        }

        public boolean acceptRequest(Request request) {
            return allowedMethodNames.indexOf(request.getMethodName()) >= 0;
        }
    }
}
