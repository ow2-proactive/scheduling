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
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.CatalogObjectValidator;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.Validator;
import org.springframework.expression.ParseException;


/**
 * @author ActiveEon Team
 * @since 11/10/2018
 */
public class CatalogObjectParserValidator extends BaseParserValidator<String> {

    // regexp which matches the CATALOG_OBJECT model basic format without parameters, i.e., CATALOG_OBJECT
    protected static final String CATALOG_BASIC_MODEL_REGEXP = ignoreCaseRegexp("CATALOG_OBJECT");

    // complete regexp which matches the CATALOG_OBJECT model format (with or without parameters)
    protected static final String CATALOG_COMPLETE_REGEXP = String.format("^%s(%s)?$",
                                                                          CATALOG_BASIC_MODEL_REGEXP,
                                                                          PARAMETER_REGEXP);

    public CatalogObjectParserValidator(String model) throws ModelSyntaxException {
        super(model, ModelType.CATALOG_OBJECT, CATALOG_COMPLETE_REGEXP);
    }

    @Override
    protected Converter<String> createConverter(String model) throws ModelSyntaxException {
        return new IdentityConverter();
    }

    @Override
    protected Validator<String> createValidator(String model, Converter<String> converter) throws ModelSyntaxException {
        List<String> parametersGroup = parseAndGetRegexGroups(model, CATALOG_COMPLETE_REGEXP);

        if (parametersGroup.size() == 0) {
            // model without parameters
            try {
                return new CatalogObjectValidator();
            } catch (ParseException e) {
                throw new ModelSyntaxException(e.getMessage(), e);
            }
        }

        String parametersGroupString = parametersGroup.get(0).trim();
        String parameters = parametersGroupString.substring(1, parametersGroupString.length() - 1); // remove surrounding brackets
        String[] splitParameters = parameters.split(",");

        String kind = "";
        String contentType = "";
        String bucketName = "";
        String objectName = "";
        switch (splitParameters.length) {
            case 1:
                kind = splitParameters[0].trim();
                break;
            case 2:
                kind = splitParameters[0].trim();
                contentType = splitParameters[1].trim();
                break;
            case 3:
                kind = splitParameters[0].trim();
                contentType = splitParameters[1].trim();
                bucketName = splitParameters[2].trim();
                break;
            case 4:
                kind = splitParameters[0].trim();
                contentType = splitParameters[1].trim();
                bucketName = splitParameters[2].trim();
                objectName = splitParameters[3].trim();
                break;
            default:
                throw new ModelSyntaxException(String.format("Illegal parameters format for the model [%s], the model CATALOG_OBJECT can have at most four parameters: kind, content type, bucket name and object name, split by comma.",
                                                             model));
        }

        try {
            return new CatalogObjectValidator(kind, contentType, bucketName, objectName);
        } catch (ParseException e) {
            throw new ModelSyntaxException(e.getMessage(), e);
        }
    }
}
