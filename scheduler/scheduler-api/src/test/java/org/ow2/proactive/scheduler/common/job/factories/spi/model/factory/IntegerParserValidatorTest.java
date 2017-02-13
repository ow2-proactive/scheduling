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


public class IntegerParserValidatorTest {

    private static final int VALID_INTEGER = 10;

    private static final String VALID_INTEGER_STRING = "" + VALID_INTEGER;

    private static final String VALID_LOWER_RANGE = "5";

    private static final String VALID_UPPER_RANGE = "20";

    private static final int VALID_INTEGER_OUT_OF_RANGE = 0;

    private static final String VALID_INTEGER_OUT_OF_RANGE_STRING = "" + VALID_INTEGER_OUT_OF_RANGE;

    private static final String INVALID_LOWER_RANGE = "5.1";

    private static final String INVALID_UPPER_RANGE = "20.1";

    private static final double INVALID_INTEGER = 10.1;

    private static final String INVALID_INTEGER_STRING = "" + INVALID_INTEGER;

    @Test
    public void testIntegerParserValidatorOK() throws ModelSyntaxException, ValidationException, ConversionException {
        Assert.assertEquals(VALID_INTEGER,
                            (int) new IntegerParserValidator(IntegerParserValidator.INTEGER_TYPE).parseAndValidate(VALID_INTEGER_STRING));
    }

    @Test(expected = ConversionException.class)
    public void testIntegerParserValidatorKO() throws ModelSyntaxException, ValidationException, ConversionException {
        new IntegerParserValidator(IntegerParserValidator.INTEGER_TYPE).parseAndValidate(INVALID_INTEGER_STRING);
    }

    @Test
    public void testIntegerParserValidatorLowerRangeOK()
            throws ModelSyntaxException, ValidationException, ConversionException {
        Assert.assertEquals(VALID_INTEGER,
                            (int) new IntegerParserValidator(IntegerParserValidator.INTEGER_TYPE +
                                                             RangeParserValidator.LEFT_RANGE_DELIMITER +
                                                             VALID_LOWER_RANGE +
                                                             RangeParserValidator.RIGHT_RANGE_DELIMITER).parseAndValidate(VALID_INTEGER_STRING));
    }

    @Test(expected = ValidationException.class)
    public void testIntegerParserValidatorLowerRangeKO()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new IntegerParserValidator(IntegerParserValidator.INTEGER_TYPE + RangeParserValidator.LEFT_RANGE_DELIMITER +
                                   VALID_LOWER_RANGE +
                                   RangeParserValidator.RIGHT_RANGE_DELIMITER).parseAndValidate(VALID_INTEGER_OUT_OF_RANGE_STRING);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testIntegerParserValidatorInvalidLowerRange()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new IntegerParserValidator(IntegerParserValidator.INTEGER_TYPE + RangeParserValidator.LEFT_RANGE_DELIMITER +
                                   INVALID_LOWER_RANGE +
                                   RangeParserValidator.RIGHT_RANGE_DELIMITER).parseAndValidate(VALID_INTEGER_STRING);
    }

    @Test
    public void testIntegerParserValidatorIntervalOK()
            throws ModelSyntaxException, ValidationException, ConversionException {
        Assert.assertEquals(VALID_INTEGER,
                            (int) new IntegerParserValidator(IntegerParserValidator.INTEGER_TYPE +
                                                             RangeParserValidator.LEFT_RANGE_DELIMITER +
                                                             VALID_LOWER_RANGE + "," + VALID_UPPER_RANGE +
                                                             RangeParserValidator.RIGHT_RANGE_DELIMITER).parseAndValidate(VALID_INTEGER_STRING));
    }

    @Test(expected = ValidationException.class)
    public void testIntegerParserValidatorIntervalKO()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new IntegerParserValidator(IntegerParserValidator.INTEGER_TYPE + RangeParserValidator.LEFT_RANGE_DELIMITER +
                                   VALID_LOWER_RANGE + "," + VALID_UPPER_RANGE +
                                   RangeParserValidator.RIGHT_RANGE_DELIMITER).parseAndValidate(VALID_INTEGER_OUT_OF_RANGE_STRING);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testIntegerParserValidatorInvalidInterval()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new IntegerParserValidator(IntegerParserValidator.INTEGER_TYPE + RangeParserValidator.LEFT_RANGE_DELIMITER +
                                   VALID_LOWER_RANGE + "," + INVALID_UPPER_RANGE +
                                   RangeParserValidator.RIGHT_RANGE_DELIMITER).parseAndValidate(VALID_INTEGER_STRING);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testIntegerParserValidatorInvalidModel()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new IntegerParserValidator("ILLEGAL_" +
                                   IntegerParserValidator.INTEGER_TYPE).parseAndValidate(VALID_INTEGER_STRING);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testIntegerParserValidatorInvalidModelRangeInverse()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new IntegerParserValidator((IntegerParserValidator.INTEGER_TYPE + RangeParserValidator.LEFT_RANGE_DELIMITER +
                                    VALID_UPPER_RANGE + "," + VALID_LOWER_RANGE +
                                    RangeParserValidator.RIGHT_RANGE_DELIMITER)).parseAndValidate(VALID_INTEGER_STRING);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testIntegerParserValidatorInvalidModelRangeEmpty()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new IntegerParserValidator((IntegerParserValidator.INTEGER_TYPE + "()")).parseAndValidate(VALID_INTEGER_STRING);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testIntegerParserValidatorInvalidModelRangeMoreThanTwo()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new IntegerParserValidator((IntegerParserValidator.INTEGER_TYPE + RangeParserValidator.LEFT_RANGE_DELIMITER +
                                    VALID_LOWER_RANGE + "," + VALID_UPPER_RANGE + "," + VALID_UPPER_RANGE +
                                    RangeParserValidator.RIGHT_RANGE_DELIMITER)).parseAndValidate(VALID_INTEGER_STRING);
    }
}
