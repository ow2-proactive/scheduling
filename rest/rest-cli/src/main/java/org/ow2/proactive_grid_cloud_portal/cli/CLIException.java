/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive_grid_cloud_portal.cli;

public class CLIException extends RuntimeException {

    public static final int REASON_UNAUTHORIZED_ACCESS = 1;

    public static final int REASON_IO_ERROR = 2;

    public static final int REASON_INVALID_ARGUMENTS = 3;

    public static final int REASON_OTHER = 4;

    private static final long serialVersionUID = 1L;

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
