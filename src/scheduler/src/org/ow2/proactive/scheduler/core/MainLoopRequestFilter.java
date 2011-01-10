/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.core;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.core.annotation.RunActivityFiltered;
import org.ow2.proactive.scheduler.util.SchedulerDevLoggers;


/**
 * Request filter for the main loop.
 * This object is made with a list of method names and provide the acceptRequest method.
 * Each method name matching a string in the list will answer true to this method.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class MainLoopRequestFilter implements RequestFilter {
    /**
	 * 
	 */
	private static final long serialVersionUID = 30L;
	public static final Logger logger_dev = ProActiveLogger.getLogger(SchedulerDevLoggers.CORE);
    private ArrayList<String> methodNames = new ArrayList<String>();

    /**
     * When acceptRequest will be invoked,
     * the SchedulerCore methods annoted with {@link RunActivityFiltered} will be served first.
     */
    public MainLoopRequestFilter(String id) {
        if (id == null) {
            throw new NullPointerException("Request Filter ID cannot be null");
        }
        for (Method m : SchedulerCore.class.getDeclaredMethods()) {
            if (m.isAnnotationPresent(RunActivityFiltered.class) &&
                id.equals(m.getAnnotation(RunActivityFiltered.class).id())) {
                logger_dev.info(m.getName() + " is filtered with id = " + id);
                methodNames.add(m.getName());
            }
        }
    }

    /**
     * @see org.objectweb.proactive.core.body.request.RequestFilter#acceptRequest(org.objectweb.proactive.core.body.request.Request)
     */
    public boolean acceptRequest(Request request) {
        for (String s : methodNames) {
            if (request.getMethodName().equals(s)) {
                return true;
            }
        }

        return false;
    }
}
