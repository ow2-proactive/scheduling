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

import java.util.Map;

import org.ow2.proactive.scripting.Script;

/**
 * Utility class which facilitates the filtering of variables defined in strings
 * and scripts by using a variable map. These variables should have ${...}
 * format. They will be replace with corresponding values specified in the
 * variable map.
 */
public class VariablesUtil {

    /** Variables pattern definition */
    private static final String variablesPattern = "\\$\\{[^\\}]+\\}";

    // non-instantiable
    private VariablesUtil() {
    }

    /**
     * Filters the specified string and replaces the variables with values
     * specified in the map.
     * 
     * @see VariablesUtil#filterAndUpdate(String, boolean, Map)
     * 
     * @param string
     *            the string which need to be filtered
     * @param variables
     *            a map which contains variable values
     * @return the filtered string
     */
    public static String filterAndUpdate(String string, Map variables) {
        return filterAndUpdate(string, false, variables);
    }

    /**
     * Filters the specified string and replaces the variables with values
     * specified in the map. <br />
     * If a variable is not present in the map, but is available as a system
     * property, it will be replaced and the map will also be updated. <br />
     * If dryrun flag is set, this method will check only whether it can filter
     * the string without any errors.
     * 
     * @param string
     *            the string which need to be filtered
     * @param dryrun
     *            an indicator which decides whether only to test the filtering.
     * @param variables
     *            a map which contains variable values
     * @return the filtered string
     */
    public static String filterAndUpdate(String string, boolean dryrun,
            Map variables) {
        if (string == null || string.isEmpty()) {
            return string;
        }
        string = string.trim();
        String[] defs = RegexpMatcher.matches(variablesPattern, string);
        String value;
        if (defs.length != 0) {
            // for each entry
            for (String def : defs) {
                // remove ${ and }
                def = def.substring(2, def.length() - 1);
                // search the key (first in variables)
                value = System.getProperty(def);
                if (value != null) {
                    variables.put(def, value);
                } else {
                    value = getValueAsString(def, variables);
                    if (value == null) {
                        throw new IllegalArgumentException("Variable '" + def
                                + "' not found in the definition (${" + def
                                + "})");
                    }
                }
                if (!dryrun) {
                    value = value.replaceAll("\\\\", "\\\\\\\\");
                    string = string.replaceFirst("\\$\\{" + def + "\\}", value);
                }
            }
        }
        return string;
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
    public static void filterAndUpdate(Script<?> script, Map variables) {
        script.setScript(filterAndUpdate(script.getScript(), variables));
        String[] params = script.getParameters();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                params[i] = filterAndUpdate(params[i], variables);
            }
        }
    }

    /*
     * Returns the value contained in the map as a string.
     */
    private static String getValueAsString(String key, Map variables) {
        Object valObj = variables.get(key);
        return (valObj == null) ? null : String.valueOf(valObj);
    }
}
