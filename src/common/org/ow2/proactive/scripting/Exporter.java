/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.scripting;

/**
 * This class is used for exporting java property, i.e. make this property available for all dependent tasks.
 */

public class Exporter {

    /** Name of the java property that contains the names of currently exported properties */
    public final static String EXPORTED_PROPERTIES_VAR_NAME = "pa.scheduler.exported.properties.names";
    public final static String EXPORTED_VARS_VAR_SEPARATOR = "%";

    /**
     * This method allows to export a java property, i.e. make this property available for all dependent tasks.
     * @param key the name of the exported property.
     * @throws IllegalArgumentException if the property key is not set.
     */
    public static void exportProperty(String key) {
        if (System.getProperty(key) == null) {
            throw new IllegalArgumentException(key + " is not set as Java Property");
            // CHECK SIZE < 255
        } else {
            String allExportedVars = System.getProperty(EXPORTED_PROPERTIES_VAR_NAME);
            if (allExportedVars == null) {
                System.setProperty(EXPORTED_PROPERTIES_VAR_NAME, "");
                allExportedVars = "";
            }
            System.setProperty(EXPORTED_PROPERTIES_VAR_NAME, allExportedVars + EXPORTED_VARS_VAR_SEPARATOR +
                key);
        }
    }
}
