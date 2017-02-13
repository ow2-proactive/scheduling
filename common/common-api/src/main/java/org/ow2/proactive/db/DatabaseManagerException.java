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
package org.ow2.proactive.db;

/**
 * DatabaseManagerException is thrown by the DataBaseManager when Hibernate exception occurs.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9.1
 */
public class DatabaseManagerException extends RuntimeException {

    /**
     * Create a new instance of DatabaseManagerException.
     */
    public DatabaseManagerException() {
        super();
    }

    /**
     * Create a new instance of DatabaseManagerException.
     *
     * @param message the message to be display
     * @param cause the throwable that cause this exception
     */
    public DatabaseManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create a new instance of DatabaseManagerException.
     *
     * @param message the message to be display
     */
    public DatabaseManagerException(String message) {
        super(message);
    }

    /**
     * Create a new instance of DatabaseManagerException.
     *
     * @param cause the throwable that cause this exception
     */
    public DatabaseManagerException(Throwable cause) {
        super(cause);
    }

}
