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
package org.ow2.proactive.scheduler.common.job.factories.spi.model.validator;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.ModelValidatorContext;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ValidationException;


public class UserFileValidator implements Validator<URI> {
    private static final Logger logger = Logger.getLogger(UserFileValidator.class);

    @Override
    public URI validate(URI parameterValue, ModelValidatorContext context) throws ValidationException {
        if (context == null || context.getSpace() == null) {
            // Sometimes the workflow is parsed and checked without scheduler instance (e.g., submitted from catalog).
            // In this case, we don't have the access of the scheduler user dataspace, so the validity check is passed.
            logger.debug(String.format("Can't check the validity of the variable value [%s], because missing the access to the scheduler user data space",
                                       parameterValue));
            return parameterValue;
        }

        // if parameterValue is not a file existing in data space, we throw ValidationException
        if (StringUtils.isBlank(parameterValue.toString())) {
            throw new ValidationException("Please provide a valid file path in the user space as the variable value.");
        }
        try {
            if (!context.getSpace().checkFileExistsInUserSpace(parameterValue.toString())) {
                throw new ValidationException(String.format("Could not find the file path [%s] in the user data space. Please add the file into the user data space or change the variable value to a valid path.",
                                                            parameterValue));
            }
        } catch (NotConnectedException | PermissionException e) {
            throw new ValidationException("Could not read user data space files from the scheduler, make sure you are connected and you have permission rights to read user data space files.",
                                          e);
        }
        return parameterValue;
    }
}
