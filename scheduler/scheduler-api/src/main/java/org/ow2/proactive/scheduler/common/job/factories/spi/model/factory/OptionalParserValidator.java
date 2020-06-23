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
package org.ow2.proactive.scheduler.common.job.factories.spi.model.factory;

import static org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.ModelValidator.OPTIONAL_VARIABLE_SUFFIX;

import org.apache.commons.lang3.StringUtils;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.converter.Converter;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.converter.NullConverter;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ModelSyntaxException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.ModelValidator;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.OptionalValidator;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.Validator;

import com.google.common.annotations.VisibleForTesting;


/**
 * @author ActiveEon Team
 * @since 14/08/19
 */
public class OptionalParserValidator<T> extends BaseParserValidator<T> {
    @VisibleForTesting
    BaseParserValidator<T> parentParserValidator;

    public OptionalParserValidator(String model, ModelType type) throws ModelSyntaxException {
        super(removeSuffix(model), type, ".+");
        if (!model.endsWith(OPTIONAL_VARIABLE_SUFFIX)) {
            throw new ModelSyntaxException("Optional Model should end with \"?\"");
        }
        this.parentParserValidator = (BaseParserValidator<T>) ModelValidator.newParserValidator(type, this.model);
    }

    @Override
    protected Converter<T> createConverter(String model) throws ModelSyntaxException {
        return new NullConverter<>(parentParserValidator.createConverter(this.model));
    }

    @Override
    protected Validator<T> createValidator(String model, Converter<T> converter) throws ModelSyntaxException {
        return new OptionalValidator<>(parentParserValidator.createValidator(this.model, converter));
    }

    private static String removeSuffix(String model) {
        return StringUtils.removeEnd(model, OPTIONAL_VARIABLE_SUFFIX);
    }
}
