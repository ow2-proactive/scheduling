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

import org.ow2.proactive.core.properties.PropertyDecrypter;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.ModelValidatorContext;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ValidationException;


public class EncryptionValidator implements Validator<String> {

    public EncryptionValidator() {
        // empty constructor
    }

    @Override
    public String validate(String parameterValue, ModelValidatorContext context) throws ValidationException {
        if (parameterValue != null && parameterValue.startsWith(PropertyDecrypter.ENCRYPTION_PREFIX)) {
            // validating value already encrypted
            try {
                PropertyDecrypter.decryptData(parameterValue);
            } catch (Exception e) {
                throw new ValidationException("Cannot decrypt value: " + parameterValue, e);
            }
        } else if (parameterValue != null) {
            // encrypt value
            String encryptedValue;
            try {
                encryptedValue = PropertyDecrypter.encryptData(parameterValue);
            } catch (Exception e) {
                throw new ValidationException("Cannot encrypt value: " + parameterValue, e);
            }
            // store new value in variables
            if (context.getSpELVariables() != null && context.getSpELVariables().getVariables() != null &&
                context.getVariableName() != null) {
                context.getSpELVariables().getVariables().put(context.getVariableName(), encryptedValue);
            }
            return encryptedValue;
        }
        return parameterValue;
    }
}
