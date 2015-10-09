/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.ow2.proactive.scripting;

/**
 * This class defines utils for java property, e.g propagating java property, i.e. make this
 * property available for all dependent tasks.
 *
 * @deprecated Use variable propagation instead
 * @since Scheduling 2.0
 */
@Deprecated
public class PropertyUtils {

    /** Name of the java property that contains the names of currently exported properties */
    public final static String PROPAGATED_PROPERTIES_VAR_NAME = "pa.scheduler.propagated.properties.names";
    public final static String EXPORTED_PROPERTIES_VAR_NAME = "pa.scheduler.exported.properties.names";
    public final static String VARS_VAR_SEPARATOR = "%";

    /**
     * This method allows to propagate a java property, i.e. make this property available for all dependent tasks.
     * @param key the name of the propagated property.
     * @throws IllegalArgumentException if the property key is not set.
     */
    public static void propagateProperty(String key) {
        checkPropertyName(key);
        // an exception is thrown key is not valid
        String allPropagatedVars = System.getProperty(PROPAGATED_PROPERTIES_VAR_NAME);
        if (allPropagatedVars == null) {
            System.setProperty(PROPAGATED_PROPERTIES_VAR_NAME, "");
            allPropagatedVars = "";
        }
        System.setProperty(PROPAGATED_PROPERTIES_VAR_NAME, allPropagatedVars + VARS_VAR_SEPARATOR + key);
    }

    /**
     * This method allows to export a java property, i.e. make this property available in native and
     * forked java task. For native task, the property is exported
     * @param key the name of the exported property.
     * @throws IllegalArgumentException if the property key is not set.
     */
    public static void exportProperty(String key) {
        checkPropertyName(key);
        // an exception is thrown key is not valid
        String allExportedVars = System.getProperty(EXPORTED_PROPERTIES_VAR_NAME);
        if (allExportedVars == null) {
            System.setProperty(EXPORTED_PROPERTIES_VAR_NAME, "");
            allExportedVars = "";
        }
        System.setProperty(EXPORTED_PROPERTIES_VAR_NAME, allExportedVars + VARS_VAR_SEPARATOR + key);
    }

    private static void checkPropertyName(String key) {
        if (System.getProperty(key) == null) {
            throw new IllegalArgumentException(key + " is not set as Java Property");
        } else if (key.length() > 255) {
            throw new IllegalArgumentException(key +
                " name is too long (propagated property name length must be less than 256).");
        } else if (key.contains(VARS_VAR_SEPARATOR)) {
            throw new IllegalArgumentException(key + " cannot contain character " + VARS_VAR_SEPARATOR);
        }
    }

}
