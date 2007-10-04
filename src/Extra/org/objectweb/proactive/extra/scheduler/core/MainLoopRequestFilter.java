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
package org.objectweb.proactive.extra.scheduler.core;

import java.io.Serializable;
import java.util.ArrayList;

import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;


/**
 * Request filter for the main loop.
 * This object is made with a list of method names and provide the acceptRequest method.
 * Each method name matching a string in the list will answer true to this method.
 *
 * @author ProActive Team
 * @version 1.0, Jul 17, 2007
 * @since ProActive 3.2
 */
public class MainLoopRequestFilter implements RequestFilter, Serializable {

    /** Serial version UID */
    private static final long serialVersionUID = 1218635121519401713L;
    private ArrayList<String> methodNames = new ArrayList<String>();

    /**
     * MainLoopRequestFilter Constructor with a list of string method name to filter.
     * When acceptRequest will be invoked,
     * only the method that have a name matching the args list will return true;
     *
     * @param args a list of method names.
     */
    public MainLoopRequestFilter(String... args) {
        for (int i = 0; i < args.length; i++) {
            methodNames.add(args[i]);
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
