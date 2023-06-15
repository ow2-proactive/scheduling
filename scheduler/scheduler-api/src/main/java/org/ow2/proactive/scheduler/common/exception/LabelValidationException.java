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
package org.ow2.proactive.scheduler.common.exception;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Exception generated when trying to create a label that is not valid.<br>
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
@PublicAPI
public class LabelValidationException extends SchedulerException {

    /**
     * Create a new instance of LabelValidationException
     *
     */
    public LabelValidationException(String label) {
        super("Label " + label +
              " should contains just letters, numbers or /-_ and it should have a maximum length of 20 characters!");
    }

    /**
     * Create a new instance of LabelValidationException
     */
    public LabelValidationException() {
    }

    /**
     * Create a new instance of LabelValidationException
     *
     * @param msg the message to attach.
     * @param cause the cause of the exception.
     */
    public LabelValidationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Create a new instance of LabelValidationException
     *
     * @param cause the cause of the exception.
     */
    public LabelValidationException(Throwable cause) {
        super(cause);
    }

}
