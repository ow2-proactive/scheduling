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

import org.ow2.proactive.scheduler.common.job.factories.spi.model.converter.Converter;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.converter.IdentityConverter;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ModelSyntaxException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.NotEmptyValidator;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.Validator;


/**
 * @author ActiveEon Team
 * @since 14/08/19
 */
public class NotEmptyParserValidator extends BaseParserValidator<String> {

    public static final String NOT_EMPTY_TYPE = "NOT_EMPTY";

    protected static final String NOT_EMPTY_TYPE_REGEXP = "[Nn][Oo][Tt]_[Ee][Mm][Pp][Tt][Yy]";

    public NotEmptyParserValidator(String model) throws ModelSyntaxException {
        super(model);
        String regexp = "^" + NOT_EMPTY_TYPE_REGEXP + "$";
        if (!model.matches(regexp)) {
            throw new ModelSyntaxException(model + " expression in model does not match " + regexp);
        }
    }

    @Override
    protected String getType() {
        return NOT_EMPTY_TYPE;
    }

    @Override
    protected String getTypeRegexp() {
        return NOT_EMPTY_TYPE_REGEXP;
    }

    @Override
    protected Class getClassType() {
        return String.class;
    }

    @Override
    protected Converter<String> createConverter(String model) throws ModelSyntaxException {
        return new IdentityConverter();
    }

    @Override
    protected Validator<String> createValidator(String model, Converter<String> converter) throws ModelSyntaxException {
        return new NotEmptyValidator();
    }
}
