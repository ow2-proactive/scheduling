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

import java.util.regex.PatternSyntaxException;

import org.ow2.proactive.scheduler.common.job.factories.spi.model.converter.Converter;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.converter.IdentityConverter;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ModelSyntaxException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.RegexpValidator;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.Validator;


public class RegexpParserValidator extends BaseParserValidator<String> {

    public static final String REGEXP_TYPE = "REGEXP";

    public static final String LEFT_DELIMITER = "(";

    public static final String RIGHT_DELIMITER = ")";

    protected static final String REGEXP_TYPE_REGEXP = "[Rr][Ee][Gg][Ee][Xx][Pp]";

    protected static final String LEFT_DELIMITER_REGEXP = "\\" + LEFT_DELIMITER;

    protected static final String RIGHT_DELIMITER_REGEXP = "\\" + RIGHT_DELIMITER;


    public RegexpParserValidator(String model) throws ModelSyntaxException {
        super(model);
    }

    @Override
    public String getType() {
        return REGEXP_TYPE;
    }

    @Override
    public String getTypeRegexp() {
        return REGEXP_TYPE_REGEXP;
    }

    @Override
    public Class getClassType() {
        return String.class;
    }

    @Override
    protected Converter<String> createConverter(String model) throws ModelSyntaxException {
        return new IdentityConverter();
    }

    @Override
    protected Validator<String> createValidator(String model, Converter<String> converter) throws ModelSyntaxException {
        String regexpRegexp = "^" + REGEXP_TYPE_REGEXP + LEFT_DELIMITER_REGEXP + "(.+)" + RIGHT_DELIMITER_REGEXP + "$";
        String regexpString = parseAndGetOneGroup(model, regexpRegexp);
        try {
            return new RegexpValidator(regexpString);
        } catch (PatternSyntaxException e) {
            throw new ModelSyntaxException(e.getMessage(), e);
        }
    }

}
