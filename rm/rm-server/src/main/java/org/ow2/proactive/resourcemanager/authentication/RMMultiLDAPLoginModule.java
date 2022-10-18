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
package org.ow2.proactive.resourcemanager.authentication;

import java.io.File;
import java.security.KeyException;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ow2.proactive.authentication.MultiLDAPLoginModule;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;


/**
 *
 * LDAP login module implementation for resource manager. Extracts LDAP configuration files from
 * resource manager configuration and uses it to authenticate users.
 *
 */
public class RMMultiLDAPLoginModule extends MultiLDAPLoginModule {

    /**
     * Returns Multiple LDAP configuration defined in resource manager configuration file
     *
     * @return a map of (domain_name,configuration_file)
     */
    @Override
    protected Map<String, String> getMultiLDAPConfig() {
        Map<String, String> answer = new HashMap<>();
        List<String> ldapConfigList = PAResourceManagerProperties.RM_MULTI_LDAP_CONFIG.getValueAsList(",");
        for (String config : ldapConfigList) {
            String[] configPair = config.split("\\s*:\\s*");
            if (configPair.length != 2) {
                throw new IllegalArgumentException("invalid multi-ldap configuration string : " + config);
            }
            String domain = configPair[0];
            String ldapFile = configPair[1];
            //test that ldap file path is an absolute path or not
            if (!(new File(ldapFile).isAbsolute())) {
                //file path is relative, so we complete the path with the scheduler home
                ldapFile = PAResourceManagerProperties.getAbsolutePath(ldapFile);
            }
            answer.put(domain, ldapFile);
        }
        return answer;
    }

    /**
     * Returns login file name from resource manager configuration file
     * Used for authentication fall back.
     * @return login file name from resource manager configuration file     *
     */
    @Override
    protected String getLoginFileName() {

        String loginFile = PAResourceManagerProperties.RM_LOGIN_FILE.getValueAsString();
        //test that login file path is an absolute path or not
        if (!(new File(loginFile).isAbsolute())) {
            //file path is relative, so we complete the path with the prefix RM_Home constant
            loginFile = PAResourceManagerProperties.getAbsolutePath(loginFile);
        }

        return loginFile;
    }

    /**
     * Returns group file name from resource manager configuration file
     * Used for group membership verification fall back.
     * @return group file name from resource manager configuration file
     */
    @Override
    protected String getGroupFileName() {
        String groupFile = PAResourceManagerProperties.RM_GROUP_FILE.getValueAsString();
        //test that group file path is an absolute path or not
        if (!(new File(groupFile).isAbsolute())) {
            //file path is relative, so we complete the path with the prefix RM_Home constant
            groupFile = PAResourceManagerProperties.getAbsolutePath(groupFile);
        }

        return groupFile;
    }

    /**
     * Returns tenant file name from resource manager configuration file
     */
    @Override
    protected String getTenantFileName() {
        String tenantFile = PAResourceManagerProperties.RM_TENANT_FILE.getValueAsString();
        //test that group file path is an absolute path or not
        if (!(new File(tenantFile).isAbsolute())) {
            //file path is relative, so we complete the path with the prefix RM_Home constant
            tenantFile = PAResourceManagerProperties.getAbsolutePath(tenantFile);
        }

        return tenantFile;
    }

    @Override
    protected PrivateKey getPrivateKey() throws KeyException {
        return Credentials.getPrivateKey(PAResourceManagerProperties.getAbsolutePath(PAResourceManagerProperties.RM_AUTH_PRIVKEY_PATH.getValueAsString()));
    }

    /**
     * Returns logger for authentication
     */
    public Logger getLogger() {
        return Logger.getLogger(RMMultiLDAPLoginModule.class);
    }
}
