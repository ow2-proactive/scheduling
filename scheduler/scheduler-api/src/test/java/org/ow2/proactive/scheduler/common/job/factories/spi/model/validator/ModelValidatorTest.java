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

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ModelSyntaxException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ValidationException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.factory.*;


public class ModelValidatorTest {

    @Test
    public void testModelValidatorNotHandled() throws ValidationException {
        ModelValidator validator = new ModelValidator("Unknown");
        String parameterValue = "Any Value";
        Assert.assertEquals(parameterValue, validator.validate(parameterValue, null));
    }

    @Test(expected = ValidationException.class)
    public void testModelValidatorValidPrefixButUnknownType() throws ValidationException {
        ModelValidator validator = new ModelValidator(ModelValidator.PREFIX + "Unknown");
        String parameterValue = "Any Value";
        validator.validate(parameterValue, null);
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("squid:S1848")
    public void testModelValidatorEmpty() {
        new ModelValidator(null);
    }

    @Test
    public void testModelValidatorInteger() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.INTEGER, IntegerParserValidator.class);
    }

    @Test
    public void testModelValidatorLong() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.LONG, LongParserValidator.class);
    }

    @Test
    public void testModelValidatorShort() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.SHORT, ShortParserValidator.class);
    }

    @Test
    public void testModelValidatorFloat() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.FLOAT, FloatParserValidator.class);
    }

    @Test
    public void testModelValidatorDouble() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.DOUBLE, DoubleParserValidator.class);
    }

    @Test
    public void testModelValidatorBoolean() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.BOOLEAN, BooleanParserValidator.class);
    }

    @Test
    public void testModelValidatorURI() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.URI, URIParserValidator.class);
    }

    @Test
    public void testModelValidatorURL() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.URL, URLParserValidator.class);
    }

    @Test
    public void testModelValidatorCRON() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.CRON, CRONParserValidator.class);
    }

    @Test
    public void testModelValidatorDatetime() throws ModelSyntaxException {
        String exampleDatetimeModel = ModelType.DATETIME + "(yyyy-MM-dd)";
        createAndCheckValidator(ModelValidator.PREFIX + exampleDatetimeModel, DateTimeParserValidator.class);
    }

    @Test
    public void testModelValidatorList() throws ModelSyntaxException {
        String exampleListModel = ModelType.LIST + "(a,b,c)";
        createAndCheckValidator(ModelValidator.PREFIX + exampleListModel, ListParserValidator.class);
    }

    @Test
    public void testModelValidatorRegexp() throws ModelSyntaxException {
        String exampleRegexpModel = ModelType.REGEXP + "([a-z]+)";
        createAndCheckValidator(ModelValidator.PREFIX + exampleRegexpModel, RegexpParserValidator.class);
    }

    @Test
    public void testSpelValidatorRegexp() throws ModelSyntaxException {
        String exampleSpelModel = ModelType.SPEL + "(#value == 'abc')";
        createAndCheckValidator(ModelValidator.PREFIX + exampleSpelModel, SPELParserValidator.class);
    }

    @Test
    public void testModelValidatorModelFromURL() throws ModelSyntaxException {
        String exampleSpelModel = ModelType.MODEL_FROM_URL + "(file:///srv/machines_list_model.txt)";
        createAndCheckValidator(ModelValidator.PREFIX + exampleSpelModel, ModelFromURLParserValidator.class);
    }

    @Test
    public void testModelValidatorCatalogObject() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.CATALOG_OBJECT, CatalogObjectParserValidator.class);
    }

    @Test
    public void testModelValidatorJSON() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.JSON, JSONParserValidator.class);
    }

    @Test
    public void testModelValidatorCredential() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.CREDENTIAL, CredentialParserValidator.class);
    }

    @Test
    public void testModelValidatorHidden() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.HIDDEN, HiddenParserValidator.class);
    }

    @Test
    public void testModelValidatorUserFile() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.USER_FILE, UserFileParserValidator.class);
    }

    @Test
    public void testModelValidatorGlobalFile() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.GLOBAL_FILE, GlobalFileParserValidator.class);
    }

    @Test
    public void testModelValidatorNotEmpty() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.NOT_EMPTY_STRING, NotEmptyParserValidator.class);
    }

    @Test(expected = ValidationException.class)
    public void testModelValidatorValidPrefixButUnknownTypeOptional() throws ValidationException {
        ModelValidator validator = new ModelValidator(ModelValidator.PREFIX + "Unknown" +
                                                      ModelValidator.OPTIONAL_VARIABLE_SUFFIX);
        String parameterValue = "Any Value";
        validator.validate(parameterValue, null);
    }

    @Test
    public void testModelValidatorIntegerOptional() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.INTEGER + ModelValidator.OPTIONAL_VARIABLE_SUFFIX,
                                OptionalParserValidator.class);
    }

    @Test
    public void testModelValidatorLongOptional() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.LONG + ModelValidator.OPTIONAL_VARIABLE_SUFFIX,
                                OptionalParserValidator.class);
    }

    @Test
    public void testModelValidatorShortOptional() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.SHORT + ModelValidator.OPTIONAL_VARIABLE_SUFFIX,
                                OptionalParserValidator.class);
    }

    @Test
    public void testModelValidatorFloatOptional() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.FLOAT + ModelValidator.OPTIONAL_VARIABLE_SUFFIX,
                                OptionalParserValidator.class);
    }

    @Test
    public void testModelValidatorDoubleOptional() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.DOUBLE + ModelValidator.OPTIONAL_VARIABLE_SUFFIX,
                                OptionalParserValidator.class);
    }

    @Test
    public void testModelValidatorBooleanOptional() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.BOOLEAN + ModelValidator.OPTIONAL_VARIABLE_SUFFIX,
                                OptionalParserValidator.class);
    }

    @Test
    public void testModelValidatorURIOptional() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.URI + ModelValidator.OPTIONAL_VARIABLE_SUFFIX,
                                OptionalParserValidator.class);
    }

    @Test
    public void testModelValidatorURLOptional() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.URL + ModelValidator.OPTIONAL_VARIABLE_SUFFIX,
                                OptionalParserValidator.class);
    }

    @Test
    public void testModelValidatorCRONOptional() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.CRON + ModelValidator.OPTIONAL_VARIABLE_SUFFIX,
                                OptionalParserValidator.class);
    }

    @Test
    public void testModelValidatorDatetimeOptional() throws ModelSyntaxException {
        String exampleDatetimeModel = ModelType.DATETIME + "(yyyy-MM-dd)";
        createAndCheckValidator(ModelValidator.PREFIX + exampleDatetimeModel + ModelValidator.OPTIONAL_VARIABLE_SUFFIX,
                                OptionalParserValidator.class);
    }

    @Test
    public void testModelValidatorListOptional() throws ModelSyntaxException {
        String exampleListModel = ModelType.LIST + "(a,b,c)";
        createAndCheckValidator(ModelValidator.PREFIX + exampleListModel + ModelValidator.OPTIONAL_VARIABLE_SUFFIX,
                                OptionalParserValidator.class);
    }

    @Test
    public void testModelValidatorRegexpOptional() throws ModelSyntaxException {
        String exampleRegexpModel = ModelType.REGEXP + "([a-z]+)";
        createAndCheckValidator(ModelValidator.PREFIX + exampleRegexpModel + ModelValidator.OPTIONAL_VARIABLE_SUFFIX,
                                OptionalParserValidator.class);
    }

    @Test
    public void testSpelValidatorRegexpOptional() throws ModelSyntaxException {
        String exampleSpelModel = ModelType.SPEL + "(#value == 'abc')";
        createAndCheckValidator(ModelValidator.PREFIX + exampleSpelModel + ModelValidator.OPTIONAL_VARIABLE_SUFFIX,
                                OptionalParserValidator.class);
    }

    @Test
    public void testModelValidatorModelFromURLOptional() throws ModelSyntaxException {
        String exampleSpelModel = ModelType.MODEL_FROM_URL + "(file:///srv/machines_list_model.txt)";
        createAndCheckValidator(ModelValidator.PREFIX + exampleSpelModel + ModelValidator.OPTIONAL_VARIABLE_SUFFIX,
                                OptionalParserValidator.class);
    }

    @Test
    public void testModelValidatorCatalogObjectOptional() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.CATALOG_OBJECT +
                                ModelValidator.OPTIONAL_VARIABLE_SUFFIX, OptionalParserValidator.class);
    }

    @Test
    public void testModelValidatorJSONOptional() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.JSON + ModelValidator.OPTIONAL_VARIABLE_SUFFIX,
                                OptionalParserValidator.class);
    }

    @Test
    public void testModelValidatorCredentialOptional() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.CREDENTIAL + ModelValidator.OPTIONAL_VARIABLE_SUFFIX,
                                OptionalParserValidator.class);
    }

    @Test
    public void testModelValidatorHiddenOptional() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.HIDDEN + ModelValidator.OPTIONAL_VARIABLE_SUFFIX,
                                OptionalParserValidator.class);
    }

    @Test
    public void testModelValidatorUserFileOptional() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.USER_FILE + ModelValidator.OPTIONAL_VARIABLE_SUFFIX,
                                OptionalParserValidator.class);
    }

    @Test
    public void testModelValidatorGlobalFileOptional() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelType.GLOBAL_FILE + ModelValidator.OPTIONAL_VARIABLE_SUFFIX,
                                OptionalParserValidator.class);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testThatNotEmptyOptionalModelThrowException() throws ModelSyntaxException {
        String model = ModelValidator.PREFIX + ModelType.NOT_EMPTY_STRING + ModelValidator.OPTIONAL_VARIABLE_SUFFIX;
        new ModelValidator(model).createParserValidator().getClass();
    }

    public void createAndCheckValidator(String model, Class<?> expectedClass) throws ModelSyntaxException {
        ModelValidator validator = new ModelValidator(model);
        Assert.assertEquals(expectedClass, validator.createParserValidator().getClass());
        // add empty leading spaces and convert to lower case to verify trimming and case insensitive
        validator = new ModelValidator("   " + model.toLowerCase());
        Assert.assertEquals(expectedClass, validator.createParserValidator().getClass());
    }
}
