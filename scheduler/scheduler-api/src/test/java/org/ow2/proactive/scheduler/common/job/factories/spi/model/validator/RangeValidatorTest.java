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


public class RangeValidatorTest {

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("squid:S1848")
    public void testRangeInvalidRange() throws Exception {
        new RangeValidator<>(20, 10);
    }

    @Test
    public void testRangeAny() throws Exception {
        int value = 10;
        RangeValidator<Integer> validator = new RangeValidator<>();
        Assert.assertEquals(value, (int) validator.validate(value, null));
    }

    @Test
    public void testRangeGreaterThanOK() throws Exception {
        int value = 10;
        RangeValidator<Integer> validator = new RangeValidator<>(value);
        Assert.assertEquals(value + 1, (int) validator.validate(value + 1, null));
        Assert.assertEquals(value, (int) validator.validate(value, null));
    }

    @Test(expected = ValidationException.class)
    public void testRangeGreaterThanKO() throws Exception {
        int value = 10;
        RangeValidator<Integer> validator = new RangeValidator<>(value);
        validator.validate(value - 1, null);
    }

    @Test
    public void testRangeIntervalOK() throws Exception {
        int minValue = 10;
        int maxValue = 20;
        RangeValidator<Integer> validator = new RangeValidator<>(minValue, maxValue);
        Assert.assertEquals(minValue, (int) validator.validate(minValue, null));
        Assert.assertEquals(maxValue, (int) validator.validate(maxValue, null));
        Assert.assertEquals(minValue + 1, (int) validator.validate(minValue + 1, null));
        Assert.assertEquals(maxValue - 1, (int) validator.validate(maxValue - 1, null));
    }

    @Test(expected = ValidationException.class)
    public void testRangeIntervalKOLower() throws Exception {
        int minValue = 10;
        int maxValue = 20;
        RangeValidator<Integer> validator = new RangeValidator<>(minValue, maxValue);
        validator.validate(minValue - 1, null);
        Assert.assertEquals(maxValue, (int) validator.validate(maxValue, null));
        Assert.assertEquals(minValue + 1, (int) validator.validate(minValue + 1, null));
        Assert.assertEquals(maxValue - 1, (int) validator.validate(minValue - 1, null));
    }

    @Test(expected = ValidationException.class)
    public void testRangeIntervalKOGreater() throws Exception {
        int minValue = 10;
        int maxValue = 20;
        RangeValidator<Integer> validator = new RangeValidator<>(minValue, maxValue);
        validator.validate(maxValue + 1, null);
    }
}
