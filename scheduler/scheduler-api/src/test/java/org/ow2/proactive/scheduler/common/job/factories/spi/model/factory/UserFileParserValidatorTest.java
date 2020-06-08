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

import static org.mockito.Mockito.when;
import static org.ow2.proactive.scheduler.common.SchedulerConstants.USERSPACE_NAME;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ow2.proactive.scheduler.common.SchedulerSpaceInterface;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.ModelValidatorContext;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ConversionException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ModelSyntaxException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ValidationException;


public class UserFileParserValidatorTest {
    final String existUserFilePath = "my folder/exist-file.txt";

    final String notExistUserFilePath = "not-exist-file.png";

    @Mock
    private ModelValidatorContext context;

    @Mock
    private SchedulerSpaceInterface schedulerSpaceInterface;

    @Before
    public void init() throws NotConnectedException, PermissionException {
        MockitoAnnotations.initMocks(this);
        when(context.getSpace()).thenReturn(schedulerSpaceInterface);
        when(schedulerSpaceInterface.checkFileExists(USERSPACE_NAME, existUserFilePath)).thenReturn(true);
        when(schedulerSpaceInterface.checkFileExists(USERSPACE_NAME, notExistUserFilePath)).thenReturn(false);
    }

    @Test
    public void testThatValidUserFileIsOK() throws ModelSyntaxException, ValidationException, ConversionException {
        String value = existUserFilePath;
        UserFileParserValidator parserValidator = new UserFileParserValidator(ModelType.USER_FILE.name());
        Assert.assertEquals(value, parserValidator.parseAndValidate(value, context));
    }

    @Test(expected = ValidationException.class)
    public void testThatInvalidUserFileThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        UserFileParserValidator parserValidator = new UserFileParserValidator(ModelType.USER_FILE.name());
        parserValidator.parseAndValidate(notExistUserFilePath, context);
    }

    @Test(expected = ValidationException.class)
    public void testThatEmptyUserFileThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        String value = "";
        UserFileParserValidator parserValidator = new UserFileParserValidator(ModelType.USER_FILE.name());
        parserValidator.parseAndValidate(value, context);
    }

    // when context is not specified, the parserValidator is expected to not check user file existence.
    @Test
    public void testThatGivenNoContextEmptyUserFileIsOK()
            throws ModelSyntaxException, ValidationException, ConversionException {
        String value = "";
        UserFileParserValidator parserValidator = new UserFileParserValidator(ModelType.USER_FILE.name());
        Assert.assertEquals(value, parserValidator.parseAndValidate(value, null));
    }

    @Test(expected = ModelSyntaxException.class)
    public void testThatInvalidModelThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new UserFileParserValidator("USER_FILEE").parseAndValidate("blabla");
    }
}
