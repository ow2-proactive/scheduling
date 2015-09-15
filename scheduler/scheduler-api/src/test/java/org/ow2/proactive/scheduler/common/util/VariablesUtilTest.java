package org.ow2.proactive.scheduler.common.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static java.util.Collections.singletonMap;
import static org.junit.Assert.*;
import static org.ow2.proactive.scheduler.common.util.VariableSubstitutor.filterAndUpdate;


public class VariablesUtilTest {

    private static final String testString = "A${foo}C";

    @Test
    public void testFilterAndUpdateWithVariableMap() {
        Map<String, String> variables = singletonMap("foo", "B");
        String updated = filterAndUpdate(testString, variables);
        assertEquals("ABC", updated);
    }

    @Test
    public void testFilterAndUpdateWithUnknownVariable() {
        String notReplaced = VariableSubstitutor.filterAndUpdate(testString,
                Collections.<Serializable, Serializable>emptyMap());
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
        assertEquals("B", variables.get("bar"));
    }

    @Test
    public void double_occurrence() throws Exception {
        assertEquals(
                "barbar",
                VariableSubstitutor.filterAndUpdate("$foo$foo",
                        Collections.<String, Serializable>singletonMap("foo", "bar")));

        assertEquals(
                "barbar",
                VariableSubstitutor.filterAndUpdate("${foo}${foo}",
                        Collections.<String, Serializable>singletonMap("foo", "bar")));
    }
}