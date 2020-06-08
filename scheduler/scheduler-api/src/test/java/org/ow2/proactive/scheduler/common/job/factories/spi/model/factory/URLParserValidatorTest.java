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

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ConversionException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ModelSyntaxException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ValidationException;


public class URLParserValidatorTest {

    @Test
    public void testThatValidURLIsOK()
            throws ModelSyntaxException, ValidationException, ConversionException, MalformedURLException {
        String value = "file://mysite";
        Assert.assertEquals(new URL(value).toExternalForm(),
                            new URLParserValidator(ModelType.URL.name()).parseAndValidate(value));
    }

    @Test(expected = ValidationException.class)
    public void testThatInvalidURLThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new URLParserValidator(ModelType.URL.name()).parseAndValidate("unknown://protocol");
    }

    @Test(expected = ModelSyntaxException.class)
    public void testThatInvalidModelThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new URLParserValidator("URLL").parseAndValidate("blabla");
    }
}
