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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.ow2.proactive.scripting.Script;


/**
 * Utility class which facilitates the filtering of variables defined in strings
 * and scripts by using a variable map. These variables should have ${...} or $...
 * format. They will be replace with corresponding values specified in the
 * variable map.
 */
public class VariableSubstitutor {

    // Maximum depth allowed in recursive variable replacements to avoid infinite loops.
    // This is not configurable as it should cover all use cases.
    public static final int MAXIMUM_DEPTH = 5;

    // non-instantiable
    private VariableSubstitutor() {
    }

    /**
     * Replaces variables in {@code input} map values using keys defined in {@code variables} map.
     *
     * @param input     a map that may contain values to replace.
     * @param variables a map which contains variable name and value pairs.
     */
    public static Map<String, String> filterAndUpdate(Map<String, String> input,
            Map<String, ? extends Serializable> variables) {

        Map<String, String> result = new HashMap<>(variables.size());

        for (Map.Entry<String, String> entry : input.entrySet()) {
            result.put(entry.getKey(), filterAndUpdate(entry.getValue(), variables));
        }

        return result;
    }

    /**
     * Creates a HashMap with variablesDictionary where values are resolved.
     *
     * Solve variables value if bound to another variable, for instance "var log=${LOG_ENV_VAR}", we
     * expect that LOG_ENV_VAR is replaced by its value. To do so we have a variables hash, that must have
     * all references and then for each variable we do a filterAndUpdate that will recursively replace
     * when needed, @see VariableSubstitutor.
     *
     * Some limitations to consider: recursive substitution limit is VariableSubstitutor.MAXIMUM_DEPTH,
     * if the variable value is a complex data structure (array, List, Vector) we will not substitute it.
     *
     * @param variables input hash containing variables and their values may reference other variables
     * @return dictionary with the same variables however with their values resolved
     */
    public static Map<String, Serializable> resolveVariables(Map<String, Serializable> variables,
            Map<String, Serializable> dictionary) {
        Map<String, Serializable> resolvedVariables = new HashMap<>();
        for (Map.Entry<String, Serializable> entry : variables.entrySet()) {
            String resolvedValue;
            if (entry.getValue() instanceof String) {
                resolvedValue = filterAndUpdate(entry.getValue().toString(), dictionary);
                resolvedVariables.put(entry.getKey(), resolvedValue);
            } else {
                resolvedVariables.put(entry.getKey(), entry.getValue());
            }
        }
        return resolvedVariables;
    }

    /**
     * Filters the specified string and replaces the variables with values
     * specified in the map.
     *
     * @param input     the string which need to be filtered
     * @param variables a map which contains variable values
     * @return the filtered string
     * @see VariableSubstitutor#filterAndUpdate(String, Map)
     */
    public static String filterAndUpdate(String input, Map<? extends Serializable, ? extends Serializable> variables) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String output = input;
        Map<String, String> substitutes = buildSubstitutes(variables);
        output = replaceRecursively(output, substitutes);

        return output;
    }

    /**
     * Replace the given string with a list of substitutions recursively. Recursion will be limited to MAXIMUM_DEPTH.
     *
     * @param value       string used to apply replacement
     * @param substitutes map of substitutions
     * @return a new string where all replacements were performed
     */
    private static String replaceRecursively(final String value, Map<String, String> substitutes) {
        boolean anyReplacement;
        String output = value;
        int depthCount = 0;
        do {
            anyReplacement = false;
            depthCount++;
            for (Map.Entry<String, String> replacement : substitutes.entrySet()) {
                if (replacement.getValue() != null) {
                    String newOutput = output.replace(replacement.getKey(), replacement.getValue());
                    anyReplacement = anyReplacement || !newOutput.equals(output);
                    output = newOutput;
                }
            }
        } while (anyReplacement && depthCount < MAXIMUM_DEPTH);
        return output;
    }

    /**
     * Filters the specified script object. It replaces the variables in the
     * script content and parameter array with the values specified in the
     * variable map.
     *
     * @param script    the script to filter
     * @param variables a map which contains variables values
     */
    public static void filterAndUpdate(Script<?> script,
            Map<? extends Serializable, ? extends Serializable> variables) {
        script.setScript(filterAndUpdate(script.getScript(), variables));
        Serializable[] params = script.getParameters();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if (params[i] != null) {
                    params[i] = filterAndUpdate(params[i].toString(), variables);
                }
            }
        }
    }

    public static Map<String, String> buildSubstitutes(Map<? extends Serializable, ? extends Serializable> variables) {
        Map<String, String> replacements = new HashMap<>();

        if (variables != null) {
            for (Map.Entry<? extends Serializable, ? extends Serializable> variable : variables.entrySet()) {
                if (variable.getValue() != null) {
                    String key = variable.getKey().toString();
                    String value = variable.getValue().toString();

                    replacements.put("$" + key, value);
                    replacements.put("$" + key.toUpperCase().replace(".", "_"), value);
                    replacements.put("${" + key + "}", value);
                }
            }
        }

        return replacements;
    }

}
