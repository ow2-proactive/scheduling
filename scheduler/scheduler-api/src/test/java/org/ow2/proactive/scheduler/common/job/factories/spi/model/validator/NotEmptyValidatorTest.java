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

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ValidationException;


/**
 * @author ActiveEon Team
 * @since 19/08/19
 */
public class NotEmptyValidatorTest {

    @Test
    public void testNotEmptyStringOK() throws ValidationException {
        NotEmptyValidator validator = new NotEmptyValidator();
        String value = "string foo";
        Assert.assertEquals(value, validator.validate(value, null));
    }

    @Test
    public void testNotEmptyStringWithSpacesOK() throws ValidationException {
        NotEmptyValidator validator = new NotEmptyValidator();
        String value = " abc ";
        Assert.assertEquals(value, validator.validate(value, null));
    }

    @Test(expected = ValidationException.class)
    public void testEmptyStringDisallowed() throws ValidationException {
        NotEmptyValidator validator = new NotEmptyValidator();
        String value = "";
        validator.validate(value, null);
    }

    @Test(expected = ValidationException.class)
    public void testNullStringDisallowed() throws ValidationException {
        NotEmptyValidator validator = new NotEmptyValidator();
        String value = null;
        validator.validate(value, null);
    }

    @Test(expected = ValidationException.class)
    public void testBlankStringDisallowed() throws ValidationException {
        NotEmptyValidator validator = new NotEmptyValidator();
        String value = "      ";
        validator.validate(value, null);
    }

    @Test(expected = ValidationException.class)
    public void testTabStringDisallowed() throws ValidationException {
        NotEmptyValidator validator = new NotEmptyValidator();
        String value = "\t";
        validator.validate(value, null);
    }
}
