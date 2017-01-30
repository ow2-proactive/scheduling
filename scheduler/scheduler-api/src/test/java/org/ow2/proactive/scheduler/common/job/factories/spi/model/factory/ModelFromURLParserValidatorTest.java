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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ConversionException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ModelSyntaxException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ValidationException;


public class ModelFromURLParserValidatorTest {

    public static final String VALID_LIST_MODEL_PARAMETER = "(1,2,3)";

    public static final String INVALID_LIST_MODEL_PARAMETER = "(1,2,";

    public static final String INVALID_URL = "wrongurl";

    public static final String UNREACHABLE_URL = "file://this_file_does_not_exist";

    public static final String VALID_VALUE = "2";

    public static final String INVALID_VALUE = "4";

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    private String lowerCaseAndAddSpaces(String input) {
        return " " + input.toLowerCase();
    }

    private File generateModelFile(String listModelParameter) throws IOException {
        File tempFile = testFolder.newFile("model");
        FileUtils.writeStringToFile(tempFile,
                                    lowerCaseAndAddSpaces(ListParserValidator.LIST_TYPE) + listModelParameter,
                                    Charset.defaultCharset());
        return tempFile;
    }

    @Test
    public void testModelFromURLParserValidatorOK()
            throws ModelSyntaxException, ValidationException, ConversionException, IOException {
        File tempFile = generateModelFile(VALID_LIST_MODEL_PARAMETER);
        Assert.assertEquals(VALID_VALUE,
                            new ModelFromURLParserValidator(ModelFromURLParserValidator.MODEL_FROM_URL_TYPE + "(" +
                                                            tempFile.toURI().toURL() +
                                                            ")").parseAndValidate(VALID_VALUE));
    }

    @Test(expected = ValidationException.class)
    public void testModelFromURLParserValidatorKO()
            throws ModelSyntaxException, ValidationException, ConversionException, IOException {
        File tempFile = generateModelFile(VALID_LIST_MODEL_PARAMETER);
        new ModelFromURLParserValidator(ModelFromURLParserValidator.MODEL_FROM_URL_TYPE + "(" +
                                        tempFile.toURI().toURL() + ")").parseAndValidate(INVALID_VALUE);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testModelFromURLParserValidatorInvalidModel()
            throws ModelSyntaxException, ValidationException, ConversionException, IOException {
        File tempFile = generateModelFile(VALID_LIST_MODEL_PARAMETER);
        new ModelFromURLParserValidator("ILLEGAL_" + ModelFromURLParserValidator.MODEL_FROM_URL_TYPE + "(" +
                                        tempFile.toURI().toURL() + ")").parseAndValidate(VALID_VALUE);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testModelFromURLParserValidatorInvalidURL()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new ModelFromURLParserValidator(ModelFromURLParserValidator.MODEL_FROM_URL_TYPE + "(" + INVALID_URL +
                                        ")").parseAndValidate(VALID_VALUE);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testModelFromURLParserValidatorUnreachableURL()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new ModelFromURLParserValidator(ModelFromURLParserValidator.MODEL_FROM_URL_TYPE + "(" + UNREACHABLE_URL +
                                        ")").parseAndValidate(VALID_VALUE);
    }

    @Test(expected = ModelSyntaxException.class)
    public void testModelFromURLParserValidatorInvalidModelInFile()
            throws ModelSyntaxException, ValidationException, ConversionException, IOException {
        File tempFile = generateModelFile(INVALID_LIST_MODEL_PARAMETER);
        new ModelFromURLParserValidator("ILLEGAL_" + ModelFromURLParserValidator.MODEL_FROM_URL_TYPE + "(" +
                                        tempFile.toURI().toURL() + ")").parseAndValidate(VALID_VALUE);
    }
}
