/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.ow2.proactive.scheduler.authentication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.config.PAProperties.PAPropertiesType;
import org.ow2.proactive.scheduler.common.scheduler.Tools;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;


/**
 * LDAPProperties contains all LDAP configuration properties.
 * 
 * You must use provided methods in order to get these properties.
 * 
 * @author The ProActiveTeam
 * @date 11 june 08
 * @since ProActive 4.0
 *
 */
@PublicAPI
public enum LDAPProperties {

    /* ***************************************************************** */
    /* ************************** LDAP PROPERTIES ********************** */
    /* ***************************************************************** */

    /** URL of a ldap used for authentication */
    SCHEDULER_LDAP_URL("pa.scheduler.ldap.url", PAPropertiesType.STRING),

    /** path in the LDAP tree users containing scheduler users entries*/
    SCHEDULER_LDAP_USERS_SUBTREE("pa.scheduler.ldap.userssubtree", PAPropertiesType.STRING),

    /** attribute in user entries that represent user's login */
    SCHEDULER_LDAP_USER_LOGIN_ATTR("pa.scheduler.ldap.user.login.attr", PAPropertiesType.STRING),

    /** DN of a group of unique Members containing users with 'users' permissions */
    SCHEDULER_LDAP_USERS_GROUP_DN("pa.scheduler.ldap.users.group.dn", PAPropertiesType.STRING),

    /** DN of a group of unique Members containing users with 'administrator' permissions */
    SCHEDULER_LDAP_ADMINS_GROUP_DN("pa.scheduler.ldap.admins.group.dn", PAPropertiesType.STRING),

    /** authentication method used to connect to LDAP : none, simple or a SASL method */
    SCHEDULER_LDAP_AUTHENTICATION_METHOD("pa.scheduler.ldap.authentication.method", PAPropertiesType.STRING),

    /** login name used to perform ldap's binding */
    SCHEDULER_LDAP_BIND_LOGIN("pa.scheduler.ldap.bind.login", PAPropertiesType.STRING),

    /** password used to perform ldap's binding */
    SCHEDULER_LDAP_BIND_PASSWD("pa.scheduler.ldap.bind.pwd", PAPropertiesType.STRING),

    /** path of the java keystore file used by LDAP module for SSL/TLS authentication */
    SCHEDULER_LDAP_KEYSTORE_PATH("pa.scheduler.ldap.keystore.path", PAPropertiesType.STRING),

    /** path of the java truststore file used by LDAP module for SSL/TLS authentication */
    SCHEDULER_LDAP_TRUSTSTORE_PATH("pa.scheduler.ldap.truststore.path", PAPropertiesType.STRING),

    /** password for the keystore defined by pa.scheduler.ldap.keystore.path */
    SCHEDULER_LDAP_KEYSTORE_PASSWD("pa.scheduler.ldap.keystore.passwd", PAPropertiesType.STRING),

    /** password for the truststore defined by pa.scheduler.ldap.truststore.path */
    SCHEDULER_LDAP_TRUSTSTORE_PASSWD("pa.scheduler.ldap.truststore.passwd", PAPropertiesType.STRING);

    /* ***************************************************************************** */
    /* ***************************************************************************** */
    /** memory entity of the properties file. */
    private static Properties prop = null;
    /** Key of the specific instance. */
    private String key;
    /** value of the specific instance. */
    private PAPropertiesType type;

    /**
     * Create a new instance of LDAPProperties
     *
     * @param str the key of the instance.
     * @param type the real java type of this instance.
     */
    LDAPProperties(String str, PAPropertiesType type) {
        this.key = str;
        this.type = type;
    }

    /**
     * Set the user java properties to the LDAPProperties.<br/>
     * User properties are defined using the -Dname=value in the java command.
     */
    private static void setUserJavaProperties() {
        for (Object o : prop.keySet()) {
            String s = System.getProperty((String) o);
            if (s != null) {
                prop.setProperty((String) o, s);
            }
        }
    }

    /**
     * Get the properties map or load it if needed.
     * 
     * @return the properties map corresponding to the default property file.
     */
    private static Properties getProperties() {
        if (prop == null) {
            String LDAPPropertiesFile = PASchedulerProperties
                    .getAbsolutePath(PASchedulerProperties.SCHEDULER_LDAP_CONFIG_FILE_PATH.getValueAsString());
            prop = new Properties();
            try {
                prop.load(new FileInputStream(new File(LDAPPropertiesFile)));
                setUserJavaProperties();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return prop;
    }

    /**
     * Override properties defined in the default configuration file,
     * by properties defined in another file.
     * @param filename path of file containing some properties to override
     */
    public static void updateProperties(String filename) {
        //load properties file if needed
        getProperties();
        Properties ptmp = new Properties();
        try {
            ptmp.load(new FileInputStream(filename));
            for (Object o : ptmp.keySet()) {
                prop.setProperty((String) o, (String) ptmp.get(o));
            }
            setUserJavaProperties();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the value of this property as an integer.
     * If value is not an integer, an exception will be thrown.
     * 
     * @return the value of this property.
     */
    public int getValueAsInt() {
        String valueS = getValueAsString();
        try {
            int value = Integer.parseInt(valueS);
            return value;
        } catch (NumberFormatException e) {
            RuntimeException re = new IllegalArgumentException(key +
                " is not an integer property. getValueAsInt cannot be called on this property");
            throw re;
        }
    }

    /**
     * Returns the value of this property as a string.
     * 
     * @return the value of this property.
     */
    public String getValueAsString() {
        return getProperties().getProperty(key);
    }

    /**
     * Returns the value of this property as a boolean.
     * If value is not a boolean, an exception will be thrown.<br>
     * The behavior of this method is the same as the {@link java.lang.Boolean#parseBoolean(String s)}. 
     * 
     * @return the value of this property.
     */
    public boolean getValueAsBoolean() {
        return Boolean.parseBoolean(getValueAsString());
    }

    /**
     * Return the type of the given properties.
     *
     * @return the type of the given properties.
     */
    public PAPropertiesType getType() {
        return type;
    }

    /**
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return getValueAsString();
    }

}
