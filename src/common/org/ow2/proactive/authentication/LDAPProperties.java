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
package org.ow2.proactive.authentication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.objectweb.proactive.annotation.PublicAPI;


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
public class LDAPProperties {

    /* ***************************************************************** */
    /* ************************** LDAP PROPERTIES ********************** */
    /* ***************************************************************** */

    /** URL of a ldap used for authentication */
    public static final String LDAP_URL = "pa.ldap.url";

    /** path in the LDAP tree users containing*/
    public static final String LDAP_USERS_SUBTREE = "pa.ldap.userssubtree";

    /** attribute in user entries that represent user's login */
    public static final String LDAP_USER_LOGIN_ATTR = "pa.ldap.user.login.attr";

    /** DN of a group of unique Members containing users with 'users' permissions */
    public static final String LDAP_USERS_GROUP_DN = "pa.ldap.users.group.dn";

    /** DN of a group of unique Members containing users with 'administrator' permissions */
    public static final String LDAP_ADMINS_GROUP_DN = "pa.ldap.admins.group.dn";

    /** authentication method used to connect to LDAP : none, simple or a SASL method */
    public static final String LDAP_AUTHENTICATION_METHOD = "pa.ldap.authentication.method";

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

    /* ***************************************************************************** */
    /* ***************************************************************************** */
    /** memory entity of the properties file. */
    private Properties prop = new Properties();

    /**
     * Create a new instance of LDAPProperties
     *
     */
    public LDAPProperties(String propertiesFileName) {
		try {
		    prop.load(new FileInputStream(new File(propertiesFileName)));
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

    public String getProperty(String key) {
    	return prop.getProperty(key);
    }
}
