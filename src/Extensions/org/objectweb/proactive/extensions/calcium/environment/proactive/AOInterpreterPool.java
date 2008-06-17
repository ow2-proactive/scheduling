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
package org.objectweb.proactive.extensions.calcium.environment.proactive;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class AOInterpreterPool implements RunActive, InitActive {

    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_ENVIRONMENT);

    private Vector<AOStageIn> pool;
    private boolean shutdown;
    private AOInterpreterPool thisStub;

    /**
     * Empty constructor for ProActive  MOP
     * Do not use directly!!!
     */
    @Deprecated
    public AOInterpreterPool() {
    }

    /**
     * Add each AOInterpreter once to the AOInterpreterPool
     *
     * @param aoi The AOInterpreter array
     */
    public AOInterpreterPool(Boolean proactiveNoise) {
        shutdown = false;
        pool = new Vector<AOStageIn>();
    }

    public void initActivity(Body body) {
        this.thisStub = (AOInterpreterPool) PAActiveObject.getStubOnThis();
    }

    public synchronized void put(AOInterpreter list[], final int times) {

        for (int i = 0; i < times; i++) {
            for (AOInterpreter interpreter : list) {
                pool.add(interpreter.getStageIn(thisStub));
            }
        }
    }

    public synchronized void put(AOStageIn aoi) {
        pool.add(aoi);
    }

    public void putInRandomPosition(AOStageIn aoi, int times) {

        for (int i = 0; i < times; i++) {
            int position = (int) Math.round((Math.random() * pool.size()));
            pool.add(position, aoi);
        }
    }

    public synchronized AOStageIn get() throws ProActiveException {
        if (shutdown) {
            throw new ProActiveException("Interpreter pool is shutting down");
        }
        return pool.remove(0);
    }

    public void shutdown() {
        logger.info("InterpreterPool is shutting down");
        this.shutdown = true;
    }

    //Producer-Consumer
    public void runActivity(Body body) {
        Service service = new Service(body);

        while (true) {
            String allowedMethodNames = "put|putInRandomPosition|shutdown";

            if ((pool != null) && !pool.isEmpty()) {
                allowedMethodNames += "get";
            }

            service.blockingServeOldest(new RequestFilterOnAllowedMethods(allowedMethodNames));
        }
    }

    protected class RequestFilterOnAllowedMethods implements RequestFilter, java.io.Serializable {
        private String allowedMethodNames;

        public RequestFilterOnAllowedMethods(String allowedMethodNames) {
            this.allowedMethodNames = allowedMethodNames;
        }

        public boolean acceptRequest(Request request) {
            return allowedMethodNames.indexOf(request.getMethodName()) >= 0;
        }
    }
}
