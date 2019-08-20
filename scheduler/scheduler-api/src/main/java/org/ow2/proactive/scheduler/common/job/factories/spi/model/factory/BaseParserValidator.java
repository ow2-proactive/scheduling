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

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.ow2.proactive.scheduler.common.job.factories.spi.model.ModelValidatorContext;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.converter.Converter;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ConversionException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ModelSyntaxException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ValidationException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.Validator;

import com.google.common.base.Strings;


public abstract class BaseParserValidator<T> implements ParserValidator<T> {

    protected String model;

    /**
     * The name of the parser type, e.g., BOOLEAN, INTEGER, etc
     */
    protected ModelType type;

    /**
     * The regexp which defines the allowed model expression.
     */
    protected String typeRegexp;

    /**
     * Construct a parser validator whose model definition should match the parser type (case insensitive)
     * @param model the model definition
     * @param type the parser type
     * @throws ModelSyntaxException
     */
    protected BaseParserValidator(String model, ModelType type) throws ModelSyntaxException {
        // By default, the model expression should match the parser type while ignoring case.
        this(model, type, ignoreCaseRegexp(type.name()));
    }

    /**
     * Construct a parser validator with a customized model definition rule.
     * @param model the model definition
     * @param type the parser type
     * @param typeRegexp the expected pattern of the model expression
     * @throws ModelSyntaxException
     */
    protected BaseParserValidator(String model, ModelType type, String typeRegexp) throws ModelSyntaxException {
        if (Strings.isNullOrEmpty(model)) {
            throw new ModelSyntaxException("Model cannot be empty");
        }
        this.model = model.trim();
        this.type = type;
        this.typeRegexp = typeRegexp;
        if (!model.matches(typeRegexp)) {
            throw new ModelSyntaxException(String.format("Model expression %s doesn't match the expected pattern: %s",
                                                         model,
                                                         typeRegexp));
        }
    }

    /**
     * Returns the class used by this parser to convert string parameter values.
     * @return class
     */
    protected Class getClassType() {
        return type.getClassType();
    }

    /**
     * Create a converter used by this parser
     * @param model model used to create the converter
     * @return converter
     * @throws ModelSyntaxException
     */
    protected abstract Converter<T> createConverter(String model) throws ModelSyntaxException;

    /**
     * Create a validator used by this parser, using eventually a converter
     * @param model model used to create the validator
     * @param converter converter eventually used to create the validator
     * @return validator
     * @throws ModelSyntaxException
     */
    protected abstract Validator<T> createValidator(String model, Converter<T> converter) throws ModelSyntaxException;

    protected String parseAndGetOneGroup(String valueToParse, String regexp) throws ModelSyntaxException {
        List<String> groups = parseAndGetRegexGroups(valueToParse, regexp);
        if (groups.size() != 1) {
            throw new ModelSyntaxException("Illegal " + type + " expression in '" + valueToParse +
                                           "', model does not match regexp " + regexp);
        }

        return groups.get(0);
    }

    protected static List<String> parseAndGetRegexGroups(String valueToParse, String regexp)
            throws ModelSyntaxException {
        try {
            Pattern pattern = Pattern.compile(regexp);
            Matcher matcher = pattern.matcher(valueToParse);
            if (matcher.find()) {
                return getMatchedGroups(matcher, matcher.groupCount());
            } else {
                throw new ModelSyntaxException("Expression '" + valueToParse + "' does not match " + regexp);
            }
        } catch (PatternSyntaxException e) {
            throw new ModelSyntaxException("Internal error, regular expression '" + regexp + "' is invalid: " +
                                           e.getMessage(), e);
        }
    }

    private static List<String> getMatchedGroups(Matcher matcher, int numberOfGroupsFound) {
        List<String> groupsFound = new LinkedList<>();
        for (int i = 0; i < numberOfGroupsFound; i++) {
            // group 0 always contain the whole match and should not be used.
            String groupMatched = matcher.group(i + 1);
            if (groupMatched != null) {
                groupsFound.add(groupMatched);
            }
        }
        return groupsFound;
    }

    @Override
    public T parseAndValidate(String parameterValue)
            throws ConversionException, ValidationException, ModelSyntaxException {
        return parseAndValidate(parameterValue, null);
    }

    @Override
    public T parseAndValidate(String parameterValue, ModelValidatorContext context)
            throws ConversionException, ValidationException, ModelSyntaxException {
        if (parameterValue == null) {
            throw new ConversionException(parameterValue, getClassType());
        }
        Converter<T> converter = createConverter(model);
        return createValidator(model, converter).validate(converter.convert(parameterValue), context);
    }

    public static String ignoreCaseRegexp(String matcher) {
        return String.format("(?i:%s)", matcher);
    }
}
