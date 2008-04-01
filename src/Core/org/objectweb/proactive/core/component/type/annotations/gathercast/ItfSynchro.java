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
package org.objectweb.proactive.core.component.type.annotations.gathercast;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * <p>Annotation for specifying timeouts and waitForAll mode at the level of interfaces.</p>
 * <p>Timeouts are only handled for invocations of methods that return a result.</b>
 * <p>When a timeout is detected, the default behavior is to throw a GathercastTimeoutException to the clients.</p>
 * <p>If waitForAll is set to true (default behaviour), the method will wait for the requests of all components binded on the gathercast interface before to be executed.</p>
 * <p>If waitForAll is set to false, the method will be executed immediately on the first request received (therefore will not wait for requests from other components binded on the gathercast interface).</p>
 * <p>Also, if waitForAll is set to false then it could not be combined with a timeout (throw an {@link org.objectweb.fractal.api.factory.InstantiationException}).</p>
 *
 * @author The ProActive Team
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ItfSynchro {
    public long DEFAULT_TIMEOUT = -1;

    /**
     * @return the timeout in seconds
     */
    long timeout() default DEFAULT_TIMEOUT;

    /**
     * @return true if the method will wait for the requests of all components binded on the gathercast interface before to be executed, false otherwise
     */
    boolean waitForAll() default true;

    //    /**
    //     * experimental
    //     */
    //    Class synchroVisitor();
}
