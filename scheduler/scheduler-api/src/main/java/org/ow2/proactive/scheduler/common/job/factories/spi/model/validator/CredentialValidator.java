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

import java.util.Set;

import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.ModelValidatorContext;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ValidationException;


/**
 * @author ActiveEon Team
 * @since 27/08/19
 */
public class CredentialValidator implements Validator<String> {
    private static final Logger logger = Logger.getLogger(CredentialValidator.class);

    @Override
    public String validate(String parameterValue, ModelValidatorContext context) throws ValidationException {
        if (context == null || context.getScheduler() == null) {
            // Sometimes the workflow is submitted without scheduler instance (e.g., submitted from catalog).
            // In this case, we don't have the access of the third-party credentials, so the validity check is passed.
            logger.warn(String.format("Can't check the validity of the variable value, because missing the access to the scheduler third-party credentials",
                                      parameterValue));
            return parameterValue;
        }

        // if parameterValue not exists in 3rd-party credentials, throw ValidationException
        try {
            Set<String> credentialKeys = context.getScheduler().thirdPartyCredentialsKeySet();
            if (!credentialKeys.contains(parameterValue)) {
                throw new ValidationException("Expected value should exist in the third-party credentials.");
            }
        } catch (NotConnectedException | PermissionException e) {
            throw new ValidationException("Exception during getting the third-party credentials.", e);
        }
        return parameterValue;
    }

}
