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
package org.ow2.proactive.scheduler.common.util;

import static java.util.Collections.singletonMap;
import static org.junit.Assert.*;
import static org.ow2.proactive.scheduler.common.util.VariableSubstitutor.filterAndUpdate;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;


public class VariablesUtilTest {

    private static final String testString = "A${foo.bar}C";

    private static final String testStringRecursive = "A_${foo.${suffix}}_C";

    private static final String testStringRecursive2 = "A_${myvar}_C";

    private static final String testStringRecursive3 = "A${expand}";

    private static final String testStringAlias1 = "A$FOO_BAR C";

    private static final String testUnixString = "$foo.bar/toto";

    @Test
    public void testFilterAndUpdateWithVariableMap() {
        Map<String, String> variables = singletonMap("foo.bar", "B");
        String updated1 = filterAndUpdate(testString, variables);
        String updated2 = filterAndUpdate(testStringAlias1, variables);
        String updated3 = filterAndUpdate(testUnixString, variables);
        assertEquals("ABC", updated1);
        assertEquals("AB C", updated2);
        assertEquals("B/toto", updated3);
    }

    @Test
    public void testFilterAndUpdateWithRecursiveDefinition() {
        Map<String, String> variables = ImmutableMap.<String, String> builder()
                                                    .put("suffix", "bar")
                                                    .put("foo.bar", "B")
                                                    .put("myvar", "${foo.${suffix}}")
                                                    .build();
        String updated1 = filterAndUpdate(testStringRecursive, variables);
        String updated2 = filterAndUpdate(testStringRecursive2, variables);
        assertEquals("A_B_C", updated1);
        assertEquals("A_B_C", updated2);
    }

    @Test(timeout = 3000, expected = IllegalStateException.class)
    public void testFilterAndUpdateInfiniteRecursion() {
        Map<String, String> variables = ImmutableMap.<String, String> builder().put("expand", "A${expand}").build();
        String updated1 = filterAndUpdate(testStringRecursive3, variables);
        assertEquals("AAAAAA${expand}", updated1);
    }

    @Test
    public void testFilterAndUpdateWithUnknownVariable() {
        String notReplaced = VariableSubstitutor.filterAndUpdate(testString,
                                                                 Collections.<Serializable, Serializable> emptyMap());
        assertEquals(testString, notReplaced);
    }

    @Test
    public void testFilterAndUpdateWithSystemProperties() {
        System.setProperty("bar", "B");
        Map<String, String> variables = new HashMap<>();
        for (Map.Entry o : System.getProperties().entrySet()) {
            variables.put(o.getKey().toString(), o.getValue().toString());
        }
        String updated = VariableSubstitutor.filterAndUpdate("A${bar}C", variables);
        assertEquals("ABC", updated);
        String updated2 = VariableSubstitutor.filterAndUpdate("A${BAR}C", variables);
        assertEquals("ABC", updated2);
        assertEquals("B", variables.get("bar"));
    }

    @Test
    public void double_occurrence() {
        assertEquals("bar bar",
                     VariableSubstitutor.filterAndUpdate("$foo $foo",
                                                         Collections.<String, Serializable> singletonMap("foo",
                                                                                                         "bar")));
        assertEquals("barbar",
                     VariableSubstitutor.filterAndUpdate("${foo}${foo}",
                                                         Collections.<String, Serializable> singletonMap("foo",
                                                                                                         "bar")));
    }

    @Test
    public void bigMap() {
        Map<String, Serializable> bigMap = new HashMap<>();
        String finalValue = "value";
        String key = "key_";
        int number = 10000;
        String value = "${" + key + (number - 1) + "}";
        bigMap.put(key + 0, value);
        for (int i = 0; i < number - 1; i++) {
            bigMap.put(key + i, value);
        }
        bigMap.put(key + (number - 1), finalValue);
        bigMap = VariableSubstitutor.resolveVariables(bigMap, bigMap);
        bigMap.values()
              .stream()
              .filter(v -> !v.equals(finalValue))
              .forEach(v -> System.out.println("Wrong value: " + v));
        Assert.assertTrue("all values should be \"" + finalValue + "\"",
                          bigMap.values().stream().allMatch(v -> v.equals(finalValue)));
    }

    @Test
    public void bigMapRecursiveDefinitions() {
        Map<String, Serializable> bigMap = new HashMap<>();
        String finalValue = "value";
        String key = "key_";
        int number = 100; // produces stack overflow error on very big number, 100 seems fairly acceptable
        bigMap.put(key + 0, finalValue);
        for (int i = 1; i < number; i++) {
            bigMap.put(key + i,
                       VariableSubstitutor.SUBSITUTE_PREFIX + key + (i - 1) + VariableSubstitutor.SUBSTITUTE_SUFFIX);
        }
        bigMap = VariableSubstitutor.resolveVariables(bigMap, bigMap);
        bigMap.values()
              .stream()
              .filter(v -> !v.equals(finalValue))
              .forEach(v -> System.out.println("Wrong value: " + v));
        Assert.assertTrue("all values should be \"" + finalValue + "\"",
                          bigMap.values().stream().allMatch(v -> v.equals(finalValue)));
    }
}
