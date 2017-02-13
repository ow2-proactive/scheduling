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
    public void testModelValidatorNotHandled() throws ModelSyntaxException, ValidationException {
        ModelValidator validator = new ModelValidator("Unknown");
        String parameterValue = "Any Value";
        Assert.assertEquals(parameterValue, validator.validate(parameterValue));
    }

    @Test(expected = ValidationException.class)
    public void testModelValidatorValidPrefixButUnknownType() throws ModelSyntaxException, ValidationException {
        ModelValidator validator = new ModelValidator(ModelValidator.PREFIX + "Unknown");
        String parameterValue = "Any Value";
        validator.validate(parameterValue);
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("squid:S1848")
    public void testModelValidatorEmpty() throws ModelSyntaxException, ValidationException {
        new ModelValidator(null);
    }

    @Test
    public void testModelValidatorInteger() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + IntegerParserValidator.INTEGER_TYPE,
                                IntegerParserValidator.class);
    }

    @Test
    public void testModelValidatorLong() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + LongParserValidator.LONG_TYPE, LongParserValidator.class);
    }

    @Test
    public void testModelValidatorShort() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ShortParserValidator.SHORT_TYPE, ShortParserValidator.class);
    }

    @Test
    public void testModelValidatorFloat() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + FloatParserValidator.FLOAT_TYPE, FloatParserValidator.class);
    }

    @Test
    public void testModelValidatorDouble() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + DoubleParserValidator.DOUBLE_TYPE, DoubleParserValidator.class);
    }

    @Test
    public void testModelValidatorBoolean() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + BooleanParserValidator.BOOLEAN_TYPE,
                                BooleanParserValidator.class);
    }

    @Test
    public void testModelValidatorURI() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + URIParserValidator.URI_TYPE, URIParserValidator.class);
    }

    @Test
    public void testModelValidatorURL() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + URLParserValidator.URL_TYPE, URLParserValidator.class);
    }

    @Test
    public void testModelValidatorDatetime() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + DateTimeParserValidator.DATETIME_TYPE,
                                DateTimeParserValidator.class);
    }

    @Test
    public void testModelValidatorList() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ListParserValidator.LIST_TYPE, ListParserValidator.class);
    }

    @Test
    public void testModelValidatorRegexp() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + RegexpParserValidator.REGEXP_TYPE, RegexpParserValidator.class);
    }

    @Test
    public void testModelValidatorModelFromURL() throws ModelSyntaxException {
        createAndCheckValidator(ModelValidator.PREFIX + ModelFromURLParserValidator.MODEL_FROM_URL_TYPE,
                                ModelFromURLParserValidator.class);
    }

    public void createAndCheckValidator(String model, Class expectedClass) throws ModelSyntaxException {
        ModelValidator validator = new ModelValidator(model);
        Assert.assertEquals(expectedClass, validator.createParserValidator().getClass());
        // add empty leading spaces and convert to lower case to verify trimming and case insensitive
        validator = new ModelValidator("   " + model.toLowerCase());
        Assert.assertEquals(expectedClass, validator.createParserValidator().getClass());
    }
}
