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

import java.security.KeyException;
import java.security.PrivateKey;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ow2.proactive.authentication.PAMLoginModule;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;


/**
 * File login module implementation for resource manager. Extracts "login" and "group" files names from
 * resource manager configuration and uses them to authenticate users.
 */
public class RMPAMLoginModule extends PAMLoginModule {

    /**
     * Returns login file name from resource manager configuration file
     */
    @Override
    protected String getLoginFileName() {
        return RMJaasConfigUtils.getLoginFileName();
    }

    /**
     * Returns group file name from resource manager configuration file
     */
    @Override
    protected String getGroupFileName() {
        return RMJaasConfigUtils.getGroupFileName();
    }

    /**
     * Returns tenant file name from resource manager configuration file
     */
    @Override
    protected String getTenantFileName() {
        return RMJaasConfigUtils.getTenantFileName();
    }

    @Override
    protected Set<String> getConfiguredDomains() {
        return RMJaasConfigUtils.getConfiguredDomains();
    }

    /**
     * Returns logger used for authentication
     */
    public Logger getLogger() {
        return Logger.getLogger(RMPAMLoginModule.class);
    }

    @Override
    protected PrivateKey getPrivateKey() throws KeyException {
        return Credentials.getPrivateKey(PAResourceManagerProperties.getAbsolutePath(PAResourceManagerProperties.RM_AUTH_PRIVKEY_PATH.getValueAsString()));
    }

    @Override
    protected boolean isLegacyPasswordEncryption() {
        return PAResourceManagerProperties.RM_LEGACY_ENCRYPTION.getValueAsBoolean();
    }

}
