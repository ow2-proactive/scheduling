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
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ConversionException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ModelSyntaxException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.RangeValidator;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.Validator;

import com.google.common.collect.Range;


public abstract class RangeParserValidator<T extends Comparable<T>> extends BaseParserValidator<T> {

    public static final String LEFT_RANGE_DELIMITER = "[";

    public static final String RIGHT_RANGE_DELIMITER = "]";

    protected static final String LEFT_RANGE_DELIMITER_REGEXP = "\\" + LEFT_RANGE_DELIMITER;

    protected static final String RIGHT_RANGE_DELIMITER_REGEXP = "\\" + RIGHT_RANGE_DELIMITER;

    protected static final String RANGE_MAIN_REGEXP = LEFT_RANGE_DELIMITER_REGEXP + "([^)]+)" +
                                                      RIGHT_RANGE_DELIMITER_REGEXP;

    public RangeParserValidator(String model) throws ModelSyntaxException {
        super(model);
    }

    @Override
    protected Validator<T> createValidator(String model, Converter<T> converter) throws ModelSyntaxException {
        if (model.matches(getTypeRegexp())) {
            return new RangeValidator();
        }
        String mainRangeRegexp = "^" + getTypeRegexp() + RANGE_MAIN_REGEXP + "$";
        String rangeString = parseAndGetOneGroup(model, mainRangeRegexp);
        try {
            return new RangeValidator<>(extractRange(rangeString, converter));
        } catch (IllegalArgumentException e) {
            throw new ModelSyntaxException(e.getMessage(), e);
        }
    }

    protected Range<T> extractRange(String value, Converter<T> converter) throws ModelSyntaxException {
        String rangeRegexp = "^([^),]+)$|^([^),]+),([^),]+)$";
        try {
            List<String> modelArguments = parseAndGetRegexGroups(value, rangeRegexp);
            if (modelArguments.size() == 1) {
                T minValue = converter.convert(modelArguments.get(0));
                return Range.atLeast(minValue);
            } else if (modelArguments.size() == 2) {
                T minValue = converter.convert(modelArguments.get(0));
                T maxValue = converter.convert(modelArguments.get(1));
                return Range.closed(minValue, maxValue);
            } else {
                throw new ModelSyntaxException("Internal error, regular expression for " + getType() + " '" +
                                               rangeRegexp + "' is invalid.");
            }
        } catch (ConversionException | IllegalArgumentException e) {
            throw new ModelSyntaxException("Illegal " + getType() + " range expression '" + value + "', " +
                                           e.getMessage(), e);
        }
    }
}
