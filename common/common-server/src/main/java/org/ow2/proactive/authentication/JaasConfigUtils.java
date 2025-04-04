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
package org.ow2.proactive.authentication;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.ow2.proactive.core.properties.PASharedProperties;


public class JaasConfigUtils {

    static String getLoginFileName() {
        String loginFile = PASharedProperties.LOGIN_FILENAME.getValueAsString();
        //test that login file path is an absolute path or not
        if (!(new File(loginFile).isAbsolute())) {
            //file path is relative, so we complete the path with the prefix RM_Home constant
            loginFile = PASharedProperties.getAbsolutePath(loginFile);
        }

        return loginFile;
    }

    static String getGroupFileName() {
        String groupFile = PASharedProperties.GROUP_FILENAME.getValueAsString();
        //test that group file path is an absolute path or not
        if (!(new File(groupFile).isAbsolute())) {
            //file path is relative, so we complete the path with the prefix RM_Home constant
            groupFile = PASharedProperties.getAbsolutePath(groupFile);
        }

        return groupFile;
    }

    static String getTenantFileName() {
        String tenantFile = PASharedProperties.TENANT_FILENAME.getValueAsString();
        //test that group file path is an absolute path or not
        if (!(new File(tenantFile).isAbsolute())) {
            //file path is relative, so we complete the path with the prefix RM_Home constant
            tenantFile = PASharedProperties.getAbsolutePath(tenantFile);
        }

        return tenantFile;
    }

    static Set<String> getConfiguredDomains() {
        if (PASharedProperties.ALLOWED_DOMAINS.isSet()) {
            return PASharedProperties.ALLOWED_DOMAINS.getValueAsList(",")
                                                     .stream()
                                                     .map(domain -> domain.toLowerCase())
                                                     .collect(Collectors.toSet());
        } else {
            // windows current machine domain
            String currentMachineDomain = System.getenv("USERDOMAIN");
            if (currentMachineDomain != null) {
                return Collections.singleton(currentMachineDomain.toLowerCase());
            } else {
                return new HashSet<>();
            }
        }
    }
}
