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

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.StringUtils;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.ModelValidatorContext;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ModelSyntaxException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ValidationException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.factory.BaseParserValidator;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.factory.ModelType;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.factory.OptionalParserValidator;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.factory.ParserValidator;

import com.google.common.base.Strings;


public class ModelValidator implements Validator<String> {

    private String model;

    public static final String PREFIX = "PA:";

    public static final String OPTIONAL_VARIABLE_SUFFIX = "?";

    public ModelValidator(String model) {
        if (Strings.isNullOrEmpty(model)) {
            throw new IllegalArgumentException("Model cannot be empty");
        }
        this.model = model.trim();
    }

    @Override
    public String validate(String parameterValue, ModelValidatorContext context) throws ValidationException {
        try {
            ParserValidator validator = createParserValidator();
            if (validator != null) {
                validator.parseAndValidate(parameterValue, context);
            }
            return parameterValue;
        } catch (Exception e) {
            throw new ValidationException(e.getMessage(), e);
        }
    }

    /**
     * Returns a registered parser validator based on the model syntax.
     * If no implementation is found of the provided model, returns null
     * as this model might be handled by a different module.
     *
     * @return a registered model parser or null if no registered parser could be found.
     * @throws ModelSyntaxException if an error occurred during the parser creation
     */
    protected ParserValidator createParserValidator() throws ModelSyntaxException {
        String uppercaseModel = model.toUpperCase();
        if (!uppercaseModel.startsWith(PREFIX)) {
            return null;
        }
        uppercaseModel = removePrefix(uppercaseModel);
        for (ModelType type : ModelType.values()) {
            if (uppercaseModel.startsWith(type.name())) {
                try {
                    String modelNoPrefixSuffix = removePrefix(model);
                    if (uppercaseModel.endsWith(OPTIONAL_VARIABLE_SUFFIX)) {
                        modelNoPrefixSuffix = StringUtils.removeEnd(modelNoPrefixSuffix, OPTIONAL_VARIABLE_SUFFIX);
                    }
                    BaseParserValidator parserValidator = (BaseParserValidator) type.getTypeParserValidator()
                                                                                    .getDeclaredConstructor(String.class)
                                                                                    .newInstance(modelNoPrefixSuffix);
                    if (uppercaseModel.endsWith(OPTIONAL_VARIABLE_SUFFIX)) {
                        return new OptionalParserValidator(modelNoPrefixSuffix, type, parserValidator);
                    } else {
                        return parserValidator;
                    }
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                        | NoSuchMethodException e) {
                    if (e instanceof InvocationTargetException) {
                        // unwrap InvocationTargetException to get more specific error message
                        Throwable exceptionCause = e.getCause();
                        if (exceptionCause instanceof ModelSyntaxException) {
                            throw (ModelSyntaxException) exceptionCause;
                        }
                    }
                    throw new ModelSyntaxException(String.format("Error during create the parser [%s] for the model [%s].",
                                                                 type.getTypeParserValidator().getSimpleName(),
                                                                 model),
                                                   e);
                }
            }
        }
        throw new ModelSyntaxException("Unrecognized type in model '" + model + "'");
    }

    private String removePrefix(String model) {
        return model.substring(PREFIX.length());
    }
}
