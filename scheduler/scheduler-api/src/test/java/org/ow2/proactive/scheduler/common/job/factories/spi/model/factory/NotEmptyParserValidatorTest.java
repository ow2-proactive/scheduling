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

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ConversionException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ModelSyntaxException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ValidationException;


/**
 * @author ActiveEon Team
 * @since 19/08/19
 */
public class NotEmptyParserValidatorTest {

    @Test
    public void testNotEmptyParserValidatorOK() throws ModelSyntaxException, ValidationException, ConversionException {
        String value = "my string foo";
        Assert.assertEquals(value,
                            new NotEmptyParserValidator(NotEmptyParserValidator.NOT_EMPTY_TYPE).parseAndValidate(value));
    }

    @Test(expected = ValidationException.class)
    public void testNotEmptyParserValidatorDisallowEmptyString()
            throws ModelSyntaxException, ValidationException, ConversionException {
        String value = "";
        new NotEmptyParserValidator(NotEmptyParserValidator.NOT_EMPTY_TYPE).parseAndValidate(value);
    }

    @Test(expected = ValidationException.class)
    public void testNotEmptyParserValidatorDisallowBlankString()
            throws ModelSyntaxException, ValidationException, ConversionException {
        String value = " ";
        new NotEmptyParserValidator(NotEmptyParserValidator.NOT_EMPTY_TYPE).parseAndValidate(value);
    }

    @Test
    public void testNotEmptyModelCaseInsensitive()
            throws ModelSyntaxException, ValidationException, ConversionException {
        String testModel = "NoT_EmPtY";
        String value = "my string foo";
        Assert.assertEquals(value, new NotEmptyParserValidator(testModel).parseAndValidate(value));
    }

    @Test(expected = ModelSyntaxException.class)
    public void testNotEmptyInvalidModel() throws ModelSyntaxException, ValidationException, ConversionException {
        String testModel = "NOT-EMPTY";
        String value = "my string foo";
        new NotEmptyParserValidator(testModel).parseAndValidate(value);
    }
}
