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
 * @since 12/10/2018
 */
public class CatalogObjectValidatorTest {

    @Test
    public void testCatalogObjectOKWithoutRevisionNumber() throws ValidationException {
        String value = "bucket_1/object10";
        Assert.assertEquals(value, new CatalogObjectValidator().validate(value, null));
    }

    @Test
    public void testCatalogObjectOKWithRevisionNumber() throws ValidationException {
        String value = "bucket_1/object10/1539310165443";
        Assert.assertEquals(value, new CatalogObjectValidator().validate(value, null));
    }

    @Test(expected = ValidationException.class)
    public void testCatalogObjectWithEmptyValue() throws ValidationException {
        String value = "";
        new CatalogObjectValidator().validate(value, null);
    }

    @Test(expected = ValidationException.class)
    public void testCatalogObjectWithInvalidValue1() throws ValidationException {
        String value = " bucket_1/";
        new CatalogObjectValidator().validate(value, null);
    }

    @Test(expected = ValidationException.class)
    public void testCatalogObjectWithInvalidValue2() throws ValidationException {
        String value = " bucket_1";
        new CatalogObjectValidator().validate(value, null);
    }

    @Test(expected = ValidationException.class)
    public void testCatalogObjectWithShortRevisionNumber() throws ValidationException {
        String value = " bucket_1/object10/123445";
        new CatalogObjectValidator().validate(value, null);
    }

    @Test(expected = ValidationException.class)
    public void testCatalogObjectKOWithInvalidRevisionNumber() throws ValidationException {
        String value = " bucket_1/object10/123445ddd";
        new CatalogObjectValidator().validate(value, null);
    }

    @Test(expected = ValidationException.class)
    public void testCatalogObjectKOWithNoRevisionNumber() throws ValidationException {
        String value = " bucket_1/object10/";
        new CatalogObjectValidator().validate(value, null);
    }

    @Test(expected = ValidationException.class)
    public void testCatalogObjectKOWithLongRevisionNumber() throws ValidationException {
        String value = " bucket_1/object10/153931016544335";
        new CatalogObjectValidator().validate(value, null);
    }
}
