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

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ConversionException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ModelSyntaxException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ValidationException;


public class DateTimeParserValidatorTest {

    private static final String VALID_DATE_FORMAT = "yyyy-M-d";
    private static final String INVALID_DATE_FORMAT = "aa-bb-cc";
    private static final String VALID_DATE = "2014-01-01";
    private static final String VALID_LOWER_RANGE = "2013-01-01";
    private static final String VALID_UPPER_RANGE = "2015-01-01";
    private static final String VALID_DATE_OUT_OF_RANGE = "2012-12-24";
    private static final String INVALID_LOWER_RANGE = "2013";
    private static final String INVALID_UPPER_RANGE = "2015";
    private static final String INVALID_DATE = "2014";

    @Test
    public void testDateTimeParserValidatorOK()
            throws ModelSyntaxException, ValidationException, ConversionException, ParseException {
        Assert.assertEquals(new SimpleDateFormat(VALID_DATE_FORMAT).parse(VALID_DATE),
                            new DateTimeParserValidator(DateTimeParserValidator.DATETIME_TYPE +
                                                        DateTimeParserValidator.LEFT_DELIMITER + VALID_DATE_FORMAT +
                                                        DateTimeParserValidator.RIGHT_DELIMITER).parseAndValidate(VALID_DATE));
    }

    @Test(expected = ConversionException.class)
    public void testDateTimeParserValidatorKO() throws ModelSyntaxException, ValidationException, ConversionException {
        new DateTimeParserValidator(DateTimeParserValidator.DATETIME_TYPE + DateTimeParserValidator.LEFT_DELIMITER +
                                    VALID_DATE_FORMAT +
                                    DateTimeParserValidator.RIGHT_DELIMITER).parseAndValidate(INVALID_DATE);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testDateTimeParserValidatorInvalidDateFormat()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new DateTimeParserValidator(DateTimeParserValidator.DATETIME_TYPE + DateTimeParserValidator.LEFT_DELIMITER +
                                    INVALID_DATE_FORMAT +
                                    DateTimeParserValidator.RIGHT_DELIMITER).parseAndValidate(VALID_DATE);
    }

    @Test
    public void testDateTimeParserValidatorLowerRangeOK()
            throws ModelSyntaxException, ValidationException, ConversionException, ParseException {
        Assert.assertEquals(new SimpleDateFormat(VALID_DATE_FORMAT).parse(VALID_DATE),
                            new DateTimeParserValidator(DateTimeParserValidator.DATETIME_TYPE +
                                                        DateTimeParserValidator.LEFT_DELIMITER + VALID_DATE_FORMAT +
                                                        DateTimeParserValidator.RIGHT_DELIMITER +
                                                        RangeParserValidator.LEFT_RANGE_DELIMITER + VALID_LOWER_RANGE +
                                                        RangeParserValidator.RIGHT_RANGE_DELIMITER).parseAndValidate(VALID_DATE));
    }

    @Test(expected = ValidationException.class)
    public void testDateTimeParserValidatorLowerRangeKO()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new DateTimeParserValidator(DateTimeParserValidator.DATETIME_TYPE + DateTimeParserValidator.LEFT_DELIMITER +
                                    VALID_DATE_FORMAT + DateTimeParserValidator.RIGHT_DELIMITER +
                                    RangeParserValidator.LEFT_RANGE_DELIMITER + VALID_LOWER_RANGE +
                                    RangeParserValidator.RIGHT_RANGE_DELIMITER).parseAndValidate(VALID_DATE_OUT_OF_RANGE);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testDateTimeParserValidatorInvalidLowerRange()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new DateTimeParserValidator(DateTimeParserValidator.DATETIME_TYPE + DateTimeParserValidator.LEFT_DELIMITER +
                                    VALID_DATE_FORMAT + DateTimeParserValidator.RIGHT_DELIMITER +
                                    RangeParserValidator.LEFT_RANGE_DELIMITER + INVALID_LOWER_RANGE +
                                    RangeParserValidator.RIGHT_RANGE_DELIMITER).parseAndValidate(VALID_DATE);
    }

    @Test
    public void testDateTimeParserValidatorIntervalOK()
            throws ModelSyntaxException, ValidationException, ConversionException, ParseException {
        Assert.assertEquals(new SimpleDateFormat(VALID_DATE_FORMAT).parse(VALID_DATE),
                            new DateTimeParserValidator(DateTimeParserValidator.DATETIME_TYPE +
                                                        DateTimeParserValidator.LEFT_DELIMITER + VALID_DATE_FORMAT +
                                                        DateTimeParserValidator.RIGHT_DELIMITER +
                                                        RangeParserValidator.LEFT_RANGE_DELIMITER + VALID_LOWER_RANGE +
                                                        "," + VALID_UPPER_RANGE +
                                                        RangeParserValidator.RIGHT_RANGE_DELIMITER).parseAndValidate(VALID_DATE));
    }

    @Test(expected = ValidationException.class)
    public void testDateTimeParserValidatorIntervalKO()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new DateTimeParserValidator(DateTimeParserValidator.DATETIME_TYPE + DateTimeParserValidator.LEFT_DELIMITER +
                                    VALID_DATE_FORMAT + DateTimeParserValidator.RIGHT_DELIMITER +
                                    RangeParserValidator.LEFT_RANGE_DELIMITER +
                                    VALID_LOWER_RANGE + "," + VALID_UPPER_RANGE +
                                    RangeParserValidator.RIGHT_RANGE_DELIMITER).parseAndValidate(VALID_DATE_OUT_OF_RANGE);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testDateTimeParserValidatorInvalidInterval()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new DateTimeParserValidator(DateTimeParserValidator.DATETIME_TYPE + DateTimeParserValidator.LEFT_DELIMITER +
                                    VALID_DATE_FORMAT + DateTimeParserValidator.RIGHT_DELIMITER +
                                    RangeParserValidator.LEFT_RANGE_DELIMITER + VALID_LOWER_RANGE + "," +
                                    INVALID_UPPER_RANGE +
                                    RangeParserValidator.RIGHT_RANGE_DELIMITER).parseAndValidate(VALID_DATE);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testDateTimeParserValidatorInvalidModel()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new DateTimeParserValidator("ILLEGAL_" + DateTimeParserValidator.DATETIME_TYPE).parseAndValidate(VALID_DATE);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testDateTimeParserValidatorInvalidModelRangeMoreThanTwo()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new DateTimeParserValidator(DateTimeParserValidator.DATETIME_TYPE + DateTimeParserValidator.LEFT_DELIMITER +
                                    VALID_DATE_FORMAT + DateTimeParserValidator.RIGHT_DELIMITER +
                                    RangeParserValidator.LEFT_RANGE_DELIMITER +
                                    VALID_LOWER_RANGE + "," + VALID_UPPER_RANGE + "," + VALID_UPPER_RANGE +
                                    RangeParserValidator.RIGHT_RANGE_DELIMITER).parseAndValidate(VALID_DATE);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testDateTimeParserValidatorInvalidModelRangeEmpty()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new DateTimeParserValidator(DateTimeParserValidator.DATETIME_TYPE + DateTimeParserValidator.LEFT_DELIMITER +
                                    VALID_DATE_FORMAT + DateTimeParserValidator.RIGHT_DELIMITER +
                                    RangeParserValidator.LEFT_RANGE_DELIMITER +
                                    RangeParserValidator.RIGHT_RANGE_DELIMITER).parseAndValidate(VALID_DATE);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testDateTimeParserValidatorInvalidModelRangeReverse()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new DateTimeParserValidator(DateTimeParserValidator.DATETIME_TYPE + DateTimeParserValidator.LEFT_DELIMITER +
                                    VALID_DATE_FORMAT + DateTimeParserValidator.RIGHT_DELIMITER +
                                    RangeParserValidator.LEFT_RANGE_DELIMITER + VALID_UPPER_RANGE + "," +
                                    VALID_LOWER_RANGE +
                                    RangeParserValidator.RIGHT_RANGE_DELIMITER).parseAndValidate(VALID_DATE);
    }
}
