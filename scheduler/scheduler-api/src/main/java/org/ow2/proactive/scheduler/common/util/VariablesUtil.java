/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
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
public class VariablesUtil {

    // non-instantiable
    private VariablesUtil() {
    }

    /**
     * Replaces variables in {@code input} map values using keys defined in {@code substitutes} map.
     *
     * @param input the map that may contain values to replace.
     * @param substitutes the map containing substitutes.
     */
    public static Map<String, String> filterAndUpdate(
            Map<String, String> input, Map<String, String> substitutes) {

        Map<String, String> result = new HashMap<>(substitutes.size());

        for (Map.Entry<String, String> entry : input.entrySet()) {
            result.put(entry.getKey(), filterAndUpdate(entry.getValue(), substitutes));
        }

        return result;
    }

    /**
     * Filters the specified string and replaces the variables with values
     * specified in the map.
     *
     * @see VariablesUtil#filterAndUpdate(String, boolean, Map)
     *
     * @param input
     *            the string which need to be filtered
     * @param variables
     *            a map which contains variable values
     * @return the filtered string
     */
    public static String filterAndUpdate(String input, Map<? extends Serializable, ? extends Serializable> variables) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String output = input;
        for (Map.Entry<String, String> replacement : buildSubstitutes(variables).entrySet()) {
            output = output.replace(replacement.getKey(), replacement.getValue());
        }

        return output;
    }

    /**
     * Filters the specified script object. It replaces the variables in the
     * script content and parameter array with the values specified in the
     * variable map.
     *
     * @param script
     *            the script to filter
     * @param variables
     *            a map which contains variables values
     */
    public static void filterAndUpdate(Script<?> script, Map<? extends Serializable, ? extends Serializable> variables) {
        script.setScript(filterAndUpdate(script.getScript(), variables));
        Serializable[] params = script.getParameters();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                params[i] = filterAndUpdate(params[i].toString(), variables);
            }
        }
    }

    public static Map<String, String> buildSubstitutes(
      Map<? extends Serializable, ? extends Serializable> variables) {
        Map<String, String> replacements = new HashMap<>();

        if (variables != null) {
            for (Map.Entry<? extends Serializable, ? extends Serializable> variable : variables.entrySet()) {
                replacements.put("$" + variable.getKey().toString(), variable.getValue().toString());
                replacements.put("${" + variable.getKey().toString() + "}", variable.getValue().toString());
            }
        }

        return replacements;
    }

}
