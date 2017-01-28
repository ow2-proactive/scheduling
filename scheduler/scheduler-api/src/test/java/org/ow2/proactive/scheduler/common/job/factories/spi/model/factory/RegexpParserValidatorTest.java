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


public class RegexpParserValidatorTest {

    public static final String VALID_MODEL_PARAMETER = "([A-Za-z]+)";

    public static final String INVALID_MODEL_PARAMETER = "([A-Za-z+)";

    public static final String VALID_VALUE = "RightMate";

    public static final String INVALID_VALUE = "NotRight123";

    @Test
    public void testRegexpParserValidatorOK() throws ModelSyntaxException, ValidationException, ConversionException {
        Assert.assertEquals(VALID_VALUE,
                            new RegexpParserValidator(RegexpParserValidator.REGEXP_TYPE +
                                                      VALID_MODEL_PARAMETER).parseAndValidate(VALID_VALUE));
    }

    @Test(expected = ValidationException.class)
    public void testRegexpParserValidatorKO() throws ModelSyntaxException, ValidationException, ConversionException {
        new RegexpParserValidator(RegexpParserValidator.REGEXP_TYPE +
                                  VALID_MODEL_PARAMETER).parseAndValidate(INVALID_VALUE);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testRegexpParserValidatorInvalidModel()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new RegexpParserValidator("ILLEGAL_" + RegexpParserValidator.REGEXP_TYPE).parseAndValidate(VALID_VALUE);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testRegexpParserValidatorInvalidRegexp()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new RegexpParserValidator(RegexpParserValidator.REGEXP_TYPE +
                                  INVALID_MODEL_PARAMETER).parseAndValidate(VALID_VALUE);
    }
}
