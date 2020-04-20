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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;
import org.jasypt.properties.EncryptableProperties;
import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.core.properties.PASharedProperties;
import org.ow2.proactive.core.properties.PropertyDecrypter;


/**
 * LDAPProperties contains all LDAP configuration properties.
 *
 * You must use provided methods in order to get these properties.
 */
@PublicAPI
public class LDAPProperties {

    /* ***************************************************************** */
    /* ************************** LDAP PROPERTIES ********************** */
    /* ***************************************************************** */

    /** URL of a ldap used for authentication */
    public static final String LDAP_URL = "pa.ldap.url";

    /** path in the LDAP tree users containing */
    public static final String LDAP_USERS_SUBTREE = "pa.ldap.userssubtree";

    /** path in the LDAP tree groups containing */
    public static final String LDAP_GROUPS_SUBTREE = "pa.ldap.groupssubtree";

    /**
     * Filter that allows to find the user dn given its scheduler login
     * {@code pa.ldap.user.filter=(&(objectclass=inetOrgPerson)(uid=%s))}
     * the {@code %s} parameter is the login used during the scheduler authentication process
     **/
    public static final String LDAP_USER_FILTER = "pa.ldap.user.filter";

    /**
     * Retrieves the group the user dn belongs to
     * {@code pa.ldap.group.filter=(&(objectclass=groupOfUniqueNames)(uniqueMember=%s))}
     * the {@code %s} parameter is the user dn.
     **/
    public static final String LDAP_GROUP_FILTER = "pa.ldap.group.filter";

    /** the attribute in the group entry that matches the jaas' group name */
    public static final String LDAP_GROUPNAME_ATTR = "pa.ldap.group.name.attr";

    /** authentication method used to connect to LDAP : none, simple or a SASL method */
    public static final String LDAP_AUTHENTICATION_METHOD = "pa.ldap.authentication.method";

    /** enable starttls negociation **/
    public static final String LDAP_START_TLS = "pa.ldap.starttls";

    public static final String LDAP_START_TLS_ANY_CERTIFICATE = "pa.ldap.starttls.any.certificate";

    public static final String LDAP_START_TLS_ANY_HOSTNAME = "pa.ldap.starttls.any.hostname";

    /** login name used to perform ldap's binding */
    public static final String LDAP_BIND_LOGIN = "pa.ldap.bind.login";

    /** password used to perform ldap's binding */
    public static final String LDAP_BIND_PASSWD = "pa.ldap.bind.pwd";

    /** path of the java keystore file used by LDAP module for SSL/TLS authentication */
    public static final String LDAP_KEYSTORE_PATH = "pa.ldap.keystore.path";

    /** path of the java truststore file used by LDAP module for SSL/TLS authentication */
    public static final String LDAP_TRUSTSTORE_PATH = "pa.ldap.truststore.path";

    /** password for the keystore defined by pa.ldap.keystore.path */
    public static final String LDAP_KEYSTORE_PASSWD = "pa.ldap.keystore.passwd";

    /** password for the truststore defined by pa.ldap.truststore.path */
    public static final String LDAP_TRUSTSTORE_PASSWD = "pa.ldap.truststore.passwd";

    /** boolean defining whether the LDAP service provider has to use connection pooling or not */
    public static final String LDAP_CONNECTION_POOLING = "pa.ldap.connection.pooling";

    /** fall back property, check user/password and group in files if user is not found in LDAP.
     * true or false */
    public static final String FALLBACK_USER_AUTH = "pa.ldap.authentication.fallback";

    /** group fall back property, check user group membership group file if user is not found in corresponding LDAP group.
     * true or false */
    public static final String FALLBACK_GROUP_MEMBERSHIP = "pa.ldap.group.membership.fallback";

    /* ***************************************************************************** */
    /* ***************************************************************************** */
    /** memory entity of the properties file. */
    private Properties prop = new Properties();

    /**
     * Create a new instance of LDAPProperties
     *
     * @param propertiesFileName properties file name
     */
    public LDAPProperties(String propertiesFileName) {
        prop = PropertyDecrypter.getDecryptableProperties();
        try (FileInputStream stream = new FileInputStream(new File(propertiesFileName))) {
            prop.load(stream);
            setUserJavaProperties();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the user java properties to the LDAPProperties.<br/>
     * User properties are defined using the -Dname=value in the java command.
     */
    private void setUserJavaProperties() {
        for (Object o : prop.keySet()) {
            String s = System.getProperty((String) o);
            if (s != null) {
                prop.setProperty((String) o, s);
            }
        }
    }

    /**
     * Retrieves the value of the property
     *
     * @param key property name
     * @return property value
     */
    public String getProperty(String key) {
        return prop.getProperty(key);
    }

    /**
     * Retrieves the value of the property
     *
     * @param key property name
     * @param defaultValue default value
     * @return property value
     */
    public String getProperty(String key, String defaultValue) {
        if (prop.containsKey(key)) {
            return prop.getProperty(key);
        } else {
            return defaultValue;
        }
    }
}
