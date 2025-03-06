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

import java.util.List;

import org.ow2.proactive.scheduler.common.job.factories.spi.model.converter.Converter;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.converter.IdentityConverter;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ModelSyntaxException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.SpelValidator;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.Validator;
import org.springframework.expression.ParseException;


public class SPEL2ParserValidator extends BaseParserValidator<String> {

    public static final String LEFT_DELIMITER = "(";

    public static final String RIGHT_DELIMITER = ")";

    protected static final String SPEL2_TYPE_REGEXP = "^" + ignoreCaseRegexp(ModelType.SPEL2.name()) + "\\" +
                                                      LEFT_DELIMITER + "([^,]+),(.+)" + "\\" + RIGHT_DELIMITER + "$";

    public SPEL2ParserValidator(String model) throws ModelSyntaxException {
        super(model, ModelType.SPEL2, SPEL2_TYPE_REGEXP);
    }

    @Override
    protected Converter<String> createConverter(String model) throws ModelSyntaxException {
        return new IdentityConverter();
    }

    @Override
    protected Validator<String> createValidator(String model, Converter<String> converter) throws ModelSyntaxException {
        List<String> groups = parseAndGetRegexGroups(model, SPEL2_TYPE_REGEXP);
        if (groups.size() != 2) {
            throw new ModelSyntaxException("Internal error, regular expression " + SPEL2_TYPE_REGEXP +
                                           " did not return 2 groups in model '" + model + "'");
        }

        try {
            return new SpelValidator(groups.get(0), groups.get(1));
        } catch (ParseException e) {
            throw new ModelSyntaxException(e.getMessage(), e);
        }
    }

}
