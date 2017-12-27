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
package org.ow2.proactive.scheduler.core.helpers;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class VariableBatchSizeIteratorTest {
    List<Integer> rawList;

    private VariableBatchSizeIterator variableBatchSizeIterator;

    @Before
    public void setUp() throws Exception {
        Integer[] integers = new Integer[42];
        for (int i = 0; i < integers.length; i++) {
            integers[i] = i;
        }
        rawList = new ArrayList<>(Arrays.asList(integers));
    }

    @Test
    public void testWithRemainingElements() throws Exception {
        variableBatchSizeIterator = new VariableBatchSizeIterator(rawList);
        List<Integer> newList = new ArrayList<>();
        newList.addAll(variableBatchSizeIterator.getNextElements(10));
        newList.addAll(variableBatchSizeIterator.getNextElements(5));
        newList.addAll(variableBatchSizeIterator.getNextElements(1));
        Assert.assertTrue("Elements should remains: 42 in raw list, 16 retrieved",
                          variableBatchSizeIterator.hasMoreElements());
        Assert.assertTrue("First 16 elements should be the same from raw list", newList.equals(rawList.subList(0, 16)));
    }

    @Test
    public void testWithOverLimitRequests() throws Exception {
        variableBatchSizeIterator = new VariableBatchSizeIterator(rawList);
        List<Integer> newList = new ArrayList<>();
        newList.addAll(variableBatchSizeIterator.getNextElements(30));
        newList.addAll(variableBatchSizeIterator.getNextElements(10));
        newList.addAll(variableBatchSizeIterator.getNextElements(10));
        Assert.assertFalse("No elements should remain: 42 in raw list, 50 requested",
                           variableBatchSizeIterator.hasMoreElements());
        Assert.assertTrue("All elements retrieved, should be equal to raw list", newList.equals(rawList));

    }

    @Test
    public void testWithSingleOverLimitRequest() throws Exception {
        variableBatchSizeIterator = new VariableBatchSizeIterator(rawList);
        List<Integer> newList = new ArrayList<>();
        newList.addAll(variableBatchSizeIterator.getNextElements(50));
        Assert.assertFalse("No elements should remain: 42 in raw list, 50 requested",
                           variableBatchSizeIterator.hasMoreElements());
        Assert.assertTrue("All elements retrieved, should be equal to raw list", newList.equals(rawList));

    }

    @Test
    public void testWithZeroRequest() throws Exception {
        variableBatchSizeIterator = new VariableBatchSizeIterator(rawList);
        List<Integer> newList = new ArrayList<>();
        newList.addAll(variableBatchSizeIterator.getNextElements(0));
        Assert.assertTrue("Elements should remain: 42 in raw list, 0 requested",
                          variableBatchSizeIterator.hasMoreElements());
        Assert.assertTrue("No elements retrieved, list should be empty", newList.isEmpty());

    }

    @Test
    public void testWithNegativeLimitRequest() throws Exception {
        variableBatchSizeIterator = new VariableBatchSizeIterator(rawList);
        try {
            variableBatchSizeIterator.getNextElements(-42);
        } catch (IllegalArgumentException e) {
            Assert.assertTrue("Exception message should be contain bad limit value", e.getMessage().contains("-42"));
            return;
        }
        fail("An IllegalArgumentException should be raised when providing a negative limit value");
    }

    @Test
    public void testWithEmptyRawList() throws Exception {
        variableBatchSizeIterator = new VariableBatchSizeIterator(new ArrayList());
        Assert.assertFalse("Empty raw list should have more elements", variableBatchSizeIterator.hasMoreElements());
        Assert.assertTrue("Retrieved elements from empty raw list should be empty",
                          variableBatchSizeIterator.getNextElements(42).isEmpty());
    }
}
