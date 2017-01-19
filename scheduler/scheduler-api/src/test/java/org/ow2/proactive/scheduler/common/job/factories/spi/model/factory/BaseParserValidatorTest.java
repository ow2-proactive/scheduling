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

import org.junit.Assert;
import org.junit.Test;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.converter.Converter;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ModelSyntaxException;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.validator.Validator;


public class BaseParserValidatorTest {

    @Test(expected = ModelSyntaxException.class)
    @SuppressWarnings("squid:S1848")
    public void testConstructorEmpty() throws ModelSyntaxException {
        getValidator("");
    }

    @Test(expected = ModelSyntaxException.class)
    @SuppressWarnings("squid:S1848")
    public void testConstructorNull() throws ModelSyntaxException {
        getValidator(null);
    }

    @Test
    public void testConstructorTrim() throws ModelSyntaxException, MalformedURLException {
        String model = "  my string with spaces  ";
        BaseParserValidator<String> validator = getValidator(model);
        Assert.assertEquals(model.trim(), validator.model);
    }

    private BaseParserValidator<String> getValidator(String model) throws ModelSyntaxException {
        return new BaseParserValidator<String>(model) {
            @Override
            protected String getType() {
                return null;
            }

            @Override
            protected String getTypeRegexp() {
                return null;
            }

            @Override
            protected Class getClassType() {
                return null;
            }

            @Override
            protected Converter<String> createConverter(String model) throws ModelSyntaxException {
                return null;
            }

            @Override
            protected Validator<String> createValidator(String model, Converter<String> converter)
                    throws ModelSyntaxException {
                return null;
            }
        };
    }
}
