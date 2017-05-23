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

import com.google.common.collect.ImmutableList;


public class ListValidatorTest {

    private ImmutableList<String> listMembers = ImmutableList.of("1", "2", "3");

    @Test
    public void testListOK() throws Exception {
        ListValidator validator = new ListValidator(listMembers);
        Assert.assertEquals("1", validator.validate("1", null));
        Assert.assertEquals("2", validator.validate("2", null));
        Assert.assertEquals("3", validator.validate("3", null));
    }

    @Test(expected = ValidationException.class)
    public void testListWrongValue() throws Exception {
        ListValidator validator = new ListValidator(listMembers);
        validator.validate("4", null);
    }

    @Test(expected = ValidationException.class)
    public void testListEmptyValue() throws Exception {
        ListValidator validator = new ListValidator(listMembers);
        validator.validate("", null);
    }

    @Test(expected = ValidationException.class)
    public void testListNullValue() throws Exception {
        ListValidator validator = new ListValidator(listMembers);
        validator.validate(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("squid:S1848")
    public void testListDuplicate() throws Exception {
        new ListValidator(ImmutableList.of("1", "2", "1", "3"));
    }
}
