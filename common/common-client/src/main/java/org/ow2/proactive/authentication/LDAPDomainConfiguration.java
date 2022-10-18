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

import static org.ow2.proactive.authentication.LDAPLoginModule.*;

import org.apache.log4j.Logger;
import org.ow2.proactive.core.properties.PASharedProperties;

import com.google.common.base.Strings;


public class LDAPDomainConfiguration {

    Logger logger;

    /** boolean defining whether connection polling has to be used */
    private final String ldapConnectionPooling;

    /** LDAP used to perform authentication */
    private final String ldapUrl;

    /** LDAP Subtree wherein users entries are searched */
    private final String usersDn;

    /**
     * LDAP Subtree wherein groups entries are searched
     * If empty, then USERS_DN is used instead
     */
    private String groupsDn;

    /**
     * The LDAP filter used to find a user
     */
    private final String ldapUserFilter;

    /**
     * The LDAP filter used to find all groups associated with a user
     */
    private final String ldapGroupFilter;

    /**
     * Name of the LDAP attribute which can be used to associate a user with a tenant
     */
    private final String tenantAttribute;

    /**
     * Name of the LDAP attribute which represents the group name in a ldap group entry
     */
    private final String groupNameAttribute;

    /**
     * Authentication method used to bind to LDAP: none, simple,
     * or one of the SASL authentication methods
     */
    private final String authenticationMethod;

    private final boolean startTls;

    private final boolean anyCertificate;

    private final boolean anyHostname;

    private final boolean useUidInGroupSearch;

    /** user name used to bind to LDAP (if authentication method is different from none) */
    private final String bindLogin;

    /** user password used to bind to LDAP (if authentication method is different from none) */
    private String bindPasswd;

    /** fall back property, check user/password and group in files if user in not found in LDAP */
    private boolean fallbackUserAuth;

    /** group fall back property, check user group membership group file if user in not found in corresponding LDAP group*/
    private boolean fallbackGroupMembership;

    /** tenant fall back property, check user tenant membership using tenant file if user is not found in LDAP or if tenant LDAP attribute is not defined */
    private boolean fallbackTenantMembership;

    public LDAPDomainConfiguration(LDAPProperties ldapProperties, Logger logger) {
        ldapConnectionPooling = ldapProperties.getProperty(LDAPProperties.LDAP_CONNECTION_POOLING);
        ldapUrl = ldapProperties.getProperty(LDAPProperties.LDAP_URL);
        usersDn = ldapProperties.getProperty(LDAPProperties.LDAP_USERS_SUBTREE);
        groupsDn = ldapProperties.getProperty(LDAPProperties.LDAP_GROUPS_SUBTREE);
        if (groupsDn == null) {
            groupsDn = usersDn;
        }

        ldapUserFilter = ldapProperties.getProperty(LDAPProperties.LDAP_USER_FILTER);
        ldapGroupFilter = ldapProperties.getProperty(LDAPProperties.LDAP_GROUP_FILTER);
        groupNameAttribute = ldapProperties.getProperty(LDAPProperties.LDAP_GROUPNAME_ATTR);
        tenantAttribute = ldapProperties.getProperty(LDAPProperties.LDAP_TENANT_ATTR);
        authenticationMethod = ldapProperties.getProperty(LDAPProperties.LDAP_AUTHENTICATION_METHOD);
        startTls = Boolean.parseBoolean(ldapProperties.getProperty(LDAPProperties.LDAP_START_TLS, "false"));
        anyCertificate = Boolean.parseBoolean(ldapProperties.getProperty(LDAPProperties.LDAP_START_TLS_ANY_CERTIFICATE,
                                                                         "false"));
        anyHostname = Boolean.parseBoolean(ldapProperties.getProperty(LDAPProperties.LDAP_START_TLS_ANY_HOSTNAME,
                                                                      "false"));
        useUidInGroupSearch = Boolean.parseBoolean(ldapProperties.getProperty(LDAPProperties.LDAP_GROUPSEARCH_USE_UID,
                                                                              "false"));
        bindLogin = ldapProperties.getProperty(LDAPProperties.LDAP_BIND_LOGIN);
        bindPasswd = ldapProperties.getProperty(LDAPProperties.LDAP_BIND_PASSWD);
        fallbackUserAuth = Boolean.valueOf(ldapProperties.getProperty(LDAPProperties.FALLBACK_USER_AUTH));
        fallbackGroupMembership = Boolean.valueOf(ldapProperties.getProperty(LDAPProperties.FALLBACK_GROUP_MEMBERSHIP));
        fallbackTenantMembership = Boolean.valueOf(ldapProperties.getProperty(LDAPProperties.FALLBACK_TENANT_MEMBERSHIP));

        //initialize system properties for SSL/TLS connection
        String keyStore = ldapProperties.getProperty(LDAPProperties.LDAP_KEYSTORE_PATH);
        if ((!Strings.isNullOrEmpty(keyStore)) && (!alreadyDefined(SSL_KEYSTORE_PATH_PROPERTY, keyStore))) {
            keyStore = PASharedProperties.getAbsolutePath(keyStore);
            System.setProperty(SSL_KEYSTORE_PATH_PROPERTY, keyStore);
            System.setProperty(SSL_KEYSTORE_PASSWD_PROPERTY,
                               ldapProperties.getProperty(LDAPProperties.LDAP_KEYSTORE_PASSWD));
        }

        String trustStore = ldapProperties.getProperty(LDAPProperties.LDAP_TRUSTSTORE_PATH);
        if ((!Strings.isNullOrEmpty(trustStore)) && (!alreadyDefined(SSL_TRUSTSTORE_PATH_PROPERTY, trustStore))) {
            trustStore = PASharedProperties.getAbsolutePath(trustStore);
            System.setProperty(SSL_TRUSTSTORE_PATH_PROPERTY, trustStore);
            System.setProperty(SSL_TRUSTSTORE_PASSWD_PROPERTY,
                               ldapProperties.getProperty(LDAPProperties.LDAP_TRUSTSTORE_PASSWD));
        }
    }

    /**
     * Checks if property is already defined.
     *
     * @param propertyName name of the property
     * @param propertyValue value of the property
     * @return true id the property is defined and its value equals the specified value.
     */
    private boolean alreadyDefined(String propertyName, String propertyValue) {

        if (propertyName != null && propertyName.length() != 0) {
            String definedPropertyValue = System.getProperty(propertyName);

            if (System.getProperty(propertyName) != null && !definedPropertyValue.equals(propertyValue)) {
                logger.debug("Property " + propertyName + " is already defined");
                logger.debug("Using old value " + propertyValue);
                return true;
            }
        }

        return false;
    }

    public String getLdapConnectionPooling() {
        return ldapConnectionPooling;
    }

    public String getLdapUrl() {
        return ldapUrl;
    }

    public String getUsersDn() {
        return usersDn;
    }

    public String getGroupsDn() {
        return groupsDn;
    }

    public String getLdapUserFilter() {
        return ldapUserFilter;
    }

    public String getLdapGroupFilter() {
        return ldapGroupFilter;
    }

    public String getGroupNameAttribute() {
        return groupNameAttribute;
    }

    public String getTenantAttribute() {
        return tenantAttribute;
    }

    public String getAuthenticationMethod() {
        return authenticationMethod;
    }

    public boolean isStartTls() {
        return startTls;
    }

    public boolean isAnyCertificate() {
        return anyCertificate;
    }

    public boolean isAnyHostname() {
        return anyHostname;
    }

    public boolean isUseUidInGroupSearch() {
        return useUidInGroupSearch;
    }

    public String getBindLogin() {
        return bindLogin;
    }

    public String getBindPasswd() {
        return bindPasswd;
    }

    public boolean isFallbackUserAuth() {
        return fallbackUserAuth;
    }

    public boolean isFallbackGroupMembership() {
        return fallbackGroupMembership;
    }

    public boolean isFallbackTenantMembership() {
        return fallbackTenantMembership;
    }
}
