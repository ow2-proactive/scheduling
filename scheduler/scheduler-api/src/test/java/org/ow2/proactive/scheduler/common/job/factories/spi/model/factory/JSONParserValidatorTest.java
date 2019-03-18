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
 * @since 06/03/2019
 */

public class JSONParserValidatorTest {

    @Test
    public void testJSONParserValidatorOK() throws ModelSyntaxException, ValidationException, ConversionException {
        String value = "{\n" + "  \"type\": \"string\", \n" + "  \"minLength\": 3,\n" + "  \"maxLength\": 7\n" + "}";
        Assert.assertEquals(value, new JSONParserValidator(JSONParserValidator.JSON_TYPE).parseAndValidate(value));
    }

    @Test(expected = ValidationException.class)
    public void testJSONParserValidatorKO() throws ModelSyntaxException, ValidationException, ConversionException {
        String value = "\"test\" : 123";
        new JSONParserValidator(JSONParserValidator.JSON_TYPE).parseAndValidate(value);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testJSONParserValidatorInvalidModel()
            throws ModelSyntaxException, ValidationException, ConversionException {
        String value = "\"test\" : 123";
        new JSONParserValidator("catalog-type").parseAndValidate(value);
    }
}
