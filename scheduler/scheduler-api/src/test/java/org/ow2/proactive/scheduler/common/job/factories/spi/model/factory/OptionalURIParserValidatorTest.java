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

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ConversionException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ModelSyntaxException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ValidationException;


public class OptionalURIParserValidatorTest {

    @Test
    public void testThatValidURIIsOK()
            throws ModelSyntaxException, ValidationException, ConversionException, URISyntaxException {
        String value = "/my/file";
        OptionalURIParserValidator parserValidator = new OptionalURIParserValidator(ModelType.OPTIONAL_URI.name());
        Assert.assertEquals(new URI(value).toString(), parserValidator.parseAndValidate(value));
    }

    @Test(expected = ValidationException.class)
    public void testThatInvalidURIThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new OptionalURIParserValidator(ModelType.OPTIONAL_URI.name()).parseAndValidate("\\&¨^¨$ù%");
    }

    @Test
    public void testThatBlankURIOK() throws ModelSyntaxException, ValidationException, ConversionException {
        String value = " ";
        OptionalURIParserValidator parserValidator = new OptionalURIParserValidator(ModelType.OPTIONAL_URI.name());
        Assert.assertEquals(value, parserValidator.parseAndValidate(value));
    }

    @Test(expected = ModelSyntaxException.class)
    public void testThatInvalidModelThrowException()
            throws ModelSyntaxException, ValidationException, ConversionException {
        new OptionalURIParserValidator("OPTIONAL_URY").parseAndValidate("blabla");
    }
}
