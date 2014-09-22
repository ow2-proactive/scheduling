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
package org.ow2.proactive.scheduler.common.exception;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Exceptions Generated when forked JVM process exit before having a java result
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.2
 */
@PublicAPI
public class ForkedJavaTaskException extends SchedulerException {

    private static final long serialVersionUID = 60L;

    private int exitCode = -1;

    /**
     * Create a new instance of JobCreationException using the given message string
     *
     * @param message the reason of the exception
     * @param exitCode return code of the java process
     */
    public ForkedJavaTaskException(String message, int exitCode) {
        super(message);
        this.exitCode = exitCode;
    }

    /**
     * Create a new instance of JobCreationException using the given message string
     *
     * @param message the reason of the exception
     * @param exitCode return code of the java process
     */
    public ForkedJavaTaskException(Throwable cause, int exitCode) {
        super(cause.getMessage(), cause);
        this.exitCode = exitCode;
    }

    /**
     * Create a new instance of JobCreationException using the given message string and cause
     *
     * @param message the reason of the exception
     * @param cause the cause of this exception
     * @param exitCode return code of the java process
     */
    public ForkedJavaTaskException(String message, Throwable cause, int exitCode) {
        super(message, cause);
        this.exitCode = exitCode;
    }

    /**
     * Get the exitCode of the exception
     *
     * @return the exitCode of the exception
     */
    public int getExitCode() {
        return exitCode;
    }

}