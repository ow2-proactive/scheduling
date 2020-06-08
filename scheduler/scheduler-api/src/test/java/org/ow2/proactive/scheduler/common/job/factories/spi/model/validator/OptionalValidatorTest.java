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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ow2.proactive.scheduler.common.SchedulerConstants.GLOBALSPACE_NAME;
import static org.ow2.proactive.scheduler.common.SchedulerConstants.USERSPACE_NAME;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.SchedulerSpaceInterface;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.ModelValidatorContext;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ValidationException;


public class OptionalValidatorTest {
    final String existGlobalFilePath = "global folder/exist-file.txt";

    final String notExistGlobalFilePath = "not-exist-global-file.png";

    final String existUserFilePath = "my folder/exist-file.txt";

    final String notExistUserFilePath = "not-exist-user-file.png";

    @Test
    public void testAllowBlankOK() throws Exception {
        String blank = " ";
        OptionalValidator<String> onlyBlankValidator = new OptionalValidator<>(new DisallowAllValidator());
        Assert.assertEquals(blank, onlyBlankValidator.validate(blank, null));
    }

    @Test
    public void testAllowBlankEmptyOK() throws Exception {
        String blank = "";
        OptionalValidator<String> onlyBlankValidator = new OptionalValidator<>(new DisallowAllValidator());
        Assert.assertEquals(blank, onlyBlankValidator.validate(blank, null));
    }

    @Test
    public void testAllowBlankTabOK() throws Exception {
        String blank = "\t";
        OptionalValidator<String> onlyBlankValidator = new OptionalValidator<>(new DisallowAllValidator());
        Assert.assertEquals(blank, onlyBlankValidator.validate(blank, null));
    }

    @Test(expected = ValidationException.class)
    public void testNotBlankValueDisallowed() throws Exception {
        String notBlank = " any string ";
        OptionalValidator<String> onlyBlankValidator = new OptionalValidator<>(new DisallowAllValidator());
        onlyBlankValidator.validate(notBlank, null);
    }

    @Test
    public void testOptionalURLBlankOK() throws Exception {
        String blank = "  ";
        OptionalValidator<String> optionalURLValidator = new OptionalValidator<>(new URLValidator());
        Assert.assertEquals(blank, optionalURLValidator.validate(blank, null));
    }

    @Test
    public void testOptionalURLValidOK() throws Exception {
        String validURL = "http://mysite?myparam=1";
        OptionalValidator<String> optionalURLValidator = new OptionalValidator<>(new URLValidator());
        Assert.assertEquals(validURL, optionalURLValidator.validate(validURL, null));
    }

    @Test(expected = ValidationException.class)
    public void testOptionalURLInvalidKO() throws Exception {
        String invalidURL = "unknown://mysite.com";
        OptionalValidator<String> optionalURLValidator = new OptionalValidator<>(new URLValidator());
        optionalURLValidator.validate(invalidURL, null);
    }

    @Test
    public void testOptionalURIBlankOK() throws Exception {
        String blank = "  ";
        OptionalValidator<String> optionalURIValidator = new OptionalValidator<>(new URIValidator());
        Assert.assertEquals(blank, optionalURIValidator.validate(blank, null));
    }

    @Test
    public void testOptionalURIValidOK() throws Exception {
        String validURI = "c:/toto";
        OptionalValidator<String> optionalURIValidator = new OptionalValidator<>(new URIValidator());
        Assert.assertEquals(validURI, optionalURIValidator.validate(validURI, null));
    }

    @Test(expected = ValidationException.class)
    public void testOptionalURIInvalidKO() throws Exception {
        String invalidURI = "invalid%uri";
        OptionalValidator<String> optionalURIValidator = new OptionalValidator<>(new URIValidator());
        optionalURIValidator.validate(invalidURI, null);
    }

    @Test
    public void testOptionalGlobalFileBlankOK() throws Exception {
        String blank = "  ";
        OptionalValidator<String> optionalGlobalFileValidator = new OptionalValidator<>(new GlobalFileValidator());
        Assert.assertEquals(blank, optionalGlobalFileValidator.validate(blank, null));
    }

    @Test
    public void testOptionalGlobalFileValidOK() throws Exception {
        OptionalValidator<String> optionalGlobalFileValidator = new OptionalValidator<>(new GlobalFileValidator());
        Assert.assertEquals(existGlobalFilePath,
                            optionalGlobalFileValidator.validate(existGlobalFilePath, mockContext()));
    }

    @Test(expected = ValidationException.class)
    public void testOptionalGlobalFileInvalidKO() throws Exception {
        OptionalValidator<String> optionalGlobalFileValidator = new OptionalValidator<>(new GlobalFileValidator());
        optionalGlobalFileValidator.validate(notExistGlobalFilePath, mockContext());
    }

    @Test
    public void testOptionalUserFileBlankOK() throws Exception {
        String blank = "  ";
        OptionalValidator<String> optionalUserFileValidator = new OptionalValidator<>(new UserFileValidator());
        Assert.assertEquals(blank, optionalUserFileValidator.validate(blank, null));
    }

    @Test
    public void testOptionalUserFileValidOK() throws Exception {
        OptionalValidator<String> optionalUserFileValidator = new OptionalValidator<>(new UserFileValidator());
        Assert.assertEquals(existUserFilePath, optionalUserFileValidator.validate(existUserFilePath, mockContext()));
    }

    @Test(expected = ValidationException.class)
    public void testOptionalUserFileInvalidKO() throws Exception {
        OptionalValidator<String> optionalUserFileValidator = new OptionalValidator<>(new UserFileValidator());
        optionalUserFileValidator.validate(notExistUserFilePath, mockContext());
    }

    public ModelValidatorContext mockContext() throws NotConnectedException, PermissionException {
        ModelValidatorContext context = mock(ModelValidatorContext.class);
        SchedulerSpaceInterface schedulerSpaceInterface = mock(SchedulerSpaceInterface.class);
        when(context.getSpace()).thenReturn(schedulerSpaceInterface);
        when(schedulerSpaceInterface.checkFileExists(GLOBALSPACE_NAME, existGlobalFilePath)).thenReturn(true);
        when(schedulerSpaceInterface.checkFileExists(GLOBALSPACE_NAME, notExistGlobalFilePath)).thenReturn(false);
        when(schedulerSpaceInterface.checkFileExists(USERSPACE_NAME, existUserFilePath)).thenReturn(true);
        when(schedulerSpaceInterface.checkFileExists(USERSPACE_NAME, notExistUserFilePath)).thenReturn(false);
        return context;
    }

    static class DisallowAllValidator implements Validator<String> {
        @Override
        public String validate(String parameterValue, ModelValidatorContext context) throws ValidationException {
            throw new ValidationException();
        }
    }
}
