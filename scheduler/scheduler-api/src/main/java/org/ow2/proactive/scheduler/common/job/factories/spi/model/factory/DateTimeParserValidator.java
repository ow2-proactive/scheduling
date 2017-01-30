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

import java.util.Date;
import java.util.List;

import org.ow2.proactive.scheduler.common.job.factories.spi.model.converter.Converter;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.converter.DateTimeConverter;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ModelSyntaxException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.AcceptAllValidator;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.RangeValidator;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.Validator;

import com.google.common.collect.Range;


public class DateTimeParserValidator extends RangeParserValidator<Date> {

    public static final String DATETIME_TYPE = "DATETIME";

    public static final String LEFT_DELIMITER = "(";

    public static final String RIGHT_DELIMITER = ")";

    protected static final String DATETIME_TYPE_REGEXP = "[Dd][Aa][Tt][Ee][Tt][Ii][Mm][Ee]";

    protected static final String LEFT_DELIMITER_REGEXP = "\\" + LEFT_DELIMITER;

    protected static final String RIGHT_DELIMITER_REGEXP = "\\" + RIGHT_DELIMITER;

    protected static final String DATETYPE_MAIN_REGEXP = DATETIME_TYPE_REGEXP + LEFT_DELIMITER_REGEXP + "([^)]+)" +
                                                         RIGHT_DELIMITER_REGEXP;

    public DateTimeParserValidator(String model) throws ModelSyntaxException {
        super(model);
    }

    @Override
    public String getType() {
        return DATETIME_TYPE;
    }

    @Override
    protected String getTypeRegexp() {
        return DATETIME_TYPE_REGEXP;
    }

    @Override
    public Class getClassType() {
        return Date.class;
    }

    @Override
    protected Converter<Date> createConverter(String model) throws ModelSyntaxException {
        List<String> modelArguments = getDateModelArguments(model);
        try {
            return new DateTimeConverter(modelArguments.get(0));
        } catch (IllegalArgumentException e) {
            throw new ModelSyntaxException(e.getMessage(), e);
        }
    }

    @Override
    protected Validator<Date> createValidator(String model, Converter<Date> converter) throws ModelSyntaxException {
        List<String> modelArguments = getDateModelArguments(model);
        if (modelArguments.size() == 2) {
            Range<Date> range = super.extractRange(modelArguments.get(1), converter);
            return new RangeValidator<>(range);
        } else {
            return new AcceptAllValidator<>();
        }
    }

    private List<String> getDateModelArguments(String model) throws ModelSyntaxException {
        String dateTimeMainRegexp = "^" + DATETYPE_MAIN_REGEXP + "$" + "|" + "^" + DATETYPE_MAIN_REGEXP +
                                    RANGE_MAIN_REGEXP + "$";

        List<String> modelArguments = parseAndGetRegexGroups(model, dateTimeMainRegexp);
        if ((modelArguments.size() == 1) || (modelArguments.size() == 2)) {
            return modelArguments;
        } else {
            throw new ModelSyntaxException("Internal error, regular expression for " + getType() + " '" +
                                           dateTimeMainRegexp + "' is invalid.");
        }
    }
}
