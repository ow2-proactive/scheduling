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

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.extensions.calcium.task.TaskPool;


public class AOTaskPool extends TaskPool implements RunActive, Serializable {
    public AOTaskPool() {
        super();
    }

    //Producer-Consumer
    public void runActivity(Body body) {
        Service service = new Service(body);

        while (true) {
            String allowedMethodNames = "getStats|addReadyRootTask|putProcessedTask|getReadyQueueLength|hasResults|isFinished|isPaniqued|panic|getStatsGlobal";

            if (getReadyQueueLength() > 0) {
                allowedMethodNames += "getReadyTask|getReadyTasks";
            }

            if (hasResults()) {
                allowedMethodNames += "getResult|";
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
