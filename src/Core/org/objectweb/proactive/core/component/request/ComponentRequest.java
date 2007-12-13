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
package org.objectweb.proactive.core.component.request;

import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.request.Request;


/**
 * Marker interface.
 *
 * @author Matthieu Morel
 *
 */
public interface ComponentRequest extends Request {
    //  strict FIFO (no priority)
    public static final short STRICT_FIFO_PRIORITY = 3;

    //  serve oldest NF before functional requests
    public static final short BEFORE_FUNCTIONAL_PRIORITY = 2;

    //  non functional FIFO : serve oldest NF if requested functional is older than oldest NF, 
    public static final short IMMEDIATE_PRIORITY = 1;

    /**
     * Tells whether the request is a call to a control interface.
     * @return true if it is an invocation on a control interface
     */
    public boolean isControllerRequest();

    public boolean isStopFcRequest();

    public boolean isStartFcRequest();

    /**
     * Indicates a possible shortcut, because a functional request has been transferred between
     * the sender and the intermediate component.
     * @param sender the sender of the functional component request
     * @param intermediate the component that the functional request has reached so far
     */
    public void shortcutNotification(UniversalBody sender, UniversalBody intermediate);

    /**
     *
     * @return the shortcut object contained in this request, null if there is no shortcut
     */
    public Shortcut getShortcut();

    /**
     *
     * @return the number of membranes that could be shortcut, 0 if there is no shortcut
     */
    public int getShortcutLength();

    /*
     * TODO_M : more comments on priorities of requests
     * @return the priority of the request
     */
    public short getPriority();

    /**
     *
     * @return the class on which the method call contained in this request must be executed
     */
    public Class<?> getTargetClass();
}
