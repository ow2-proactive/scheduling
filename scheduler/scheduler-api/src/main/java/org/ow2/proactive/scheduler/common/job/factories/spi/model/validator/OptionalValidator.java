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

import org.apache.commons.lang3.StringUtils;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.ModelValidatorContext;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ValidationException;


/**
 * Validate all the blank parameter value. When the value is not blank, use its specific validator to check the value validity.
 * @param <T>
 */
public class OptionalValidator<T> implements Validator<T> {
    Validator<T> validator;

    public OptionalValidator(Validator<T> validator) {
        this.validator = validator;
    }

    @Override
    public T validate(T parameterValue, ModelValidatorContext context, boolean isVariableHidden)
            throws ValidationException {
        // When the parameter value is not provided, it's validated. Otherwise, use its proper validator
        if (parameterValue == null || StringUtils.isBlank(parameterValue.toString())) {
            return parameterValue;
        } else {
            return validator.validate(parameterValue, context, isVariableHidden);
        }
    }
}
