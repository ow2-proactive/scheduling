/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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

package org.ow2.proactive_grid_cloud_portal.cli;

public class CLIException extends RuntimeException {

    public static final int REASON_UNAUTHORIZED_ACCESS = 1;
    public static final int REASON_IO_ERROR = 2;
    public static final int REASON_INVALID_ARGUMENTS = 3;
    public static final int REASON_OTHER = 4;

    private static final long serialVersionUID = 60L;
    private final int reason;
    private String stackTrace;

    public CLIException(int reason, String message) {
        this(reason, message, (Throwable) null);
    }

    public CLIException(int reason, Throwable cause) {
        this(reason, null, cause);
    }

    public CLIException(int reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public CLIException(int reason, String message, String stackTrace) {
        this(reason, message);
        this.stackTrace = stackTrace;
    }

    public int reason() {
        return reason;
    }

    public String stackTrace() {
        return stackTrace;
    }

}
