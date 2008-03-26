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
package org.objectweb.proactive.core.component.exceptions;

import org.objectweb.proactive.core.ProActiveRuntimeException;


/**
 * Gathercast interface transform several invocations into a single invocation,
 * by gathering the invocation parameters and redistributing the results.
 * <p>
 * It is possible to configure a maximum time to wait until all connected client
 * interfaces have sent a request. If this timeout is reached before, a runtime
 * GathercastTimeoutException is thrown.
 * <p>
 * This exception is declared as a runtime exception so that invocations on
 * gathercast interfaces are <b>asynchronous</b><br>
 *
 * @author The ProActive Team
 *
 */
public class GathercastTimeoutException extends ProActiveRuntimeException {
    public GathercastTimeoutException() {
        super();
    }

    public GathercastTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public GathercastTimeoutException(String message) {
        super(message);
    }

    public GathercastTimeoutException(Throwable cause) {
        super(cause);
    }
}
