/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.scheduler.common.exception;

/**
 * AlreadyConnectedException is thrown if a client already connected try to connect again
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public class AlreadyConnectedException extends SchedulerException {

    /**
     * Attaches a message to the AlreadyConnectedException.
     *
     * @param msg message attached.
     */
    public AlreadyConnectedException(String msg) {
        super(msg);
    }

    /**
     * Create a new instance of AlreadyConnectedException.
     */
    public AlreadyConnectedException() {
        super();
    }

    /**
     * Create a new instance of AlreadyConnectedException with the given message and cause
     *
     * @param msg the message to attach.
     * @param cause the cause of the exception.
     */
    public AlreadyConnectedException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Create a new instance of AlreadyConnectedException with the given cause.
     *
     * @param cause the cause of the exception.
     */
    public AlreadyConnectedException(Throwable cause) {
        super(cause);
    }
}
