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
package org.ow2.proactive.scheduler.permissions;

import java.security.Permission;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.ow2.proactive.permissions.ClientPermission;


/**
 *
 * This permission allows to handle other users jobs that contains the generic
 * information specified in the security.java.policy-server
 *
 *
 */
public class HandleJobsWithGenericInformationPermission extends ClientPermission {

    private final static String DESCRIPTION = "Handle Jobs that contains this specific Generic Information";

    private Map<String, String> genericInformation = new HashMap<String, String>();

    /**
     * Construct the permission with specified authorization string.
     *
     * @param keyValue
     *            string that represents a key value comma separated String. Ex
     *            : "KEY1=VALUE1,KEY2=VALUE2"
     */
    public HandleJobsWithGenericInformationPermission(String keysValuesCommaSeparated) {
        super(DESCRIPTION);
        if (keysValuesCommaSeparated != null) {
            String[] keysValues = keysValuesCommaSeparated.split("[\\s,]+");
            populateGenericInformation(keysValues);
        }

    }

    /**
     * Construct the permission with specified authorization genericInformations
     * Map.
     *
     * @param genericInformations
     *            map that contains all the generic information of a job that it
     *            needs to be authorised
     * 
     */
    public HandleJobsWithGenericInformationPermission(Map<String, String> genericInformations) {
        super(DESCRIPTION);
        this.genericInformation = genericInformations;
    }

    /**
     * check that the given permission matches with this permission
     */
    @Override
    public boolean implies(Permission p) {
        if (!(p instanceof HandleJobsWithGenericInformationPermission)) {
            return false;
        }
        HandleJobsWithGenericInformationPermission fsp = (HandleJobsWithGenericInformationPermission) p;
        // check incoming permission and permission given by the security file
        return !genericInformation.isEmpty() &&
               externalGenericInformationContainsMyGenericInformation(fsp.genericInformation);

    }

    private void populateGenericInformation(String[] keysValues) {
        for (int i = 0; i < keysValues.length; i++) {
            if (keysValues[i] != null && keysValues[i].contains("=")) {
                StringTokenizer st = new StringTokenizer(keysValues[i], "=");
                this.genericInformation.put(st.nextToken(), st.nextToken());
            }
        }
    }

    private boolean
            externalGenericInformationContainsMyGenericInformation(Map<String, String> externalGenericInformation) {

        for (Map.Entry<String, String> entry : genericInformation.entrySet()) {
            String value = externalGenericInformation.get(entry.getKey());
            if (value == null || !value.equals(entry.getValue())) {
                return false;
            }
        }

        return true;
    }
}
