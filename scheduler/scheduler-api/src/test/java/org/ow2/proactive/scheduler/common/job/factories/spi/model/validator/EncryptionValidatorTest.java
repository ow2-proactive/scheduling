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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ow2.proactive.core.properties.PropertyDecrypter;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.ModelValidatorContext;
import org.ow2.proactive.scheduler.common.job.factories.spi.model.exceptions.ValidationException;


public class EncryptionValidatorTest {

    private ModelValidatorContext context;

    @Before
    public void before() {
        Map<String, Serializable> variables = new HashMap<>();
        variables.put("foo", "value");
        context = new ModelValidatorContext(variables, null, null);
        context.setVariableName("foo");
    }

    @Test
    public void testEncryptedReadOK() throws ValidationException {
        String value = "some_data";
        String encryptedValue = PropertyDecrypter.encryptData(value);
        Assert.assertEquals(encryptedValue, new EncryptionValidator().validate(encryptedValue, context));
    }

    @Test(expected = ValidationException.class)
    public void testEncryptedReadKO() throws ValidationException {
        String value = "some_data";
        String encryptedValue = PropertyDecrypter.ENCRYPTION_PREFIX +
                                PropertyDecrypter.getDefaultEncryptor().encrypt(value) + "blabla" +
                                PropertyDecrypter.ENCRYPTION_SUFFIX;
        new EncryptionValidator().validate(encryptedValue, context);
    }

    @Test
    public void testEncryptedWriteOK() throws ValidationException {
        String value = "some_data";
        String encryptedValue = new EncryptionValidator().validate(value, context);
        // decrypt returned string, which uses ENC(crypted_data) format
        Assert.assertEquals(value, PropertyDecrypter.decryptData(encryptedValue));

        // decrypt variable stored
        Map<String, Serializable> variables = context.getSpELVariables().getVariables();
        encryptedValue = (String) variables.get(context.getVariableName());
        Assert.assertEquals(value, PropertyDecrypter.decryptData(encryptedValue));
    }
}
