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


public class DoubleParserValidatorTest {

    private static final double VALID_DOUBLE = 10.1;

    private static final String VALID_DOUBLE_STRING = "" + VALID_DOUBLE;

    private static final String VALID_LOWER_RANGE = "5.1";

    private static final String VALID_UPPER_RANGE = "20.1";

    private static final double VALID_DOUBLE_OUT_OF_RANGE = 0.1;

    private static final String VALID_DOUBLE_OUT_OF_RANGE_STRING = "" + VALID_DOUBLE_OUT_OF_RANGE;

    private static final String INVALID_LOWER_RANGE = "a";

    private static final String INVALID_UPPER_RANGE = "b";

    private static final String INVALID_DOUBLE_STRING = "aa";

    @Test
    public void testDoubleParserValidatorOK() throws ModelSyntaxException, ValidationException, ConversionException {
        Assert.assertEquals(VALID_DOUBLE,
                            new DoubleParserValidator(DoubleParserValidator.DOUBLE_TYPE).parseAndValidate(VALID_DOUBLE_STRING),
                            0.0);
    }

    @Test(expected = ConversionException.class)
    public void testDoubleParserValidatorKO() throws ModelSyntaxException, ValidationException, ConversionException {
        new DoubleParserValidator(DoubleParserValidator.DOUBLE_TYPE).parseAndValidate(INVALID_DOUBLE_STRING);
    }

    @Test
    public void testDoubleParserValidatorLowerRangeOK()
            throws ModelSyntaxException, ValidationException, ConversionException {
        Assert.assertEquals(VALID_DOUBLE,
                            new DoubleParserValidator(DoubleParserValidator.DOUBLE_TYPE +
                                                      RangeParserValidator.LEFT_RANGE_DELIMITER + VALID_LOWER_RANGE +
                                                      RangeParserValidator.RIGHT_RANGE_DELIMITER).parseAndValidate(VALID_DOUBLE_STRING),
                            0.0);
    }

    @Test(expected = ValidationException.class)
    @SuppressWarnings("squid:S1848")
    public void testDoubleParserValidatorLowerRangeKO()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new DoubleParserValidator(DoubleParserValidator.DOUBLE_TYPE + RangeParserValidator.LEFT_RANGE_DELIMITER +
                                  VALID_LOWER_RANGE +
                                  RangeParserValidator.RIGHT_RANGE_DELIMITER).parseAndValidate(VALID_DOUBLE_OUT_OF_RANGE_STRING);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testDoubleParserValidatorInvalidLowerRange()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new DoubleParserValidator(DoubleParserValidator.DOUBLE_TYPE + RangeParserValidator.LEFT_RANGE_DELIMITER +
                                  INVALID_LOWER_RANGE +
                                  RangeParserValidator.RIGHT_RANGE_DELIMITER).parseAndValidate(VALID_DOUBLE_STRING);
    }

    @Test
    public void testDoubleParserValidatorIntervalOK()
            throws ModelSyntaxException, ValidationException, ConversionException {
        Assert.assertEquals(VALID_DOUBLE,
                            new DoubleParserValidator(DoubleParserValidator.DOUBLE_TYPE +
                                                      RangeParserValidator.LEFT_RANGE_DELIMITER + VALID_LOWER_RANGE +
                                                      "," + VALID_UPPER_RANGE +
                                                      RangeParserValidator.RIGHT_RANGE_DELIMITER).parseAndValidate(VALID_DOUBLE_STRING),
                            0.0);
    }

    @Test(expected = ValidationException.class)
    public void testDoubleParserValidatorIntervalKO()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new DoubleParserValidator(DoubleParserValidator.DOUBLE_TYPE + RangeParserValidator.LEFT_RANGE_DELIMITER +
                                  VALID_LOWER_RANGE + "," + VALID_UPPER_RANGE +
                                  RangeParserValidator.RIGHT_RANGE_DELIMITER).parseAndValidate(VALID_DOUBLE_OUT_OF_RANGE_STRING);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testDoubleParserValidatorInvalidInterval()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new DoubleParserValidator(DoubleParserValidator.DOUBLE_TYPE + RangeParserValidator.LEFT_RANGE_DELIMITER +
                                  VALID_LOWER_RANGE + "," + INVALID_UPPER_RANGE +
                                  RangeParserValidator.RIGHT_RANGE_DELIMITER).parseAndValidate(VALID_DOUBLE_STRING);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testDoubleParserValidatorInvalidModel()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new DoubleParserValidator("ILLEGAL_" + DoubleParserValidator.DOUBLE_TYPE).parseAndValidate(VALID_DOUBLE_STRING);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testDoubleParserValidatorInvalidModelRangeMoreThanTwo()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new DoubleParserValidator(DoubleParserValidator.DOUBLE_TYPE + RangeParserValidator.LEFT_RANGE_DELIMITER +
                                  VALID_LOWER_RANGE + "," +
                                  VALID_UPPER_RANGE + "," + VALID_UPPER_RANGE +
                                  RangeParserValidator.RIGHT_RANGE_DELIMITER).parseAndValidate(VALID_DOUBLE_STRING);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testDoubleParserValidatorInvalidModelRangeEmpty()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new DoubleParserValidator(DoubleParserValidator.DOUBLE_TYPE + "()").parseAndValidate(VALID_DOUBLE_STRING);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testDoubleParserValidatorInvalidModelRangeInverse()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new DoubleParserValidator(DoubleParserValidator.DOUBLE_TYPE + RangeParserValidator.LEFT_RANGE_DELIMITER +
                                  VALID_UPPER_RANGE + "," + VALID_LOWER_RANGE +
                                  RangeParserValidator.RIGHT_RANGE_DELIMITER).parseAndValidate(VALID_DOUBLE_STRING);
    }
}
