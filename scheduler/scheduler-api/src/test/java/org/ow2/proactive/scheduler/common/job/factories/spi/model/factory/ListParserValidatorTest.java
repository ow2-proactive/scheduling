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


public class ListParserValidatorTest {

    public static final String VALID_MODEL_PARAMETER = ListParserValidator.LEFT_DELIMITER + "1,2,3" +
                                                       ListParserValidator.RIGHT_DELIMITER;

    public static final String INVALID_MODEL_PARAMETER = ListParserValidator.LEFT_DELIMITER + "1,2,3"; // missing right delimiter

    public static final String VALID_VALUE = "2";

    public static final String INVALID_VALUE = "4";

    @Test
    public void testListParserValidatorOK() throws ModelSyntaxException, ValidationException, ConversionException {
        Assert.assertEquals(VALID_VALUE,
                            new ListParserValidator(ListParserValidator.LIST_TYPE +
                                                    VALID_MODEL_PARAMETER).parseAndValidate(VALID_VALUE));
    }

    @Test(expected = ValidationException.class)
    public void testListParserValidatorKO() throws ModelSyntaxException, ValidationException, ConversionException {
        new ListParserValidator(ListParserValidator.LIST_TYPE + VALID_MODEL_PARAMETER).parseAndValidate(INVALID_VALUE);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testListParserValidatorInvalidModel()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new ListParserValidator("ILLEGAL_" + ListParserValidator.LIST_TYPE).parseAndValidate(VALID_VALUE);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testListParserValidatorInvalidList()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new ListParserValidator(ListParserValidator.LIST_TYPE + INVALID_MODEL_PARAMETER).parseAndValidate(VALID_VALUE);
    }
}
